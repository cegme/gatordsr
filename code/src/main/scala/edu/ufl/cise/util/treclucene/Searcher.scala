package edu.cise.ufl.util.treclucene

import java.util.ArrayList
import java.util.regex.Pattern
import scala.Array.canBuildFrom
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.CollectionStatistics
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.store.MMapDirectory
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.util.Version
import org.apache.lucene.search.PhraseQuery
import scala.collection.JavaConversions._
import edu.ufl.cise.Logging
import org.apache.lucene.search.TopDocs
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.document.Document
import java.io.PrintWriter

object Searcher extends Logging {

  val SEARCH_INDEX_TYPE = "clean_visible"
  //val SEARCH_INDEX_TYPE = "gpgfile"

  //var filedir = new java.io.File("/var/tmp/lucene")
  //var filedir = new java.io.File("/media/sdc/kbaindex/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-04-17-15")
  //var filedir = new java.io.File("/media/sdc/optimizedindex/") //index of wikipedia
  var filedir = new java.io.File("/media/sdc/optimizedindex/") //index of corpus

  val directory = new NIOFSDirectory(filedir)
  val analyzer = new StandardAnalyzer(Version.LUCENE_43);

  val queryParser = new QueryParser(Version.LUCENE_43, SEARCH_INDEX_TYPE, analyzer)

  // 1. create the index
  val index = new MMapDirectory(filedir)

  val reader = DirectoryReader.open(directory)
  val searcher = new IndexSearcher(reader)

  val FULL_PATH_GPG_REGEX_STR = ".*?(\\d{4}-\\d{2}-\\d{2}-\\d{2}).*/(.*)";
  val FULL_PATH_GPG_REGEX = Pattern.compile(FULL_PATH_GPG_REGEX_STR);

  def getStats(searcher: IndexSearcher): Unit = {
    val stats: CollectionStatistics = searcher.collectionStatistics(SEARCH_INDEX_TYPE)

    logInfo("field: %s".format(stats.field))
    logInfo("docCount: %d".format(stats.docCount))
    logInfo("maxDoc: %d".format(stats.maxDoc))
    logInfo("sumDocFreq: %d".format(stats.sumDocFreq))
    logInfo("sumTotalTermFreq: %d".format(stats.sumTotalTermFreq))
    logInfo("-" * 40)

  }

  def printAllDocs(searcher: IndexSearcher): Unit = {
    var i = 0;
    while (i <= searcher.getIndexReader.numDocs) {
      logInfo("doc(%d): %s".format(i, searcher.doc(i).toString))
      i += 1
    }
  }

  def printDocument(d: Document, logNote: String = ""): Unit = {
    val gpgFile = d.get(SEARCH_INDEX_TYPE)

    val m = FULL_PATH_GPG_REGEX.matcher(gpgFile);
    m.find()
    val s1 = m.group(1);
    val s2 = m.group(2);

    println("ling>" + s1 + " | " + s2 + " | " + d.get("si_index") + " | " +
      //d.get("si_docid")
      //d.get("clean_visible")+
      "aab5ec27f5515cb8a0cec62d31b8654e" + " || " + logNote);
  }

  def printToFile(d: Document, pw: PrintWriter, logNote: String = ""): Unit = {
    val gpgFile = d.get(SEARCH_INDEX_TYPE)

    val m = FULL_PATH_GPG_REGEX.matcher(gpgFile);
    m.find()
    val s1 = m.group(1);
    val s2 = m.group(2);

    pw.println("ling>" + s1 + " | " + s2 + " | " + d.get("si_index") + " | " +
      //d.get("si_docid")
      //d.get("clean_visible")+
      "aab5ec27f5515cb8a0cec62d31b8654e" + " || " + logNote);
  }

  def processAllTopDocs(docs: TopDocs, luceneDocumentProcessor: (Document, String) => Unit, logInfo: String): Unit = {
    docs.scoreDocs foreach { docId =>
      val d = searcher.doc(docId.doc)
      luceneDocumentProcessor(d, logInfo)
    }
  }

  def main(args: Array[String]) {

    if (args.length < 1) {
      println("Usage: run 'My query'")
      System.exit(1)
    }

    val concatedArgs = args.map(s => {
      if (s == ",")
        "OR"
      else if (s != "AND" && s != "OR")
        "\"" + s + "\""
      else
        s
    }).reduce((s1, s2) => s1 + " " + s2)

    //      val allArgs = args.mkString(" ")
    //    logInfo("allArgs"+allArgs)

    searchTermQuery(args);

    println("All arguments: " + concatedArgs)
    searchQueryParser("", concatedArgs)
  }

  def searchTermQuery(args: Array[String]) {

    val query = new TermQuery(new Term(SEARCH_INDEX_TYPE, args(0).toLowerCase))
    var docs = searcher.search(query, 2000)
    println("TermQuery found: " + docs.scoreDocs.length)
//    processAllTopDocs(docs, printDocument, "");

    //    getStats(searcher)
    //printAllDocs(searcher)

    val q = new PhraseQuery()
    q.add(new Term(SEARCH_INDEX_TYPE, args(0).toLowerCase))
    docs = searcher.search(q, 2000)
    println("PhraseQuery found: " + docs.scoreDocs.length)
//    processAllTopDocs(docs, printDocument, "")

    //searcher.close
    reader.close
  }

  def aliasListToLuceneQuery(aliasList: ArrayList[String]): String = {
    val aliases = aliasList.toList.distinct

    val  escapeChars ="[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\*\\?]";
    val concatedArgs = aliases.map(s => {
      if (s == ",")
        "OR"
      else if (s != "AND" && s != "OR")
        "\"" + s.replaceAll(escapeChars, "\\\\$0").replaceAll("\"", "\\\\\"") + "\""
      else
        s
    }).reduce((s1, s2) => s1 + " OR " + s2)

    concatedArgs.toLowerCase().replace(" or ", " OR ")
  }

  def searchEntity(logNote: String, aliasList: ArrayList[String]) {
    val concatedArgs = aliasListToLuceneQuery(aliasList)
    searchQueryParser(logNote, concatedArgs)
  }

  def searchQueryParser(logNote: String, querystr: String) {
    //		System.out.println("\nSearching for '" + searchString + "' using QueryParser");
    //		//Directory directory = FSDirectory.getDirectory(INDEX_DIRECTORY);
    //		val indexSearcher = new IndexSearcher(directory);
    //
    //		val queryParser = new QueryParser(FIELD_CONTENTS, new StandardAnalyzer());
    //		Query query = queryParser.parse(searchString);
    //		System.out.println("Type of query: " + query.getClass().getSimpleName());
    //		Hits hits = indexSearcher.search(query);
    //		displayHits(hits);

    // the "title" arg specifies the default field to use
    // when no field is explicitly specified in the query.
    //val q = new QueryParser(Version.LUCENE_43, "clean_visible", analyzer).parse(querystr);

    val q = queryParser.parse(querystr);

    // 3. search
    val hitsPerPage = 1000000;
    val reader = DirectoryReader.open(index);
    val searcher = new IndexSearcher(reader);
    val collector = TopScoreDocCollector.create(hitsPerPage, true);
    searcher.search(q, collector);

    val docs = collector.topDocs()
    val hits = docs.scoreDocs;

    // 4. display results
    println(hits.length + "\t hits for: " + querystr);

//    processAllTopDocs(docs, printDocument, logNote)

    // reader can only be closed when there
    // is no need to access the documents any more.
    // reader.close();
  }
}
