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

object Pipeline extends Logging {

  var isInitialized = false;

  // ssplit to preprocess the document to get sentences, nlppipeline fully annotate each sentence
  private var ssplit: StanfordCoreNLP = null
  private var nlppipeline: StanfordCoreNLP = null
  // a bloom filter to check relations
  private var bf: (String => Boolean) = null

  var num = 0
  
  def init1() {
    // initialize ssplit and Stanford NLP pipeline 
    val props0 = new Properties();
    props0.put("annotators", "tokenize, ssplit")
    ssplit = new StanfordCoreNLP(props0)

    val props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma, ner")
    nlppipeline = new StanfordCoreNLP(props)

    // initialize the bloomfilter using the ReVerb relation list
    bf = RelationChecker.createWikiBloomChecker
  }

  // get a Pipeline object for specific text and query
  def getPipeline(query: SSFQuery): Pipeline = {
    init1();
    new Pipeline(query)
  }

  def main(args: Array[String]) {
    // extract relations from a string
    val text = "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865. " +
      "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865."
    val pipeline = getPipeline(new SSFQuery("Abraham Lincoln", "president of"))
    pipeline.run(text)
    /*
    //val z = new CachedFaucet(sc, "2012-05-01", 0)
    val sc = new SparkContext("local", "gatordsr", "$YOUR_SPARK_HOME",
    List("target/scala-2.9.2/gatordsr_2.9.2-0.01.jar"))
    val sr = new StreamRange
    sr.addFromDate("2012-05-01")
    sr.addFromHour(0)
    //sr.addToDate("2012-05-01")
    //sr.addToHour(0)
    val z = new CachedFaucet(sc, sr)

    val it = z.iterator
    if(it.hasNext) {
      {
        val rdd = it.next
        logInfo("The count: %d".format(rdd.count))
        logInfo(rdd.first.toString)
        logInfo("Is body null: %s".format(rdd.first.body == null))
        //sc.getRDDStorageInfo.foreach{x => logInfo("##%s".format(x.toString))}
        //logInfo(sc.hadoopConfiguration.toString)
        //sc.getExecutorMemoryStatus.foreach{x => logInfo("==%s".format(x.toString))}
        //System.gc
      }
    }
    lazy val z1 = z.iterator.reduce(_ union _) // Combine RDDS
      //logInfo("Total records: %d".format(z1.count))
      //logInfo("Total records: %d".format(z.getAllRDDS.count)) */
  }

}

class Pipeline(query: SSFQuery) extends Logging with Serializable {

  // use to store the extracted relations
  //private var triples: ArrayList[Triple] = new ArrayList[Triple]

  // break a single sentence to corresponding array list of words and marks (mark = 0, non-entity; 1, entity)
  def breakSentence(tokens: java.util.List[CoreLabel], words: ArrayList[String], marks: ArrayList[java.lang.Integer]) {
    val size = tokens.size()
    for (j <- 0 until size) {
      // this is the text of the token
      val token = tokens.get(j)
      // this is the POS tag of the token
      val pos = token.get[String, PartOfSpeechAnnotation](classOf[PartOfSpeechAnnotation])
      // this is the NER label of the token
      val ne = token.get[String, NamedEntityTagAnnotation](classOf[NamedEntityTagAnnotation])
      //println(pos.toString())
      words.add(token.value())
      if (ne.length() > 1 && (pos.toString().equals("NNP") || pos.toString().equals("NNPS"))) // length > 1, ne is an entity
        marks.add(1)
      else
        marks.add(0)
    }
  }

  // transform a list of Strings to a single String
  def transform(array: java.util.List[String]): String =
    {
      var s = ""
      for (i <- 0 until array.size()) {
        if (i != array.size() - 1)
          s += array.get(i) + " "
        else
          s += array.get(i)
      }

      return s
    }

  // used to extract two entities located at the nearest distance of the relation
  def getEntity(words: java.util.List[String], marks: java.util.List[Integer], flag: Int): String =
    {
      var s = ""

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
          s = transform(words.subList(k3, k4))
        } else
          s = "N*A" // means no entity found
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
          s = transform(words.subList(k2, k1 + 1))
        } else
          s = "N*A"

      }

      return s
    }

  // extract the relations from the array of words and marks
  def getRelations(words: ArrayList[String], marks: ArrayList[Integer]): ArrayList[Triple] =
    {
      var results = new ArrayList[Triple] // used to store results
      val size = words.size()
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
              results.add(relation)
            }
          }
        }
      }
      return results
    }

  // extract relations from sentences
  def extract(sentence: CoreMap, triples: ArrayList[Triple]) {
    val tokens = sentence.get[java.util.List[CoreLabel], TokensAnnotation](classOf[TokensAnnotation])
    val size = tokens.size()
    var words = new ArrayList[String](size)
    var marks = new ArrayList[java.lang.Integer](size)
    // break sentence to words and marks
    breakSentence(tokens, words, marks)
    // extract relations
    val results = getRelations(words, marks)
    // for (relation <- results.toArray()) logInfo(relation.toString())
    if (results.size() != 0)
      triples.addAll(results)
  }

  // the main logic
  def run(text: String): ArrayList[Triple] =
    {
	  val in = System.nanoTime()
	  num = num + 1
      var triples: ArrayList[Triple] = new ArrayList[Triple]
      // create an empty Annotation just with the given text
      val document = new Annotation(text)
      // annotate the document
      ssplit.annotate(document)
      // nlppipeline.annotate(document)
      // get sentences
      val sentences = document.get[java.util.List[CoreMap], SentencesAnnotation](classOf[SentencesAnnotation])
      // extract relations from each sentence, and parsing each sentence, and dcoref each sentence
      //sparkContext.parallelize(sentences.toArray()).foreach(sentence => 
      var x = 0.0
      var y = 0.0
      val out0 = System.nanoTime()
      sentences.toArray().foreach(sentence =>
        {
          // measure time cost
          val begin = System.nanoTime()
          nlppipeline.annotate(sentence.asInstanceOf[Annotation]);
          val end1 = System.nanoTime()
          extract(sentence.asInstanceOf[Annotation], triples)
          val end2 = System.nanoTime()
          x = x + end1 - begin
          y = y + end2 - end1
          
        })
        
      val out = System.nanoTime()
      val c = out - in
      val c0 = out0 - in
      
      logInfo("pipeline ends executeion for document " + num + " : prepare " + c0/1000000.0 + "ms, " +
      		"annotation " + x/1000000.0 + "ms, " + " extraction " + y/1000000.0 + "ms, total " + c/1000000.0 + "ms")
      //println(triples)
      return triples
    }
}
