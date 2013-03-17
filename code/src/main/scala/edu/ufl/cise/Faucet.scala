package edu.ufl.cise

import scala.sys.process._

import kba.{ ContentItem, CorpusItem, StreamItem, StreamTime }

import org.apache.thrift.transport.TMemoryInputTransport
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TTransportException

/**
 * This class reads a URL line by line
 */
class URLLineReader(url: String) extends Iterator[String] {
  val reader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.net.URL(url).openStream()))
  var line: String = null;

  def hasNext = {
    line = reader.readLine()
    line != null
  }

  def next = line
}

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
    //("curl -s http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/2012-05-02-00/news.f451b42043f1f387a36083ad0b089bfd.xz.gpg" #| "gpg --no-permission-warning --trust-model always --output - --decrypt -" #| "xz --decompress").!!
    //("curl -s http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/2012-05-02-00/news.f451b42043f1f387a36083ad0b089bfd.xz.gpg" #| "gpg --no-permission-warning --trust-model always --output - --decrypt -" #| "xz --decompress").lines_!
    val baos = new java.io.ByteArrayOutputStream
    "curl -s http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/2012-05-02-00/news.f451b42043f1f387a36083ad0b089bfd.xz.gpg" #| "gpg --no-permission-warning --trust-model always --output - --decrypt -" #| "xz --decompress" #> baos
    baos
  }

  /**
   * Gets, dencrypts and uncompresses the requested file and returns an ByteStream.
   */
  def GrabGPG(date: String, fileName: String): java.io.ByteArrayOutputStream = {
    logInfo("Fetching, decrypting and decompressing with GrabGPG(%s,%s)".format(date, fileName))

    val baos = new java.io.ByteArrayOutputStream
    (("curl -s http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/" +
      "kba-stream-corpus-2012/%s/%s").format(date, fileName) #|
      "gpg --no-permission-warning --trust-model always --output - --decrypt -" #|
      "xz --decompress" #> baos).!!
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
  def getItem(iter: Iterator[Option[StreamItem]]): Stream[Option[StreamItem]] = {
    try {
      if (!iter.hasNext) {
        Stream.empty
      } else {
        Stream.cons(iter.next, getItem(iter))
      }
    } catch {
      case e: TTransportException => logDebug("Error in getItem"); Stream.empty
    }
  }

  /**
   * Specify a date of the form "YYYY-MM-DD-HH" and the name of the file
   * and returns an option stream containing those StreamItems.
   *
   * Example usage:
   *   GetStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
   */
  def GetStreams(date: String, fileName: String): Stream[Option[StreamItem]] = {
    logInfo("Running GetStreams(%s,%s) ".format(date, fileName))

    val data = GrabGPG(date, fileName)
    val transport = new TMemoryInputTransport(data.toByteArray)
    val protocol = new TBinaryProtocol(transport)

    // Stop streaming after the first None
    val streamiter = Stream.continually(mkStreamItem(protocol))
      .takeWhile(_ match { case None => false; case _ => true })
      .toIterator
    getItem(streamiter)
  }

  /**
   * return the files pertaining to specific date.
   */
  def getStreams(date: String): Stream[Option[StreamItem]] = {
    val reader = new URLLineReader("http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/" + date)
    val html = (for (line <- reader) yield line).mkString("")
    val string = html
    val pattern = """a href="(news[^"]+)"""".r

    var stream = Stream[Option[StreamItem]]()
    pattern.findAllIn(string).matchData foreach {
      m => println(m.group(1))
     val z =  GetStreams(date, m.group(1))
       logInfo("The first StreamItem: %s ".format(z.head.toString))
       logInfo("Length of stream is %d".format(z.length))
       if(stream == {})
          stream = z
       stream = Stream.concat(stream, z) 
    }
    return stream
  }

  def getStreamsToString(si: Stream[Option[StreamItem]]): String = {
    var s = ""
    s = si.flatten.mkString
    return s
  }

  def main(args: Array[String]) = {

    logInfo("""Running test with GetStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")""")
    val z = GetStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
    logInfo("The first StreamItem: %s ".format(z.head.toString))
    logInfo("Length of stream is %d".format(z.length))

    logInfo("\n")
    //    logInfo(getStreamsToString(z))
    val z2 = getStreams("2012-05-02-00")
  }

}
