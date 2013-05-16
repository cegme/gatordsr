package edu.ufl.cise

import scala.collection.JavaConversions._
import scala.collection.generic.IdleSignalling
import scala.collection.generic.Signalling
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.WeakHashMap
import scala.collection.parallel
import scala.collection.parallel.SeqSplitter
import scala.sys.process.stringToProcess

import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels

import edu.ufl.cise.util.URLLineReader
import fileproc.RemoteGPGRetrieval
import streamcorpus.StreamItem
import spark.RDD
import spark.SparkContext
import spark.SparkContext._


class CachedFaucet(
  val sc:SparkContext = new SparkContext("local[32]", "gatordsr", "$YOUR_SPARK_HOME", List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar")),
  val sr:StreamRange
  ) extends Faucet with Logging with Serializable {

  /** Used internally to identify fileNames and dates */
  protected case class CacheKey(val date:String, val fileName:String)

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

      sr.getFileList.map{ _ match { 
        case (date,file) => new CacheKey(date,file)
        case _ => throw new Exception("Invalid format") }
      }
  }

  /** 
    * This is a public function to get a list of all the files
    * that this faucet class will iterator over
    */
  final def getKeyList = keyList.map(x => (x.date, x.fileName)) 
  

  /**
   * This class is an internal iterator that makes each
   * file a Seq object. It also iterate serially over
   * the files.
   */ 
  protected class StreamIteratorSeq
  extends Iterator[Iterator[StreamItem]] {
    val iterator = keyList.iterator

    override def hasNext:Boolean = {
      iterator.hasNext
    }

    override def next:Iterator[StreamItem] ={
      val c:CacheKey = iterator.next
      getStreamCompressed(c.date, c.fileName)
    }
  }
  
  /**
   * This class is an internal iterator that makes each
   * file a RDD object. It also iterate serially over
   * the files.
   */ 
  protected class StreamIterator
  extends Iterator[RDD[StreamItem]] {
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
      val rdd = getStreamCompressed(c.date, c.fileName)
      sc.parallelize(rdd.toSeq)
      // If it is not, get it and put it in the cache
    }
  }


  /**
    * This parallel iterator is for iterating over gpg files. 
    * We turn each gpg file into an RDD[StreamItem].
    * This implementation was taken from 
    * http://docs.scala-lang.org/overviews/parallel-collections/custom-parallel-collections.html
    */
  protected class PStreamIterator
    extends parallel.immutable.ParSeq[RDD[StreamItem]] {

    val docs = keyList.toArray

    override def size: Int = docs.size 
    def length: Int = size

    def apply(i: Int):RDD[StreamItem] = {
      val c:CacheKey = docs(i)
      val rdd = getStreamCompressed(c.date, c.fileName)
      sc.parallelize(rdd.toSeq)
    }

    def seq = {
      iterator
      .toSeq
      .asInstanceOf[scala.collection.immutable.Seq[spark.RDD[streamcorpus.StreamItem]]]
    }

    def splitter = new ParFileSplitter(docs, 0, docs.size)

    class ParFileSplitter(private var fileDocs:Seq[CacheKey],
                          private var current:Int,
                          private var totalSize:Int)
    extends SeqSplitter[RDD[StreamItem]] {

      var signalDelegate: Signalling = IdleSignalling
   
      final def hasNext:Boolean = current < totalSize

      final def next:RDD[StreamItem] = {
        val c:CacheKey = docs(current)
        current += 1
        val rdd = getStreamCompressed(c.date, c.fileName)
        sc.parallelize(rdd.toSeq)
      }

      def remaining = totalSize - current

      def dup = new ParFileSplitter(docs, current, totalSize)

      def split = {
        // TODO we can make this split in larger bundles...
        val rem = remaining
        if (rem >= 2) psplit(rem / 2, rem - rem/2)
        else Seq(this)
      }

      def psplit(sizes: Int*): Seq[ParFileSplitter] = {
        val splitted = new ArrayBuffer[ParFileSplitter](sizes.sum)
        for (sz <- sizes) {
          val next = (current + sz) min totalSize
          splitted += new ParFileSplitter(docs, current, next)
          current = next
        }
        if (remaining > 0) splitted += new ParFileSplitter(docs, current, totalSize)
        splitted
      }
    }
  }

  /**
   * This class is an internal iterator that makes each
   * file a Seq object. It also iterate serially over
   * the files.
   */ 
  protected class PStreamIteratorSeq
    extends parallel.immutable.ParSeq[Iterator[StreamItem]] {

    val docs = keyList.toArray

    override def size: Int = docs.size 
    def length: Int = size

    def apply(i: Int):Iterator[StreamItem] = {
      val c:CacheKey = docs(i)
      getStreamCompressed(c.date, c.fileName)
    }

    def seq = {
      iterator
      .toSeq
      .asInstanceOf[scala.collection.immutable.Seq[Iterator[streamcorpus.StreamItem]]]
    }

    def splitter = new ParFileSplitter(docs, 0, docs.size)

    class ParFileSplitter(private var fileDocs:Seq[CacheKey],
                          private var current:Int,
                          private var totalSize:Int)
    extends SeqSplitter[Iterator[StreamItem]] {

      var signalDelegate: Signalling = IdleSignalling
   
      final def hasNext:Boolean = current < totalSize

      final def next:Iterator[StreamItem] = {
        val c:CacheKey = docs(current)
        current += 1
        getStreamCompressed(c.date, c.fileName)
      }

      def remaining = totalSize - current

      def dup = new ParFileSplitter(docs, current, totalSize)

      def split = {
        // TODO we can make this split in larger bundles...
        val rem = remaining
        if (rem >= 2) psplit(rem / 2, rem - rem/2)
        else Seq(this)
      }

      def psplit(sizes: Int*): Seq[ParFileSplitter] = {
        val splitted = new ArrayBuffer[ParFileSplitter](sizes.sum)
        for (sz <- sizes) {
          val next = (current + sz) min totalSize
          splitted += new ParFileSplitter(docs, current, next)
          current = next
        }
        if (remaining > 0) splitted += new ParFileSplitter(docs, current, totalSize)
        splitted
      }
    }
  }

  /**
    * This fetches a particular file and turns it into an Array.
    */
