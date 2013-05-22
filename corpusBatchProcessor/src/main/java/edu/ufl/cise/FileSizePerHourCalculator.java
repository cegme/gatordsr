package edu.ufl.cise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSizePerHourCalculator {

	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws FileNotFoundException, ParseException {

		System.out.println("Generating report.");
		String localBaseDir = "/home/morteza/2013Corpus/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
		String serverBaseDir = "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
		String baseDir = "";

		PrintWriter pw = new PrintWriter(args[0] + ".csv");
		File dir = new File(localBaseDir);
		if (dir.isDirectory())
			baseDir = localBaseDir;
		else
			baseDir = serverBaseDir;
		Scanner sc = new Scanner(new File(args[0]));
		DateFormat df = CorpusBatchProcessor.logTimeFormat;
		Pattern p = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})"
				+ " Total (\\d+) Files  SIs: (\\d+) \\+SIs: ?(\\d+) (.*)");

		String line;
		Date dateStart = null;
		long size = 0;
		while (sc.hasNext()) {
			line = sc.nextLine();
			Matcher m = p.matcher(line);
			if (m.find()) {
				String dateStr = m.group(1);

				Date dateTemp = df.parse(dateStr);
				if (dateStart == null)
					dateStart = dateTemp;
				String SIs = m.group(3);
				String SIsPlus = m.group(4);
				String fileStr = baseDir + m.group(5);
				// System.out.println(fileStr);

				File temp = new File(fileStr);
				size += temp.length();

				// Get msec from each, and subtract.
				long diff = dateTemp.getTime() - dateStart.getTime();
				long diffSeconds = diff / 1000 % 60;
				long diffMinutes = diff / (60 * 1000) % 60;
				long diffHours = diff / (60 * 60 * 1000) % 60;
				// System.out.println("Time in seconds: " + diffSeconds + " seconds.");
				// System.out.println("Time in minutes: " + diffMinutes + " minutes.");
				// System.out.println("Time in hours: " + diffHours + " hours.");

				String tempSizeStr = FileProcessor.fileSizeToStr(size, "MB");
				String reportLine = diffHours + ":" + diffMinutes + ":" + diffSeconds + ","
						+ tempSizeStr//.substring(0, tempSizeStr.length() - 2)
						;
				pw.println(reportLine);
				//System.out.println(reportLine);
			}
		}
		pw.close();
	}

}
