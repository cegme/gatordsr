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


object Pipeline extends Logging {

  /** This keeps track of how many times run is called. */
  val num = new java.util.concurrent.atomic.AtomicInteger
  
  val entity_list = new ArrayList[Entity]
  Preprocessor.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json", entity_list)
  lazy val entities = entity_list.toArray(Array[Entity]())
  // store sentence information into the file
  //SimpleJob.filterSentences(1000)
  filterEntities
 
  // from sentences create entities
  def filterEntities = {
    val pw = new PrintWriter("resources/test/ee.txt")
    val lines = Source.fromFile("resources/test/ss.txt").getLines()
    lines.foreach( line => {
      //println(line)
      val array = line.split(" ")
      //println(array(0) + " " + array(1) + " " + Integer.parseInt(array(2)) + " " + Integer.parseInt(array(3)))
      val sentence = getSentence(array(0), array(1), Integer.parseInt(array(2)), Integer.parseInt(array(3)))
      
      val ls = new LingSentence(sentence)
      val entity_list = ls.extractEntities()
      //println()
      val tokens = sentence.getTokens().toArray(Array[Token]())
      //println(SimpleJob.transform(tokens))
      //println(SimpleJob.getNER(tokens))
      //println(SimpleJob.getMentionID(tokens))
      //println(SimpleJob.getEquivID(tokens))
      //println(entities(Integer.parseInt(array(4))).topic_id)
      //println(ls.entity_list.toArray(Array[LingEntity]()).mkString("---"))
      
      val target = entities(Integer.parseInt(array(4)))
      var index = 0
      pw.print(target.entity_type + "-" + target.group + "---")
      for(i <- 0 until entity_list.size()){
        val entity = entity_list.get(i)
        if (index < entity.begin) pw.print(SimpleJob.transform(tokens.slice(index, entity.begin)) + "- ")
        if (entity.entity_type.equals(target.entity_type) && entity.content.contains(array(5))){
          // find the target entity
          pw.print("{" + entity.content + "} - ")
        }
        else pw.print("[" + entity.content + "] - ")
        index = entity.end + 1
      }
      pw.print("\n")
      pw.flush()
    })
    pw.close()
  }
   
  

  
  def generateSamples(){
    // TODO: use the entity type and the name to match the entity
    // TODO: with the matched entity, generate samples with the KBAOutput information 
    // so that after annotate we can have proper results
    // TODO: change the KBAOutput
    // the group the entity_type and other things
  }  
  
  def findSlot(entity : LingEntity, tokens : Array[Token]){
    // val pattern = new Pattern("", "", "", "", 1)
    
    //TODO: find the corresponding patterns that fits the entity
    
    //TODO: for each pattern, match according to directions and 
  }
  
  def patternMatch(pattern : Pattern, entity : LingEntity, tokens : Array[Token]){
    // match pattern
    val size = tokens.size
    
    if (pattern.dir == 0){ // match left
      val s = tokens.slice(0, entity.begin)
      // TODO: take care of the null string
      if (pattern.target_type2 == null){ // normal patterns
          if (s.contains(pattern.pattern)){ // find the match
          // create a slot using KBAOutput Information
        }
        
      }
      else{ // contact_meet_place_time for PER
          if (s.contains(pattern.pattern)){ // find the match
          // create a slot using KBAOutput Information
        }
      }
    }
    
    else { // match right
      
      val s = tokens.slice(entity.end + 1, size) // the string to be matched
      
      if (pattern.target_type2 == null){ // normal patterns
        if (s.contains(pattern.pattern)){ // find the match
          // create a slot using KBAOutput Information
        }
        
      }
      else{ // contact_meet_place_time for PER
        if (s.contains(pattern.pattern)){ // find the match
          // create a slot using KBAOutput Information
        }
      }
    }
    
  }
  
    // get the specified stream item
  def getStreamItem(date_hour : String, filename : String, num : Integer) = RemoteGPGRetrieval.getStreams(date_hour, filename).get(num)
  // get the specified sentence
  def getSentence(date_hour : String, filename : String, num : Integer, sid : Integer) = 
    RemoteGPGRetrieval.getStreams(date_hour, filename).get(num).body.sentences.get("lingpipe").get(sid)
  
  def main(args: Array[String]) {
    //filterSentences()
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
