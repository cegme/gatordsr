package edu.ufl.cise.pipeline

import java.lang._
import java.util.ArrayList

class Pattern(eid : Int, sid : Int, pattern : String, dir : Integer){
  var entity : String = null
  var slot : String = null
}



class Entity(val entity_type: String, val group: String, val topic_id: String) {

  def this(entity_type: String, group: String, topic_id: String, human_readable: String) = {
    this(entity_type, group, topic_id)
    add(human_readable)
  }

  val names = new ArrayList[String] // the list of alias names for Entity

  def add(name: String) = names.add(name) // add one more alias name for the entity

}

class Slot(entity_type : String, slot:String){

  val names = new ArrayList[String] // the list of alias names for Slot, extracted from the WordNet
  
  def add(name:String) = names.add(name) // add one more alias name for the slot
  
}