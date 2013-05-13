package edu.ufl.cise

import java.util.ArrayList
import opennlp.tools.chunker.ChunkerME
import opennlp.tools.chunker.ChunkerModel
import java.io.FileInputStream
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.postag.POSModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel

object Pattern extends Logging {
  
  val entity_list = new ArrayList[Entity]
  val slot_list = new ArrayList[Slot]
  val pattern_list = new ArrayList[Pattern]
  
  def main(args: Array[String]){
    chunking("AAbraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865.")
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
  
  def chunking(s:String){
    
    // opennlp tokenizer, postagger and chunker
    val tokenizer = new TokenizerME(new TokenizerModel(this.getClass().getClassLoader().getResourceAsStream("en-token.bin")))
	val tagger = new POSTaggerME(new POSModel(this.getClass().getClassLoader().getResourceAsStream("en-pos-maxent.bin")))
    val chunker = new ChunkerME(new ChunkerModel(this.getClass().getClassLoader().getResourceAsStream("en-chunker.bin")))
    
    
    val sent = tokenizer.tokenize(s)

    val pos = tagger.tag(sent)

    val tag = chunker.chunk(sent, pos).toList;
    //val probs = chunker.probs().toSeq;
    //val topSequences = chunker.topKSequences(sent, pos).toSeq;
    
    println(tag)
    //println(probs)
    //println(topSequences)
    
  }

  

  
}

class Pattern(entity:Entity, slot:Slot, regex:String){
  var relation:Triple = null // generate the corresponding result relation triple
  
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
  // the trec kba ccr ssf topic json file
  
}

class Slot(slot:String, names:Array[String]){
  // slot represents the slot type, may change into integer instead of string
  // names is the list of all the alias names extracted from the WordNET
  
}