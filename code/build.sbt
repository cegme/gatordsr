name := "gatordsr"

version := "0.01"

scalaVersion := "2.9.2"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

scalacOptions ++= Seq("-unchecked", "-deprecation")

//scalacOptions += "memoryMaximumSize=9G"

//javaOptions ++= Seq("-XX:+HeapDumpOnOutOfMemoryError", "-XX:MinHeapFreeRatio=60", "-XX:-PrintGC", "-XX:+UseParallelGC")

//javaOptions ++= Seq("-Xmx9G", "-Xms5G")

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "opennlp maven repo" at "http://repo1.maven.org/maven2/org/apache/opennlp/"   

resolvers += "repo.codahale.com" at "http://repo.codahale.com"

libraryDependencies += "net.sf.jwordnet" % "jwnl" % "1.4_rc3"

libraryDependencies += "edu.mit" % "jwi" % "2.2.3"            

libraryDependencies += "org.apache.thrift" % "libthrift" % "0.9.0"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "1.3.4"

libraryDependencies += "org.spark-project" % "spark-core_2.9.2" % "0.7.0"

libraryDependencies += "org.spark-project" % "spark-streaming_2.9.2" % "0.7.0"

libraryDependencies += "edu.stanford.nlp.models" % "stanford-corenlp-models" % "1.3.4" from "http://scalasbt.artifactoryonline.com/scalasbt/repo/edu/stanford/nlp/stanford-corenlp/1.3.4/stanford-corenlp-1.3.4-models.jar"

libraryDependencies += "com.google.guava" % "guava" % "14.0.1"

libraryDependencies += "edu.washington.cs.knowitall" % "reverb-core" % "1.4.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "org.tukaani" % "xz" % "1.2"

libraryDependencies += "org.apache.commons" % "commons-compress" % "1.5"

libraryDependencies += "org.apache.opennlp" % "opennlp-tools" % "1.5.3"

libraryDependencies += "org.apache.opennlp" % "opennlp-uima" % "1.5.3"

libraryDependencies += "org.apache.opennlp" % "opennlp-maxent" % "3.0.3"

libraryDependencies += "org.apache.opennlp" % "open-nlp-ner-per-models" % "1.5.3" from "http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin"

libraryDependencies += "edu.washington.cs.knowitall.ollie" % "ollie-core_2.9.2" % "1.0.3"

libraryDependencies += "org.maltparser" % "maltparser" % "1.7"

libraryDependencies += "org.apache.lucene" % "lucene-core" % "4.3.1"

//libraryDependencies += "org.apache.lucene" % "lucene-analyzers" % "3.6.2"
libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % "4.3.1"

libraryDependencies += "org.apache.lucene" % "lucene-queryparser" % "4.3.1"

libraryDependencies += "com.codahale" % "jerkson_2.9.1" % "0.5.0"
