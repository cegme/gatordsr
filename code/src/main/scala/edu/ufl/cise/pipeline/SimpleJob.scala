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

object SimpleJob extends Logging{
  
  val entity_list = new ArrayList[Entity]
  Preprocessor.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json", entity_list)
  //println("entity list size" + entity_list.size())
  lazy val entities = entity_list.toArray(Array[Entity]())
    
  def main(args:Array[String]){
    readFile("resources/test/entities.psv")
    //readFile("resources/test/total.txt")
    //checkLabels()
    
  }
  
  def filter(si:StreamItem)
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
    //println(sb)
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
    //2011-12-07-17 social-225-6703bd68f42ee7e87997448e2a9f4b44-68bd868673798ab75ccc676f08e0f13e.sc.xz.gpg 96
    //val filename = "social-225-6703bd68f42ee7e87997448e2a9f4b44-68bd868673798ab75ccc676f08e0f13e.sc.xz.gpg"
    //val list = RemoteGPGRetrieval.getStreams("2011-12-07-17", filename)
    //val si = list.get(96)
/*    val filename = "news-261-b43f91d3b5529ff018ab4a03e3712b6a-f16b7d8deef22d31d07e75a78a8a0a4f.sc.xz.gpg"
    val list = RemoteGPGRetrieval.getStreams("2011-11-11-15", filename)
    val si = list.get(57)*/
    val filename = "social-323-570f2bb2235ab6efe34398cae3f68549-0997c67434c786626c475c3d62a31408.sc.xz.gpg"
    val list = RemoteGPGRetrieval.getStreams("2011-12-31-23", filename)
    val si = list.get(0)
    println(si.body.sentences.isEmpty())
    println(si.body.clean_visible)
/*    si.body.sentences.get("lingpipe").toArray(Array[streamcorpus.Sentence]()).foreach(sentence => {
    //println(sentence.getTokens().toArray().mkString(" "))
    val tokens = sentence.getTokens().toArray(Array[Token]())
    if(tokens(0).entity_type == null) println("empty")
    println( tokens(0).getToken() + ":" + tokens(0).getEntity_type() + ":" + tokens(0).getEquiv_id() + ":" + tokens(0).getDependency_path() + ":" +  
        tokens(0).getLemma() + ":" +tokens(0).getMention_id()+ ":" + tokens(0).getPos())
    })*/
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
      //if(!si.body.labels.isEmpty()) filter(si)
      
      //if(!si.body.labels.isEmpty()) {
        //pw.println(si.body.labels)
        //pw.flush()
        //println(si.body.labels)}
      
      filter(si)
      
      si.clear()

      num = num + 1
    })
    //pw.close
  }
}