//  def getStreamCompressed(date:String, fileName:String): Iterator[StreamItem] = {
//    logInfo("getStreamCompressed")
//    val xzGPG = grabGPGCompressed(date, fileName)
//    val is = new ByteArrayInputStream(xzGPG.toByteArray)
//    val bais = new XZCompressorInputStream(is)
//    val transport = new TIOStreamTransport(bais)
//    transport.open
//    val protocol = new TBinaryProtocol(transport)
//    assert(transport.isOpen)
//
//   Iterator.continually(mkStreamItem(protocol)) //TODO adds items one bye one to the stream
//      .takeWhile(_ match { case None => transport.close; xzGPG.reset; false; case _ => true })
//      .map { _.get }
//      //.toArray
//
//    // If we keep ths reset/close here it cause the items not to be read
//    //xzGPG.reset // Stop a leak
//    //transport.close
//  }
  
  def getStreamCompressed(date:String, fileName:String): Iterator[StreamItem] = {
     RemoteGPGRetrieval.getStreams(date, fileName).toIterator
  }



  /**Iterators****************************************************************/
  /** This is a serial itterator that uses RDDs for each object */
  lazy val iterator:Iterator[RDD[StreamItem]] = new StreamIterator

  /** A parallel stream iterator that uses RDD for collections */
  lazy val pIterator:PStreamIterator = new PStreamIterator

  /** A streamm iterator that does not use RDD */
  lazy val sIterator:Iterator[Iterator[StreamItem]] = new StreamIteratorSeq

  /** A parallel stream iterator that does not use RDD */
  lazy val psIterator:PStreamIteratorSeq = new PStreamIteratorSeq

  //def createNewStreamIterator = new StreamIterator <-- Iterate as a stream instead of chuncks
  /**************************************************************************/

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
    sr.addToDate("2012-05-02")
    sr.addToHour(0)
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
