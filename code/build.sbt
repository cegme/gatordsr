name := "gatordsr"

version := "0.01"

scalaVersion := "2.9.2"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "opennlp sourceforge repo" at "http://opennlp.sourceforge.net/maven2"

libraryDependencies += "org.apache.thrift" % "libthrift" % "0.9.0"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "1.3.4"

libraryDependencies += "org.spark-project" % "spark-core_2.9.2" % "0.7.0"

libraryDependencies += "org.spark-project" % "spark-streaming_2.9.2" % "0.7.0"

libraryDependencies += "com.typesafe.akka" % "akka-camel_2.10.0-M7" % "2.1-M2"


libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.3"

libraryDependencies += "edu.stanford.nlp.models" % "stanford-corenlp-models" % "1.3.4" from "http://scalasbt.artifactoryonline.com/scalasbt/repo/edu/stanford/nlp/stanford-corenlp/1.3.4/stanford-corenlp-1.3.4-models.jar"

libraryDependencies += "com.google.guava" % "guava" % "14.0.1"

libraryDependencies += "edu.washington.cs.knowitall" % "reverb-core" % "1.4.1"

libraryDependencies += "edu.washington.cs.knowitall" % "reverb-models" % "1.4.0"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

