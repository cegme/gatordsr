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

import com.google.common.base.Stopwatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.{MILLISECONDS, NANOSECONDS, SECONDS}


object Pipeline extends Logging {

  /** This keeps track of how many times run is called. */
  val num = new java.util.concurrent.atomic.AtomicInteger
  
  // load entities and patterns from files
  val entity_list = new ArrayList[Entity]
  Preprocessor.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json", entity_list)
  lazy val entities = entity_list.toArray(Array[Entity]())
  
  val pattern_list = new ArrayList[Pattern]
  Preprocessor.initPatternList("resources/test/pattern.txt", pattern_list)
  lazy val patterns = pattern_list.toArray(Array[Pattern]())

  logInfo("entities and patterns are loaded")
  
  // preprocessing, to generate indexes for sentences from indexes for stream items
  //SimpleJob.filterSentences(3000) // FIXME SLOOOWWW
  
  
  //logInfo("start to generate results")
  // the main logic, used to generate KBA outputs

  def main(args : Array[String]){
    
  //annotate()
  SimpleJob.filterSentences(3000,args(0)) // FIXME SLOOOWWW
  }
 
  //TODO
def annotateSI(streamItem: StreamItem)={

}

  def annotate(sentence: streamcorpus.Sentence, sentenceStr: String, targetIndex: Int, variable: String) = {
   
     if(s.toLowerCase().contains(name.toLowerCase())){ 

      // get the token array of that sentence
      val tokens = sentence.getTokens().toArray(Array[Token]())

            val array = sentenceStr.split(", ")

      // get the list of lingpipe entities from the stream corpus sentence
//      t_extractEntities.start
      val entity_list = SimpleJob.extractEntities(sentence)
  //    t_extractEntities.stop
   //   t_extractEntities_total += t_extractEntities.elapsed(NANOSECONDS)
//      logInfo("ExtractEntities time: %sns. Total %ssecs. Avg %sns, num: %s".format(t_extractEntities.elapsed(NANOSECONDS), NANOSECONDS.toSeconds(t_extractEntities_total), t_extractEntities_total/num, num))i
    //  t_extractEntities.reset

      // find the entity in the KBA entity list that is matched in this sentence
     //val target = entities(Integer.parseInt(array(4)))
      val target = entities(targetIndex)

     // val index = getCorresEntity(target, entity_list, array(5))
       val index = getCorresEntity(target, entity_list, variable)


      if (index != -1){// when finding the target index in the list of Ling Entities, try to match the patterns in that sentence
        // start to try to find all the patterns fit for that entity
        val entity = entity_list.get(index)
        closePatternMatch(entity, index, tokens, entity_list, array)
      }
      
    }
  }
  
  // find the possible results by looking at two nearest entities
  def closePatternMatch(entity : LingEntity, index : Integer, 
    tokens : Array[Token], entities : ArrayList[LingEntity], array:Array[String]){
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
  
  // for the entity and the target, judge whether there is a pattern matched, if so, generating results.
  def getKBAOutput(entity:LingEntity, target:LingEntity, tokens : Array[Token], direction : Integer, array: Array[String]){
    if (direction == 0){
      // find the patterns that fit with the two entities
      val pats = findClosePattern(entity, target, "left")
      //println("patterns: " + pats)
      val s = SimpleJob.transform(tokens.slice(target.end + 1, entity.begin))
      //println("To be matched: " + s)
      pats.toArray(Array[Pattern]()).foreach(pattern => {
      // match each pattern here
        if (s.toLowerCase().contains(pattern.pattern)){
           // match, create KBA Output
           val comment = "# " + target.content + " " + s + " " + entity.content + " --- " + SimpleJob.transform(tokens)
           KBAOutput.add(array(6), entity.topic_id, 1000, array(0), pattern.slot, entity.equiv_id, getByteRange(target, tokens), comment)
        }
      })
    }
    else{
      // find the patterns that fit with the two entities
	  val pats = findClosePattern(entity, target, "right")
	  //println("patterns: " + pats)
	  val s = SimpleJob.transform(tokens.slice(entity.end + 1, target.begin))
	  //println(s + ": to be matched")
	  pats.toArray(Array[Pattern]()).foreach(pattern => {
      // match each pattern here
	    if (s.toLowerCase().contains(pattern.pattern)){
		  // match, create KBA Output
	      val comment = "# " + entity.content + " " + s + " " + target.content + " --- " + SimpleJob.transform(tokens)
		  KBAOutput.add(array(6), entity.topic_id, 1000, array(0), pattern.slot, entity.equiv_id, getByteRange(target, tokens), comment)
        }
     })
    }
  }
  
  // get the byte range of one LingPipe Entity
  def getByteRange(target : LingEntity, tokens:Array[Token]) : String = {
    val first = tokens(target.begin).getOffsets().get(OffsetType.findByValue(1)).first
    val last = tokens(target.end).getOffsets().get(OffsetType.findByValue(1)).first + 
              tokens(target.end).getOffsets().get(OffsetType.findByValue(1)).length
    first + "-" + last
  }

  // find the corresponding patterns for two entities
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
  
  // get the corresponding entity for the name that is matched in the sentence
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
