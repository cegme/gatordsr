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
      val array = line.split(" ")
      val sentence = getSentence(array(0), array(1), Integer.parseInt(array(2)), Integer.parseInt(array(3)))
      val ls = new LingSentence(sentence)
      val entity_list = ls.extractEntities()
      val tokens = sentence.getTokens().toArray(Array[Token]())
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
  
  def annnotate = {
    val lines = Source.fromFile("resources/test/ss.txt").getLines()
    lines.foreach( line => {
      // parse parameters
      val array = line.split(" ")
      // get that sentence
      val sentence = getSentence(array(0), array(1), Integer.parseInt(array(2)), Integer.parseInt(array(3)))
      // get the list of lingpipe entities
      val entity_list = new LingSentence(sentence).extractEntities()
      // get the token array
      val tokens = sentence.getTokens().toArray(Array[Token]())
      val target = entities(Integer.parseInt(array(4)))
      val index = getCorresEntity(target, entity_list, array(5))
      if (index != -1){
        // start to try to 
      }
      
      // find the corresponding lingpipe entity with the kba entity 
    })
  }
  
  def getCorresEntity(target: Entity, entity_list: ArrayList[LingEntity], name : String) = {
      var index = -1
      for(i <- 0 until entity_list.size()){
        val entity = entity_list.get(i)
        if (entity.entity_type.equals(target.entity_type) && entity.content.contains(name)){
          index = i
        }
      }
    index
  }
  
  
  
  def findSlot(entity : LingEntity, tokens : Array[Token]){
    // val pattern = new Pattern("", "", "", "", 1)
    
    //TODO: find the corresponding patterns that fits the entity
    
    //TODO: for each pattern, match according to directions and 
  }
  
  def patternMatch(pattern : Pattern, entity : LingEntity, index : Integer, tokens : Array[Token], entities : ArrayList[LingEntity]){
    // match pattern
    val size = tokens.size
    
    if (pattern.dir == 0){ // match left
      val s = SimpleJob.transform(tokens.slice(0, entity.begin))
      // TODO: take care of the null string
      if (pattern.target_type2 == null){ // normal patterns
          if (s.contains(pattern.pattern)){ // find the match
          // create a slot using KBAOutput Information
          val array = s.split(pattern.pattern)
          val m = array(0).split(" ").size
          val index = entities.indexOf(entity)
          val po = findLeftEntity(pattern.entity_type, entities, m, index)
          if (po != -1){
            // TODO: create a result
          }
          
        }
        
      }
      else{ // contact_meet_place_time for PER
          if (s.contains(pattern.pattern)){ // find the match
          // create a slot using KBAOutput Information
        }
      }
    }
    
    else { // match right     
      val s = SimpleJob.transform(tokens.slice(entity.end + 1, size)) // the string to be matched      
      if (pattern.target_type2 == null){ // normal patterns
        if (s.contains(pattern.pattern)){ // find the match
          // create a slot using KBAOutput Information
          val m = s.split(pattern.pattern)(1).split(" ").size
          val p = entity.end + m
          val po = findRightEntity(pattern.entity_type, entities, p, index)
          if (po != -1){
            // TODO: create a row result
          }
        }
        
      }
      else{ // contact_meet_place_time for PER
        if (s.contains(pattern.pattern)){ // find the match
          // create a slot using KBAOutput Information
          
        }
      }
    }
    
  }
  
  def findRightEntity(entity_type : String, entity_list : ArrayList[LingEntity], start : Integer, index : Integer): Integer = {
    var exist = false
    for (i <- index + 1 until entity_list.size()){
      if (entity_list.get(i).begin > start && entity_list.get(i).entity_type.equals(entity_type)){
        exist = true
        return 	i
      }
    }
    -1
  }
  
  def findRightEntity2(entity_type1 : String, entity_type2: String, entity_list : ArrayList[LingEntity], start : Integer, index : Integer): ArrayList[Integer] = {
    var exist = false
    val array = new ArrayList[Integer]
    for (i <- index + 1 until entity_list.size()){
      if (entity_list.get(i).begin > start && (entity_list.get(i).entity_type.equals(entity_type1) ||
          entity_list.get(i).entity_type.equals(entity_type2))){
        exist = true
        array.add(i)
      }
    }
    array
  }
  
  def findLeftEntity(entity_type : String, entity_list : ArrayList[LingEntity], end : Integer, index : Integer): Integer = {
    var exist = false
    for (i <- index -1 to 0 by -1){
      if (entity_list.get(i).end < end && entity_list.get(i).entity_type.equals(entity_type)){
        exist = true
        return 	i
      }
    }
    -1
  }
  
  def findLeftEntity2(entity_type1 : String, entity_type2: String, entity_list : ArrayList[LingEntity], end : Integer, index : Integer): ArrayList[Integer] = {
    var exist = false
    val array = new ArrayList[Integer]
    for (i <- index -1 to 0 by -1){
      if (entity_list.get(i).end < end && (entity_list.get(i).entity_type.equals(entity_type1) ||
          entity_list.get(i).entity_type.equals(entity_type2))){
        exist = true
        array.add(i)
      }
    }
    array
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
