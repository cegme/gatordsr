package edu.ufl.cise;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import edu.ufl.cise.pipeline.Entity;
import edu.ufl.cise.pipeline.Preprocessor;

/**
 * This class will keep track of the statistics of the KBA runs that we had for the first
 * submission. <br>
 * 
 * We want to c compute the following metrics:<br>
 * <br>
 * 1. For each entity, how many documents (with lingpipe) matched the alias stage?<br>
 * <br>
 * 2. For each entity, "how many sentences" matched the filter stage (ie, before running the
 * extraction rules, but after filtering at the sentence level)?<br>
 * <br>
 * 3. For each entity, how many sentences have slot values (ie, final output)? We already generate
 * the slot values as soon as we find them. <br>
 * <br>
 * 4. For each entity, how many documents with only clean_visible match the alias stage (ie, these
 * documents we skipped for the submission)? needs to be doen b.c. the file that chris gives with
 * cpp script only talks about gpg filesand I skip non-lingpipe documents, I need to look at ling
 * pipe ones. Specifying where we find the match in the outputformat is the process that we take.<br>
 * <br>
 * <br>
 * Important -- only count unique (entity, sentence) pairs. If a sentences (or document) is
 * processed multiple times per entity for some reason (ie. bug), keep count of the duplicate
 * processing (for each entity).
 * 
 * @author morteza
 * 
 */
public class SystemStatisticsGenerator {

	/**
	 * HashMap will do the work as we will not update the map but only update the counts which are
	 * atomic ooperators themeslves.
	 */
	// public final Hashtable<Entity, AtomicLong>
	// ALIAS_STAGE_MATCHED_DOCUMENT_WITH_LINGPIPE_HM;
	//
	// public AtomicLong a = new AtomicLong(0);
	//
	// public FirstSubmissionStatisticsGenerator(List<Entity> listEntity) {
	// ALIAS_STAGE_MATCHED_DOCUMENT_WITH_LINGPIPE_HM = new Hashtable<Entity,
	// AtomicLong>();
	// for (Entity e : listEntity) {
	// ALIAS_STAGE_MATCHED_DOCUMENT_WITH_LINGPIPE_HM.put(e, new AtomicLong(0));
	// }
	// }
	//
	/**
	 * The input is the output of the system, output is the above per-enity statistics.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		List<Entity> listEntity = Preprocessor.entity_list();
		List<String> listLogs = DirList.getFileList(SETTINGS.LOG_DIR, "");
		for (String s : listLogs) {
			Scanner sc = new Scanner(new File(s));
			String line = sc.nextLine();

			line = "ling>2012-02-16-23 | news-337-dfbde5b83aba149fcaa0cd7d2097447e-241783b11bc675f521068f3b3a5e9e71.sc.xz.gpg | 43 | fdd93a691c236dfdd9a5af15ee579bae || http://en.wikipedia.org/wiki/Edgar_Bronfman,_Jr., ";

			String raw = "raw>";
			String cleanVisible = "cleanVisible>";
			String ling = "ling>";

			String record = "";
			if (line.substring(0, ling.length()) == ling) {
				record = line.substring(ling.length());
				getRecord(ling, line);
			}
			if (line.substring(0, ling.length() - 1) == raw) {
				record = line.substring(ling.length());
			}
			if (line.substring(0, ling.length() - 1) == cleanVisible) {
				record = line.substring(ling.length());
			}
		}

	}

	private static List<String> getRecord(String prefix, String line) {
		String entityList = line.substring(line.indexOf("||"));

		System.out.println(entityList);
		return null;
	}
}
