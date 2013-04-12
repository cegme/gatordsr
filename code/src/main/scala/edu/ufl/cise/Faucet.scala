package edu.ufl.cise

import java.io.ByteArrayInputStream
import java.text.DecimalFormat
import scala.sys.process.stringToProcess
import scala.sys.process.ProcessLogger
import scala.util.matching.Regex.Match
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TIOStreamTransport
import edu.ufl.cise.util.StreamItemUtil
import edu.ufl.cise.util.URLLineReader
import kba.StreamItem
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.net.URL
import spark.SparkContext
import spark.streaming.Seconds
import spark.streaming.StreamingContext
import org.apache.thrift.transport.TTransportException
import scala.collection.mutable.ArrayBuffer

/**
 * we need to read a whole directory and append the StreamItems.
 * TODO: receive filtering options and e.g. only filter some dates or hours.
 * TODO: put delays on the thread based on real delays.
 *
 * TODO: takewhile will evaluate all the items in the stream. what's the use of
 * iterator anyways? in getStreams(date: String, fileName: String)
 *
 * TODO: get link to the KBA example source code, some code cleanup,
 * putting functions in order of dependency
 *
 * Scala ProcessBuilder runs shell commands as pipelines
 *
 * To run:
 * ~/gatordsr/code $ sbt
 * ~/gatordsr/code $ run Faucet
 * [1]
 *
 */

object Faucet extends Logging {

  lazy val numberFormatter = new DecimalFormat("00")
  val BASE_URL = "http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/"
  val MAX_FROM_DATE = "2011-10-07"
  val MAX_FROM_HOUR = 14
  val MAX_TO_DATE = "2012-05-02"
  val MAX_TO_HOUR = 0

