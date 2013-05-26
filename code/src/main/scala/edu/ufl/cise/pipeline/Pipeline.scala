package edu.ufl.cise.pipeline


import edu.ufl.cise.SSFQuery
import edu.ufl.cise.Triple
import edu.ufl.cise.Logging
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.postag.POSModel
import opennlp.tools.chunker.ChunkerME
import opennlp.tools.chunker.ChunkerModel
import streamcorpus.StreamItem
import streamcorpus.Sentence
import java.util.ArrayList
import edu.ufl.cise.KBAOutput
import java.util.LinkedList
import edu.ufl.cise.RemoteGPGRetrieval
import streamcorpus.Token


object Pipeline extends Logging {

  /** This keeps track of how many times run is called. */
  val num = new java.util.concurrent.atomic.AtomicInteger
  // OpenNLP tokenizer, pos tagger, chunker
  val tokenizer = new TokenizerME(new TokenizerModel(this.getClass().getClassLoader().getResourceAsStream("en-token.bin")))
  val tagger = new POSTaggerME(new POSModel(this.getClass().getClassLoader().getResourceAsStream("en-pos-maxent.bin")))
  val chunker = new ChunkerME(new ChunkerModel(this.getClass().getClassLoader().getResourceAsStream("en-chunker.bin")))
  logInfo("OpenNLP tokenizer, pos tagger, chunker are loaded")
  
  // store all queries and patterns in some file
  // could use more fancy data structure, for example, Pattern class
  val patterns = new ArrayList[String]()
  val queries = new ArrayList[SSFQuery]()
  val dirs = new ArrayList[java.lang.Integer]()
  // add one pattern into list
  def addPattern(entity : String, slot : String, pattern : String, dir:java.lang.Integer){
    patterns.add(pattern)
    queries.add(new SSFQuery(entity, slot))
    dirs.add(dir)
  } 
  // hard-coded patterns
  val pp0 = """(Benjamin Bronfman|Bronfman)(.*)(interest|of|with|in|son)"""
  val pp1 = """(John H. Lang|John Lang)(.*)(join|enlist|with|transfer|serve)"""
  val pf0 = """Stuart Powell Field(.*)(locate|in)""" 
  val pf1 = """in(.*)Stuart Powell Field""" 
  val pf2 = """Fargo Air Museum(.*)(locate|in)"""
  val pf3 = """in.*Fargo Air Museum"""
  val po0 = ("""(drink|drank|met|meet|in)(.*)(GandB Coffee|G & B|G&B|Glanville & Babinski)""" )
  val po1 = ("""(drink|drank|met|meet|in)(.*)(Blossom Coffee)""" )
  
  println(pp0)
  println(pp1)
  println(pf0)
  println(pf1)
  println(pf2)
  println(pf3)
  println(po0)
  println(po1)
  
  // add patterns
  addPattern("Benjamin Bronfman", "affiliate", Pipeline.pp0, 1)
  addPattern("John H. Lang", "affiliate", Pipeline.pp1, 1)
  addPattern("Stuart Powell Field", "affiliate", Pipeline.pf0, 1)
  addPattern("Stuart Powell Field", "affiliate", Pipeline.pf1, 0)
  addPattern("Fargo Air Museum", "affiliate", Pipeline.pf2, 1)
  addPattern("Fargo Air Museum", "affiliate", Pipeline.pf3, 0)
  addPattern("Glanville & Babinski Coffee", "affiliate", Pipeline.po0, 0)
  addPattern("Blossom Coffee", "affiliate", Pipeline.po1, 0)
   
  //logInfo("Pre-coded patterns are loaded")

  // get a Pipeline object for specific text and query
  def getPipeline(patterns:ArrayList[String], queries:ArrayList[SSFQuery], dirs:ArrayList[java.lang.Integer]): Pipeline = new Pipeline(patterns, queries, dirs)
  
