/*** SimpleJob.scala ***/
package edu.ufl.cise

import scala.collection.JavaConversions._

import spark.SparkContext
import spark.streaming.StreamingContext
import SparkContext._

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


    val sr = new StreamRange
    sr.addFromDate("2011-10-07")
    sr.addFromHour(14)
    sr.addToDate("2011-10-07")
    sr.addToHour(14)
    //sr.addToDate("2011-10-08")
    val sc = new SparkContext("local[64]", "gatordsr", "$YOUR_SPARK_HOME",
      List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
    val z = new CachedFaucet(sc, sr)
    //val z = new CachedFaucet(SparkIntegrator.sc, sr)

    logInfo("About to start the iterator")
    z.piterator.map{rdd =>
            logInfo("RDD size: %d".format(rdd.count))
            rdd.filter{ si => si.body != null &&
                        si.body.cleansed != null && 
                        si.body.cleansed.array.length > 0 
            }
            //.take(5) // Only take a few of the documents
            .flatMap{ si => 
              //logInfo("%s".format(new String(si.body.cleansed.array, "UTF-8")))
    val query = new SSFQuery("roosevelt", "president of")
    val pipe = Pipeline.getPipeline(query)
              pipe.run(new String(si.body.cleansed.array, "UTF-8").toLowerCase)
            }
            //.take(1) // Only take the first relation
            .collect // Get need to make this process happen
          }
          //.take(10)
          .foreach(t => logInfo("%s".format(t)))
          

     //logInfo("Document count of %s is : %d".format(sr,z.piterator.map{rdd => rdd.count()}.sum))


  }


  def main(args: Array[String]) = {
  
    // testStringFilter

    testPipeline

  }
}
