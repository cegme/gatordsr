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
import java.io.PrintWriter
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.net.URL
import java.io.InputStream
import java.io.BufferedInputStream
import org.apache.thrift.transport.TTransport
import org.apache.thrift.transport.TIOStreamTransport
import java.io.ByteArrayInputStream

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

object FaucetTest23 extends Logging {

  lazy val numberFormatter = new DecimalFormat("00")

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
  
  def grabFile():Array[Byte] = {
    
    val u = new URL("http://www.java2s.com/binary.dat");
    val uc = u.openConnection();
    val contentType = uc.getContentType();
    val contentLength = uc.getContentLength();
    if (contentType.startsWith("text/") || contentLength == -1) {
      throw new IOException("This is not a binary file.");
    }
    val raw = uc.getInputStream();
    val in = new BufferedInputStream(raw);
    val data = new Array[Byte](contentLength);
    var bytesRead = 0;
    var offset = 0;
    var err = false;
    while (offset < contentLength && ! err) {
      bytesRead = in.read(data, offset, data.length - offset);
      if (bytesRead == -1)
        err = true;
      offset = offset + bytesRead;
    }
    in.close();

    if (offset != contentLength) {
      throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
    }//data is filled with content

    
    return null;
  }

  /**
   * Creates a StreamItem from a protocol. return an Option[StramItem] just in case
   * for some of them we don't have data we are safe.
   */
  def mkStreamItem(protocol: org.apache.thrift.protocol.TProtocol): Option[StreamItem] = {
    val s = new StreamItem
    var successful = false
    try {
      s.read(protocol)
      successful = true
    } catch {
      case e: Exception => logDebug("Error in mkStreamItem"); None
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
   *   getStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
   */
  def getStreams(date: String, fileName: String): Iterator[Option[StreamItem]] = {
    //logInfo("Running GetStreams(%s,%s) ".format(date, fileName))

    val data = grabGPG(date, fileName)
    val bais = new ByteArrayInputStream(data.toByteArray())
    val transport = new TIOStreamTransport(bais)
    transport.open()
    val protocol = new TBinaryProtocol(transport)

    // Stop streaming after the first None. TODO why? what could happen? end of file?
    val it = Stream.continually(mkStreamItem(protocol)) //TODO adds items one bye one to the stream
      .takeWhile(_ match { case None => false; case _ => true })
      .toIterator
      
      transport.close()
      it
  }

  /**
   * Return the files pertaining to specific date.
   */
  def getStreams(date: String, hour: Int): Iterator[Option[StreamItem]] = {
    // This adds zero in case of a one digit number
    val hourStr = numberFormatter.format(hour)

    //get list of files in a date-hour directory
    val folderName = "%s-%s".format(date, hourStr)
    val reader = new URLLineReader("http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/%s".format(folderName))
    val html = reader.toList.mkString
    val pattern = """a href="([^"]+.gpg)""".r

    /**
     * A round-about way to call getStream on sets of files
     * while still making it lazy.
     * Go ahead and try and improve it.
     */
    def lazyFileGrabber(fileIter: Iterator[Match]): Iterator[Option[StreamItem]]= {
      def lazyGrab(file: Match): Iterator[Option[StreamItem]] = {
        for (si <- getStreams(folderName, file.group(1)))
          yield si
      }
      fileIter.map { lazyGrab(_) }.flatMap(x => x)
    }
    lazyFileGrabber(pattern.findAllIn(html).matchData)
  }

  /**
   * Returns all the streams for all the hours of a day
   */
  def getStreams(date: String): Iterator[Option[StreamItem]] = {
    (0 to 23)
      .map { getStreams(date, _) } // Get all the streams for this
      .view // Make getting the streams lazy
      .reduceLeft(_ ++ _) // Concatenate the iterators
  }

  def main(args: Array[String]) = {

    logInfo("""Running test with GetStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")""")
    val z = getStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
    val si = z.next.get
    logInfo("The first StreamItem is: %s ".format(si.toString))
    logInfo("Length of stream is: %d".format(z.length))

    println(new String(si.body.raw.array(), "UTF-8"))
    
    def getString(array: Array[Byte]): String = new String(array, "UTF-8")

   // val a = si.body.raw.asCharBuffer()
    //a.flip()
   // val s = a.toString()
    //println(s)
    
   // println(a)
//    val raw_body = getString(si.body.raw.array)
//    val cleansed_body = getString(si.body.cleansed.array)
//
//    // write two strings into files 
//    val pwRaw = new PrintWriter(new File("raw.html"))
//    pwRaw.write(raw_body)
//    pwRaw.close()
//    val pwCleansed = new PrintWriter(new File("cleansed.html"))
//    pwCleansed.write(cleansed_body)
//    pwCleansed.close()

    //val z2 = getStreams("2012-05-01")    
    //logInfo(z2.take(501).length.toString)
  }

}
