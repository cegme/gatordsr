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
  def getStreams(date: String, fileName: String): Iterator[StreamItem] = {
    val data = grabGPG(date, fileName)
    val bais = new ByteArrayInputStream(data.toByteArray())
    val transport = new TIOStreamTransport(bais)
    transport.open()
    val protocol = new TBinaryProtocol(transport)

    // Stop streaming after the first None. TODO why? what could happen? end of file?
    val it = Stream.continually(mkStreamItem(protocol)) //TODO adds items one bye one to the stream
      .takeWhile(_ match { case None => false; case _ => true })
      .map{_.get}
      .toIterator

    transport.close()
    it
  }

  /**
   * Return the files pertaining to specific date.
   */
  def getStreams(date: String, hour: Int): Iterator[StreamItem] = {
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
    def lazyFileGrabber(fileIter: Iterator[Match]): Iterator[StreamItem] = {
      def lazyGrab(file: Match): Iterator[StreamItem] = {
        for (si <- getStreams(folderName, file.group(1)))
          yield si
      }
      fileIter.map { lazyGrab(_) }.flatMap(x => x)
    }
    lazyFileGrabber(pattern.findAllIn(html).matchData)
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

  def getStreamsDateRange(dateFrom: String, dateTo: String): Iterator[StreamItem] = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd");
    val dFrom = sdf.parse(dateFrom)
    val dTo = sdf.parse(dateTo)

    if (dFrom.after(dTo))
      return null

    var tempDate = dFrom
    val c = Calendar.getInstance();

    var it = Iterator[StreamItem]()
    while (tempDate.before(dTo) || tempDate.equals(dTo)) {
      val dateStr = sdf.format(tempDate)
      val z = getStreams(dateStr)
      if (it.isEmpty)
        it = z
      else
        z.append(it)

      c.setTime(tempDate);
      c.add(Calendar.DATE, 1); // number of days to add
      tempDate = c.getTime()
    }

    //getStreams(date, 0, 23)
    it
  }

  def main(args: Array[String]) = {
    logInfo("""Running test with GetStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")""")
//    val z = getStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
//    val si = z.next.get
//    logInfo("The first StreamItem is: %s ".format(si.toString))
//    logInfo("Length of stream is: %d".format(z.length))
//
//    println(new String(si.body.raw.array(), "UTF-8"))
    
    //    println(StreamItemUtil.toString(si))
    //val z2 = getStreams("2012-05-01")    
    //logInfo(z2.take(501).length.toString)
    
    val z3 = getStreamsDateRange("2011-10-08", "2011-10-11")
    //logInfo(z3.take(501).length.toString)
  }
}
