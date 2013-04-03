package edu.ufl.cise

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.Properties
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.ie.NERClassifierCombiner
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.pipeline.ParserAnnotator
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.pipeline.DeterministicCorefAnnotator
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.Annotator
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation
import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.trees.semgraph.SemanticGraph
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation
import edu.stanford.nlp.dcoref.CorefChain
import java.util.ArrayList
import java.util.HashMap
import edu.ufl.cise.util.RelationChecker

// used to represent relations during the sentence relation extraction
class Relation(entity0:String, slot:String, entity1:String)
{
  override def toString():String = 
	{
		val s = entity0 + " - " + slot + " - " + entity1
		return s
	}
}

object Pipeline extends Logging{
	
	// preprocessing pipelines, parser, and corefernce annotator
	private var prepipeline : StanfordCoreNLP = null
	private var parser: ParserAnnotator = null
	private var dcoref : DeterministicCorefAnnotator = null
	// the entity coreference map
	private var corefMap : java.util.Map[Integer, String] = null
	// the important sentences that contain the desired entity
	private var critSens : ArrayList[Integer] = new ArrayList[Integer]
	// used in future for storing interesting sentences
	private var sentences : ArrayList[ArrayList[String]] = null
	// use to store the extracted relations
	private var relations : ArrayList[Relation] = null
	// a bloom filter to check relations
	private val bf = RelationChecker.createWikiBloomChecker
	
	def init()
	{
		var props = new Properties()
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		// preprocessing pipeline for tokenize, ssplit, pos, lemma and ner
		prepipeline = new StanfordCoreNLP(props)
		//prepipeline.addAnnotator(annotator)
		// parser
		val verbose = false
		parser = new ParserAnnotator(verbose, -1)
	  	//coreference with default properties
		dcoref = new DeterministicCorefAnnotator(new Properties)
	}
	
	// used to filter out irrelevant documents, will be implemented in future
	def filter (document:Annotation) : Boolean =
	{
		// output the ner results on tokens
		var tokens = document.get[java.util.List[CoreLabel], TokensAnnotation](classOf[TokensAnnotation])
		for (i <- 0 until tokens.size())
		{
			// get the named entity for each token
			val token = tokens.get(i)
			val pos = token.get[String, PartOfSpeechAnnotation](classOf[PartOfSpeechAnnotation]);
			val ne = token.get[String, NamedEntityTagAnnotation](classOf[NamedEntityTagAnnotation])
			// println(token.value() + " : " + ne)
			// println(token.beginPosition())
			// println(token.endPosition())
			// println(token.index())
		} 
		
		// filter here: check entities by using ne labels
		// to filter needs quick pre-label
		return true
	}
	
	// prepare for relation extraction 
	def prepare(document:Annotation)
	{	
		// get coreference entities
		val graph = document.get[java.util.Map[Integer, CorefChain],CorefChainAnnotation](classOf[CorefChainAnnotation])
		corefMap = new HashMap[Integer, String] // used to store entities and corresponding corerference group
		for(i <-1 to graph.size()) // to traverse the coreference graph
		{
			val corefChain : CorefChain = graph.get(i) // for each coref chain
			if (corefChain != null)
			{
			val mentions = corefChain.getMentionsInTextualOrder()
			if (!mentions.isEmpty())
			{
				for (mention <- mentions.toArray())
				{
					val s = mention.toString().split("\"")(1).split("\"")(0)				
					// maybe filter here in future
					// add the reference of the entity
					corefMap.put(i, s)
					// mark the critical sentences
					val l = Integer.parseInt(mention.toString().split("sentence ")(1))
					if (!critSens.contains(l)) critSens.add(l)
				}
			}
			}
		}
	}
	
	// break a single sentence to corresponding array list of words and marks (mark = 0, non-entity; 1, entity)
	def breakSentence(tokens : java.util.List[CoreLabel], words:ArrayList[String], marks:ArrayList[Integer])
	{
		val size = tokens.size()
		for (j <- 0 until size){
				// this is the text of the token
				val token = tokens.get(j)
				// this is the POS tag of the token
				val pos = token.get[String, PartOfSpeechAnnotation](classOf[PartOfSpeechAnnotation])
				// this is the NER label of the token
				val ne = token.get[String, NamedEntityTagAnnotation](classOf[NamedEntityTagAnnotation])
				words.add(token.value())			
				if (ne.length() > 1) // length > 1, ne is an entity
					marks.add(1)
				else
					marks.add(0)
					
			}
			
	}
	// transform a list of Strings to a single String
	def transfer(array:java.util.List[String]):String =
	{
	   var s = ""
	   for (i <- 0 until array.size())
	   {
		   if ( i != array.size() - 1)
			   s += array.get(i) + " "
		   else
			   s += array.get(i)
	   }
	   
	   return s
	}
	
