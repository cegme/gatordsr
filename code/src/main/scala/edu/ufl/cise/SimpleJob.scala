/*** SimpleJob.scala ***/
package edu.ufl.cise

import spark.SparkContext
import spark.streaming.StreamingContext
import SparkContext._

object SimpleJob extends Application {
  
  val logFile = "/var/log/syslog" // Should be some file on your system
  
  val sc = new SparkContext("local[2]", "gatordsr", "$YOUR_SPARK_HOME",
    List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
  val logData = sc.textFile(logFile, 2).cache()
  val numAs = logData.filter(line => line.contains("a")).count()
  val numBs = logData.filter(line => line.contains("b")).count()
  println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
}
