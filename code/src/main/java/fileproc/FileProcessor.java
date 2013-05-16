package fileproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class FileProcessor {

	int threadCount = 32;
	static String localDirSDD = "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	static String localDirSDE = "/media/sde/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	static String localDirPrefix = "s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	String AWS_URL = "http://s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";

	private boolean isStorableInSDD(int size) {
		String s = "/media/sdd";
		File file = new File(s);
		long totalSpace = file.getTotalSpace();
		// total disk space in bytes.
		long usableSpace = file.getUsableSpace();
		// /unallocated / free disk space in bytes.
		long freeSpace = file.getFreeSpace();
		// unallocated / free disk space in bytes.
		// System.out.println(" === Partition Detail ===");
		//
		// System.out.println(" === bytes ===");
		// System.out.println("Total space : " + totalSpace + " bytes");
		// System.out.println("Usable space : " + usableSpace + " bytes");
		// System.out.println("Free space : " + freeSpace + " bytes");
		//
		// System.out.println(" === mega bytes ===");
		// System.out.println("Total space : " + totalSpace / 1024 / 1024 +
		// " mb");
		// System.out.println("Usable space : " + usableSpace / 1024 / 1024
		// + " mb");
		// System.out.println("Free space : " + freeSpace / 1024 / 1024 +
		// " mb");
		return usableSpace - size > 0;
	}

	private void Execute() {

		try {
			download();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// private boolean isAlreadyDownloaded(String localDir, String file, String
	// dir){
	//
	// File f = new File(localDir + localDirPrefix);
	// if(f.exists()) { /* do something */ }
	// }

	/**
	 * This method will download the corpus into two disks, fills one first then
	 * fills the other one.
	 */
	private void download() {

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		URL url;
		InputStream is = null;
		BufferedReader br;
		try {
			url = new URL(
					"http://s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/index.html");
			is = url.openStream(); // throws an IOException
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		br = new BufferedReader(new InputStreamReader(is));

		int i = 0;
		boolean finished = false;
		String line;
		while (!finished) {
			Pattern p = Pattern.compile("a href=\"([^\"]+)\"");

			try {
				line = br.readLine();

				if (line == null)
					finished = true;
				else {
					final Matcher m1 = p.matcher(line);

					if (m1.find()) {
						String linkStr = m1.group(1);
						// System.out.println(linkStr);
						final String dir = linkStr.substring(0,
								linkStr.indexOf('/'));
						System.out.println(dir);

						Runnable worker = new Thread(i++ + " " + linkStr) {
							public void run() {
								int size;
								try {
									size = getDirSize(dir);

									if (isStorableInSDD(size))
										downloadDir(localDirSDD, dir);
									else
										downloadDir(localDirSDE, dir);

								} catch (Exception e) {
									e.printStackTrace();
								}
							};
						};
						executor.execute(worker);
					} else if (i > 1) {// skip initial no line
						finished = true;
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		System.out.println("Finished all threads");
	}

	int getDirSize(String dir) throws Exception {
		String line;
		int size = 0;

		URL url = new URL(AWS_URL + dir + "/index.html");
		URLConnection conn;
		InputStream is = url.openStream();
		BufferedReader br2 = new BufferedReader(new InputStreamReader(is));

		Pattern p = Pattern.compile("a href=\"([^\"]+)\"");
		while ((line = br2.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.find()) {
				String linkStr = m.group(1);
				if (linkStr.contains("gpg")) {
					String gpgFileURL = AWS_URL + dir + "/" + linkStr;

					url = new URL(gpgFileURL);
					conn = url.openConnection();
					int tempSize = conn.getContentLength();

					if (tempSize < 0)
						System.out.println(dir + "/" + linkStr
								+ " Could not determine file size.");
					else {
						// System.out.println(dir + "/" + linkStr + " Size: "
						// + size);
						size += tempSize;
					}
					try {
						conn.getInputStream().close();
					} catch (Exception e) {
						System.err.println(dir + " " + gpgFileURL);
						e.printStackTrace();
					}
				}
			}
		}
		return size;
	}

	void downloadDir(String localDir, String dir) throws Exception {
		String line;

		InputStream is;
		URL url = new URL(AWS_URL + dir + "/index.html");
		is = url.openStream();
		BufferedReader br2 = new BufferedReader(new InputStreamReader(is));

		Pattern p = Pattern.compile("a href=\"([^\"]+)\"");
		while ((line = br2.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.find()) {
				String linkStr = m.group(1);
				if (linkStr.contains("gpg")) {

					// System.out.println(linkStr);
					String gpgFileURL = AWS_URL + dir + "/" + linkStr;

					// printFileSize(gpgFileURL);

					String commandWget = "wget -nc -q -P " + localDir + dir
							+ "/ " + gpgFileURL;

					// String commandDecrypytHDFS = "wget -O - "
					// + gpgStr
					// +
					// " |   gpg --no-permission-warning --trust-model always  "
					// + " | hdfs dfs "
					// + localDir + dir + "/"
					// + linkStr.substring(0, linkStr.length() - 4);

					String command = commandWget;
					runShellCommand(command);
				}
			}
		}
	}

	public static InputStream runShellCommand(String command) {
		String line;
		String[] cmd = { "/bin/sh", "-c", command };
		System.out.println(command);
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd);

			// System.out.println(process.exitValue());
			// BufferedReader stdOut = new BufferedReader(new InputStreamReader(
			// process.getInputStream()));
			// BufferedReader stdErr = new BufferedReader(new InputStreamReader(
			// process.getErrorStream()));

			// return IOUtils.toByteArray(process.getInputStream());
			return process.getInputStream();
			// while ((line = stdOut.readLine()) != null) {
			// System.out.println(line);
			// }
			// System.out.println("");
			// while ((line = stdErr.readLine()) != null) {
			// System.out.println(line);
			// }
			// process.destroy();
		} catch (IOException e) {
			System.err.println(command);
			e.printStackTrace();
		}
		return null;
		// return process.getInputStream();
	}

	private void printFileSize(String gpgFileURL) throws Exception {
		URL url = new URL(gpgFileURL);
		URLConnection conn = url.openConnection();
		int size = conn.getContentLength();
		if (size < 0)
			System.out.println(gpgFileURL + " Could not determine file size.");
		else
			System.out.println(gpgFileURL + " Size: " + size);
		conn.getInputStream().close();
	}

	public static void main(String[] args) {
		FileProcessor fp = new FileProcessor();
		fp.Execute();
	}
}
