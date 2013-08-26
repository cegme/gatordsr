package edu.ufl.cise;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ufl.cise.pipeline.Entity;
import edu.ufl.cise.pipeline.Preprocessor;

public class IndexLogReader {

	// public static final String LOG_FILES_BASE_DIR =
	// "/home/morteza/trec/runs/";
	public static final String ENTITY_LOG_PATTERN = "^>(\\d{4}-\\d{2}-\\d{2})/.*?gpg.*";

	public static final String FILE_LOG_PATTERN = "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})"
			+ " Total (\\d+) Files \\d+.. SIs: (\\d+) \\+SIs: ?(\\d+) Thread\\(\\d+\\)(.*)";

	public static final String TO_PROCESS_LOG_PATTERN = "";
	private static final Pattern pFileLog = Pattern.compile(FILE_LOG_PATTERN);
	static Boolean TRUE = new Boolean(true);

	public static String getPreLoggedFileName(String lineFileLog) {
		if (lineFileLog.length() > 0 && lineFileLog.charAt(0) != '>') {
			Matcher matcher = pFileLog.matcher(lineFileLog);
			if (matcher.find()) {
				// System.out.println(matcher.group(0));
				return matcher.group(5);
			}
		}
		return null;
	}

	public static Hashtable<String, Boolean> getPreLoggedFileList(String dir)
			throws FileNotFoundException {
		List<String> oldLogs = DirList.getFileList(dir, null);
		Hashtable<String, Boolean> preLoggedFileList = new Hashtable<String, Boolean>();
		for (String s : oldLogs) {
			Scanner sc = new Scanner(new File(s));
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String filePath = IndexLogReader.getPreLoggedFileName(line);
				// System.out.println(filePath);
				if (filePath != null)
					preLoggedFileList.put(filePath, TRUE);
			}
		}
		return preLoggedFileList;
	}

	public static String getToProcessFileName(String lineFileLog) {
		if (lineFileLog.length() > 0 && lineFileLog.charAt(0) == '+') {
			return lineFileLog.substring(3);
		}
		return null;
	}

	/**
	 * File List generated by the cpp file
	 * 
	 * @param dir
	 * @return
	 * @throws FileNotFoundException
	 */
	public static Hashtable<String, Boolean> getToProcessFileList(String dir)
			throws FileNotFoundException {
		List<String> oldLogs = DirList.getFileList(dir, null);
		Hashtable<String, Boolean> toProcessFileList = new Hashtable<String, Boolean>();
		for (String s : oldLogs) {
			Scanner sc = new Scanner(new File(s));
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String filePath = IndexLogReader.getToProcessFileName(line);
				// System.out.println(filePath);
				if (filePath != null) {
					// System.out.println("% " + filePath);
					toProcessFileList.put(filePath, TRUE);
				}
			}
		}
		return toProcessFileList;
	}

	/**
	 * 
	 * Get Total file size processed so far.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {

		Scanner sc = new Scanner(new File(
				"/Users/morteza/zProject/submission.txt.oneLiner.sorted.uniq"));
		ArrayList<Entity> a = new ArrayList<Entity>();
		Preprocessor
				.initEntityList(
						"resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json",
						a);

		HashMap<String, Integer> hm = new HashMap<String, Integer>();

		HashMap<String, Integer> hslotsTotal = new HashMap<String, Integer>();

		HashMap<String, HashSet<String>> hslots = new HashMap<String, HashSet<String>>();

		hslotsTotal.put("Affiliate", new Integer(0));
		hslotsTotal.put("AssociateOf", new Integer(0));
		hslotsTotal.put("Contact_Meet_PlaceTime", new Integer(0));
		hslotsTotal.put("AwardsWon", new Integer(0));
		hslotsTotal.put("DateOfDeath", new Integer(0));
		hslotsTotal.put("CauseOfDeath", new Integer(0));
		hslotsTotal.put("Titles", new Integer(0));
		hslotsTotal.put("FounderOf", new Integer(0));
		hslotsTotal.put("EmployeeOf", new Integer(0));
		hslotsTotal.put("Contact_Meet_Entity", new Integer(0));
		hslotsTotal.put("TopMembers", new Integer(0));
		hslotsTotal.put("FoundedBy", new Integer(0));
		hslotsTotal.put("Samples", new Integer(0));

		for (Entity entity : a) {
			hm.put(entity.target_id(), new Integer(0));
		}
		// while (sc.hasNextLine()) {
		// String line = sc.nextLine();
		// if (line.contains("ling>")) {
		// String[] arr = line.split(" \\| ");
		// String date_hour = arr[0].split(">")[1];
		// String filename = arr[1];
		// String si_num = arr[2];
		// String topics = line.split("\\|\\| ")[1];
		// //System.out.println(date_hour + filename + si_num + topics);
		// hm.put(topics, hm.get(topics) + 1);
		// }
		// }

		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.contains("FoundedBy")) {
				System.out.println();
			}
			String[] arr = line.split(" ");
			hm.put(arr[3], hm.get(arr[3]) + 1);
			hslotsTotal.put(arr[8], hslotsTotal.get(arr[8]) + 1);
			// System.out.println(arr[8]);
			if (hslots.get(arr[8]) == null)
				hslots.put(arr[8], new HashSet<String>());
			hslots.get(arr[8]).add(arr[3]);
			// hslots.put(arr[8], value)
		}

		System.out
				.println("# of times the entity appears in slot values triples");
		for (Entity entity : a) {

			System.out.println(entity.target_id() + "\t"
					+ hm.get(entity.target_id()));
		}
		System.out.println("\nTotal instances of slot value found");
		for (String s : hslotsTotal.keySet()) {
			System.out.println(s + "\t" + hslotsTotal.get(s));
		}
		System.out
				.println("\n# of entities that we have found the slot value for");

		for (String s : hslots.keySet()) {
			System.out.println(s + "\t" + hslots.get(s).size());
		}

	}
}
