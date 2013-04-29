package wordnet.jwi;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.IndexWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

/**
 * MIT Java Wordnet Interface
 * @author www.cse.iitb.ac.in/~cs626-449/JWI_JWKTL/DemoWordNet.java
 * 
 */
public class JWIFullDemo {

	public static void main(String args[]) {
		WordNet w = new WordNet();
		String word = "best";
		System.out.println("Word: " + word);
		w.searchWord(word, POS.ADJECTIVE);
		w.searchWord(word, POS.ADVERB);
		w.searchWord(word, POS.NOUN);
		w.searchWord(word, POS.VERB);

		// Set<String> s = w.getAntonyms(word);
		// Iterator it = s.iterator();
		// while (it.hasNext()) {
		// Object o = it.next();
		// System.out.println(o);
		// }

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

		/*
		 * A word is having a different WordId in different synsets. Each Word
		 * is having a unique Index.
		 */

		// Get the index associated with the word, 'book' with Parts of Speech
		// e.g. NOUN.
		IIndexWord idxWord = dictionary.getIndexWord(key, pos);

		System.out
				.println("-----------------------------------------------------------------------------------");
		System.out
				.println("-----------------------------------------------------------------------------------");
		System.out.println("-----------------------------Look up for: " + key
				+ "[" + pos.toString().toUpperCase() + "]" + "------");

		// System.out
		// .println("-----------------------------(Word, Par-Of-Speech) frequency  -> "
		// + idxWord.getTagSenseCount() + "------");
		System.out
				.println("-----------------------------------------------------------------------------------");
		System.out
				.println("-----------------------------------------------------------------------------------");

		int i = 1;

		/*
		 * getWordIDs() returns all the WordID associated with a index (the list
		 * of meanings of the word)
		 */
		for (IWordID wordID : idxWord.getWordIDs()) {
			// Construct an IWord object representing word associated with
			// wordID
			IWord iword = dictionary.getWord(wordID);
			System.out.println("SENSE->" + i);
			System.out.println("---------");

			List<IWordID> antonymIds = dictionary.getWord(wordID)
					.getRelatedWords(Pointer.ANTONYM);
			// get lemmas for each word ids

			String strCount = "stack";
			try {
				System.out.println(strCount + ": count("
						+ dictionary.getIndexWord(
								new IndexWordID(strCount, pos))
								.getTagSenseCount() + ")");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (IWordID wordId : antonymIds) {
				System.out.println("A: count("
						+ dictionary.getIndexWord(
								new IndexWordID(wordID.getLemma(), pos))
								.getTagSenseCount() + ")"); // getWord(wordId).getLemma());
			}

			// for (IWordID iwid : word.getRelatedWords(Pointer.ANTONYM)) {
			// System.out.println("lem " + iwid);
			// ISynsetID isid = iwid.getSynsetID();
			// ISynset is = dictionary.getSynset(isid);
			// List<ISynsetID> list = is.getRelatedSynsets(Pointer.ALSO_SEE);
			// printSynset(list);
			// }

			// Get the synset in which word is present.
			ISynset wordSynset = iword.getSynset();

			// process the wordsynset :

			{
				checkGetWords(wordSynset, wordID, pos);

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
			}

			System.out.println();
			i++;
		}
	}

	/**
	 * print synonyms
	 * 
	 * @param wordSynset
	 */
	private void checkGetWords(ISynset wordSynset, IWordID wordID, POS pos) {
		System.out.print("count("
				+ dictionary.getIndexWord(
						new IndexWordID(wordID.getLemma(), pos))
						.getTagSenseCount() + ") Synset {");
		// dictionary.getSynset(wordSynset.getID());

		// Returns all the words present in the synset wordSynset
		for (IWord synonym : wordSynset.getWords()) {
			System.out.print(synonym.getLemma() + ", ");
		}
		System.out.print("}" + "\n");
	}

	/**
	 * print related synsets regarding to the pointer type
	 * 
	 * @param wordSynset
	 * @param iPointer
	 */
	void checkRelatedSynSets(ISynset wordSynset, IPointer iPointer) {
		List<ISynsetID> list = wordSynset.getRelatedSynsets(iPointer);
		if (list.size() != 0) {
			System.out.println("\t" + iPointer + " {");
			printSynset(list);
			System.out.print("\t}" + "\n");
		}
	}

	/**
	 * Semantically related (not lexically)
	 * 
	 * @param wordSynset
	 */
	void checkRelatedMap(ISynset wordSynset) {
		System.out.println("getRelatedMap " + " {");

		// Returns all the words present in the synset wordSynset
		for (Object o : wordSynset.getRelatedMap().keySet().toArray()) {
			System.out.println("\t" + ((IPointer) o).getName() + " => ");
			List<ISynsetID> list = wordSynset.getRelatedMap().get(o);
			printSynset(list);
			System.out.println();
		}
		System.out.print("}" + "\n");
	}

	void printSynset(List<ISynsetID> list) {
		// Returns all the words present in the synset wordSynset
		for (ISynsetID isid : list) {
			System.out.print("\t\t pos[" + isid.getPOS() + "]: ");

			for (IWord iw : dictionary.getSynset(isid).getWords()) {
				System.out.print(iw.getLemma() + "   ");
			}
			System.out.println();
		}
	}

	public Set<String> getAntonyms(String value) {
		Set<String> lemmas = new HashSet<String>();
		Set<IWordID> antonymIds = new HashSet<IWordID>();
		List<IWordID> ids = getWordIdsForPos(value, POS.NOUN);
		ids.addAll(getWordIdsForPos(value, POS.VERB));
		ids.addAll(getWordIdsForPos(value, POS.ADVERB));
		ids.addAll(getWordIdsForPos(value, POS.ADJECTIVE));
		// get word ids of all antonyms
		for (IWordID wordId : ids) {
			antonymIds.addAll(dictionary.getWord(wordId).getRelatedWords(
					Pointer.ANTONYM));
		}
		// get lemmas for each word ids
		for (IWordID wordId : antonymIds) {
			lemmas.add(dictionary.getWord(wordId).getLemma());
		}

		return lemmas;
	}

	/**
	 * Returns all word id for a given word with its corresponding {@link POS}.
	 * 
	 * @param value
	 *            the word
	 * @return the list of word ids, an empty list if the association of
	 *         {@code value} and {@code pos} does not exist
	 */
	private List<IWordID> getWordIdsForPos(String value, POS pos) {
		// Get the index word for all POS !.
		IIndexWord idxWord = dictionary
				.getIndexWord(new IndexWordID(value, pos));

		// Obtains all word ids for this word!
		List<IWordID> allIds = new ArrayList<IWordID>();
		if (idxWord != null)
			allIds.addAll(idxWord.getWordIDs());
		return allIds;
	}
}
