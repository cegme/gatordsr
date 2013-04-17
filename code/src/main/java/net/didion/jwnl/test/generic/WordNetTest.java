package net.didion.jwnl.test.generic;

import java.util.ArrayList;

import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNetTest {
public static void main(String[] args) {
	ArrayList<String> synonyms=new ArrayList<String>();
	
	  System.setProperty("wordnet.database.dir", "");
	  String wordForm = "make";
	  
	  Synset[] synsets = database.getSynsets(wordForm,SynsetType.VERB);
	  if (synsets.length > 0) {
	       for (int i = 0; i < synsets.length; i++) {
	    String[] wordForms = synsets[i].getWordForms();
	    for (int j = 0; j < wordForms.length; j++) {
	           if(!synonyms.contains(wordForms[j])){
	        synonyms.add(wordForms[j]); }
	                }
	           }
	     }
}
}


