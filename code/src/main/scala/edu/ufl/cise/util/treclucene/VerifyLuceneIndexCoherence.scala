package edu.ufl.cise.util.treclucene

import org.apache.lucene.index.IndexReader
import java.io.File
import org.apache.lucene.store.FSDirectory
import java.util.Scanner
import edu.cise.ufl.util.treclucene.Searcher
import java.util.HashSet
import java.util.ArrayList
import scala.collection.JavaConversions._
import java.io.PrintWriter

object VerifyLuceneIndexCoherence {

  def main(args: Array[String]): Unit = {

    val indexDirectory = new File("/media/sdc/optimizedindex/");
    val reader = IndexReader.open(FSDirectory.open(indexDirectory));
    val num = reader.numDocs();

    val pwLTI = new PrintWriter(new File("LuceneTotalIndex.txt"))

    val pwDiff = new PrintWriter(new File("LuceneTotalIndexDiffStreamingSystem.txt"))

    val luceneIndexHashSet = new HashSet[String]()
    for (i <- 0 to reader.maxDoc()) {
      //   if (reader.isDeleted(i))
      //        continue;
      val doc = reader.document(i);
      val docId = doc.get("gpgfile");
      //println(docId)
      val b = luceneIndexHashSet.add(docId)
      if (b) {
        pwLTI.print(docId)
        println("#>" + docId)
      }
    }
    pwLTI.println("Completed.")
    pwLTI.close()

    val sc = new Scanner(new File("/media/sde/totalGPGsList.txt"))

    val arr = new ArrayList[String]()
    while (sc.hasNext()) {
      val gpgFileStr = sc.nextLine()
      //   Searcher.searchTermQuery(Array(gpgFileStr))
      if (!luceneIndexHashSet.contains(gpgFileStr)) {
        // arr.add(gpgFileStr)
        println("+>" + gpgFileStr)
      } else {
        println("@>" + gpgFileStr)
        pwDiff.println("@>" + gpgFileStr)
      }
    }

    pwDiff.println("Completed.")
    pwDiff.close()

    //  arr.toList.foreach(println)

  }

}