package test;

import java.net.URL;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

/**
 * 
 * @author www.cse.iitb.ac.in/~cs626-449/JWI_JWKTL/DemoWordNet.java
 * 
 */
public class DemoWordNet {

	public static void main(String args[]) {
		WordNet w = new WordNet();
		w.searchWord("book");

	}
}

class WordNet {
	/*
	 * IDictionary is the main interface for acessing WordNet dictionary Files.
	 * Dictionary class implements IDictionary interface.
	 */
	public IDictionary dictionary = null;

	WordNet() {
		try {
			/*
			 * 'path' holds the loaction of the WordNet dictionary files. In
			 * this code it is assumed that the dictionary files are located
			 * under "WordNet/dict3.0/" directory. With the WordNet directory &
			 * this class present in same directory
			 */
			String path = "./resources/wordnet/dict/";
			URL url = new URL("file", null, path);

			// construct the dictionary object and open it
			dictionary = new Dictionary(url);
			dictionary.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void searchWord(String key) {

		/*
		 * A word is having a different WordId in different synsets. Each Word
		 * is having a unique Index.
		 */

		// Get the index associated with the word, 'book' with Parts of Speech
		// NOUN.
		IIndexWord idxWord = dictionary.getIndexWord("book", POS.NOUN);

		System.out.println("Word ->" + key);
		System.out.println("-------------");
		System.out.println("-------------");

		int i = 1;

		/*
		 * getWordIDs() returns all the WordID associated with a index
		 */
		for (IWordID wordID : idxWord.getWordIDs()) {
			// Construct an IWord object representing word associated with
			// wordID
			IWord word = dictionary.getWord(wordID);
			System.out.println("SENSE->" + i);
			System.out.println("---------");

			// Get the synset in which word is present.
			ISynset wordSynset = word.getSynset();

			System.out.print("Synset " + i + " {");

			// Returns all the words present in the synset wordSynset
			for (IWord synonym : wordSynset.getWords()) {
				System.out.print(synonym.getLemma() + ", ");
			}
			System.out.print("}" + "\n");

			checkRelatedSynSets(wordSynset);

			checkRelatedMap(wordSynset);

			// Returns the gloss associated with the synset.
			System.out.println("GLOSS -> " + wordSynset.getGloss());

			System.out.println();
			i++;
		}
	}
	
	void checkRelatedSynSets(ISynset wordSynset){
		System.out.println("getRelatedSynsets " + " {");

		// Returns all the words present in the synset wordSynset
		for (ISynsetID isid : wordSynset.getRelatedSynsets(Pointer.REGION)) {
			System.out.print("\t"+isid.getPOS() + ", ");
			
			for (IWord hypernymWord : dictionary.getSynset(isid).getWords()) {
				System.out.println("\t#_#" + hypernymWord.getLemma());
			}
		}
		System.out.print("}" + "\n");
	}
	
	void checkRelatedMap(ISynset wordSynset){
		System.out.println("getRelatedMap " +  " {");

		// Returns all the words present in the synset wordSynset
		for (Object o : wordSynset.getRelatedMap().keySet().toArray()) {
			System.out.print("\t" +((IPointer) o).getName() + " => ");
			List<ISynsetID> list = wordSynset.getRelatedMap().get(o);
			for (ISynsetID isid : list)
				System.out.print( isid.getPOS());
			System.out.println();
		}
		System.out.print("}" + "\n");
	}
	
}