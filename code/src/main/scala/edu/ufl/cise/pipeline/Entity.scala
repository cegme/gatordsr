package edu.ufl.cise.pipeline

import java.util.ArrayList

class Entity(entity_type:String, group:String, topic_id:String){

  val names = new ArrayList[String] // the list of alias names for Entity
  
  //TODO: initialize the names from wikipedia or twitter information from file
  
  def add(name:String) = names.add(name) // add one more alias name for the entity
  
}