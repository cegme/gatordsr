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
import streamcorpus.StreamItem
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
import edu.ufl.cise.util.StreamItemWrapper
import edu.ufl.cise.util.StreamItemWrapper
import test.DirList
import scala.collection.JavaConversions._
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

  val DIRECTORY = "/home/morteza/2013Corpus/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/"

  val SDD_MIN_DATE = "2011-10-05"
  val SDD_MIN_HOUR = 0
  val SDD_MAX_DATE = "2012-08-18"
  val SDD_MAX_HOUR = 1

  lazy val numberFormatter = new DecimalFormat("00")
  val BASE_URL = "http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/"
  val MAX_FROM_DATE = "2011-10-07"
  val MAX_FROM_HOUR = 14
  val MAX_TO_DATE = "2012-05-02"
  val MAX_TO_HOUR = 0

  //val text = "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865."
  // val query = new SSFQuery("Abraham Lincoln", "president of") 
  //  val query = new SSFQuery("roosevelt", "president")
  //  Pipeline.init()
  //lazy val pipeline = Pipeline.getPipeline(query)

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
  def grabGPG(day: String, hour: Int, fileName: String): java.io.ByteArrayOutputStream = {
    // logInfo("Fetching, decrypting and decompressing with GrabGPG(%s,%s)".format(date, fileName))
    // logInfo(date + " " + fileName)

    //    val fetchFileCommandOnline = ("curl -s http://s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/" +
    //      "%s/%s").format(date, fileName)

    val baos = new java.io.ByteArrayOutputStream
    // Use the linux file system to download, decrypt and decompress a file
    //    if (date != null) {
    //      (
    //        fetchFileCommandOnline #| //get the file, pipe it
    //        "gpg --no-permission-warning --trust-model always --output - --decrypt -" #| //decrypt it, pipe it
    //        "xz --decompress" #> //decompress it
    //        baos) ! // ! Executes the previous commands, 
    //    } else {
    //2011-10-07-14
    println("gpg --no-permission-warning --trust-model always --output - --decrypt " + fileName +
      "xz --decompress")
    ("gpg --no-permission-warning --trust-model always --output - --decrypt " + fileName) #|
      "xz --decompress" #>
      baos !;
    //    }
    val temp = baos.toByteArray()
    println(temp.size)
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
  def getStreams(date: String, hour: Int, fileName: String, data: ByteArrayOutputStream): List[StreamItemWrapper] = {
    val bais = new ByteArrayInputStream(data.toByteArray())
    val transport = new TIOStreamTransport(bais)
    transport.open()
    val protocol = new TBinaryProtocol(transport)

    var list = List[StreamItemWrapper]()

    var index: Int = 0
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
      list = new StreamItemWrapper(date, hour, fileName, index, si) :: list
      index = index + 1
    }

    transport.close()
    println(list.size)
    list
  }

  /**
   * Format directory names. Here, This adds zero in case of a one digit number
   */
  def getDirectoryName(date: String, hour: Int): String = {
    val hourStr = numberFormatter.format(hour)
    "%s-%s".format(date, hourStr)
  }

  def processData(date: String, hour: Int, fileName: String, data: ByteArrayOutputStream): Unit = {
    val list = getStreams(date, hour, fileName, data)

    //val rooseveltDoc: String = new String(list.apply(115).streamItem.body.clean_html, "UTF-8")
    val rooseveltDoc: String = list.apply(115).streamItem.body.clean_html
    println(rooseveltDoc)

    // print roosevelt count
    val rdd = SparkIntegrator.sc.parallelize(list, SparkIntegrator.NUM_SLICES)

    val tempFilter = list.filter(p => {
      print("id: " + p)
      //      if (p.streamItem.title != null)
      //        println("  ----   " + new String(p.streamItem.title.getCleansed(), "UTF-8"))

      if (p.streamItem.body != null && p.streamItem.body.clean_html != null) {
        println(p.streamItem.body.clean_html)
//        val bb = p.streamItem.body.cleansed.array
//        if (bb.length > 0) {
//          val str = new String(bb, "UTF-8")
//          val strEnglish = str.toLowerCase().replaceAll("[^A-Za-z0-9\\p{Punct}]", " ").replaceAll("\\s+", " ")
//            .replaceAll("(\r\n)+", "\r\n").replaceAll("(\n)+", "\n").replaceAll("(\r)+", "\r").toLowerCase()
//          //println(str)
//          strEnglish.contains("roosevelt")
//        } else
//          false
//      } else
//        false
      }
        true
    })

//    tempFilter.foreach(p => {
//      println("==================================nokte\n" +
//        "============================\n============================\n")
//      val str = new String(p.streamItem.body.cleansed.array(), "UTF-8")
//      val strEnglish = str.toLowerCase().replaceAll("[^A-Za-z0-9\\p{Punct}]", " ").replaceAll("\\s+", " ")
//        .replaceAll("(\r\n)+", "\r\n").replaceAll("(\n)+", "\n").replaceAll("(\r)+", "\r").toLowerCase()
//      println(strEnglish)
//    })

    println(tempFilter.size)

    //    val a = (new ArrayList[Triple] { new Triple("", "", "") }).toArray()
    //    println("StreamItem list size is: " + list.size)
    //    val temp = SparkIntegrator.sc.parallelize(list, SparkIntegrator.NUM_SLICES)
    //      .map(p =>
    //        {
    //          if (p.body != null && p.body.cleansed != null) {
    //            val bb = p.body.cleansed.array
    //            if (bb.length > 0) {
    //              val str = new String(bb, "UTF-8")
    //
    //              val strEnglish = str.toLowerCase().replaceAll("[^A-Za-z0-9\\p{Punct}]", " ").replaceAll("\\s+", " ")
    //                .replaceAll("(\r\n)+", "\r\n").replaceAll("(\n)+", "\n").replaceAll("(\r)+", "\r")
    //              val b = pipeline.run(strEnglish)
    //              b.toArray
    //            } else {
    //              a
    //            }
    //          } else
    //            a
    //        }).flatMap(x => { println(); x })
    //      .filter(p =>
    //        {
    //          val t = p.asInstanceOf[Triple]
    //          //  val e0DicArr = WordnetUtil.getSynonyms(e0, POS.NOUN)
    //          print(query.entity + "->(" + t + ")   ")
    //
    //          t.entity0.contains(query.entity) //||
    //        })
    //      .foreach(t => logInfo("Answer: %s".format(t.toString)))

  }

  def getStreamsOffline(day: String, hour: Int, fileName: String) {
    val data = grabGPG(day, hour, fileName);
    processData(day, hour, fileName, data);
  }

  /**
   * Test the operation of the Faucet class
   */
  def main(args: Array[String]) = {
    val fileList = DirList.getFileList(DIRECTORY).toList
    println("total file count on disk sdd is: " + fileList.size)

    //    val sc = new SparkContext("local[128]", "gatordsr", "$YOUR_SPARK_HOME",
    //      List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))

    //    val rdd = sc.parallelize(fileList.take(5), 32)
    //  println("total file count on disk sdd is: " + rdd.count)
    println("total file count on disk sdd is: " + fileList.size)

    val tempFilter = fileList.map(p => {
      val temp = p.asInstanceOf[String]
      val pattern = """.*language/([^/]+)-(.+)/(.+)""".r

      val dayHourFileList = pattern.findAllIn(temp).matchData.toArray
      val day = dayHourFileList.apply(0).group(1)
      val hour = new Integer(dayHourFileList.apply(0).group(2))
      val fileName = dayHourFileList.apply(0).group(3)
      val data = grabGPG(day, hour, temp)
      val tempArr = data.toByteArray()     
      getStreams(day, hour, fileName, data)
    }).flatMap(x => x).filter(p => {
      var res = false
      println("filter--")      
      if (p.streamItem.body != null){
        val document = p.streamItem.body.getClean_visible()
      if (document != null) {
          val strEnglish = document.toLowerCase().replaceAll("[^A-Za-z0-9\\p{Punct}]", " ").replaceAll("\\s+", " ")
            .replaceAll("(\r\n)+", "\r\n").replaceAll("(\n)+", "\n").replaceAll("(\r)+", "\r").toLowerCase()
          res = strEnglish.contains("the")
      } else
        res = false
      }
      res
    })

    //println("total file count on disk sdd after filter is: " + tempFilter.count)
    println("total file count on disk sdd after filter is: " + tempFilter.size)
    tempFilter.foreach(p => {
      logInfo(p.toString())
    })

  }

}
