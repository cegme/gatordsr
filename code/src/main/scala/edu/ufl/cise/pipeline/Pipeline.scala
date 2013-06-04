package edu.ufl.cise.pipeline



import edu.ufl.cise.Logging
import streamcorpus.StreamItem
import streamcorpus.Sentence
import java.util.ArrayList
import edu.ufl.cise.KBAOutput
import edu.ufl.cise.RemoteGPGRetrieval
import streamcorpus.Token
import java.lang.Integer
import scala.io.Source
import java.io.PrintWriter
import org.apache.thrift.protocol.TProtocol
import streamcorpus.OffsetType


object Pipeline extends Logging {

  /** This keeps track of how many times run is called. */
  val num = new java.util.concurrent.atomic.AtomicInteger
  
  val entity_list = new ArrayList[Entity]
  Preprocessor.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json", entity_list)
  lazy val entities = entity_list.toArray(Array[Entity]())
  
  val pattern_list = new ArrayList[Pattern]
  Preprocessor.initPatternList("resources/test/pattern.txt", pattern_list)
  lazy val patterns = pattern_list.toArray(Array[Pattern]())

  logInfo("entities and patterns are loaded")
  
  // preprocessing
  // SimpleJob.filterSentences(3000)
  
  //logInfo("entities and patterns are loaded")
  
  KBAOutput
  
  logInfo("start to generate results")
  
  annotate()

  def main(args : Array[String]){
    
  }
  
  def annotate() = {
    val lines = Source.fromFile("resources/test/ss.txt").getLines().slice(1000, 3000)
    var num = 1
    lines.foreach( line => {
      // parse parameters
      val array = line.split(" ")
      // get that sentence
      val sentence = SimpleJob.getRemoteSentence(array(0), array(1), Integer.parseInt(array(2)), Integer.parseInt(array(3)))
      //val sentence = SimpleJob.getLocalSentence(array(0), array(1), Integer.parseInt(array(2)), Integer.parseInt(array(3)))
      // get the list of lingpipe entities
      logInfo("processing: " + num + " " + array(0) + " " + array(1))
      val entity_list = new LingSentence(sentence).extractEntities()
      // get the token array
      val tokens = sentence.getTokens().toArray(Array[Token]())
      //println("the original sentence: " + SimpleJob.transform(tokens))
      val target = entities(Integer.parseInt(array(4)))
      val index = getCorresEntity(target, entity_list, array(5))
      //println("find the corresponding entity: " + index)
      if (index != -1){
        // start to try to find all the patterns fit for that entity
        val entity = entity_list.get(index)
        closePatternMatch(entity, index, tokens, entity_list)
      }
      
      num = num + 1
      
      // find the possible two nearest patterns
      def closePatternMatch(entity : LingEntity, index : Integer, 
      tokens : Array[Token], entities : ArrayList[LingEntity]){
        // the first entity
        if(index == 0 && index != entities.size() -1){
          //println("As the first entity")
          val target = entities.get(index + 1)
          // match right nearest patterns         
          getKBAOutput(entity, target, tokens, 1, array)
        }      
        // the last entity
        if(index != 0 && index == entities.size() - 1){
          //println("As the last entity")
          val target = entities.get(index - 1)
          // match right nearest patterns
          getKBAOutput(entity, target, tokens, 0, array)
        }
        // the entity in the middle
        if(index != 0 && index != entities.size() -1 ){         
          //println("entity in the middle")
          val target1 = entities.get(index + 1)
          // match right nearest patterns
          getKBAOutput(entity, target1, tokens, 1, array)
          
          val target0 = entities.get(index - 1)
          // match right nearest patterns
          getKBAOutput(entity, target0, tokens, 0, array)      
        }        
      }   
    })
    
  }
  
  def getKBAOutput(entity:LingEntity, target:LingEntity, tokens : Array[Token], direction : Integer, array: Array[String]){
    if (direction == 0){
      val pats = findClosePattern(entity, target, "left")
      //println("patterns: " + pats)
      val s = SimpleJob.transform(tokens.slice(target.end + 1, entity.begin))
      //println(s + ": to be matched")
      pats.toArray(Array[Pattern]()).foreach(pattern => {
      // match each pattern here
        if (s.toLowerCase().contains(pattern.pattern)){
           // match, create KBA Output
           KBAOutput.add(array(6), entity.topic_id, 1000, array(0), pattern.slot, entity.equiv_id, getByteRange(target, tokens))
        }
      })
    }
    else{
	  val pats = findClosePattern(entity, target, "right")
	  //println("patterns: " + pats)
	  val s = SimpleJob.transform(tokens.slice(entity.end + 1, target.begin))
	  //println(s + ": to be matched")
	  pats.toArray(Array[Pattern]()).foreach(pattern => {
      // match each pattern here
	    if (s.toLowerCase().contains(pattern.pattern)){
		  // match, create KBA Output
		  KBAOutput.add(array(6), entity.topic_id, 1000, array(0), pattern.slot, entity.equiv_id, getByteRange(target, tokens))
        }
     })
    }
  }
  
  def getByteRange(target : LingEntity, tokens:Array[Token]) : String = {
    val first = tokens(target.begin).getOffsets().get(OffsetType.findByValue(1)).first
    val last = tokens(target.end).getOffsets().get(OffsetType.findByValue(1)).first + 
              tokens(target.end).getOffsets().get(OffsetType.findByValue(1)).length
    first + "-" + last
  }
  
  
  def findClosePattern(entity : LingEntity, target : LingEntity, direction : String) = {
    val pats = new ArrayList[Pattern]
    //TODO: find the corresponding patterns that fits the entity
    patterns.foreach(pattern => {
      if (pattern.entity_type.equals(entity.entity_type) && 
          target.entity_type.equals(pattern.target_type) && pattern.direction.equals(direction))
        pats.add(pattern)
    })
    pats
  }
  
  def getCorresEntity(target: Entity, entity_list: ArrayList[LingEntity], name : String) = {
      var index = -1
      for(i <- 0 until entity_list.size()){
        val entity = entity_list.get(i)
        if (entity.entity_type.equals(target.entity_type) && entity.content.contains(name)){
          index = i
          entity.topic_id = target.topic_id
          entity.group = target.group
        }
      }
    index
  }
  
}

class Pipeline() extends Logging with Serializable {
  
 
  
 def transform(tokens:Array[Token]):String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
      sb.append(token.token).append(" ")
    })
    //println(sb)
    sb.toString().toLowerCase()
  }

  
  def run(si:StreamItem) {
    Pipeline.num.incrementAndGet
    // for each sentence, match the pattern
    // TODO: get the sentence string
    si.body.sentences.get("lingpipe").toArray(Array[streamcorpus.Sentence]()).foreach(sentence => {
      //println(sentence.getTokens().toArray().mkString(" "))
      val tokens = sentence.getTokens().toArray(Array[Token]())
      
    })
  }
}
