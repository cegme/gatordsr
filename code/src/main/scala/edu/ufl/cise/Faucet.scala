package edu.ufl.cise

import scala.annotation.tailrec
import scala.sys.process._
import scala.sys.process.ProcessLogger
import scala.util.matching.Regex.Match


import java.text.DecimalFormat

import kba.{ ContentItem, CorpusItem, StreamItem, StreamTime }

import org.apache.thrift.transport.TMemoryInputTransport
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TTransportException

import edu.ufl.cise.util.URLLineReader

/**
 * we need to read a whole directory and append the StreamItems. later on receive filtering
 * options and e.g. only filter some dates or hours. later on put delays on the thread based
 * on real delays.
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

  /**
   * Loads the secrekey for decryprion.
   * This requires the file 'trec-kba-rsa.secret-key' to be in the
   * gatordsr/code directory.
   */
  def loadKey: Unit = {
    logInfo("loadKey")
    "gpg --no-permission-warning --import trec-kba-rsa.secret-key".!
  }

  def test = {
    val baos = new java.io.ByteArrayOutputStream
    "curl -s http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/2012-05-02-00/news.f451b42043f1f387a36083ad0b089bfd.xz.gpg" #| "gpg --no-permission-warning --trust-model always --output - --decrypt -" #| "xz --decompress" #> baos
    baos
  }

  /**
   * Gets, dencrypts and uncompresses the requested file and returns an ByteStream.
   */
  def grabGPG(date: String, fileName: String): java.io.ByteArrayOutputStream = {
    logInfo("Fetching, decrypting and decompressing with GrabGPG(%s,%s)".format(date, fileName))

    val baos = new java.io.ByteArrayOutputStream
    (("curl -s http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/" +
      "kba-stream-corpus-2012/%s/%s").format(date, fileName) #|
      "gpg --no-permission-warning --trust-model always --output - --decrypt -" #|
      "xz --decompress" #> baos) ! ProcessLogger(line => ()) // Silence the linux stderr
    baos
  }

  /*
     * Creates a StreamItem from a protocol.
     */
  def mkStreamItem(protocol: org.apache.thrift.protocol.TProtocol): Option[StreamItem] = {
    val s = new StreamItem
    var successful = false
    try {
      s.read(protocol)
      successful = true
    } catch {
      case e: TTransportException => logDebug("Error in mkStreamItem"); None
    }
    if (successful) Some(s) else None
  }

  /**
   * Specify a date of the form "YYYY-MM-DD-HH" and the name of the file
   * and returns an option stream containing those StreamItems.
   *
   * The returned stream is large and materialized.
   *
   * Example usage:
   *   GetStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
   */
  def getStreams(date: String, fileName: String): Iterator[Option[StreamItem]] = {
    //logInfo("Running GetStreams(%s,%s) ".format(date, fileName))

    val data = grabGPG(date, fileName)
    val transport = new TMemoryInputTransport(data.toByteArray)
    val protocol = new TBinaryProtocol(transport)

    // Stop streaming after the first None
    Stream.continually(mkStreamItem(protocol))
      .takeWhile(_ match { case None => false; case _ => true })
      .toIterator
  }

  /**
   * Returns all the streams for all the hours of a day
   */
  def getStreams(date: String): Iterator[StreamItem] = {
    (0 to 23)
      .map{getStreams(date, _)} // Get all the streams for this
      .view // Make getting the streams lazy
      .reduceLeft(_ ++ _) // Concatenate the iterators
  }

  /**
   * return the files pertaining to specific date.
   */
  def getStreams(date: String, hour: Int): Iterator[StreamItem] = {
    // This adds zero in case of a one digit number
    val hourStr = numberFormatter.format(hour) 

    //get list of files in a date-hour directory
    val folderName = "%s-%s".format(date,hourStr)
    val reader = new URLLineReader("http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/%s".format(folderName))
    val html = reader.toList.mkString
    val pattern = """a href="([^"]+.gpg)""".r

    /**
     * Uses recursion to lazily grab streams.
     */
    def lazyFileGrabber(fileIter:Iterator[Match]):Iterator[StreamItem] = {
      def lazyGrab(file:Match):Iterator[StreamItem] = {
        for(si <- getStreams(folderName, file.group(1)).flatten)
          yield si
      }
      //fileIter.map{lazyGrab(_)}.reduceLeft((a,b) => b)
      fileIter.map{lazyGrab(_)}.flatMap(x=>x)
    }
    lazyFileGrabber(pattern.findAllIn(html).matchData)
  }

  /**
   * Returns the string representation of a whole stream
   */
  def getStreamsToString(si: Stream[Option[StreamItem]]): String = {
    si.flatten.mkString
  }


  def main(args: Array[String]) = {

    logInfo("""Running test with GetStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")""")
    val z = getStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
    logInfo("The first StreamItem: %s ".format(z.next.toString))
    logInfo("Length of stream is %d".format(z.length))

    //    logInfo(getStreamsToString(z))
    val z2 = getStreams("2012-05-01")
    //for (v <- z2) 
    //  logInfo("---"+v.toString)
    //logInfo(z2.head.get + "")
    //logInfo(z2.length.toString)
    logInfo(z2.take(51).length.toString)
  }

}