	// used to extract two entities located at the nearest distance of the relation
	def getEntity(words:java.util.List[String], marks:java.util.List[Integer], flag:Int) : String = 
	{
		var s = ""
		
		if (flag == 1) // flag = 1, extract entity behind the relation, 0 before the relation
		{
			var k3 = -1
			var k4 = -1
						
			for (m <- 0 until words.size())
			{
				if (marks.get(m) != 0 && k3 == -1 && k4 == -1)
				{
					k3 = m
				}
							
				if (marks.get(m) == 0 && k3 != -1 && k4 == -1)
				{
					k4 = m
				}
			}
					
			if(k3 != -1 && k4 != -1)
			{
				s = transfer(words.subList(k3, k4))
			}
			
			else
			    s = "N*A" // means no entity found
		}
		else
		{
			var k1 = -1
			var k2 = -1	
			for (k <- words.size() -1 to 0 by -1)
			{
				if (marks.get(k) != 0 && k1 == -1 && k2 == -1)
				{
						k1 = k
				}
							
				if (marks.get(k) == 0 && k1 != -1 && k2 == -1)
				{
					k2 = k
				}
			}
			
			k2 = k2 + 1
			if (k2 != -1 && k1 != -1)
			{
				s = transfer(words.subList(k2, k1 + 1))
			}
			else
				s = "N*A"
			
		}
		  
		return s
	}
	
	// extract the relations from the array of words and marks
	def getRelations(words:ArrayList[String], marks:ArrayList[Integer]): ArrayList[Relation] =
	{
		var results = new ArrayList[Relation] // used to store results
		val size = words.size()
		for ( i <- 0 until size) // check all the possible i and j postions
		{
			for (j <- i until size)
			{	
			    val s =  transfer(words.subList(i, j + 1)).toLowercase()
				if (bf(s)) // use the bloom filter to check
				{
					// println(s + " => " + true)
					val entity0 = getEntity(words.subList(0, i), marks.subList(0, i), -1)
					val entity1 = getEntity(words.subList(j + 1, size), marks.subList(j+1, size),1)
					if (!entity0.equals("N*A") && !entity1.equals("N*A"))
					{
						val relation = new Relation(entity0, s, entity1)
						results.add(relation)
					}
				}	
			}
		}
		return results
	}
	
	// extract relations from sentences
	def extract(document:Annotation)
	{
		val sentences = document.get[java.util.List[CoreMap], SentencesAnnotation](classOf[SentencesAnnotation])
		for (i <- 0 until sentences.size())
		{
			// get each sentence
			val sentence = sentences.get(i)
			val line = sentence.toString()
			// output the possible relations between two named entities
			println(sentence.toString())

			val tokens = sentence.get[java.util.List[CoreLabel], TokensAnnotation](classOf[TokensAnnotation])
			val size = tokens.size()
			// var x = tokens.toArray()
			var words = new ArrayList[String](size)
			var marks = new ArrayList[Integer](size)
			// break sentence to words and marks
			breakSentence(tokens, words, marks)
			// extrac relations
			relations = getRelations(words, marks)
			// log each relation
			for(relation <- relations.toArray())logInfo(relation.toString())
		}
		
	}
	
	// used to exactly match two relation triples
	def matchTriples(query : Array[String], relation : Array[String]) : Boolean = 
	{
		if (query(0).equalsIgnoreCase(relation(0)) && query(0).equalsIgnoreCase(relation(0)) && query(0).equalsIgnoreCase(relation(0)))
			return true
		else
			return false
	}
	
	// the main logic
	def run(text:String)
	{

		// create an empty Annotation just with the given text
		val document = new Annotation(text)
		
		// preprocessing the document and get the named entities
		prepipeline.annotate(document)
		// filter out the document
		val valid = filter(document)
		if (!valid) return
		
		// parsing the document
		parser.annotate(document)
		// coreference resolution
		dcoref.annotate(document)
		
		// extract relations
		prepare(document)
		extract(document)
		
		// match the triples
		
		
		// pipeline ends
		logInfo("pipeline ends")
	}
	
	
	def main (args: Array[String])
	{
	  	// initialize the annotators
		init()
		// extract relations from a string
		val text = "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865."
		run(text)
	}
	
}



