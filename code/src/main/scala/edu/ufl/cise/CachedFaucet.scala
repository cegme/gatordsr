package edu.ufl.cise

import java.io.ByteArrayInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.GZIPInputStream
import java.io.FileInputStream
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TIOStreamTransport
import org.apache.thrift.transport.TFileTransport
import org.apache.thrift.transport.TStandardFile
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import kba.StreamItem
import spark.SparkContext
import spark.RDD
import spark.storage.StorageLevel
import spark.SparkContext._
import scala.sys.process.stringToProcess
import scala.sys.process.ProcessLogger
import scala.collection.mutable.WeakHashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import scala.collection.parallel.SeqSplitter
import scala.collection.generic.Signalling
import scala.collection.generic.IdleSignalling
import edu.ufl.cise.util.URLLineReader
import scala.collection.parallel.immutable.ParSeq

class CachedFaucet(

  val sr: StreamRange) extends Faucet with Logging with Serializable {

  val sc: SparkContext = new SparkContext("local[4]", "gatordsr", "$YOUR_SPARK_HOME", List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))

  protected case class CacheKey(val date: String, val fileName: String)

  private lazy val cache: WeakHashMap[CacheKey, RDD[StreamItem]] = new WeakHashMap[CacheKey, RDD[StreamItem]]()

  private val keyBF = BloomFilter.create(Funnels.stringFunnel, 60000, .0001)

  private lazy val keyList: List[CacheKey] = {
    logInfo("Priming the iterator file list")

    sr.getFileList.map {
      _ match {
        case (date, file) => new CacheKey(date, file)
        case _ => throw new Exception("Invalid format")
      }
    }
  }

  final def getKeyList = keyList.map(x => (x.date, x.fileName))

  protected class StreamIterator
    extends Iterator[RDD[StreamItem]] {
    val iterator = keyList.iterator

    override def hasNext: Boolean = {
      iterator.hasNext
    }

    override def next: RDD[StreamItem] = {
      val c: CacheKey = iterator.next

      getRDDCompressed(sc, c.date, c.fileName)

    }
  }

  def getRDDZ(sc: SparkContext, date: String, fileName: String): RDD[StreamItem] = {
    logInfo("Fetching, decrypting and decompressing with GrabGPG(%s,%s)".format(date, fileName))

    val tmpFile = java.io.File.createTempFile("%s-%s".format(date, fileName), ".tmp")

    val baos = new java.io.FileOutputStream(tmpFile)
    (("http://s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/%s/%s").format(date, fileName) #| //get the file, pipe it
      "gpg --no-permission-warning --trust-model always --output - --decrypt -" #|
      "xz --decompress" #>
      baos) !! ProcessLogger(line => ())

    baos.flush

    logInfo("Got the file1. It is size %f MBs".format(tmpFile.length / 1000.0 / 1000))

    System.gc();
    val transport = new TIOStreamTransport(new FileInputStream(tmpFile))
    transport.open
    val protocol = new TBinaryProtocol(transport)

    val a = Stream.continually(mkStreamItem(protocol))
      .takeWhile(_ match { case None => transport.close(); logInfo("-"); false; case _ => true })
      .map { _.get }
      .toArray

    tmpFile.delete
    logInfo("Deleted tmp file of size %f MBs".format(tmpFile.length / 1000.0 / 1000))
    sc.parallelize(a)
  }

  def getRDDCompressed(sc: SparkContext, date: String, fileName: String): RDD[StreamItem] = {
    logInfo("getRDDCompressed")
    val xzGPG = grabGPGCompressed(date, fileName)
    val is = new ByteArrayInputStream(xzGPG.toByteArray)
    val bais = new XZCompressorInputStream(is)
    val transport = new TIOStreamTransport(bais)
    transport.open
    val protocol = new TBinaryProtocol(transport)
    assert(transport.isOpen)

    val a = Iterator.continually(mkStreamItem(protocol))
      .takeWhile(_ match { case None => transport.close; xzGPG.reset; logInfo("-"); false; case _ => true })
      .map { _.get }
      .toSeq

    sc.parallelize(a)
  }

  lazy val iterator: Iterator[RDD[StreamItem]] = new StreamIterator

  def getAllRDDS: RDD[StreamItem] = {
    keyList
      .par
      .map { c =>
        getRDDCompressed(sc, c.date, c.fileName)
      }
      .reduce(_ union _)
  }
}

object CachedFaucet extends Faucet with Logging {

  def main(args: Array[String]): Unit = {

    val sr = new StreamRange
    sr.addFromDate("2011-10-05")
    sr.addFromHour(00)
    //sr.addToDate("2012-05-01")
    sr.addToHour(00)
    val z = new CachedFaucet(sr)

    val it = z.iterator
    if (it.hasNext) {
      {
        val rdd = it.next
        logInfo("The count: %d".format(rdd.count))
        logInfo("Is body null: %s".format(rdd.first.body == null))
      }
    }
    lazy val z1 = z.iterator.reduce(_ union _) // Combine RDDS

    z.iterator.map { rdd =>
      logInfo("RDD size: %d".format(rdd.count))
      rdd.filter { si =>
        {
          if ((si.body != null)
            && (si.body.cleansed != null)
            && (si.body.cleansed.array.length > 0)) {
            val s = new String(si.body.cleansed.array, "UTF-8")
            println(s)
            s.toLowerCase.contains("the")
          }
          false
        }
      } //.foreach(si => println(si.title))
    }
      .foreach(t => t.foreach(si => println(si.title)))
  }

}
