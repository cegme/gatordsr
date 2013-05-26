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

  // get a Pipeline object for specific text and query
  def getPipeline(query: SSFQuery): Pipeline = new Pipeline(query)
  
  def main(args: Array[String]) {
    val pipe0 = getPipeline(new SSFQuery("aa", "bb"))
    pipe0.matches("aa bb Sam Jackson")
    
    // sample query
    val pipe = getPipeline(new SSFQuery("Thanks", "nothing"))
    val fileName = "social-458-b51e990263a58e94a88d22a8be8502d1-d71caa2571e6e6aa16da0cdae2a4dfc7.sc.xz.gpg";
    val list = RemoteGPGRetrieval.getStreams("2011-11-03-05", fileName)
    list.toArray(Array[StreamItem]()).foreach(si => {
      if(si.body != null && si.body.sentences != null)  
        pipe.run(si)
    })
    
  }
}


class Pipeline(query: SSFQuery) extends Logging with Serializable {
  def entity = query.entity
  def slot = query.slotName
  // pattern matching
  val pattern = ("""(""" + entity +  ".*" + slot + """)""" + """(.*)""").r
  // extract the first NP after matched substring
  def extractFirstNP(s:String):String = {
    // using chunking to extract NP from a string
    //println(s)
    val sent:Array[String] = Pipeline.tokenizer.tokenize(s)
    val pos = Pipeline.tagger.tag(sent)
    val tag = Pipeline.chunker.chunk(sent, pos).toArray
    //println(tag.mkString(" "))
    // find the first NP
    var begin = -1;
    var end = -1
    for (i <- 0 until tag.size){
      if(begin != -1 && end == -1 && tag(i).equals("I-NP")) end = i
      if(begin == -1 && tag(i).equals("B-NP")) begin = i
    }
    // from array to sub-array
    val result = sent.slice(begin, end + 1)
    //println(result.mkString(" "))
    return result.mkString(" ")
  }

  // match pattern and retrieve corresponding triple
  def matches(s : String):Triple = {
    s match {
      case pattern(p, q) => {
        val triple = new Triple (entity, slot, extractFirstNP(q)); 
        KBAOutput.add(triple);
        //println(triple); 
        triple}
      case _ => null
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
      val t = matches(transform(tokens))
      if (t != null) logInfo(t.toString)
    })
  }
}
