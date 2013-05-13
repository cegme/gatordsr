package edu.ufl.cise

import java.util.ArrayList
import opennlp.tools.chunker.ChunkerME
import opennlp.tools.chunker.ChunkerModel
import java.io.FileInputStream

object Pattern extends Logging {
  
  val entity_list = new ArrayList[Entity]
  val slot_list = new ArrayList[Slot]
  val pattern_list = new ArrayList[Pattern]
  
  def main(args: Array[String]){
    chunking()
  }
  
  
  // initialize entity_list, slot_list, pattern_list
  def init(){
    // TODO: initialize the entity list from entity file containing all the 150 entities
    // TODO: initialize the slot list from slot files, 13 files
    // TODO: figure out all these file formats
    // TODO: with the entity_list and the slot_list, initialize all the possible patterns and store them into the pattern list
  }
  
  // test a single string using a single pattern
  def test(s:String, regex:String){
    val pattern = new Pattern(null, null, regex)// create a new pattern
    if (pattern.matches(s))
      log.info("match")
    else
      log.info("no match")
    
      
    

  }
  
  def chunking(){
    
    val modelIn = new FileInputStream("en-chunker.bin");
    val model = new ChunkerModel(modelIn);
    val chunker = new ChunkerME(model);
    
    val sent = Array( "Rockwell", "International", "Corp.", "'s",
    "Tulsa", "unit", "said", "it", "signed", "a", "tentative", "agreement",
    "extending", "its", "contract", "with", "Boeing", "Co.", "to",
    "provide", "structural", "parts", "for", "Boeing", "'s", "747",
    "jetliners", "." )

    val pos = Array( "NNP", "NNP", "NNP", "POS", "NNP", "NN",
    "VBD", "PRP", "VBD", "DT", "JJ", "NN", "VBG", "PRP$", "NN", "IN",
    "NNP", "NNP", "TO", "VB", "JJ", "NNS", "IN", "NNP", "POS", "CD", "NNS",
    "." )

    val tag = chunker.chunk(sent, pos);
    val probs = chunker.probs();
    val topSequences = chunker.topKSequences(sent, pos);
    
    println(tag)
    println(probs)
    println(topSequences)
  }

  

  
}

class Pattern(entity:Entity, slot:Slot, regex:String){
  var relation:Triple // generate the corresponding result relation triple
  
  def matches(s:String):Boolean = 
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