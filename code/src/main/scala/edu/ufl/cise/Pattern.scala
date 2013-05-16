package edu.ufl.cise

import java.util.ArrayList
import opennlp.tools.chunker.ChunkerME
import opennlp.tools.chunker.ChunkerModel
import java.io.FileInputStream
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.postag.POSModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import scala.util.parsing.json.JSON
import scala.io.Source
import java.io.File

object Pattern extends Logging {
  
  val entity_list = new ArrayList[Entity]
  val slot_list = new ArrayList[Slot]
  val pattern_list = new ArrayList[Pattern]
  
  val tokenizer = new TokenizerME(new TokenizerModel(this.getClass().getClassLoader().getResourceAsStream("en-token.bin")))
  val tagger = new POSTaggerME(new POSModel(this.getClass().getClassLoader().getResourceAsStream("en-pos-maxent.bin")))
  val chunker = new ChunkerME(new ChunkerModel(this.getClass().getClassLoader().getResourceAsStream("en-chunker.bin")))

  
  
  def main(args: Array[String]){
   // println(new Slot("per", "affiliate").names)
    // println(
    // println(new Pattern("xx", "yy").extractFirstNP("Abraham Lincoln is the 16th president of United States"))//)
     new Pattern("xx", "yy").matches("xxabyyaaccddxxyyzz")
	 // test()
  }
  
  // initialize entity_list, slot_list, pattern_list
  def init(){
    // TODO: initialize the entity list from entity file containing all the 150 entities
    initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json")
    // TODO: initialize the slot list from slot files, 13 files
    // TODO: figure out all these file formats
    // TODO: with the entity_list and the slot_list, initialize all the possible patterns and store them into the pattern list  
  }
  
   def initEntityList(filename:String){
   // ($schema,http://trec-kba.org/schemas/v1.1/filter-topics.json)
   val json = JSON.parseFull(Source.fromFile(filename).mkString)
   val map:Map[String,Any] = json.get.asInstanceOf[Map[String, Any]]
   //println(map.iterator.next)
   val entities : List[Any] = map.get("targets").get.asInstanceOf[List[Any]]
   entities.foreach( target => {
     val entity : Map[String,Any] = target.asInstanceOf[Map[String, Any]]
     entity_list.add(new Entity(entity.get("entity_type").toString, 
         entity.get("group").toString, entity.get("target_id").toString))
   })
  }
  
  // test a single string using a single pattern
  def test(){
    // create patterns from the entity and slot
    val bm = new Entity("PER", "bronfman", "http://en.wikipedia.org/wiki/Benjamin_Bronfman")
    bm.add("Benjamin Bronfman")
    bm.add("Bronfman")
    
    val pa = new Slot("PER", "Affiliate")
    pa.names.toArray().foreach(s => {
      bm.names.toArray().foreach(e => {
        pattern_list.add(new Pattern(e.asInstanceOf[String], s.asInstanceOf[String]))
      })
    })
    
    val s = Source.fromFile("resources/test/bm.txt").mkString
    
    // for each pattern in the pattern list, match for some string
    pattern_list.toArray().foreach(p => {
      if(p.asInstanceOf[Pattern].matches(s)) println("match")
    })
    
    
    // TODO: how to efficiently match all that many patterns
    // change the API now make the pattern complicated? instead of creating too many patterns?
  }
  
  def chunking(s:String){
    // opennlp tokenizer, postagger and chunker

    val sent = tokenizer.tokenize(s)
    val pos = tagger.tag(sent)
    val tag = chunker.chunk(sent, pos).toList
    //val probs = chunker.probs().toSeq;
    //val topSequences = chunker.topKSequences(sent, pos).toSeq;
    println(tag)
    //println(probs)
    //println(topSequences)
  }


}


class Pattern(entity:String,id:Int, slot:String, sid:Int){
  var relation:Triple = null // generate the corresponding result relation triple
  val regex = ("(?!"+ entity + ")" + entity + "[a-zA-Z0-9 ]*" + slot + "([a-zA-Z0-9 ]*)").r
  //println(regex)
  
  def this(entity:String, slot:String) = this(entity,-1, slot, -1)
  
  def matches(s:String):Boolean = {
    // TODO: return whether the target string matches the pattern
	// TODO: generate corresponding triple result for this matched pattern
    // regex.findFirstIn(s).
    s match {
      case regex => println("matches")
    } // with many patterns how? 
    
    //println(regex.findFirstIn(s).grouped(1))
    
    println(regex.findAllIn(s).matchData.foreach(m=> println(m.group(1))))
    
    return !regex.findAllIn(s).isEmpty
  }
  
  def extractFirstNP(s:String):String = {
    // using chunking to extract NP from a tail string of the orignal string after the matching point
    
    val sent:Array[String] = Pattern.tokenizer.tokenize(s)
    val pos = Pattern.tagger.tag(sent)
    val tag = Pattern.chunker.chunk(sent, pos).toArray
    // println(tag)
    // val reg = "B-NP [I-NP ]"
    var begin = -1;
    var end = -1
    for (i <- 0 until tag.size){
      if(begin != -1 && end == -1 && !tag(i).equals("I-NP")) end = i
      if(begin == -1 && tag(i).equals("B-NP")) begin = i
     
    }
    
    println(tag.toList)
    println(begin)
    println(end)
    // from array of string to a subarray
    //val result:Array[String] = new Array[String](end - begin)
   //sent.copyToArray(result, begin, end - begin)
    val result = sent.slice(begin, end)
    // result.mkString(" ");
    return result.mkString(" ")
    
    // return sent.toString()
  }
  
}

class Entity(entity_type:String, group:String, topic_id:String){

  val names = new ArrayList[String] // the list of alias names for Entity
  
  //TODO: initialize the names from wikipedia or twitter information from file
  
  def add(name:String) = names.add(name) // add one more alias name for the entity
  
}

class Slot(entity_type:String, slot:String){

  val names = new ArrayList[String] // the list of alias names for Slot, extracted from the WordNet
  
  // initialize names from the ontology file
  Source.fromFile("resources/ontology/" + entity_type.toLowerCase() + "_" + slot.toLowerCase() + ".txt")
  .getLines().foreach(name => names.add(name))
  
  def add(name:String) = names.add(name) // add one more alias name for the slot
  
}