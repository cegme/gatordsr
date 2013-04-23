package edu.ufl.cise.util

import edu.mit.jwi.IDictionary
import edu.mit.jwi.Dictionary
import java.net.URL
import edu.mit.jwi.item.Pointer
import edu.mit.jwi.item.POS
import scala.collection.JavaConversions._
import net.didion.jwnl.data.Pointer
import java.io.File
import edu.ufl.cise.Logging

/**
 * Utility object handling Wordnet features we need.
 */
object WordnetUtil extends Logging {
  val path = "./resources/wordnet/dict/";

  val url = new URL("file", null, path);
  val dictionary: IDictionary = new Dictionary(url);
  dictionary.open();

  def main(args: Array[String]): Unit = {
    getSynonyms("best", POS.NOUN)
  }

  /**
   * Get all the synonyms of a keyword regarded a noun POS.
   */
  def getSynonyms(keyword: String): Seq[String] = {
    getSynonyms(keyword, POS.NOUN)
  }

  /**
   * Get all the synonyms of a keyword regarded with a part of speech of POS.
   */
  def getSynonyms(keyword: String, pos: POS): Seq[String] = {
    val idxWord = dictionary.getIndexWord(keyword, pos)
    if (idxWord != null) {
      val listIWord = idxWord.getWordIDs().flatMap(a => dictionary.getWord(a).getSynset().
        getWords().map(_.getLemma().replace('_', ' ')))
      val res = listIWord //.map(a => a.getLemma()).distinct
      logInfo(res.mkString(", "))
      res
    } else
      List[String]()
  }
}