package edu.ufl.cise.util.treclucene

import org.apache.lucene.index.IndexReader
import java.io.File
import org.apache.lucene.store.FSDirectory
import java.util.Scanner
import edu.cise.ufl.util.treclucene.Searcher

object VerifyLuceneIndexCoherence {

  def main(args: Array[String]): Unit = {

    //   val sc = new Scanner(new File("/media/sde/totalGPGsList.txt"))
    //    while (sc.hasNext()) {
    //      val gpgFileStr = sc.nextLine()
    //      Searcher.searchTermQuery(Array(gpgFileStr))
    //    }

    val indexDirectory = new File("/media/sdc/kbaindex/");
    val reader = IndexReader.open(FSDirectory.open(indexDirectory));
    val num = reader.numDocs();
    //for (int i = 0; i < num; ++i) println(r.get('gpgfile'));
    //
    //  }

    for (i <- 0 to reader.maxDoc()) {
      //   if (reader.isDeleted(i))
      //        continue;
      val doc = reader.document(i);
      val docId = doc.get("gpgfile");
      println(docId)
    }
  }

}