    val sc = new SparkContext("local[2]", "gatordsr", "$YOUR_SPARK_HOME",
      List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
    val ssc = new StreamingContext("local[2]", "gatordsrStreaming", Seconds(2),
      "$YOUR_SPARK_HOME", List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
  val NUM_SLICES = 2

  val SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Gets, dencrypts and uncompresses the requested file and returns an ByteStream.
   *
   * gpg --no-permission-warning --import trec-kba-rsa.secret-key
   * Loads the secret key for decryption. This requires the
   * file 'trec-kba-rsa.secret-key' to be in the gatordsr/code directory.
   *
   * This only needs to be done once per file system.
   * (Unless the gpg is reset or something.)
   */
  def grabGPG(date: String, fileName: String): java.io.ByteArrayOutputStream = {
    logInfo("Fetching, decrypting and decompressing with GrabGPG(%s,%s)".format(date, fileName))

    val baos = new java.io.ByteArrayOutputStream
    // Use the linux file system to download, decrypt and decompress a file
    (("curl -s http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/" +
      "kba-stream-corpus-2012/%s/%s").format(date, fileName) #| //get the file, pipe it
      "gpg --no-permission-warning --trust-model always --output - --decrypt -" #| //decrypt it, pipe it
      "xz --decompress" #> //decompress it
      baos) ! ProcessLogger(line => ()) // ! Executes the previous commands, 
    //Silence the linux stdout, stderr

    baos //return 
  }

  /**
   * Creates a StreamItem from a protocol. return an Option[StramItem] just in case
   * for some of them we don't have data we are safe.
   */
  //  def mkStreamItem(protocol: org.apache.thrift.protocol.TProtocol): Option[StreamItem] = {
  //    val s = new StreamItem
  //    var successful = false
  //    try {
  //      s.read(protocol)
  //      successful = true
  //    } catch {
  //      case e: Exception => logDebug("Error in mkStreamItem"); None
  //    }
  //    if (successful) Some(s) else None
  //  }

  /**
   * Specify a date of the form "YYYY-MM-DD-HH" and the name of the file
   * and return all the stream items in one gpg file. 
   *
   *
   * Example usage:
   *   getStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
   */
  def getStreams(date: String, fileName: String): Array[StreamItem] = {
    val data = grabGPG(date, fileName)
    val bais = new ByteArrayInputStream(data.toByteArray())
    val transport = new TIOStreamTransport(bais)
    transport.open()
    val protocol = new TBinaryProtocol(transport)

    val arrayBuffer = ArrayBuffer[StreamItem]()
    val si = new StreamItem
    var exception = false
    while (!exception) {

      try {
        si.read(protocol);
      } catch {
        case e: TTransportException =>
          if (e.getType() == TTransportException.END_OF_FILE) logDebug("End of File")
          else logDebug("Exception happened.")
          exception = true
        case e: Exception =>
          logDebug("Error in mkStreamItem")
          exception = true
      }
      arrayBuffer += si      
    }

    transport.close()

    arrayBuffer.toArray
  }

  def getDirectoryName(date: String, hour: Int): String = {
    // This adds zero in case of a one digit number
    val hourStr = numberFormatter.format(hour)
    "%s-%s".format(date, hourStr)
  }

  /**
   * Return the files pertaining to specific date.
   */
  def getStreams(date: String, hour: Int): Iterator[StreamItem] = {
    val directoryName = getDirectoryName(date, hour)
    val reader = new URLLineReader(BASE_URL + format(directoryName))
    val html = reader.toList.mkString
    val pattern = """a href="([^"]+.gpg)""".r

    // returns an array of Stream item as when we read a file the whole content of it is already in
    //memory, making it an iterator does no help. iterator helps us avoid loading the next file before the 
    //previous file is processed.
    
    val dayHourFileList = pattern.findAllIn(html).matchData.toArray
    for(fileName<-dayHourFileList){
      val arr = getStreams(directoryName, fileName.group(1))
      val rdd = sc.parallelize(arr, NUM_SLICES)
    }
    
    
    
   
  }

  /**
   * Returns streams in a specific hour range of a specific date
   */
  def getStreams(date: String, hour0: Int, hour1: Int): Iterator[StreamItem] = {
    if (hour0 < 0 || hour1 > 23)
      null
    else {
      (hour0 to hour1)
        .map { getStreams(date, _) } // Get all the streams for this
        .view // Make getting the streams lazy
        .reduceLeft(_ ++ _) // Concatenate the iterators    
    }
  }

  /**
   * Returns all the streams for all the hours of a day
   */
  def getStreams(date: String): Iterator[StreamItem] = {
    getStreams(date, 0, 23)
  }

  /**
   * Return the data between specific date ranges.
   */
  def getStreamsDateRange(dateFrom: String, dateTo: String): Iterator[StreamItem] = {

    val dFrom = SIMPLE_DATE_FORMAT.parse(dateFrom)
    val dTo = SIMPLE_DATE_FORMAT.parse(dateTo)

    if (dFrom.after(dTo))
      return null

    var tempDate = dFrom
    val c = Calendar.getInstance();

    var it = Iterator[StreamItem]()
    while (tempDate.before(dTo) || tempDate.equals(dTo)) {
      val dateStr = SIMPLE_DATE_FORMAT.format(tempDate)
      val z = getStreams(dateStr)
      if (it.isEmpty)
        it = z
      else
        it = it ++ z

      c.setTime(tempDate);
      c.add(Calendar.DATE, 1); // number of days to add
      tempDate = c.getTime()
    }

    //getStreams(date, 0, 23)
    it
  }

  /**
   * Test the operation of the Faucet class
   */
  def main(args: Array[String]) = {

    //logInfo("""Running test with GetStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")""")
    //    val z = getStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
    //    val si = z.next.get
    //    logInfo("The first StreamItem is: %s ".format(si.toString))
    //    logInfo("Length of stream is: %d".format(z.length))
    //
    //    println(new String(si.body.raw.array(), "UTF-8"))

    //    println(StreamItemUtil.toString(si))
    //    val z2 = getStreams("2012-05-01")    
    //    logInfo(z2.take(501).length.toString)

    //    val z3 = getStreamsDateRange("2011-10-08", "2011-10-11")
    //    logInfo(z3.take(501).length.toString)
    //println(getAllDataSize(MAX_FROM_DATE, MAX_FROM_HOUR, MAX_TO_DATE, MAX_TO_HOUR))
  }

  /**
   * Return the file size of all compressed data
   */
  def getAllDataSize(fromDateStr: String, fromHour: Int, toDateStr: String, toHour: Int): BigInt = {
    var sumSize = BigInt(0)

    val fromDate = SIMPLE_DATE_FORMAT.parse(fromDateStr)
    val toDate = SIMPLE_DATE_FORMAT.parse(toDateStr)

    var tempDate = fromDate
    val c = Calendar.getInstance();

    while (tempDate.before(toDate) || tempDate.equals(toDate)) {
      val dateStr = SIMPLE_DATE_FORMAT.format(tempDate)

      var size: Int = 0

      val directoryName = getDirectoryName(SIMPLE_DATE_FORMAT.format(tempDate), 17)
      val reader = new URLLineReader(BASE_URL + format(directoryName))
      val html = reader.toList.mkString
      val pattern = """a href="([^"]+.gpg)""".r

      pattern.findAllIn(html).matchData foreach {
        m =>

          val str = BASE_URL + directoryName + "/" + m.group(1)
          if (str.toUpperCase().contains("WIKI"))
            println(str)
          val url = new URL(str);
          val conn = url.openConnection();
          size = conn.getContentLength();
          if (size < 0)
            System.out.println("Could not determine file size.");
          else {
            //System.out.println(size);
            sumSize += size
          }
          conn.getInputStream().close();

      }

      c.setTime(tempDate);
      c.add(Calendar.DATE, 1); // number of days to add
      tempDate = c.getTime()
      print(sumSize + "---")
    }
    println("Total: " + sumSize)
    return sumSize
  }
}
