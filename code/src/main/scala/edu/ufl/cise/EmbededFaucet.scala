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

import scala.collection.mutable.ListBuffer

/**
 * TODO: put delays on the thread based on real delays.
 * TODO: wrap StreamItems in Option?
 *
 * To run:
 * ~/gatordsr/code $ sbt
 * ~/gatordsr/code $ run Faucet
 * [1]
 *
 */

object EmbededFaucet extends Logging {

  lazy val numberFormatter = new DecimalFormat("00")
  val BASE_URL = "http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/"
  val MAX_FROM_DATE = "2011-10-07"
  val MAX_FROM_HOUR = 14
  val MAX_TO_DATE = "2012-05-02"
  val MAX_TO_HOUR = 0

  val text = "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865."
  val query = new SSFQuery("Abraham Lincoln", "president of")
  lazy val pipeline = Pipeline.getPipeline(query)

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
      baos) ! // ! Executes the previous commands, 
    //Silence the linux stdout, stderr

    baos
  }

  /**
   * Specify a date of the form "YYYY-MM-DD-HH" and the name of the file
   * and return all the stream items in one gpg file.
   *
   * When we read a file the whole content of it is already in memory, making it
   * an iterator does no help. iterator helps us avoid loading the next file before the
   * previous file is processed.
   *
   * Example usage:
   *   getStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
   */
  def getStreams(date: String, fileName: String): List[StreamItem] = {
    val data = grabGPG(date, fileName)
    val bais = new ByteArrayInputStream(data.toByteArray())
    val transport = new TIOStreamTransport(bais)
    transport.open()
    val protocol = new TBinaryProtocol(transport)

    var list = List[StreamItem]()

    var exception = false
    while (!exception) {

      val si = new StreamItem
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
      list = si :: list
    }

    transport.close()
    list

  }

  /**
   * Format directory names. Here, This adds zero in case of a one digit number
   */
  def getDirectoryName(date: String, hour: Int): String = {
    val hourStr = numberFormatter.format(hour)
    "%s-%s".format(date, hourStr)
  }

  /**
   * Return the files pertaining to specific date.
   */
  def getStreams(date: String, hour: Int): Unit = {
    
    val directoryName = getDirectoryName(date, hour)
    val reader = new URLLineReader(BASE_URL + format(directoryName))
    val html = reader.toList.mkString
    val pattern = """a href="([^"]+.gpg)""".r

    val dayHourFileList = pattern.findAllIn(html).matchData.toArray

    for (fileName <- dayHourFileList) {
      val list = getStreams(directoryName, fileName.group(1))
      val rdd = SparkIntegrator.sc.parallelize(list, SparkIntegrator.NUM_SLICES)
      //all streamitems of one file in parallel
      println("Hello Spark!")
      val temp = rdd.map(p =>
        {
          if (p.body != null && p.body.cleansed != null) {
            val bb = p.body.cleansed.array
            if (bb.length > 0) {
              val str = new String(bb, "UTF-8")
              //println(str)
              //println("Enter pipeline")
              val b = Pipeline.getPipeline(query).run(str)
              //println("exit pipeline")
              str
            } else
              " "
          }
          " "
        }).flatMap(line => line.split(" "))
        .map(word => if (word.toLowerCase()
          .contains("today"))
          1
        else
          0)
      .reduce(_ + _)
      //if (temp.count > 0) {
      //  val count = temp.reduce(_ + _)
      //  println("Found Today: " + count)
      //}

      println("Found Today: " + temp)

      //.foreach(println _)
    }
  }

  /**
   * Returns streams in a specific hour range of a specific date
   */
  def getStreams(date: String, hour0: Int, hour1: Int): Unit = {
    (hour0 to hour1)
    .map(h => getStreams(date, h))
    //    for (h <- hour0 to hour1) {
    //      getStreams(date, h)
    //    }
  }

  /**
   * Returns all the streams for all the hours of a day
   */
  def getStreams(date: String): Unit = {
    getStreams(date, 0, 23)
  }

  /**
   * Return the data between specific date ranges.
   */
  def getStreamsDateRange(dateFrom: String, dateTo: String): List[StreamItem] = {

    //    val dFrom = SIMPLE_DATE_FORMAT.parse(dateFrom)
    //    val dTo = SIMPLE_DATE_FORMAT.parse(dateTo)
    //
    //    if (dFrom.after(dTo))
    //      return null
    //
    //    var tempDate = dFrom
    //    val c = Calendar.getInstance();

    //    val list = List[StreamItem]();
    //    
    //    
    //
    //    while (tempDate.before(dTo) || tempDate.equals(dTo)) {
    //      val dateStr = SIMPLE_DATE_FORMAT.format(tempDate)
    //      val z = getStreams(dateStr)
    //      //list.addall
    //      list.prepend(z)
    //
    //      c.setTime(tempDate);
    //      c.add(Calendar.DATE, 1); // number of days to add
    //      tempDate = c.getTime()
    //    }
    //    list
    null
  }

  /**
   * Test the operation of the Faucet class
   */
  def main(args: Array[String]) = {
    Pipeline.init
    val z3 = getStreams("2011-10-08", 5)
    //val z3 = getStreams("2011-10-08")
    //val z3 = getStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")

  }

}