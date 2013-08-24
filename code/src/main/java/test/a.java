//package test;
//
///*
// * Created on Apr 21, 2003
// *
// */
//
//import java.io.IOException;
//import java.util.LinkedList;
//
//import org.apache.lucene.analysis.standard.*;
//import org.apache.lucene.document.*;
//import org.apache.lucene.index.*;
//import org.apache.lucene.search.*;
//import org.apache.lucene.store.*;
//
//public class a {
//
//	public static void main(String[] args) 
//	throws IOException, ParseException {
//		RAMDirectory indexStore = new RAMDirectory();
//		Query query = null;
//
//		String docs[] = {
//			"aaa bbb ccc",
//			"aaa ddd eee",
//			"aaa ddd fff",
//			"aaa dee fff",
//			"AAA fff ggg",
//			"ggg hhh iii",
//			"123 123 1z2",	// document containing z for 
//					// WildcardQuery example.
//			"999 123 123",	// document for fuzzy search.
//			"9x9 123 123",	// document for fuzzy search.
//			"99 123 123",	// document for fuzzy search.
//			"xxx yyy zzz"
//		};
//		
//		IndexWriter writer = new IndexWriter(indexStore, 
//			new StandardAnalyzer(), true);
//		for (int j = 0; j < docs.length; j++) {
//			Document d = new Document();
//			d.add(Field.Text("body", docs[j]));
//			writer.addDocument(d);
//		}
//		writer.close();
//
//		IndexReader indexReader = IndexReader.open(indexStore);
//
//		System.out.println("Searchable Documents: "
//			 + indexReader.numDocs());
//		System.out.println("");
//		System.out.println("NOTE: This search is" 
//			+ " case-insensitive.");
//		System.out.println("--------------------------------");
//		System.out.println("");
//
//		System.out.println("****** BooleanQuery Example *******");
//		System.out.println("FIND DOCUMENTS WITH " 
//			+ "AAA, GGG, or XXX");
//		System.out.println("-------------------------------");
//		BooleanQuery bq = new  BooleanQuery();
//		Term aaa = new Term("body", "aaa");
//		Term ggg = new Term("body", "ggg");
//		Term xxx = new Term("body", "xxx");
//		bq.add(new TermQuery(aaa), false, false);
//		bq.add(new TermQuery(ggg), false, false);
//		bq.add(new TermQuery(xxx), false, false);
//		RunAllQueries.runQueryAndDisplayResults(indexStore, bq);	
//		System.out.println("");
//		System.out.println("FIND DOCUMENTS WITH AAA or Z*");
//		System.out.println("------------------------------------");
//		bq = new  BooleanQuery();
//		bq.add(new TermQuery(new Term("body", "aaa")), false, false);
//		bq.add(new PrefixQuery(new Term("body", "z")), false, false);
//		RunAllQueries.runQueryAndDisplayResults(indexStore, bq);	
//
//		System.out.println("\n****** DocFreq Example ******");
//		System.out.println("Frequency of 'DDD' Term: " 
//			+ indexReader.docFreq(new Term("body", "ddd")));
//
//		System.out.println("\n****** FuzzyQuery Example *******");
//		System.out.println("DOCUMENTS SIMILAR TO 999." 
//			+ " NOTE THAT 9x9 IS FOUND BUT NOT 99");
//		System.out.println("------------------" 
//			+ "-----------------------------------------");
//		query = new FuzzyQuery(new Term("body", "999"));
//		RunAllQueries.runQueryAndDisplayResults(indexStore, query);
//		System.out.println("\n*** PhrasePrefixQuery Example ****");
//		// First, print all searchable terms.
//		System.out.println("ALL SEARCHABLE TERMS");
//		System.out.println("--------------------");
//		LinkedList termsWithPrefix = new LinkedList();
//		TermEnum te = indexReader.terms();
//		do {
//		  if (te.term() != null) {
//		    if (te.term().text() != null) {
//		      System.out.println("!" + te.term().text() + "!");
//		    }
//		  }
//		} while (te.next());
//		// Second, show that terms() returns terms
//		// that from the specified term to the end
//		// of the term list. So if you look for 
//		// pi* then paa would not be found. However,
//		// phh and saa would be found.
//		System.out.println("");
//		System.out.println("SEARCHABLE TERMS FROM H to Z");
//		System.out.println("---------------------------");
//		termsWithPrefix = new LinkedList();
//		te = indexReader.terms(new Term("body", "h*"));
//		do {
//			System.out.println(te.term().text());			
//		} while (te.next());
//		// Use pattern-matching to find terms that start
//		// with the letter h.
//		System.out.println("");
//		System.out.println("SEARCHABLE TERMS STARTING WITH D");
//		System.out.println("--------------------------------");
//		termsWithPrefix = new LinkedList();
//		String pattern = "d*";
//		te = indexReader.terms(new Term("body", pattern));
//		while (te.term().text().matches(pattern)) {
//			System.out.println(te.term().text());			
//			termsWithPrefix.add(te.term());
//			if (te.next() == false) 
//				break;
//		}
//		// Find documents that match "aaa h*".
//		PhrasePrefixQuery ppq = new PhrasePrefixQuery();
//		ppq.add(new Term("body", "aaa"));
//		ppq.add((Term[]) termsWithPrefix.toArray(new Term[0]));
//		System.out.println("");
//		RunAllQueries.runQueryAndDisplayResults(indexStore, ppq);
//
//		System.out.println("\n****** PhraseQuery Example ******");
//		System.out.println("NOTE: 2 documents are found " 
//			+ "with 'aaa ddd' in order.");
//		System.out.println("--------------" 
//			+ "------------------------");
//		PhraseQuery pq = new  PhraseQuery();		
//		pq.add(new Term("body", "aaa"));
//		pq.add(new Term("body", "ddd"));
//		RunAllQueries.runQueryAndDisplayResults(indexStore, pq);
//		System.out.println("");
//		System.out.println("NOTE: ZERO documents are" 
//			+ " found with 'xxx ddd' in order.");
//		System.out.println("-----------" 
//			+ "---------------------------");
//		pq = new  PhraseQuery();		
//		pq.add(new Term("body", "xxx"));
//		pq.add(new Term("body", "ddd"));
//		RunAllQueries.runQueryAndDisplayResults(indexStore, pq);
//
//		System.out.println("\n****** PrefixQuery Example ******");
//		System.out.println("DOCUMENTS STARTING WITH D");
//		System.out.println("-------------------------");
//		query = new PrefixQuery(new Term("body", "d"));
//		RunAllQueries.runQueryAndDisplayResults(indexStore, query);
//
//		System.out.println("\n****** RangeQuery Example ******");
//		System.out.println("DOCUMENTS FROM" 
//			+ " AAA to FFF, not inclusive");
//		System.out.println("-------------" 
//			+ "----------------------------------");
//		Term t1 = new Term("body", "aaa");
//		Term t2 = new Term("body", "fff");
//		query = new RangeQuery(t1, t2, false);
//		RunAllQueries.runQueryAndDisplayResults(indexStore, query);
//		System.out.println("");
//		System.out.println("DOCUMENTS FROM AAA" 
//			+ " to FFF, inclusive");
//		System.out.println("---------------------------------");
//		query = new RangeQuery(t1, t2, true);
//		RunAllQueries.runQueryAndDisplayResults(indexStore, query);
//
//		System.out.println("\n***** TermQuery Example *****");
//		System.out.println("DOCUMENTS THAT CONTAIN AAA");
//		System.out.println("--------------------------");
//		query = new TermQuery(new Term("body", "aaa"));
//		RunAllQueries.runQueryAndDisplayResults(indexStore, query);
//
//		System.out.println("\n***** WildcardQuery Example *****");
//		System.out.println("DOCUMENTS STARTING WITH D");
//		System.out.println("-------------------------------");
//		query = new WildcardQuery(new Term("body", "d*"));
//		RunAllQueries.runQueryAndDisplayResults(indexStore, query);
//		System.out.println("");
//		System.out.println("DOCUMENTS ENDING WITH E");
//		System.out.println("--------------------------------");
//		query = new WildcardQuery(new Term("body", "*e"));
//		RunAllQueries.runQueryAndDisplayResults(indexStore, query);
//		System.out.println("");
//		System.out.println("DOCUMENTS THAT CONTAIN Z");
//		System.out.println("--------------------------------");
//		query = new WildcardQuery(new Term("body", "*z*"));
//		RunAllQueries.runQueryAndDisplayResults(indexStore, query);
//
//		System.out.println("");
//		System.out.println("Done.");
//	}
//
//	public static void 
//	runQueryAndDisplayResults(Directory indexStore, Query q) 
//	throws IOException {
//		IndexSearcher searcher = new IndexSearcher(indexStore);
//		Hits hits = searcher.search(q);
//		int _length = hits.length();
//		System.out.println("HITS: " + _length);
//		for (int i = 0; i < _length; i++) {
//			Document doc = hits.doc(i);
//			Field field = doc.getField("body");
//			System.out.println("  value: " + field.stringValue());
//		}
//		searcher.close();
//	}
//}