package edu.ufl.cise

import java.io.ByteArrayInputStream

import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TIOStreamTransport

import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels

import kba.StreamItem

import spark.SparkContext
import spark.RDD
import spark.storage.StorageLevel
import spark.SparkContext._

import scala.collection.mutable.WeakHashMap
import scala.collection.mutable.ListBuffer

import edu.ufl.cise.util.URLLineReader


class CachedFaucet(
  val sc:SparkContext = new SparkContext("local", "gatordsr", "$YOUR_SPARK_HOME", List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar")),
  val dateFrom:String,
  val hour:Int
  ) extends Faucet with Logging {

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

    // TODO make this more general not just using the dateFrom, hour
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
  }

  def getRDD(sc:SparkContext, date:String, fileName:String): RDD[StreamItem] = {
    
    System.gc();
    val data = grabGPG(date, fileName)
    val bais = new ByteArrayInputStream(data.toByteArray())
    val transport = new TIOStreamTransport(bais)
    transport.open()
    val protocol = new TBinaryProtocol(transport)

    // Stop streaming after the first None. TODO why? what could happen? end of file?
    val a = Stream.continually(mkStreamItem(protocol)) //TODO adds items one bye one to the stream
      .takeWhile(_ match { case None => transport.close(); false; case _ => true })
      .map { _.get }

    //transport.close()
    val rdd = sc.makeRDD[StreamItem](a)//.persist(StorageLevel.DISK_ONLY)
    rdd
  }

  private class StreamIterator extends Iterator[RDD[StreamItem]] {
    val iterator = keyList.iterator

    override def hasNext:Boolean = {
      iterator.hasNext
    }

    override def next:RDD[StreamItem] = {
      val c:CacheKey = iterator.next
      
      // Check to see if this cache was in the key
      //val z:RDD[StreamItem] = getRDD(sc, c.date, c.fileName)
      //cache.put(c, getRDD(sc, c.date, c.fileName))
      //cache.get(c).get
      System.gc()
      getRDD(sc, c.date, c.fileName)
      // If it is not, get it and put it in the cache
    }
  }

  lazy val iterator:Iterator[RDD[StreamItem]] = new StreamIterator
}



object CachedFaucet extends Faucet with Logging {


  def getRDD(sc:SparkContext, date:String, hour:Int): Iterator[RDD[StreamItem]] = {
    val directoryName = getDirectoryName(date, hour)
    val reader = new URLLineReader(BASE_URL + "%s".format(directoryName))
    val html = reader.toList.mkString
    val pattern = """a href="([^"]+.gpg)""".r
   
    pattern.findAllIn(html).matchData
      .map(m => getRDD(sc, directoryName, m.group(1)))
  }


  def getRDD(sc:SparkContext, date:String, fileName:String): RDD[StreamItem] = {
    
    System.gc();
    val data = grabGPG(date, fileName)
    val bais = new ByteArrayInputStream(data.toByteArray())
    val transport = new TIOStreamTransport(bais)
    transport.open()
    val protocol = new TBinaryProtocol(transport)

    // Stop streaming after the first None. TODO why? what could happen? end of file?
    val a = Stream.continually(mkStreamItem(protocol)) //TODO adds items one bye one to the stream
      .takeWhile(_ match { case None => transport.close(); false; case _ => true })
      .map { _.get }

    //transport.close()
    val rdd = sc.makeRDD[StreamItem](a).persist(StorageLevel.DISK_ONLY)
    rdd
  }


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

    val z = new CachedFaucet(sc, "2012-05-01", 0)
    val it = z.iterator
    while(it.hasNext) {
      {
        it.next.count
      }
      System.gc
    }
  }
}
