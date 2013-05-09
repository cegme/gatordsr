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
import scala.collection.JavaConversions._
import scala.collection.parallel


import edu.ufl.cise.util.URLLineReader


class CachedFaucet(
  val sc:SparkContext = new SparkContext("local[32]", "gatordsr", "$YOUR_SPARK_HOME", List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar")),
  val sr:StreamRange
  ) extends Faucet with Logging with Serializable {

  private case class CacheKey(val date:String, val fileName:String)

  /** This cache helps manage the garbage. **/
  private lazy val cache:WeakHashMap[CacheKey,RDD[StreamItem]] = new WeakHashMap[CacheKey,RDD[StreamItem]]()

  /** This bloom filter is to check to see if a RDD once existed here **/
  private val keyBF = BloomFilter.create(Funnels.stringFunnel, 60000, .0001)

  /** 
  * This list is for iterating throught the keys in their proper order. 
  * This has the list of files to iterate through groups of streams.
  **/
  private lazy val keyList:List[CacheKey] = {
    logInfo("Priming the iterator file list")

    /*// TODO make this more general not just using the dateFrom, hour
    val directoryName = getDirectoryName(dateFrom, hour)
    val reader = new URLLineReader(BASE_URL + "%s".format(directoryName))
    val html = reader.toList.mkString
    val pattern = """a href="([^"]+.gpg)""".r

    Stream.continually(directoryName)
      .zip(pattern.findAllIn(html).matchData.toArray.map(_.group(1)))
      .map { _ match {
        case (date:String, fileName:String) =>  CacheKey(date, fileName)
        case _ => throw new Exception }
      }
      .toList
      */
      sr.getFileList.map{ _ match { 
        case (date,file) => new CacheKey(date,file)
        case _ => throw new Exception("Invalid format") }
      }
  }

  final def getKeyList = keyList.map(x => (x.date, x.fileName)) 
  
  
  private class StreamIterator extends Iterator[RDD[StreamItem]] {
    val iterator = keyList.iterator

    override def hasNext:Boolean = {
      iterator.hasNext
    }

    override def next:RDD[StreamItem] = {
      val c:CacheKey = iterator.next
      
      // Check to see if this cache was in the key
      //val z:RDD[StreamItem] = getRDD(sc, c.date, c.fileName)
      //cache.put(c, getRDDZ(sc, c.date, c.fileName))
      //cache.get(c).get
      //getRDDZ(sc, c.date, c.fileName)
      getRDDCompressed(sc, c.date, c.fileName)
      // If it is not, get it and put it in the cache
    }
  }


  /*private class PStreamIterator 
    extends parallel.immutable.ParIterable[RDD[StreamItem]] {

    val docs = keyList

    private lazy val keyListSize:Int = keyList.size
    def size:Int = keyListSize // This needs to be constant time

    //def splitter: IterableSplitter[RDD[StreamIteem]]

    //def split: Seq[Splitter]

    //class ParFileSplitter

  }*/



  def getRDDZ(sc:SparkContext, date:String, fileName:String): RDD[StreamItem] = {
    logInfo("Fetching, decrypting and decompressing with GrabGPG(%s,%s)".format(date, fileName))

    // TODO can I not decompress and do with the GZIPOutputStream class?
    val tmpFile = java.io.File.createTempFile("%s-%s".format(date, fileName),".tmp")

    val baos = new java.io.FileOutputStream(tmpFile)
    //bais.connect(baos)
    //baos.connect(bais)
    // Use the linux file system to download, decrypt and decompress a file
    (("curl -s http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/" +
      "kba-stream-corpus-2012/%s/%s").format(date, fileName) #| //get the file, pipe it
      "gpg --no-permission-warning --trust-model always --output - --decrypt -" #| //decrypt it, pipe it
      "xz --decompress" #> //decompress it
      baos) !! ProcessLogger(line => ()) // ! Executes the previous commands, 
      //tmpFile) !! ProcessLogger(line => ()) // ! Executes the previous commands, 

    baos.flush
    //val bais = new java.io.FileInputStream(tmpFile)

    logInfo("Got the file1. It is size %f MBs".format(tmpFile.length/1000.0/1000))

    System.gc();
    //val data = grabGPG(date, fileName)
    //val bais = new ByteArrayInputStream(baos.toByteArray())
    //val transport = new TFileTransport(new TStandardFile(tmpFile.getAbsolutePath), true)
    //val transport = new TFileTransport(tmpFile.getAbsolutePath, true)
    //val transport = new TIOStreamTransport(bais)
    val transport = new TIOStreamTransport(new FileInputStream(tmpFile))
    transport.open
    //logInfo("numChunks: %d".format(transport.getNumChunks()))
    val protocol = new TBinaryProtocol(transport)

    // Stop streaming after the first None. TODO why? what could happen? end of file?
    val a = Stream.continually(mkStreamItem(protocol)) //TODO adds items one bye one to the stream
      .takeWhile(_ match { case None => transport.close(); logInfo("-"); false; case _ => true })
      .map { _.get }
      .toArray

    //transport.close
    tmpFile.delete
    logInfo("Deleted tmp file of size %f MBs".format(tmpFile.length/1000.0/1000))
    //val rdd = sc.makeRDD[StreamItem](a)//.persist(StorageLevel.DISK_ONLY)
    //rdd
    sc.parallelize(a)
  }

  def getRDDCompressed(sc:SparkContext, date:String, fileName:String): RDD[StreamItem] = {
    logInfo("getRDDCompressed")
    val xzGPG = grabGPGCompressed(date, fileName)
    val is = new ByteArrayInputStream(xzGPG.toByteArray)
    val bais = new XZCompressorInputStream(is)
    val transport = new TIOStreamTransport(bais)
    transport.open
    val protocol = new TBinaryProtocol(transport)
    assert(transport.isOpen)


    val a = Iterator.continually(mkStreamItem(protocol)) //TODO adds items one bye one to the stream
      .takeWhile(_ match { case None => transport.close; xzGPG.reset; logInfo("-"); false; case _ => true })
      .map { _.get }
      .toSeq

    // If we keep ths reset/close here it cause the items not to be read
    //xzGPG.reset // Stop a leak
    //transport.close

    sc.parallelize(a)
  }


  /**
   * TODO change this to an function?
   */
  lazy val iterator:Iterator[RDD[StreamItem]] = new StreamIterator

  //def createNewStreamIterator = new StreamIterator

  def getAllRDDS:RDD[StreamItem] = {
    //iterator.reduce(_ union_)
    //sc.parallelize(keyList)
    keyList
      .par
      .map{ c =>
        getRDDCompressed(sc, c.date, c.fileName)
      }
     .reduce( _ union _) 
  }
}


object CachedFaucet extends Faucet with Logging {
  
  def main(args: Array[String]):Unit = {

    //System.setProperty("spark.storage.memoryFraction", "0.66")
    //System.setProperty("spark.rdd.compress", "true")
    //System.setProperty("SPARK_MEM", "8G")
     //logInfo("SPARK_MEM: %s".format(System.getProperty("SPARK_MEM")))
    val sc = new SparkContext("local", "gatordsr", "$YOUR_SPARK_HOME",
      List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
    //sc.setCheckpointDir("/home/cgrant/data/checkpoint/mycheckpoints2")

    //lazy val z0 = getRDD(sc, "2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
    //lazy val z0b = getRDD(sc, "2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
    //logInfo("Stream Count z0: " + z0.count())
    //z0.foreach( si => println(si.schost))
    //logInfo("Union count: " + z0.union(z0b).count())

    // Not working yet

    //lazy val z1 = getRDD(sc, "2012-05-01", 0)
    //logInfo("Stream Count z1: " + z1.foldLeft(0L)((a:Long,b:RDD[StreamItem]) => a + b.count))

    
    //val z = new CachedFaucet(sc, "2012-05-01", 0)
    val sr = new StreamRange
    sr.addFromDate("2012-05-01")
    sr.addFromHour(0)
    //sr.addToDate("2012-05-01")
    //sr.addToHour(0)
    val z = new CachedFaucet(sc, sr)

    val it = z.iterator
    if(it.hasNext) {
      {
        val rdd = it.next
        logInfo("The count: %d".format(rdd.count))
        logInfo(rdd.first.toString)
        logInfo("Is body null: %s".format(rdd.first.body == null))
        //sc.getRDDStorageInfo.foreach{x => logInfo("##%s".format(x.toString))}
        //logInfo(sc.hadoopConfiguration.toString)
        //sc.getExecutorMemoryStatus.foreach{x => logInfo("==%s".format(x.toString))}
        //System.gc
      }
    }
    lazy val z1 = z.iterator.reduce(_ union _) // Combine RDDS
      //logInfo("Total records: %d".format(z1.count))
      //logInfo("Total records: %d".format(z.getAllRDDS.count))
  }

}
