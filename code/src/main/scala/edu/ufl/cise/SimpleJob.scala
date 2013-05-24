///*** SimpleJob.scala ***/
//package edu.ufl.cise
//
//import java.util.concurrent.TimeUnit
//
//import scala.collection.mutable.ListBuffer
//import scala.collection.JavaConversions._
//import scala.util.DynamicVariable
//
//import com.google.common.base.Stopwatch
//
//import spark.SparkContext
//import spark.SparkContext._
//import spark.streaming.StreamingContext
//
//import edu.ufl.cise.pipeline._
//
//object SimpleJob extends Logging {
//  
//  def testStringFilter(word:String = "roosevelt") {
//
//    val sr = new StreamRange
//    sr.addFromDate("2011-10-07")
//    sr.addFromHour(14)
//    sr.addToDate("2011-10-07")
//    sr.addToHour(15)
//    val z = new CachedFaucet(SparkIntegrator.sc,sr)
//
//    lazy val z1 = z.iterator.reduce(_ union _) // Combine RDDS
//
//    val filtered = z1.map(si => si.body.getClean_visible().toLowerCase())
//                    .filter(s => s.contains("roosevelt"))
//
//    filtered.foreach(s => logInfo("%s\n\n\n\n".format(s)))
//    logInfo("filtered.count: %d".format(filtered.count))
//  }
//
//  def testPipeline {
//
//    val query = new SSFQuery("roosevelt", "president of")
//    val pipe = Pipeline.getPipeline(query)
//
//    val sr = new StreamRange
//    sr.addFromDate("2011-10-07")
//    //sr.addFromHour(14)
//    //sr.addToDate("2011-10-07")
//    //sr.addToHour(14)
//    sr.addToDate("2011-10-08")
//    val sc = new SparkContext("local[64]", "gatordsr", "$YOUR_SPARK_HOME",
//      List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
//    val z = new CachedFaucet(SparkIntegrator.sc, sr)
//    //val z = new CachedFaucet(SparkIntegrator.sc, sr)
//
//    logInfo("About to start the iterator")
//
//    val toc = new Stopwatch
//    toc.start
//
//    //val watch = sc.broadcast(stopwatch)
//    val siCount = sc.accumulator(0)
//    
//    z.psIterator
//      .foreach{rdd =>
//        //logInfo("RDD size: %d".format(rdd.count))
//        val tuples = rdd.withFilter{ si => si.body != null &&
//                    si.body.getClean_visible() != null && 
//                    si.body.getClean_visible().length() > 0 //&&
//                    //new String(si.body.cleansed.array, "UTF-8").toLowerCase.indexOf(query.entity) > 0
//                    //si.body.cleansed.array.toArray.indexOfSlice(query.entity.getBytes) > 0
//        }
//        .withFilter( si => si.body.getClean_visible().toLowerCase.contains(query.entity))
//        .flatMap{ si => 
//          logInfo("File %s size: %d".format(si.doc_id, si.body.getClean_visible().length))
//          siCount += 1
//          val body = si.body.getClean_visible().toLowerCase
//          pipe.run(body)
//        }
//        .foreach(t => logInfo("Tuple: %s".format(t)))
//        //tuples.foreach(t => logInfo("Tuple: %s".format(t)))
//        logInfo("Total si's: %d".format(siCount.value))
//        System.gc
//      }
//      System.gc
//       
//      toc.stop
//      logInfo("Processed %d StreamItems in %s seconds".format(siCount.value, toc.elapsed(TimeUnit.SECONDS)))
//      sc.stop
//      logInfo("%s Stopped the Spark Context".format("*40"))
//
//  }
//
//
//  def testWhileLoopPipeline {
//
//    val query = new SSFQuery("roosevelt", "president of")
//    val pipe = Pipeline.getPipeline(query)
//
//    val sr = new StreamRange
//    sr.addFromDate("2011-10-05")
//    sr.addFromHour(0)
//    //sr.addToDate("2011-10-07")
//    //sr.addToHour(14)
//    sr.addToDate("2011-10-05")
//    sr.addToHour(0)
//    val sc = new SparkContext("local[64]", "gatordsr", "$YOUR_SPARK_HOME",
//      List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
//    val z = new CachedFaucet(sc, sr)
//    //val z = new CachedFaucet(SparkIntegrator.sc, sr)
//
//    logInfo("About to start the iterator")
//
//    val toc = new Stopwatch
//    toc.start
//
//    //val watch = sc.broadcast(stopwatch)
//    val siCount = sc.accumulator(0)
//
//    //val tuplesList = new ListBuffer
//    
//    val gpgIterator = z.psIterator
//
//    gpgIterator.foreach{ gpgFile => 
//
//      gpgFile
//        .withFilter{_.body != null}
//       // .withFilter{_.body.getClean_visible() != null}
//        .withFilter{_.body.getClean_visible() != null}
//        //.withFilter{_.body.cleansed.array.containsSlice("oosevelt")}
//        .withFilter{si => si.body.getClean_visible().toLowerCase.contains(query.entity)}
//        .foreach{ si => 
//          siCount += 1
//          
//          val body = si.body.getClean_visible().toLowerCase
//          
//          // Run the pipeline code
//          //val tuples = pipe.run(body)
//          //logInfo("Tuple size: %d".format(tuples.size))
//          
//        }
//        System.gc
//        logInfo("gpgFile.ize: %s".format(gpgFile.size))
//    }
//
//    toc.stop
//    logInfo("Processed %d StreamItems in %s seconds".format(siCount.value, toc.elapsed(TimeUnit.SECONDS)))
//    sc.stop
//    logInfo("Stopped the Spark Context %s".format("*"*40))
//    
//
//  }
//
//  def main(args: Array[String]) = {
//  
//    // testStringFilter
//
//    //testPipeline
//
//    testWhileLoopPipeline
//
//  }
//}
