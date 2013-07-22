package edu.cise.ufl.util.treclucene

import edu.ufl.cise.Logging

import org.apache.lucene.index.DirectoryReader
//import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.Term
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.CollectionStatistics
import org.apache.lucene.store.NIOFSDirectory


object Searcher extends Logging {

  //var filedir = new java.io.File("/var/tmp/lucene")
  var filedir = new java.io.File("/media/sdc/kbaindex/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-04-17-15")
  val directory = new NIOFSDirectory(filedir)

  def getStats (searcher:IndexSearcher):Unit = {
    val stats:CollectionStatistics = searcher.collectionStatistics("clean_visible")

    logInfo("field: %s".format(stats.field))
    logInfo("docCount: %d".format(stats.docCount))
    logInfo("maxDoc: %d".format(stats.maxDoc))
    logInfo("sumDocFreq: %d".format(stats.sumDocFreq))
    logInfo("sumTotalTermFreq: %d".format(stats.sumTotalTermFreq))
    logInfo("-"*40)

  }

  def printAllDocs (searcher:IndexSearcher):Unit = {
    var i = 0;
    while (i <= searcher.getIndexReader.numDocs) {
      logInfo("doc(%d): %s".format(i, searcher.doc(i).toString))
      i += 1
    }

  }


  def main(args: Array[String]) {

    if (args.length < 1 || args.length > 2) {
      println("Usage: run 'My query'")
      System.exit(1)
    }

    logInfo(args(0))

    val reader = DirectoryReader.open(directory)
    val searcher = new IndexSearcher(reader)
    val query = new TermQuery(new Term("clean_visible", args(0).toLowerCase))

    getStats(searcher)
    //printAllDocs(searcher)

    val docs = searcher.search(query,2)

    docs.scoreDocs foreach { docId =>
      val d = searcher.doc(docId.doc)
      logInfo("Result: %s".format(d.get("si_index")))
      logInfo("Result: %s".format(d.get("gpgfile")))
      logInfo("Result: %s".format(d.get("clean_visible")))
    }

    //searcher.close
    reader.close

  }

}
