package edu.ufl.cise.util.treclucene

import org.apache.lucene.index.IndexReader
import java.io.File
import org.apache.lucene.store.FSDirectory
import java.util.Scanner
import edu.cise.ufl.util.treclucene.Searcher
import java.util.HashSet
import java.util.ArrayList

import scala.collection.JavaConversions._

object VerifyLuceneIndexCoherence {

  def main(args: Array[String]): Unit = {

    val indexDirectory = new File("/media/sdc/optimizedindex/");
    val reader = IndexReader.open(FSDirectory.open(indexDirectory));
    val num = reader.numDocs();

    val luceneIndexHashSet = new HashSet[String]()
    for (i <- 0 to reader.maxDoc()) {
      //   if (reader.isDeleted(i))
      //        continue;
      val doc = reader.document(i);
      val docId = doc.get("gpgfile");
      //println(docId)
      luceneIndexHashSet.add(docId)
    }

    val sc = new Scanner(new File("/media/sde/totalGPGsList.txt"))

    val arr = new ArrayList[String]()
    while (sc.hasNext()) {
      val gpgFileStr = sc.nextLine()
      //   Searcher.searchTermQuery(Array(gpgFileStr))
      if (!luceneIndexHashSet.contains(gpgFileStr)) {
        arr.add(gpgFileStr)
      }
    }

    arr.toList.foreach(println)

  }

}