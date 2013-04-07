package edu.ufl.cise

import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker
import edu.washington.cs.knowitall.extractor.ReVerbExtractor
import edu.washington.cs.knowitall.extractor.conf.ReVerbOpenNlpConfFunction

object RelationExtractor extends Logging{
	// TODO: use reverb to extract the relations in the sentences
	// how to represent a sentence and manipulate words in it?
  
	def run()
	{
		val sentStr = "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865."
		 // val sentStr = "Michael McGinn is the mayor of Seattle.";

        // Looks on the classpath for the default model files.
        val chunker = new OpenNlpSentenceChunker();
        val sent = chunker.chunkSentence(sentStr);

        // Prints out the (token, tag, chunk-tag) for the sentence
       // println(sentStr);
        for (i <- 0 until sent.getLength()) {
            val token = sent.getToken(i);
            val posTag = sent.getPosTag(i);
            val chunkTag = sent.getChunkTag(i);
           // println(token + " " + posTag + " " + chunkTag);
        }

        // Prints out extractions from the sentence.
        val reverb = new ReVerbExtractor();
        val confFunc = new ReVerbOpenNlpConfFunction();
        val it = reverb.extract(sent).iterator()
        
        while(it.hasNext())
        {
        	val extr = it.next()        
        	val conf = confFunc.getConf(extr);
            println("Arg1 = " + extr.getArgument1() + " Rel = " + extr.getRelation()  + " Arg 2= " + extr.getArgument2() + 
                " Conf = " + conf);
        }
	}
  
	def main (args: Array[String])
	{
		run()
		logInfo("relation extraction")
	}
}