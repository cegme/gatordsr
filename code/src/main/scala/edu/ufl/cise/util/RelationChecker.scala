package edu.ufl.cise.util

import java.io.{BufferedReader,FileInputStream,InputStreamReader,StringReader}
import java.util.zip.GZIPInputStream

import scala.io.Source
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels

import edu.stanford.nlp.ling.Word
import edu.stanford.nlp.process.PTBTokenizer
import edu.stanford.nlp.process.WordTokenFactory


import edu.ufl.cise.Logging


object RelationChecker extends Logging {

  val MAXRELATIONLENGTH = 5

  private val wikiFileName = "reverb_clueweb_relations-1.1.txt.gz"
  private val WIKIRELATIONS = 2*1220394
  private val wikiRelationBF = BloomFilter.create(Funnels.stringFunnel, WIKIRELATIONS, .01)

  private lazy val isFilterLoaded:Boolean = {
    logInfo("Retrieving the relation file %s ...".format(wikiFileName))
    val fileStream = this.getClass.getClassLoader.getResourceAsStream(wikiFileName)
    val gzipStream = new GZIPInputStream(fileStream)
    val decoder = new InputStreamReader(gzipStream, "UTF-8")
    //val buffered = new BufferedReader(decoder)

    // Load the bloom Filter
    // Example line: 888\twas buriedin\tbe bury in
    val LineRegex = """(\d+)\t(.+)\t(.+)""".r
    Source.fromInputStream(gzipStream).getLines.foreach{ line =>
      line match {
        case LineRegex(freq:String,rel1:String,rel2:String) => {
          wikiRelationBF.put(rel1)
          wikiRelationBF.put(rel2) 
          
        }
        case x => logError("Bad line from %s: %s".format(wikiFileName,x))
      }
    }
    logInfo("Finished building the wikirelation bloom filter.")
    true
  }

  
  /**
   * Use this function to create an object that takes a string
   * and returns true if that string is infact a relation that 
   * was scraped from the wikipedia using ReVerb.
   * A bloomfilter is used so the answers are probabilistic.
   * If there is an error you get a function that always returns
   * false.
   *
   * Example:
   *
   *  val bf = createWikiBloomChecker
   *  bf("is born in") // returns true
   *  bf("is paid to") // returns true
   *  bf("hello") // probably will return false
   *
   */
  def createWikiBloomChecker:(String => Boolean) = {
   if(isFilterLoaded)
      wikiRelationBF.mightContain
    else
      (key:String) => false
  }

  def WikiRelations(text:String):Iterator[String] = {
    val tokenizer = new PTBTokenizer(new StringReader(text), new WordTokenFactory, "")
    val sentence = tokenizer.tokenize
    logInfo("Extracting relation from the sentence %s".format(sentence))
    val bf = createWikiBloomChecker

    (1 until MAXRELATIONLENGTH).view.map{ relLength =>
      sentence.sliding(relLength).map{_.mkString(" ")}
    }
      .flatMap(x => x)
      .filter{bf(_)}
      .toIterator
  }


  def main(args: Array[String]) {

    val bf = createWikiBloomChecker
    logInfo("was born in => " + bf("was born in").toString)
    logInfo("is paid to => " + bf("was born in").toString)
    logInfo("hello => " + bf("was born in").toString)

    val testSentence = "Did you know that Bejing is the capital of China?"
    val rels = WikiRelations(testSentence)
    rels.foreach{ r => logInfo("Relation: %s".format(r))}
  }
      

}
