package edu.ufl.cise.pipeline

import edu.ufl.cise.RemoteGPGRetrieval
import java.io.PrintWriter
import scala.io.Source
import scala.util.parsing.json.JSON
import edu.ufl.cise.Logging
import java.util.ArrayList
import streamcorpus.StreamItem
import streamcorpus.Token
import java.io.FileOutputStream
import java.io.File
import java.lang.Integer

object SimpleJob extends Logging{
  // method library
  def main(args:Array[String]){
  }
  
  def getPOS(tokens:Array[Token]):String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
        sb.append(token.pos).append(" ")
    })
    sb.toString()
  }
  
  def getNER(tokens:Array[Token]):String = {
        var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
        sb.append(token.entity_type).append(" ")
    })
    //println(sb)
    sb.toString()
  }
  
  def getMentionID(tokens:Array[Token]):String = {
        var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
        sb.append(token.mention_id).append(" ")
    })
    //println(sb)
    sb.toString()
  }
  def getEquivID(tokens:Array[Token]):String = {
        var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
        sb.append(token.equiv_id).append(" ")
    })
    //println(sb)
    sb.toString()
  }
  
  def transform(tokens:Array[Token]):String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
        sb.append(token.token).append(" ")
    })
    //println(sb)
    sb.toString()
  }
   
  def transform2(tokens:Array[Token]):String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
      if(token.getEntity_type() != null)
        sb.append(token.token).append(" ")
    })
    //println(sb)
    sb.toString()
  }
  
  def filterSentences (n : Integer) = {
    // read from a file to get all the streamitems, use the filtered entity to filter
    var num = 1
    val pw = new PrintWriter("resources/test/ss.txt")   
    val lines = Source.fromFile("resources/entity/totalEntityList.txt").getLines.slice(0, n)
    lines.foreach(line => {
      // parse one line to get parameters
      val array = line.split(" \\| ") 
      val date_hour = array(0).split(">")(1)
      val filename = array(1)
      val si_num = array(2)
      val topics = line.split("\\|\\| ")(1).split(", ")
      println(num + " " + date_hour + " " + filename + " " + si_num)     
      //println(topics.mkString(" "))
      val ens = findEntity(topics)
      val list = RemoteGPGRetrieval.getStreams(date_hour, filename)
      val si = list.get(Integer.parseInt(si_num))
      val ss = si.body.sentences.get("lingpipe")
      // process all the sentences
      for(i <- 0 until ss.size()){
        val s = SimpleJob.transform2(ss.get(i).getTokens().toArray(Array[Token]()))
        //println(s.toLowerCase())
        //println(s)
        ens.foreach(e => {
          Pipeline.entities(e).names.toArray(Array[String]()).foreach(name => {
            //println(name.toLowerCase())
            if(s.toLowerCase().contains(name.toLowerCase())){
              // print line to the file, containing date_hour, filename, si_num, sentence i and entity index e
              //println(s)
              // println(name)
              //println(num + " " + date_hour + " " + filename + " " + si_num + " " + i + " " + e + " " + Pipeline.entities(e).topic_id)
              pw.println(date_hour + " " + filename + " " + si_num + " " + i + " " + e + " " + name + " " +
                  si.doc_id  + " " +  Pipeline.entities(e).topic_id)
              pw.flush()
              // store sentences into files, too             
            }
          })
        })
      }
      num = num + 1     
    })
    // improve the performance
     pw.close()
    // store the filtered sentences into a file
  }
  
  // find corresponding entities by name
  def findEntity(topics : Array[String]) = {
    // get the indexes of the entities contained in one document
    val ens = new Array[Integer](topics.size)
    var index = 0
    topics.foreach(topic => {
      for (i <-0 until Pipeline.entities.size){
        if(Pipeline.entities(i).topic_id.toLowerCase().equals(topic.toLowerCase())){
          ens(index) = i
          index = index + 1
        }  
      }
    })
    ens
  }
    
    // from sentences create entities
  def filterEntities(entities : Array[Entity]) = {
    val pw = new PrintWriter("resources/test/ee.txt")
    val lines = Source.fromFile("resources/test/ss.txt").getLines()
    lines.foreach( line => {
      val array = line.split(" ")
      val sentence = getRemoteSentence(array(0), array(1), Integer.parseInt(array(2)), Integer.parseInt(array(3)))
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
  
  
  // get the specified stream item
  def getRemoteStreamItem(date_hour : String, filename : String, num : Integer) = RemoteGPGRetrieval.getStreams(date_hour, filename).get(num)
  // get the specified sentence
  def getRemoteSentence(date_hour : String, filename : String, num : Integer, sid : Integer) = 
    RemoteGPGRetrieval.getStreams(date_hour, filename).get(num).body.sentences.get("lingpipe").get(sid)
  
  def getLocalSentence(date_hour : String, filename : String, num : Integer, sid : Integer) = 
    RemoteGPGRetrieval.getStreams(date_hour, filename).get(num).body.sentences.get("lingpipe").get(sid)  
 
      // get the specified stream item
  def getLocalStreamItem(date_hour : String, filename : String, num : Integer) = RemoteGPGRetrieval.getStreams(date_hour, filename).get(num)
    
}

