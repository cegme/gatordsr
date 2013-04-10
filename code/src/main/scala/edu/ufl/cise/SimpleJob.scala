/*** SimpleJob.scala ***/
package edu.ufl.cise

import spark.SparkContext
import spark.streaming.StreamingContext


object SimpleJob extends Application {
  var sc: SparkContext = null
  var stc: StreamingContext = null
}
