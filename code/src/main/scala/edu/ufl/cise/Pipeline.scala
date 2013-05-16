package edu.ufl.cise

import Pipeline._
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.ufl.cise.util.RelationChecker
import java.util.ArrayList
import edu.stanford.nlp.pipeline.ParserAnnotator
import edu.stanford.nlp.pipeline.DeterministicCorefAnnotator
import java.util.Properties
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation
import spark.SparkContext
import com.google.common.base.Stopwatch
import java.util.concurrent.TimeUnit
import java.util.Date

import scala.collection.JavaConversions._


object Pipeline extends Logging {

  // ssplit to preprocess the document to get sentences, nlppipeline fully annotate each sentence
  private lazy val ssplit: StanfordCoreNLP = {
    val props0 = new Properties();
    props0.put("annotators", "tokenize, ssplit")
    new StanfordCoreNLP(props0)
  }

  private lazy val nlppipeline: StanfordCoreNLP = {
    val props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma, ner")
    new StanfordCoreNLP(props)
  }

  /**
    * A bloom filter to check relations
    * initialize the bloomfilter using the ReVerb relation list
    */
  protected lazy val bf: (String => Boolean) = RelationChecker.createWikiBloomChecker

  /** This keeps track of how many times run is called. */
  val num = new java.util.concurrent.atomic.AtomicInteger

  // get a Pipeline object for specific text and query
  def getPipeline(query: SSFQuery): Pipeline = new Pipeline(query)

  def main(args: Array[String]) {
    // extract relations from a string
    val text = "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865. " +
      "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865."
    val pipeline = getPipeline(new SSFQuery("Abraham Lincoln", "president of"))
    pipeline.run(text).toSeq.foreach{t => logInfo("Triple: %s".format(t))}
  }

}

class Pipeline(query: SSFQuery) extends Logging with Serializable {

  // use to store the extracted relations
  //private var triples: ArrayList[Triple] = new ArrayList[Triple]

  // break a single sentence to corresponding array list of words and marks (mark = 0, non-entity; 1, entity)
  def breakSentence(tokens: java.util.List[CoreLabel]): (Array[String], Array[Int]) = {
   
    /** This inner function returns a 1 if this is a named entity and a 0 otherwise */
    def makeMark(token:CoreLabel): Int = {
      val pos = token.get[String, PartOfSpeechAnnotation](classOf[PartOfSpeechAnnotation])
      if(!(pos.toString.equals("NNP") || pos.toString.equals("NNPS"))) 0
      else if (token.get[String, NamedEntityTagAnnotation](classOf[NamedEntityTagAnnotation]).length > 1) 1
      else 0
    }

    //([words],[marks])
    (tokens.map{_.value}.toArray, tokens.map{makeMark}.toArray)
  }

  // transform a list of Strings to a single String
  def transform(array: java.util.List[String]): String = array.mkString(" ")
    

  // used to extract two entities located at the nearest distance of the relation
  def getEntity(words: java.util.List[String], marks: java.util.List[Int], flag: Int): String =
    {
      var sb = new java.lang.StringBuilder

      if (flag == 1) // flag = 1, extract entity behind the relation, 0 before the relation
      {
        var k3 = -1
        var k4 = -1

        for (m <- 0 until words.size()) {
          if (marks.get(m) != 0 && k3 == -1 && k4 == -1) {
            k3 = m
          }

          if (marks.get(m) == 0 && k3 != -1 && k4 == -1) {
            k4 = m
          }
        }

        if (k3 != -1 && k4 != -1) {
          sb.append(transform(words.subList(k3, k4)))
          //sb ++= transform(words.subList(k3, k4))
        } else
          sb.append("N*A") // means no entity found
          //sb ++= "N*A" // means no entity found
      } else {
        var k1 = -1
        var k2 = -1
        for (k <- words.size() - 1 to 0 by -1) {
          if (marks.get(k) != 0 && k1 == -1 && k2 == -1) {
            k1 = k
          }

          if (marks.get(k) == 0 && k1 != -1 && k2 == -1) {
            k2 = k
          }
        }

        k2 = k2 + 1
        if (k2 != -1 && k1 != -1) {
          sb.append(transform(words.subList(k2, k1 + 1)))
          //sb ++= transform(words.subList(k2, k1 + 1))
        } else
          sb.append("N*A")
          //sb ++= "N*A"

      }

      sb.toString
    }

  // extract the relations from the array of words and marks
  def getRelations(_words: Array[String], _marks: Array[Int]): Array[Triple] =
    {
      //var results = new scala.collection.mutable.ArrayBuffer[Triple] // used to store results
      val words = _words.toBuffer
      val marks = _marks.toBuffer
      val size = words.size
      val results = new scala.collection.mutable.ArrayBuffer[Triple] // used to store results
      for (i <- 0 until size) // check all the possible i and j postions
      {
        for (j <- i until size) {
          // use the bloom-filter to check
          val s = transform(words.subList(i, j + 1)).toLowerCase()
          if (bf(s) || bf("is " + s)) // add "is " to recognize possible relations
          {
            val entity0 = getEntity(words.subList(0, i), marks.subList(0, i), -1)
            val entity1 = getEntity(words.subList(j + 1, size), marks.subList(j + 1, size), 1)
            if (!entity0.equals("N*A") && !entity1.equals("N*A")) {
              val relation = new Triple(entity0, s, entity1)
              results.append(relation)
            }
          }
        }
      }
      results.toArray
    }

  // extract relations from sentences
  def extract(sentence: CoreMap): Array[Triple] = {
    val tokens = sentence.get[java.util.List[CoreLabel], TokensAnnotation](classOf[TokensAnnotation])
    
    // break sentence to words and marks
    val (words, marks) = breakSentence(tokens)
    // extract relations
    val triples = getRelations(words, marks)
    triples
  }

  // the main logic
  def run(text: String): Array[Triple] = {
	  num.incrementAndGet
    //var triples: ArrayList[Triple] = new ArrayList[Triple]
    // create an empty Annotation just with the given text
    val document = new Annotation(text)
    // annotate the document
    ssplit.annotate(document)
    // nlppipeline.annotate(document)
    // get sentences
    val sentences = document.get[java.util.List[CoreMap], SentencesAnnotation](classOf[SentencesAnnotation])

    // extract relations from each sentence, and parsing each sentence, and dcoref each sentence
    sentences.flatMap{ s => 
      // Detailed annotation
      nlppipeline.annotate(s.asInstanceOf[Annotation]);
      // Get the triples
      extract(s.asInstanceOf[Annotation])
    }.toArray
  }
}
