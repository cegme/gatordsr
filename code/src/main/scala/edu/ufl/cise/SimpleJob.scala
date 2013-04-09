/*** SimpleJob.scala ***/



object SimpleJob extends Application {
  val logFile = "/var/log/syslog" // Should be some file on your system
  val sc = new SparkContext("local", "Simple Job", "$YOUR_SPARK_HOME",
    List("target/scala-2.9.2/simple-project_2.9.2-1.0.jar"))
  val logData = sc.textFile(logFile, 2).cache()
  val numAs = logData.filter(line => line.contains("a")).count()
  val numBs = logData.filter(line => line.contains("b")).count()
  println("Lines with a: %s, Lines with b: %s".format(numAs, numBs))
}
