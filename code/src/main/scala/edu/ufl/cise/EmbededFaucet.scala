package edu.ufl.cise

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.parallel.immutable.ParSeq
import scala.sys.process.stringToProcess

import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TIOStreamTransport
import org.apache.thrift.transport.TTransportException

import edu.ufl.cise.util.StreamItemWrapper
import streamcorpus.StreamItem

object EmbededFaucet extends Logging {

  // val DIRECTORY = "/home/morteza/2013Corpus/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/"
  val DIRECTORY = "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/"
  val FILTER = ""
  val query = "president"

  val numberFormatter = new DecimalFormat("00")

  val SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  def grabGPGLocal(day: String, hour: Int, fileName: String): java.io.ByteArrayOutputStream = {
    logInfo(day + "/" + hour + "/" + fileName)
    val baos = new java.io.ByteArrayOutputStream
    ("gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt " + fileName) #|
      "xz --decompress" #>
      baos !;
    baos
  }

  /**
   * Grab gpg from remote server on ssh.
   *
   */
  def grabGPGSSH(fileName: String): java.io.ByteArrayOutputStream = {
    logInfo(fileName)
    val baos = new java.io.ByteArrayOutputStream
    //println(("sshpass -p 'trecGuest' ssh trecGuest@sm321-01.cise.ufl.edu 'cat " + fileName + "'") #| (" gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt " + fileName) #| "xz --decompress" )
    ("sshpass -p 'trecGuest' ssh trecGuest@sm321-01.cise.ufl.edu 'cat /media/sde/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-11-03-05/WEBLOG-89-15957f5baef21e2cda6dca887b96e23e-e3bb3adf7504546644d4bc2d62108064.sc.xz.gpg' ") #| (" gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt " + fileName) #| " xz --decompress" #> baos !;
    baos
  }

  def getStreams(date: String, hour: Int, fileName: String, data: ByteArrayOutputStream): ParSeq[edu.ufl.cise.util.StreamItemWrapper] = {
    val bais = new ByteArrayInputStream(data.toByteArray())
    val transport = new TIOStreamTransport(bais)
    transport.open()
    val protocol = new TBinaryProtocol(transport)

    var list = List[StreamItemWrapper]().par

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
          logDebug("Error in mkStreamItem") //TODO stop before exception
          exception = true
      }
      list = list :+ new StreamItemWrapper(date, hour, fileName, index, si)
      index = index + 1
    }
    transport.close()
    list
  }

  def main(args: Array[String]) = {

    collection.parallel.ForkJoinTasks.defaultForkJoinPool.setParallelism(32)

    val fileList = DirList.getFileList(DIRECTORY, FILTER).toList.par
    println("total file count on disk sdd is: " + fileList.size)

    val siCount = new AtomicInteger(0)
    val siFilteredCount = new AtomicInteger(0)

    val tempFilter = fileList.foreach(p => {
      val temp = p.asInstanceOf[String]
      val pattern = """.*language/([^/]+)-(.+)/(.+)""".r

      val dayHourFileList = pattern.findAllIn(temp).matchData.toArray
      val day = dayHourFileList.apply(0).group(1)
      val hour = new Integer(dayHourFileList.apply(0).group(2))
      val fileName = dayHourFileList.apply(0).group(3)
      val data = grabGPGLocal(day, hour, temp)
      // val data = grabGPGSSH("/media/sde/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-11-03-05/WEBLOG-89-15957f5baef21e2cda6dca887b96e23e-e3bb3adf7504546644d4bc2d62108064.sc.xz.gpg")
      val tempArr = data.toByteArray()
      val sis = getStreams(day, hour, fileName, data)
      siCount.addAndGet(sis.size)

      sis.foreach(p => {
        var res = false

        if (p.streamItem.body != null) {
          val document = p.streamItem.body.getClean_visible()
          if (document != null) {
            val strEnglish = document.toLowerCase().replaceAll("[^A-Za-z0-9\\p{Punct}]", " ").replaceAll("\\s+", " ")
              .replaceAll("(\r\n)+", "\r\n").replaceAll("(\n)+", "\n").replaceAll("(\r)+", "\r").toLowerCase()
            res = strEnglish.contains(query)
          } else
            res = false
        }
        if (res == true) {
          // println("Found")
          val str = p.toString
          println(str)
          siFilteredCount.incrementAndGet()
        }
      })
    })

    println("total file count on disk" + DIRECTORY + " before filter is: " + siCount.get())
    println("total file count on disk " + DIRECTORY + "after filter is: " + siFilteredCount.get())
    //    tempFilter.foreach(p => {
    //      logInfo(p.toString())
    //    })
  }

}
