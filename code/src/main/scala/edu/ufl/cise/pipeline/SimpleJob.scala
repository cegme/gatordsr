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
    readFile("resources/test/entities.psv")
    //readFile("resources/test/total.txt")
    //checkLabels()
  }
  
  def filter(entities: Array[Entity], si:StreamItem)
  {
    val it = si.body.sentences.get("lingpipe").iterator()
    var index = 0
    while(it.hasNext()){
      val sentence = it.next()
      val tokens = sentence.getTokens().toArray(Array[Token]())
      val ss = transform2(tokens)
      findEntity(ss)
      index = index + 1
      // find entity and output the information of the sentence containing the entities
      def findEntity(s:String)
      {
        entities.foreach(e => {
          e.names.toArray(Array[String]()).foreach(name => {
          if(s.toLowerCase().contains(name.toLowerCase())){
            val pw = new PrintWriter(new FileOutputStream(new File("resources/slot/" + si.source + "_" + e.entity_type +"_" + e.group + ".txt"), true))
            // output to a file
            val x = transform(tokens)
            pw.append(name + "|" + si.doc_id+ "|" + si.stream_time.getZulu_timestamp() + "|" + index + "\n" + x + "\n")
            //println("resources/slot/" + e.entity_type +"_" + e.group + ".txt")
            //pw.println(e.entity_type + "---" + e.group + "---" +name + "---" + s + "\n")
            pw.append(getPOS(tokens) + "\n")
            pw.append(getNER(tokens) + "\n")
            pw.append(getMentionID(tokens) + "\n")
            pw.append(getEquivID(tokens) + "\n")
            pw.flush()
            pw.close()
          }
          })
        })
      }
    }
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
  
  def extractEntity(){
    
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
  
  
  def checkLabels(){
    val filename = "social-323-570f2bb2235ab6efe34398cae3f68549-0997c67434c786626c475c3d62a31408.sc.xz.gpg"
    val list = RemoteGPGRetrieval.getStreams("2011-12-31-23", filename)
    val si = list.get(0)
    println(si.body.sentences.isEmpty())
    println(si.body.clean_visible)
    si.body.sentences.get("lingpipe").toArray(Array[streamcorpus.Sentence]()).foreach(sentence => {
    //println(sentence.getTokens().toArray().mkString(" "))
    val tokens = sentence.getTokens().toArray(Array[Token]())
    if(tokens(0).entity_type == null) println("empty")
    println( tokens(0).getToken() + ":" + tokens(0).getEntity_type() + ":" + tokens(0).getEquiv_id() + ":" + tokens(0).getDependency_path() + ":" +  
        tokens(0).getLemma() + ":" +tokens(0).getMention_id()+ ":" + tokens(0).getPos())
    })
  }
  
  def readFile(filename: String){
    //val pw = new PrintWriter("resources/test/labels.txt")
    //val pipe = Pipeline.getPipeline(Pipeline.patterns, Pipeline.queries, Pipeline.dirs)
    var num = 1
    Source.fromFile(filename).getLines.foreach(line => {
      val array = line.split("\\|") 
      //println(num + " " + array(0).split(">")(1) + " " + array(1) + " " + array(2))
      println(num + " " + array(0) + " " + array(1) + " " + array(2))
      //val list = RemoteGPGRetrieval.getStreams(array(0).split(">")(1), array(1))
      val list = RemoteGPGRetrieval.getStreams(array(0), array(1))
      val si = list.get(Integer.parseInt(array(2)))
      //filter(si)
      si.clear()
      num = num + 1
    })
    //pw.close
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
}