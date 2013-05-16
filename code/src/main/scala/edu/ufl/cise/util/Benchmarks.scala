package edu.ufl.cise.util

import java.util.concurrent.TimeUnit

import com.google.common.base.Stopwatch

import scala.collection.JavaConversions._

import spark.SparkContext

import edu.ufl.cise.{CachedFaucet, Logging, StreamRange}

object Benchmark extends Logging {


  def main(args:Array[String]) {

    val sc = new SparkContext("local[64]", "gatordsr", "$YOUR_SPARK_HOME",
      List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
    
    // Read and count all the file
    val sr = new StreamRange
   sr.addFromDate("2011-10-07")
    sr.addFromHour(14)
    sr.addToDate("2011-10-07")
    sr.addToHour(14)
    val z = new CachedFaucet(sr) 
    //z.getKeyList.foreach(x => logInfo("%s/%s".format(x._1,x._2)))
    val stopwatch = new Stopwatch

    // Time to get all the RDDS
    stopwatch.start
    val data = z.pIterator.reduce(_ union _)
    stopwatch.stop

    val timeGetAllRDDS = stopwatch.elapsed(TimeUnit.MILLISECONDS)

    // Time to count all the RDDS
    stopwatch.reset
    stopwatch.start
    val siCount = data.count
    stopwatch.stop

    val timeSiCount = stopwatch.elapsed(TimeUnit.MILLISECONDS)


    // Search for a string in the si items bodies
    // And count all the si items with that string
    stopwatch.reset
    stopwatch.start
    val searchString1 = "roosevelt"
    //val stringCount = z.iterator.map(rdd => rdd.count).sum
    val stringCount = z.iterator.map(rdd => rdd.filter(si => si.body.getRaw != null)
                                               .filter(si => new String(si.body.getRaw.array, "utf-8").toLowerCase.contains(searchString1))
                                               .count)
                                 .sum
    //val stringCount = z.iterator.map(rdd => rdd.filter(si => new String(si.body.cleansed.array, "UTF-8").toLowerCase.contains(searchString1)).count).sum
    stopwatch.stop

    val timeStringSearch = stopwatch.elapsed(TimeUnit.MILLISECONDS)

    logInfo("Time to GetAllRDDS: %s ms".format(timeGetAllRDDS.toString))
    logInfo("Time to count the %d items in the RDD: %s ms".format(siCount, timeSiCount))
    logInfo("Time to find %d instances of '%s': %s ms".format(stringCount, searchString1, timeStringSearch))


  }
}