  def main(args: Array[String]) {
    val pipe0 = getPipeline(patterns, queries, dirs)
    pipe0.matches("Bronfman has a son named Ikhyd Edgar Arular Bronfman, who was born on February 11, 2009.")
    pipe0.matches("John H. Lang (1889–1970) was an American who served with the Canadian Army in World War I " +
    		"and then with the United States Navy through World War II and the end of his career")
    pipe0.matches("Stuart Powell Field (ICAO: KDVK, FAA LID: DVK) is a public-use airport located 3 nautical miles " +
    		"(5.6 km; 3.5 mi) south of the central business district of Danville")
    pipe0.matches("The Fargo Air Museum is an aviation related museum in Fargo, North Dakota. " +
    		"It is located near Hector International Airport in the northern part of the city.")
    pipe0.matches("the space at LA’s Sqirl has been transformed over the last month into a shared temple of toasts, teas, " +
    		"and endlessly delicious coffee courtesy of G&B, run by Charles Babinski and Kyle Glanville.")
    pipe0.matches("Jim Jackson likes to drink coffee at Blossom Coffee")
    
  }
}

class Pipeline(patterns:ArrayList[String], queries:ArrayList[SSFQuery], dirs:ArrayList[java.lang.Integer] ) extends Logging with Serializable {
  
  // extract the first NP after matched substring
  def extRightNP(s:String):String = {
    // using chunking to extract NP from a string
    val sent:Array[String] = Pipeline.tokenizer.tokenize(s)
    val pos = Pipeline.tagger.tag(sent)
    val tag = Pipeline.chunker.chunk(sent, pos).toArray
    // find the first NP for the right substring
    var begin = -1;
    var end = -1
    var flag = false
    for (i <- 0 until tag.size){
      if(begin != -1 && tag(i).equals("B-NP")) flag = false
      if(begin == -1 && tag(i).equals("B-NP")){begin = i; flag = true}
      if(begin != -1 && flag && tag(i).equals("I-NP")) end = i
    }
    // from array to sub-array
    var result:Array[String] = null
    if (begin != - 1 && end == -1)
     result = sent.slice(begin, begin + 1)
    if (begin != - 1 && end != -1)
     result = sent.slice(begin, end + 1)
    //println(result.mkString(" "))
    return result.mkString(" ")
  }

  def extLeftNP(s:String):String = {
    //println("left NP: " + s)
    val sent:Array[String] = Pipeline.tokenizer.tokenize(s)
    val pos = Pipeline.tagger.tag(sent)
    val tag = Pipeline.chunker.chunk(sent, pos).toArray
    // find the first NP for the left substring
    //println(tag.mkString(" "))
    var begin = -1;
    var end = -1
    var flag = false
    for (i <- tag.size -1  to 0 by -1){
      if(begin == -1 && tag(i).equals("B-NP")) begin = i
      if(begin == -1 && end == -1 && tag(i).equals("I-NP")) end = i
    }
    // from array to sub-array
    var result:Array[String] = null
    if (begin != - 1 && end == -1)
      result = sent.slice(begin, begin + 1)
    if (begin != - 1 && end != -1)
     result = sent.slice(begin, end + 1)
    //println(result.mkString(" "))
    if(result != null && result.length != 0 )
      result.mkString(" ")
    else null
  }
  
  // match pattern and retrieve corresponding triple
  def matches(s : String) = {
    // for each pattern
    for (i <- 0 until patterns.size()){
      val t = s.split(patterns.get(i))
      if (!t.isEmpty && !t(0).equals(s)){
        //println(patterns.get(i))
        if(dirs.get(i) == 0){
          val np = extLeftNP(t(0))
          if (np != null){
            val triple = new Triple (queries.get(i).entity, queries.get(i).slotName, np); 
            KBAOutput.add(triple); 
            //println(triple)
          }
        }
        else{
          if(t.size > 1){
            val np = extRightNP(t(1))
            if (np != null){
              val triple = new Triple (queries.get(i).entity, queries.get(i).slotName, np); 
              KBAOutput.add(triple); 
              //println(triple)
            }
          }
        }
      }
    }
  }
  
  def transform(tokens:Array[Token]):String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
      sb.append(token.token).append(" ")
    })
    //println(sb)
    sb.toString()
  }
  
  // the main logic
  def run(text: String) = {
	println(matches(text))  
    Pipeline.num.incrementAndGet + ""
  }
  
  def run(si:StreamItem) {
    Pipeline.num.incrementAndGet
    // for each sentence, match the pattern
    // TODO: get the sentence string
    si.body.sentences.get("lingpipe").toArray(Array[streamcorpus.Sentence]()).foreach(sentence => {
      //println(sentence.getTokens().toArray().mkString(" "))
      val tokens = sentence.getTokens().toArray(Array[Token]())
      matches(transform(tokens))
    })
  }
}
