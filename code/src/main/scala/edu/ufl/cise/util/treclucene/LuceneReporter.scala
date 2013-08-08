package edu.ufl.cise.util.treclucene

import edu.ufl.cise.pipeline.Preprocessor
import scala.collection.JavaConversions.asScalaBuffer
import edu.ufl.cise.pipeline.Entity
import edu.cise.ufl.util.treclucene.Searcher


object LuceneReporter extends App {

  println("Hi")

  Preprocessor
    .initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json");
  val elist = Preprocessor.entity_list
  val entities = elist.toArray(Array[Entity]())
  
  entities.foreach(e => {
    Searcher.searchEntity("", e.names)
  })
  
}