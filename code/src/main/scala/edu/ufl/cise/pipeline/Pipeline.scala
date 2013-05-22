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
import fileproc.RemoteGPGRetrieval
import java.util.LinkedList


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
    // extract relations from a string
    //val text = "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865. " +
    //  "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865."
    //val pipeline = getPipeline(new SSFQuery("Abraham Lincoln", "president of"))
    // pipeline.run(text).toSeq.foreach{t => logInfo("Triple: %s".format(t))}
    val p = getPipeline(new SSFQuery("aa", "bb"))
    p.matches("aaxdbbiop[ssv")
    
    val pipe = getPipeline(new SSFQuery("a", "b"))
    
 	val fileName = "WEBLOG-89-15957f5baef21e2cda6dca887b96e23e-e3bb3adf7504546644d4bc2d62108064.sc.xz.gpg";
    val list = RemoteGPGRetrieval.getStreams("2012-11-03-05", fileName)
    list.toArray(Array[StreamItem]()).foreach(si => {
      // println(si.asInstanceOf[StreamItem].source) // source attribute
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
    val sent:Array[String] = Pipeline.tokenizer.tokenize(s)
    val pos = Pipeline.tagger.tag(sent)
    val tag = Pipeline.chunker.chunk(sent, pos).toArray
    // find the first NP
    var begin = -1;
    var end = -1
    for (i <- 0 until tag.size){
      if(begin != -1 && end == -1 && !tag(i).equals("I-NP")) end = i
      if(begin == -1 && tag(i).equals("B-NP")) begin = i
    }
    // from array to sub-array
    val result = sent.slice(begin, end)
    return result.mkString(" ")
  }

  // match pattern and retrieve corresponding triple
  def matches(s : String):Triple = {
    s match {
      case pattern(p, q) => val triple = new Triple (entity, slot, extractFirstNP(q)); KBAOutput.add(triple); triple
      case _ => null
    }
  }
  
  // the main logic
  def run(text: String) = {
	  Pipeline.num.incrementAndGet + ""
      
  }
  
  def run(si:StreamItem) {
    Pipeline.num.incrementAndGet
    // for each sentence, match the pattern
    si.body.sentences.get("lingpipe").toArray(Array[Sentence]()).foreach(sentence => {
      logInfo(matches(sentence.toString()).toString)
    })
  }
}
