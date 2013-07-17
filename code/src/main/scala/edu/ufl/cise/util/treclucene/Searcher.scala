package edu.cise.ufl.util.treclucene

import edu.ufl.cise.Logging

import org.apache.lucene.index.DirectoryReader
//import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.Term
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.store.NIOFSDirectory


object Searcher extends Logging {

  var filedir = new java.io.File("/var/tmp/lucene")
  val directory = new NIOFSDirectory(filedir)

  def main(args: Array[String]) {

    if (args.length < 1 || args.length > 2) {
      println("Usage: run 'My query'")
      System.exit(1)
    }

    logInfo(args(0))

    val reader = DirectoryReader.open(directory)
    val searcher = new IndexSearcher(reader)
    val query = new TermQuery(new Term("clean_visible", args(0).toLowerCase))

    val docs = searcher.search(query, 10)

    docs.scoreDocs foreach { docId =>
      val d = searcher.doc(docId.doc)
      logInfo("Result: %s".format(d.get("si_index")))
    }

    //searcher.close
    reader.close

  }

}
