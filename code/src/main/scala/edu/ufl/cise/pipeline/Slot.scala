package edu.ufl.cise.pipeline

import java.util.ArrayList
import scala.io.Source

class Slot(entity_type:String, slot:String){

  val names = new ArrayList[String] // the list of alias names for Slot, extracted from the WordNet
  
  // initialize names from the ontology file
  Source.fromFile("resources/ontology/" + entity_type.toLowerCase() + "_" + slot.toLowerCase() + ".txt")
  .getLines().foreach(name => names.add(name))
  
  def add(name:String) = names.add(name) // add one more alias name for the slot
  
}