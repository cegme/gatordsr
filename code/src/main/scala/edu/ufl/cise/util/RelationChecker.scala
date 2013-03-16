package edu.ufl.cise.util

import java.io.{BufferedReader,FileInputStream,InputStreamReader}
import java.util.zip.GZIPInputStream

import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels

import scala.io.Source

import edu.ufl.cise.Logging


object RelationChecker extends Logging {

  private val wikiFileName = "reverb_clueweb_relations-1.1.txt.gz"
  private val WIKIRELATIONS = 2*1220394
  private val wikiRelationBF = BloomFilter.create(Funnels.stringFunnel, WIKIRELATIONS, .0001)

  private lazy val isFilterLoaded:Boolean = {
    //val fileStream = new FileInputStream(wikiFileName)
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


  def main(args: Array[String]) {

    val bf = createWikiBloomChecker
    logInfo("was born in => " + bf("was born in").toString)
    logInfo("is paid to => " + bf("was born in").toString)
    logInfo("hello => " + bf("was born in").toString)
  }
      

}
