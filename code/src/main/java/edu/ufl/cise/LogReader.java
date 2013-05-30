package edu.ufl.cise;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogReader {

	public static final String	LOG_FILES_BASE_DIR	= "/home/morteza/trec/runs/";
	public static final String	ENTITY_LOG_PATTERN	= "^>(\\d{4}-\\d{2}-\\d{2})/.*?gpg.*";

	public static final String	FILE_LOG_PATTERN		= "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})"
																											+ " Total (\\d+) Files \\d+.. SIs: (\\d+) \\+SIs: ?(\\d+) Thread\\(\\d+\\)(.*)";

	public static String getFileName(String lineFileLog) {
		Pattern pFileLog = Pattern.compile(FILE_LOG_PATTERN);
		if (lineFileLog.length() > 0 && lineFileLog.charAt(0) != '>') {
			Matcher matcher = pFileLog.matcher(lineFileLog);
			if (matcher.find()) {
				// System.out.println(matcher.group(0));
				return matcher.group(5);
			}
		}
		return null;
	}

	static Boolean	TRUE	= new Boolean(true);

	public static Hashtable<String, Boolean> getPreLoggedFileList(String dir) throws FileNotFoundException {
		List<String> oldLogs = DirList.getFileList(dir, null);
		Hashtable<String, Boolean> preLoggedFileList = new Hashtable<String, Boolean>();
		for (String s : oldLogs) {
			Scanner sc = new Scanner(new File(s));
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String filePath = LogReader.getFileName(line);
				// System.out.println(filePath);
				if (filePath != null)
					preLoggedFileList.put(filePath, TRUE);
			}
		}
		return preLoggedFileList;
	}

	/**
	 * 
	 * Get Total file size processed so far.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {

		Pattern pEntity = Pattern.compile(ENTITY_LOG_PATTERN);

		String lineEntity = ">2011-10-08-15/social-292-bba11a194150414d9f683164d0dd05ee-1c8a01976ae9fd0b448605d9902fb0f7.sc.xz.gpg/154/65f1fff7a781732d244d70a211440743< http://en.wikipedia.org/wiki/Dunkelvolk";
		String lineFileLog = "2013-05-28 00:32:29 Total 2 Files 0MB SIs: 327 +SIs: 3 Thread(1)2011-10-05-01/arxiv-3-182a1d0a179563dbdfa2e88a37da70aa-6c44f1f07325fd0c48f4d9f4f771f768.sc.xz.gpg";

		System.out.println(getFileName(lineFileLog));

		Hashtable<String, Boolean> hash = getPreLoggedFileList(CorpusBatchProcessor.LOG_DIR_LOCAL_OLD);
		System.out.println(hash);

		// if (lineEntity.length() > 0 && lineEntity.charAt(0) == '>') {
		// Matcher matcher = pEntity.matcher(lineEntity);
		// if (matcher.find()) {
		// System.out.println(matcher.group(0));
		// }
		// }

		// List<String> list = DirList.getFileList(LOG_FILES_BASE_DIR, "Log.txt");
		//
		//
		// for (String filePath : list) {
		// Scanner sc = new Scanner(new File(filePath));
		// while (sc.hasNextLine()) {
		// String line = sc.nextLine();
		//
		// if (line.length() > 0 && line.charAt(0) == '>') {
		// Matcher matcher = p.matcher(line);
		// if (matcher.find()) {
		// System.out.println(matcher.group(0));
		// }
		// }
		// }
		// }
	}
}
