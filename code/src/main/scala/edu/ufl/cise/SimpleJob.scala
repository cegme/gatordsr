/*** SimpleJob.scala ***/
package edu.ufl.cise

import java.util.concurrent.TimeUnit

import scala.collection.JavaConversions._
import scala.util.DynamicVariable

import com.google.common.base.Stopwatch

import spark.SparkContext
import spark.SparkContext._
import spark.streaming.StreamingContext

object SimpleJob extends Logging {
  
  def testStringFilter(word:String = "roosevelt") {

    val sr = new StreamRange
    sr.addFromDate("2011-10-07")
    sr.addFromHour(14)
    sr.addToDate("2011-10-07")
    sr.addToHour(14)
    val z = new CachedFaucet(SparkIntegrator.sc, sr)

    lazy val z1 = z.iterator.reduce(_ union _) // Combine RDDS

    val filtered = z1.map(si => new String(si.body.cleansed.array(), 
                                            "UTF-8").toLowerCase())
                    .filter(s => s.contains("roosevelt"))

    filtered.foreach(s => logInfo("%s\n\n\n\n".format(s)))
    logInfo("filtered.count: %d".format(filtered.count))
  }

  def testPipeline {

    val query = new SSFQuery("roosevelt", "president of")
    val pipe = Pipeline.getPipeline(query)

    val sr = new StreamRange
    sr.addFromDate("2011-10-07")
    //sr.addFromHour(14)
    //sr.addToDate("2011-10-07")
    //sr.addToHour(14)
    sr.addToDate("2011-10-08")
    val sc = new SparkContext("local[64]", "gatordsr", "$YOUR_SPARK_HOME",
      List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
    val z = new CachedFaucet(sc, sr)
    //val z = new CachedFaucet(SparkIntegrator.sc, sr)

    logInfo("About to start the iterator")

    val toc = new Stopwatch
    toc.start

    //val watch = sc.broadcast(stopwatch)
    val siCount = sc.accumulator(0)
    
    z.pIterator
      .map{rdd =>
        //logInfo("RDD size: %d".format(rdd.count))
        val tuples = rdd.filter{ si => si.body != null &&
                    si.body.cleansed != null && 
                    si.body.cleansed.array.length > 0 //&&
                    //new String(si.body.cleansed.array, "UTF-8").toLowerCase.indexOf(query.entity) > 0
                    //si.body.cleansed.array.toArray.indexOfSlice(query.entity.getBytes) > 0
        }
        .filter( si => new String(si.body.cleansed.array, "UTF-8").toLowerCase.contains(query.entity))
        .flatMap{ si => 
          logInfo("File %s size: %d".format(si.doc_id, si.body.cleansed.array.length))
          siCount += 1
          val body = new String(si.body.cleansed.array, "UTF-8").toLowerCase
          pipe.run(body)
        }
        tuples.foreach(t => logInfo("Tuple: %s".format(t)))
        System.gc
      }
       
      toc.stop
      logInfo("Processed %d StreamItems in %s seconds".format(siCount.value, toc.elapsed(TimeUnit.SECONDS)))
      sc.stop
      logInfo("%s Stopped the Spark Context".format("*40"))

  }


  def main(args: Array[String]) = {
  
    // testStringFilter

    testPipeline

  }
}
