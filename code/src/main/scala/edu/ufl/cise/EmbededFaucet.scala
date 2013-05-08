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
import java.io.ByteArrayOutputStream
import java.util.Arrays
import java.io.PrintWriter
import java.io.File
import java.util.ArrayList
import edu.ufl.cise.util.WordnetUtil
import edu.mit.jwi.item.POS

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
  // val query = new SSFQuery("Abraham Lincoln", "president of") 
  val query = new SSFQuery("roosevelt", "president")
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

    val fetchFileCommandOnline = ("curl -s http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/" +
      "kba-stream-corpus-2012/%s/%s").format(date, fileName)
    val fetchFileCommandOffline = "echo " + fileName
    var fetchFileCommand = if (date != null) fetchFileCommandOnline else fetchFileCommandOffline

    val baos = new java.io.ByteArrayOutputStream
    // Use the linux file system to download, decrypt and decompress a file
    if (date != null) {
      (
        fetchFileCommandOnline #| //get the file, pipe it
        "gpg --no-permission-warning --trust-model always --output - --decrypt -" #| //decrypt it, pipe it
        "xz --decompress" #> //decompress it
        baos) ! // ! Executes the previous commands, 
      //Silence the linux stdout, stderr
    } else {
      "gpg --no-permission-warning --trust-model always --output - --decrypt /home/morteza/zproject/trec-kba/social.3a51732f846b630e98c9f02e1fd0c8d4.xz.gpg" #| //2011-10-07-14
        "xz --decompress" #> //decompress it
        baos !
    }
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
  def getStreams(data: ByteArrayOutputStream): List[StreamItem] = {
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
      val data = grabGPG(directoryName, fileName.group(1))
      processData(data)
    }
  }

  def processData(data: ByteArrayOutputStream): Unit = {
    val list = getStreams(data)

    val pw: PrintWriter = new PrintWriter(new File("/home/morteza/streamitem200.txt"));
    for (i <- (0 to 200))
      try {
        pw.println("morteza " + i)
        val temp = new String(list.apply(i).body.cleansed.array, "UTF-8")
        pw.println(temp)
        pw.println("morteza " + i)
      } catch {
        case e: Exception =>
          logDebug("Error in get")
      }
    //pipeline.run(new String(list.apply(115).body.cleansed.array, "UTF-8"));
    println(new String(list.apply(115).body.cleansed.array, "UTF-8"))

    //val rdd = SparkIntegrator.sc.parallelize(list, SparkIntegrator.NUM_SLICES)
    //    val temp = rdd.map(p =>
    //      {
    //        if (p.body != null && p.body.cleansed != null) {
    //          val bb = p.body.cleansed.array
    //          if (bb.length > 0) {
    //            val str = new String(bb, "UTF-8").toLowerCase()
    //            // val b = pipeline.run(str)
    //            str
    //          } else
    //            " "
    //        } else
    //          " "
    //      })
    //      .filter(_.contains("roosevelt"))
    //    println(temp.count)

    val a = (new ArrayList[Triple] { new Triple("", "", "") }).toArray()

    //list
    val temp = SparkIntegrator.sc.parallelize(list, SparkIntegrator.NUM_SLICES)
    .map(p =>
      {
        if (p.body != null && p.body.cleansed != null) {
          val bb = p.body.cleansed.array
          if (bb.length > 0) {
            val str = new String(bb, "UTF-8")
            val strEnglish = str.replaceAll("[^A-Za-z0-9\\p{Punct}]", " ")
            val b = pipeline.run(strEnglish)
            b.toArray
          } else {
            a
          }
        } else
          a
      }).flatMap(x => x)
      .filter(p =>
        {
          val t = p.asInstanceOf[Triple]
          //  val e0DicArr = WordnetUtil.getSynonyms(e0, POS.NOUN)

          t.entity0.equals(query.entity) //||
          //query.entity.toLowerCase.contains(p.entity0.toLowerCase()) ||
          //query.slotName.toLowerCase.contains(p.slot.toLowerCase()) ||
          //p.slot.toLowerCase.equalsIgnoreCase(query.slotName.toLowerCase)
        })
      .foreach(t => logInfo("Answer: %s".format(t.toString)))
  }

  /**
   * Returns streams in a specific hour range of a specific date
   */
  def getStreams(date: String, hour0: Int, hour1: Int): Unit = {
    (hour0 to hour1)
      .map(h => getStreams(date, h))
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

  def getStreams(date: String, fileName: String) {
    val data = grabGPG(date, fileName);
    processData(data);
  }

  def getStreamsOffline(fileName: String) {
    val data = grabGPG(null, fileName);
    processData(data);
  }

  /**
   * Test the operation of the Faucet class
   */
  def main(args: Array[String]) = {
    //val z3 = getStreams("2011-10-08", 5)
    //getStreams("2011-10-08-5", "social.7e67c3f4fdee17f0c07751b075e3f649.xz.gpg")
    getStreams(null, null)
    // val z3 = getStreams("2011-10-08")
    // getStreamsOffline("/home/morteza/zproject/trec-kba/social.3a51732f846b630e98c9f02e1fd0c8d4.xz.gpg")
  }

}