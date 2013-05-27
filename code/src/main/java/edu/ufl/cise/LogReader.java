package edu.ufl.cise;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogReader {

	public static final String	LOG_FILES_BASE_DIR	= "/home/morteza/trec/runs/";
	public static final String	ENTITY_LOG_PATTERN	= "^>(\\d{4}-\\d{2}-\\d{2})/.*?gpg.*";

	public static final String	FILE_LOG_PATTERN		= "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})"
																											+ " Total (\\d+) Files  SIs: (\\d+) \\+SIs: ?(\\d+) (.*)";

	/**
	 * 
	 * Get Total file size processed so far.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Pattern p = Pattern.compile(ENTITY_LOG_PATTERN);

		String line = ">2011-10-08-15/social-292-bba11a194150414d9f683164d0dd05ee-1c8a01976ae9fd0b448605d9902fb0f7.sc.xz.gpg/154/65f1fff7a781732d244d70a211440743< http://en.wikipedia.org/wiki/Dunkelvolk";

		if (line.length() > 0 && line.charAt(0) == '>') {
			Matcher matcher = p.matcher(line);
			if (matcher.find()) {
				System.out.println(matcher.group(0));
			}
		}

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
