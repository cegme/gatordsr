package edu.cise.ufl.util.treclucene

import edu.ufl.cise.Logging
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.Term
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.CollectionStatistics
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.MMapDirectory


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

  // searchTermQuery(args);

    val allArgs = args.mkString(" ")
    println("All arguments: " + allArgs)
    searchQueryParser(allArgs)
  }
  
  def searchTermQuery(args: Array[String]){
     val reader = DirectoryReader.open(directory)
    val searcher = new IndexSearcher(reader)
    val query = new TermQuery(new Term("clean_visible", args(0).toLowerCase))

    getStats(searcher)
    //printAllDocs(searcher)

    val docs = searcher.search(query,2)
    println("TermQuery found: " + docs.scoreDocs.length )

//    docs.scoreDocs foreach { docId =>
//      val d = searcher.doc(docId.doc)
//      logInfo("Result: %s".format(d.get("si_index")))
//      logInfo("Result: %s".format(d.get("gpgfile")))
//      logInfo("Result: %s".format(d.get("clean_visible")))
//    }

    //searcher.close
    reader.close
  }
  
 def searchQueryParser( querystr: String) {
//		System.out.println("\nSearching for '" + searchString + "' using QueryParser");
//		//Directory directory = FSDirectory.getDirectory(INDEX_DIRECTORY);
//		val indexSearcher = new IndexSearcher(directory);
//
//		val queryParser = new QueryParser(FIELD_CONTENTS, new StandardAnalyzer());
//		Query query = queryParser.parse(searchString);
//		System.out.println("Type of query: " + query.getClass().getSimpleName());
//		Hits hits = indexSearcher.search(query);
//		displayHits(hits);
   
   
   
   val analyzer = new StandardAnalyzer(Version.LUCENE_40);
   
    // 1. create the index
   // val index = new RAMDirectory();
   val index =  new MMapDirectory(filedir);
   
       // the "title" arg specifies the default field to use
    // when no field is explicitly specified in the query.
    val q = new QueryParser(Version.LUCENE_40, "clean_visible", analyzer).parse(querystr);

    // 3. search
    val hitsPerPage = 10000000;
    val reader = DirectoryReader.open(index); //TODO check index is full
    val searcher = new IndexSearcher(reader);
    val collector = TopScoreDocCollector.create(hitsPerPage, true);
    searcher.search(q, collector);
    val hits = collector.topDocs().scoreDocs;
   
    // 4. display results
    System.out.println("QueryParser found: " + hits.length + " hits.");
    //for(int i=0;i<hits.length;++i)
  
//    hits.foreach(f => {
//      val docId = f.doc;
//      val d = searcher.doc(docId);
//      println( d.get("gpgfile") +"\t"+ d.get("si_index") +  "\t" + d.get("clean_visible"));
//    })

    // reader can only be closed when there
    // is no need to access the documents any more.
    reader.close();

	}
}
