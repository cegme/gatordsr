package edu.ufl.cise

import spark.streaming.StreamingContext
import spark.SparkContext
import spark.streaming.Seconds
/**
 * Here we will add the functionality to utilize spark features.
 */
object SparkIntegrator {

  val sc = new SparkContext("local[64]", "gatordsr", "$YOUR_SPARK_HOME",
    List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
//  val ssc = new StreamingContext("local[2]", "gatordsrStreaming", Seconds(2),
//    "$YOUR_SPARK_HOME", List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
  val NUM_SLICES = 16
  
}