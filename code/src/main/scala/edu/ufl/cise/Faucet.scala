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

  /*
   * Builds a Stream using the iterator. This uses tail recursion.
   * There will inevitably be an error, we just keep calm and catch it
   */
  @tailrec
  def getItem(iter: Iterator[Option[StreamItem]], acc:Stream[Option[StreamItem]] = Stream.empty): Stream[Option[StreamItem]] = {
    if (!iter.hasNext) {
      acc
    } else {
      val si = iter.next
      getItem(iter, si #:: acc)
    }
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
  def getStreams(date: String, fileName: String): Stream[Option[StreamItem]] = {
    //logInfo("Running GetStreams(%s,%s) ".format(date, fileName))

    val data = grabGPG(date, fileName)
    val transport = new TMemoryInputTransport(data.toByteArray)
    val protocol = new TBinaryProtocol(transport)

    // Stop streaming after the first None
    lazy val streamiter = Stream.continually(mkStreamItem(protocol))
      .takeWhile(_ match { case None => false; case _ => true })
      .toIterator
    getItem(streamiter)
  }

  /**
   * Returns all the streams for all the hours of a day
   */
  def getStreams(date: String): Stream[Option[StreamItem]] = {
    @tailrec
    def getStreamsHelper(hour:Int, acc:Stream[Option[StreamItem]] = Stream.empty):Stream[Option[StreamItem]] = {
      if (hour > 23) 
        acc
      else
        getStreamsHelper(hour+1, acc ++ getStreams(date, hour))
    }
    getStreamsHelper(0)
  }

  /**
   * return the files pertaining to specific date.
   */
  def getStreams(date: String, hour: Int): Stream[Option[StreamItem]] = {
    // This adds zero in case of a one digit number
    val hourStr = numberFormatter.format(hour) 

    //get list of files in a date-hour directory
    val folderName = "%s-%s".format(date,hourStr)
    val reader = new URLLineReader("http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/%s".format(folderName))
    val html = (for (line <- reader) yield line).mkString
    val pattern = """a href="([^"]+.gpg)""".r

    /**
     * Uses recursion to lazily grab streams.
     */
    @tailrec
    //def lazyFileGrabber(fileIter:Iterator[Match], acc:Stream[Option[StreamItem]] = Stream.empty):Stream[Option[StreamItem]] = {
    def lazyFileGrabber(fileIter:Stream[Match], acc:Stream[Option[StreamItem]] = Stream.empty):Stream[Option[StreamItem]] = {
      if (fileIter.isEmpty) acc
      else {
        val file = fileIter.head.group(1)
        lazyFileGrabber(fileIter.tail, acc ++ getStreams(folderName, file))
      }
    }
    lazyFileGrabber(pattern.findAllIn(html).matchData.toStream)
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
    logInfo("The first StreamItem: %s ".format(z.head.toString))
    logInfo("Length of stream is %d".format(z.length))

    //    logInfo(getStreamsToString(z))
    lazy val z2 = getStreams("2012-05-01")
    //for (v <- z2) 
    //  logInfo("---"+v.toString)
    //logInfo(z2.head.get + "")
  }

}
