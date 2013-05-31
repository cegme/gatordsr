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
  SimpleJob.filterSentences(3)
  filterEntities
 
  // from sentences create entities
  def filterEntities = {
    val lines = Source.fromFile("resources/test/ss.txt").getLines()
    lines.foreach( line => {
      //println(line)
      val array = line.split(" ")
      //println(array(0) + " " + array(1) + " " + Integer.parseInt(array(2)) + " " + Integer.parseInt(array(3)))
      val sentence = getSentence(array(0), array(1), Integer.parseInt(array(2)), Integer.parseInt(array(3)))
      
      val ls = new LingSentence(sentence)
      ls.extractEntities
      //println()
      val tokens = sentence.getTokens().toArray(Array[Token]())
      println(SimpleJob.transform(tokens))
      println(SimpleJob.getNER(tokens))
      println(SimpleJob.getMentionID(tokens))
      println(SimpleJob.getEquivID(tokens))
      println(entities(Integer.parseInt(array(4))).topic_id)
      println(ls.entity_list.toArray(Array[LingEntity]()).mkString("---"))
      
    })
  }
  

  

  
  def generateSamples(){
    // TODO: use the entity type and the name to match the entity
    // TODO: with the matched entity, generate samples with the KBAOutput information 
    // so that after annotate we can have proper results
  }  
  
  def annotate(){
    
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
