package edu.ufl.cise

import java.util.ArrayList

object Pattern {
  
  val entity_list = new ArrayList[Entity]
  val slot_list = new ArrayList[Slot]
  val pattern_list = new ArrayList[Pattern]
  
  // initialize entity_list, slot_list, pattern_list
  def init(){
    // TODO: initialize the entity list from entity file containing all the 150 entities
    // TODO: initialize the slot list from slot files, 13 files
    // TODO: figure out all these file formats
    // TODO: with the entity_list and the slot_list, initialize all the possible patterns and store them into the pattern list
  }
  
}

abstract class Pattern(entity:Entity, slot:Slot, regex:String){
  var relation:Triple // generate the corresponding result relation triple
  
  def patternMatch(s:String):Boolean = 
  {
    // TODO: return whether the target string matches the pattern
	// TODO: generate corresponding triple result for this matched pattern
    return !regex.r.findAllIn(s).isEmpty
  }
  
}

class Entity(addr:String, names:Array[String]){
  // addr represents the ip address of the entity's wikipedia page or twitter page
  // names is the list of all the alias names of that entity
  
}

class Slot(slot:String, names:Array[String]){
  // slot represents the slot type, may change into integer instead of string
  // names is the list of all the alias names extracted from the WordNET
  
}