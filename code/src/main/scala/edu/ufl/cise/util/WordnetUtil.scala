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
    
    val listIWord = idxWord.getWordIDs().map(a => dictionary.getWord(a).getSynset().getWords())
//    listIWord.foreach(a => dictionary.getWord(a).getSynset().getWords())
    listIWord.foreach(a => println(a.get(0).getLemma()))
//    println(listIWord)
//    val d = listIWord.foreach(_.getLemma())
    //val iword = dictionary.getWord(wordID);
//    println(listIWord)
//    println(d)

//    for (wordID <- idxWord.getWordIDs()) {
//      val iword = dictionary.getWord(wordID);
//      val wordSynset = iword.getSynset();
//
//      for (synonym <- wordSynset.getWords()) {
//        System.out.print(synonym.getLemma() + ", ");
//      }
//    }

    return null
  }

}