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
		w.searchWord("best", POS.ADJECTIVE);
		w.searchWord("best", POS.ADVERB);
		w.searchWord("best", POS.NOUN);
		w.searchWord("best", POS.VERB);

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
			 * under "./resources/wordnet/dict/" directory. With the WordNet
			 * directory & this class present in same directory
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

	/**
	 * Seearch for a word with specific Part-Of-Speech in mind
	 * 
	 * @param key
	 * @param pos
	 */
	public void searchWord(String key, POS pos) {

		System.out
				.println("-----------------------------------------------------------------------------------");
		System.out
				.println("-----------------------------------------------------------------------------------");
		System.out.println("-----------------------------Word -> " + key
				+ "------------");
		System.out
				.println("-----------------------------Par-Of-Speech searched for -> "
						+ pos + "------");
		System.out
				.println("-----------------------------------------------------------------------------------");
		System.out
				.println("-----------------------------------------------------------------------------------");

		/*
		 * A word is having a different WordId in different synsets. Each Word
		 * is having a unique Index.
		 */

		// Get the index associated with the word, 'book' with Parts of Speech
		// e.g. NOUN.
		IIndexWord idxWord = dictionary.getIndexWord(key, pos);

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

			System.out.println("getRelatedSynsets");
			checkRelatedSynSets(wordSynset, Pointer.ALSO_SEE);
			checkRelatedSynSets(wordSynset, Pointer.ANTONYM);
			checkRelatedSynSets(wordSynset, Pointer.ATTRIBUTE);
			checkRelatedSynSets(wordSynset, Pointer.CAUSE);
			checkRelatedSynSets(wordSynset, Pointer.DERIVATIONALLY_RELATED);
			checkRelatedSynSets(wordSynset, Pointer.DERIVED_FROM_ADJ);
			checkRelatedSynSets(wordSynset, Pointer.ENTAILMENT);
			checkRelatedSynSets(wordSynset, Pointer.HOLONYM_MEMBER);
			checkRelatedSynSets(wordSynset, Pointer.HOLONYM_PART);
			checkRelatedSynSets(wordSynset, Pointer.HOLONYM_SUBSTANCE);
			checkRelatedSynSets(wordSynset, Pointer.HYPERNYM);
			checkRelatedSynSets(wordSynset, Pointer.HYPERNYM_INSTANCE);
			checkRelatedSynSets(wordSynset, Pointer.HYPONYM);
			checkRelatedSynSets(wordSynset, Pointer.HYPONYM_INSTANCE);
			checkRelatedSynSets(wordSynset, Pointer.MERONYM_MEMBER);
			checkRelatedSynSets(wordSynset, Pointer.MERONYM_PART);
			checkRelatedSynSets(wordSynset, Pointer.MERONYM_SUBSTANCE);
			checkRelatedSynSets(wordSynset, Pointer.PARTICIPLE);
			checkRelatedSynSets(wordSynset, Pointer.PERTAINYM);
			checkRelatedSynSets(wordSynset, Pointer.REGION);
			checkRelatedSynSets(wordSynset, Pointer.REGION_MEMBER);
			checkRelatedSynSets(wordSynset, Pointer.SIMILAR_TO);
			checkRelatedSynSets(wordSynset, Pointer.TOPIC);
			checkRelatedSynSets(wordSynset, Pointer.TOPIC_MEMBER);
			checkRelatedSynSets(wordSynset, Pointer.USAGE);
			checkRelatedSynSets(wordSynset, Pointer.USAGE_MEMBER);
			checkRelatedSynSets(wordSynset, Pointer.VERB_GROUP);

			checkRelatedMap(wordSynset);

			// Returns the gloss associated with the synset.
			System.out.println("GLOSS -> " + wordSynset.getGloss());

			System.out.println();
			i++;
		}
	}

	void checkRelatedSynSets(ISynset wordSynset, IPointer iPointer) {
		List<ISynsetID> list = wordSynset.getRelatedSynsets(iPointer);
		if (list.size() != 0) {
			System.out.println("\t" + iPointer + " {");

			// Returns all the words present in the synset wordSynset
			for (ISynsetID isid : list) {
				System.out.print("\t\t pos[" + isid.getPOS() + "]: ");

				for (IWord hypernymWord : dictionary.getSynset(isid).getWords()) {
					System.out.print(hypernymWord.getLemma() + "   ");
				}
				System.out.println();
			}
			System.out.print("\t}" + "\n");
		}
	}

	void checkRelatedMap(ISynset wordSynset) {
		System.out.println("getRelatedMap " + " {");

		// Returns all the words present in the synset wordSynset
		for (Object o : wordSynset.getRelatedMap().keySet().toArray()) {
			System.out.println("\t" + ((IPointer) o).getName() + " => ");
			List<ISynsetID> list = wordSynset.getRelatedMap().get(o);
			for (ISynsetID isid : list) {
				System.out.print("\t\t");
				System.out.println(dictionary.getSynset(isid).getWords().get(0)
						.getLemma());
			}
			System.out.println();
		}
		System.out.print("}" + "\n");
	}

}
