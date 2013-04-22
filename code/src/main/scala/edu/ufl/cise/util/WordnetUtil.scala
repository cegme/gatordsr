package edu.ufl.cise.util

import edu.mit.jwi.IDictionary
import edu.mit.jwi.Dictionary
import java.net.URL
import edu.mit.jwi.item.Pointer
import edu.mit.jwi.item.POS
import scala.collection.JavaConversions._
import net.didion.jwnl.data.Pointer
import java.io.File

object WordnetUtil {

  val path = "./resources/wordnet/dict/";

  //  val f = new File(path)
  //  println(f.isDirectory())

  val url = new URL("file", null, path);
  val dictionary: IDictionary = new Dictionary(url);
  dictionary.open();

  def main(args: Array[String]): Unit = {
    getSynonyms("best", POS.ADJECTIVE)
  }

  def getSynonyms(word: String, pos: POS): Array[String] = {

    val idxWord = dictionary.getIndexWord(word, pos);

    val listIWord = idxWord.getWordIDs().flatMap(a => dictionary.getWord(a).getSynset().getWords())
    listIWord.map(a => a.getLemma()).foreach(println)

    println("Java direct code translation:"); 
    for (wordID <- idxWord.getWordIDs()) {
      val iword = dictionary.getWord(wordID);
      val wordSynset = iword.getSynset();

      for (synonym <- wordSynset.getWords()) {
        println(synonym.getLemma() + ", ");
      }
    }

    return null
  }

}