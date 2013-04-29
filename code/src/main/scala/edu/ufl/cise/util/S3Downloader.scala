package edu.ufl.cise.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.net.URL
import java.text.DecimalFormat
import scala.sys.process._

/**
 * This class will download, decrypt all the data from S3
 */
object S3Downloader {

  val numberFormatter = new DecimalFormat("00")

  val BASE_URL = "http://s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/"

  val MAX_FROM_DATE = "2011-10-07"
  val MAX_FROM_HOUR = 14
  val MAX_TO_DATE = "2012-05-02"
  val MAX_TO_HOUR = 0

  val LOCAL_DIR = "/home/morteza/tmp/"

  val SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Format directory names. Here, This adds zero in case of a one digit number
   */
  def getDirectoryName(date: String, hour: Int): String = {
    val hourStr = numberFormatter.format(hour)
    "%s-%s".format(date, hourStr)
  }

  def main(args: Array[String]): Unit = {
    println(getAllDataSize(MAX_FROM_DATE, MAX_FROM_HOUR, MAX_TO_DATE, MAX_TO_HOUR))
  }

  def grabGPG(date: String, fileName: String): java.io.ByteArrayOutputStream = {

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

      for (hour <- (0 to 23)) {

        val directoryName = getDirectoryName(SIMPLE_DATE_FORMAT.format(tempDate), hour)
        val reader = new URLLineReader(BASE_URL + format(directoryName) + "/index.html")
        val html = reader.toList.mkString
        val pattern = """a href="([^"]+.gpg)""".r

        "mkdir " + LOCAL_DIR + directoryName ! //create the directory

        pattern.findAllIn(html).matchData foreach {
          m =>

            val fileName = m.group(1)
            val str = BASE_URL + directoryName + "/" + fileName

            val baos = new java.io.ByteArrayOutputStream
            // Use the linux file system to dow nload, decrypt and decompress a file
            val curlstr = "curl  " + str + " > " + LOCAL_DIR + directoryName + "/" + fileName
            val grepstr = "gpg --no-permission-warning --trust-model always --output " + LOCAL_DIR + fileName.substring(0, fileName.length() - 4) +
              "- --decrypt " + fileName;

            curlstr ! //#| grepstr !

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
    }
    println("Total: " + sumSize)
    return sumSize

  }

}