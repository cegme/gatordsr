package edu.ufl.cise.util

import scala.util.parsing.json.JSON
import scala.io.Source
import edu.ufl.cise.pipeline.Preprocessor

object EntityList {

  def main(args: Array[String]): Unit = {
     
    Preprocessor.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json")
  }

}