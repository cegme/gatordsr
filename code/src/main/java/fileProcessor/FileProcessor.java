package fileProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileProcessor {

	public static void main(String[] args) {

		int threadCount = 32;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		try {
			URL url;
			InputStream is = null;
			BufferedReader br;
			BufferedReader br2;
			url = new URL(
					"http://s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/index.html");
			is = url.openStream(); // throws an IOException
			br = new BufferedReader(new InputStreamReader(is));

			String s = "";

			int i = 0;
			boolean finished = false;
			while (!finished) {

				// Thread t1 = myThread(br.readLine());

				Thread t = null;
				Pattern p = Pattern.compile("a href=\"([^\"]+)\"");
				final Matcher m1 = p.matcher(br.readLine());

				if (m1.find()) {
					String linkStr = m1.group(1);
					System.out.println(linkStr);
					final String dir = linkStr.substring(0,
							linkStr.indexOf('/'));
					System.out.println(dir);

					Runnable worker = new Thread(i++ + " " + linkStr) {
						public void run() {
							downloadDir(dir);
						};
					};
					executor.execute(worker);
				} else if (i > 1) {//skip initial no line
					finished = true;
				}
			}

			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			System.out.println("Finished all threads");

		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	// static Thread myThread(String line) {
	// Thread t = null;
	// Pattern p = Pattern.compile("a href=\"([^\"]+)\"");
	// final Matcher m1 = p.matcher(line);
	// if (m1.find()) {
	// t = new Thread() {
	// public void run() {
	// String linkStr = m1.group(1);
	// System.out.println(linkStr);
	// String dir = linkStr.substring(0, linkStr.indexOf('/'));
	// System.out.println(dir);
	// downloadDir(dir);
	// };
	// };
	// t.start();
	// }
	// return t;
	// }

	static void downloadDir(String dir) {
		try {
			String localDir = "/media/sdd/corpus/";

			(new File(localDir + dir)).mkdirs();

			String line;

			URL url = new URL(
					"http://s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/"
							+ dir + "/index.html");
			InputStream is = url.openStream(); // throws an IOException
			BufferedReader br2 = new BufferedReader(new InputStreamReader(is));

			Pattern p = Pattern.compile("a href=\"([^\"]+)\"");
			while ((line = br2.readLine()) != null) {
				Matcher m = p.matcher(line);
				if (m.find()) {
					String linkStr = m.group(1);
					if (linkStr.contains("gpg")) {
						System.out.println(linkStr);
						String gpgStr = "http://s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/"
								+ dir + "/" + linkStr;

						String command = "wget -O - "
								+ gpgStr
								+ " |   gpg --no-permission-warning --trust-model always --output "
								+ localDir + dir + "/"
								+ linkStr.substring(0, linkStr.length() - 4);

						String[] cmd = { "/bin/sh", "-c", command };

						System.out.println(command);
						Process process = Runtime.getRuntime().exec(cmd);
						// System.out.println(process.exitValue());
						System.out.println(process.toString());
						BufferedReader stdOut = new BufferedReader(
								new InputStreamReader(process.getInputStream()));
						BufferedReader stdErr = new BufferedReader(
								new InputStreamReader(process.getErrorStream()));
						while ((line = stdOut.readLine()) != null) {
							System.out.println(line);
						}

						System.out.println("///////////////////////////////");
						while ((line = stdErr.readLine()) != null) {
							System.out.println(line);
						}
						process.destroy();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
