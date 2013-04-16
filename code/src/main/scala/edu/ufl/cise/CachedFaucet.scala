package edu.ufl.cise

import java.io.ByteArrayInputStream

import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TIOStreamTransport

import kba.StreamItem

import spark.SparkContext
import spark.RDD
import spark.storage.StorageLevel
import spark.SparkContext._

import edu.ufl.cise.util.URLLineReader


object CachedFaucet extends Faucet with Logging {

  def getRDD(sc:SparkContext, date:String, hour:Int): Iterator[RDD[StreamItem]] = {
    val directoryName = getDirectoryName(date, hour)
    val reader = new URLLineReader(BASE_URL + format(directoryName))
    val html = reader.toList.mkString
    val pattern = """a href="([^"]+.gpg)""".r
   
    System.gc();
    pattern.findAllIn(html).matchData
      .map(m => getRDD(sc, directoryName, m.group(1)))
  }



  def getRDD(sc:SparkContext, date:String, fileName:String): RDD[StreamItem] = {
    val data = grabGPG(date, fileName)
    val bais = new ByteArrayInputStream(data.toByteArray())
    val transport = new TIOStreamTransport(bais)
    transport.open()
    val protocol = new TBinaryProtocol(transport)

    // Stop streaming after the first None. TODO why? what could happen? end of file?
    val a = Stream.continually(mkStreamItem(protocol)) //TODO adds items one bye one to the stream
      .takeWhile(_ match { case None => transport.close(); false; case _ => true })
      .map { _.get }
      //.toIterator
      //.toArray

    //transport.close()
    val rdd = sc.makeRDD[StreamItem](a).persist(StorageLevel.DISK_ONLY)
    //rdd.checkpoint
    //logInfo("RDD Count: " + rdd.count)
    rdd
    //sc.makeRDD[StreamItem](getStreams(date,fileName).toArray)
  }

  //def getRDD(sc:SparkContext, date:String, hour:Int): Iterator[RDD[StreamItem]] = {
  //  val directoryName = getDirectoryName(date, hour)
  //  val reader = new URLLineReader(BASE_URL + format(directoryName))
  //  val html = reader.toList.mkString
  //  val pattern = """a href="([^"]+.gpg)""".r
  //  
  //  //pattern.findAllIn(html).matchData
  //  //  .map(m => getRDD(sc, directoryName, m.group(1)))
  //  def lazyFileGrabber(fileIter: Iterator[Match]): Iterator[RDD[StreamItem]] = {
  //    def lazyGrab(file: Match): RDD[StreamItem] = {
  //      getRDD(sc, directoryName, file.group(1))
  //    }
  //    fileIter.map { lazyGrab(_) }
  //  }
  //  val it = lazyFileGrabber(pattern.findAllIn(html).matchData)
  //  it
  //}


  def main(args: Array[String]):Unit = {

    //System.setProperty("spark.storage.memoryFraction", "0.66")
    //System.setProperty("spark.rdd.compress", "true")
    //System.setProperty("SPARK_MEM", "8G")
     //logInfo("SPARK_MEM: %s".format(System.getProperty("SPARK_MEM")))
    val sc = new SparkContext("local", "gatordsr", "$YOUR_SPARK_HOME",
      List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
    //sc.setCheckpointDir("/home/cgrant/data/checkpoint/mycheckpoints2")

    lazy val z0 = getRDD(sc, "2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
    lazy val z0b = getRDD(sc, "2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
    logInfo("Stream Count z0: " + z0.count())
    //z0.foreach( si => println(si.schost))
    logInfo("Union count: " + z0.union(z0b).count())

    // Not working yet

    lazy val z1 = getRDD(sc, "2012-05-01", 0)
    logInfo("Stream Count z1: " + z1.foldLeft(0L)((a:Long,b:RDD[StreamItem]) => a + b.count))
  }
}
