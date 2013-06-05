package edu.ufl.cise;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S3CorpusDownloader {

	int						threadCount			= 32;
	static String	localDirLaptop	= "/home/morteza/2013Corpus/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	static String	localDirSDD			= "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	static String	localDirSDE			= "/media/sde/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
    static String   localDirTEST        = "/media/sde/tempStorage/";
	static String	localDirPrefix	= "s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	String				AWS_URL					= "http://s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";

	// private boolean isStorableInSDD(int size) {
	// String s = "/media/sdd";
	// File file = new File(s);
	// // long totalSpace = file.getTotalSpace();
	// // total disk space in bytes.
	// long usableSpace = file.getUsableSpace();
	// // /unallocated / free disk space in bytes.
	// // long freeSpace = file.getFreeSpace();
	// // unallocated / free disk space in bytes.
	// // System.out.println(" === Partition Detail ===");
	// //
	// // System.out.println(" === bytes ===");
	// // System.out.println("Total space : " + totalSpace + " bytes");
	// // System.out.println("Usable space : " + usableSpace + " bytes");
	// // System.out.println("Free space : " + freeSpace + " bytes");
	// //
	// // System.out.println(" === mega bytes ===");
	// // System.out.println("Total space : " + totalSpace / 1024 / 1024 +
	// // " mb");
	// // System.out.println("Usable space : " + usableSpace / 1024 / 1024
	// // + " mb");
	// // System.out.println("Free space : " + freeSpace / 1024 / 1024 +
	// // " mb");
	// return usableSpace - size > 0;
	// }

	private void Execute() {

		try {
			download();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method will download the corpus into two disks, fills one first then
	 * fills the other one.
	 */
	private void download() {

		//
		// ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		//

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
		Boolean TRUE = new Boolean(true);

		final Hashtable<String, Boolean> alreadyDownloadedTable = new Hashtable<String, Boolean>();
		{
			List<String> alreadyDownloaded = null;
			alreadyDownloaded = DirList.getFileList(localDirSDD, null);
			for (String s : alreadyDownloaded) {
                String tempFileStr = s.substring(localDirSDD.length());
                //System.out.println(tempFileStr);
				alreadyDownloadedTable.put(tempFileStr, TRUE);
			}
			alreadyDownloaded = DirList.getFileList(localDirSDE, null);
			for (String s : alreadyDownloaded) {
			     String tempFileStr = s.substring(localDirSDE.length());
                //System.out.println(tempFileStr);
				alreadyDownloadedTable.put(tempFileStr, TRUE);

                //System.out.println(s);
               // alreadyDownloadedTable.put(s.substring(localDirSDE.length()), TRUE);
			}
			alreadyDownloaded = null;
			alreadyDownloaded = DirList.getFileList(localDirLaptop, null);
			for (String s : alreadyDownloaded) {
                 String tempFileStr = s.substring(localDirLaptop.length());
                //System.out.println(tempFileStr);
				alreadyDownloadedTable.put(tempFileStr, TRUE);

				//System.out.println(s);
                //alreadyDownloadedTable.put(s.substring(localDirLaptop.length()), TRUE);
			}
			alreadyDownloaded = null;
		}

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
						final String dir = linkStr.substring(0, linkStr.indexOf('/'));
						System.out.println(dir);

						//
						// Runnable worker = new Thread(i++ + " " + linkStr) {
						// public void run() {
						//

						// int size;
						try {
							// size = getDirSize(dir);
							//
							// if (isStorableInSDD(size))
							// downloadDir(localDirSDD, dir);
							// else

							//downloadDir(localDirSDE, dir, alreadyDownloadedTable);
                            downloadDir(localDirTEST, dir, alreadyDownloadedTable);


						} catch (Exception e) {
							e.printStackTrace();
						}

						//
						// };
						// };
						// executor.execute(worker);
						//

					} else if (i > 1) {// skip initial no line
						finished = true;
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		//
		// executor.shutdown();
		// while (!executor.isTerminated()) {
		// try {
		// Thread.sleep(500);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		//

		System.out.println("Finished all threads");
	}

	// int getDirSize(String dir) throws Exception {
	// String line;
	// int size = 0;
	//
	// URL url = new URL(AWS_URL + dir + "/index.html");
	// URLConnection conn;
	// InputStream is = url.openStream();
	// BufferedReader br2 = new BufferedReader(new InputStreamReader(is));
	//
	// Pattern p = Pattern.compile("a href=\"([^\"]+)\"");
	// while ((line = br2.readLine()) != null) {
	// Matcher m = p.matcher(line);
	// if (m.find()) {
	// String linkStr = m.group(1);
	// if (linkStr.contains("gpg")) {
	// String gpgFileURL = AWS_URL + dir + "/" + linkStr;
	//
	// url = new URL(gpgFileURL);
	// conn = url.openConnection();
	// int tempSize = conn.getContentLength();
	//
	// if (tempSize < 0)
	// System.out.println(dir + "/" + linkStr +
	// " Could not determine file size.");
	// else {
	// // System.out.println(dir + "/" + linkStr + " Size: "
	// // + size);
	// size += tempSize;
	// }
	// try {
	// conn.getInputStream().close();
	// } catch (Exception e) {
	// System.err.println(dir + " " + gpgFileURL);
	// e.printStackTrace();
	// }
	// }
	// }
	// }
	// return size;
	// }

	void downloadDir(String localDir, String dir, Hashtable<String, Boolean> alreadyDownloadedTable)
			throws Exception {
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

					String fileToFind = dir + "/" + linkStr;
					if (!alreadyDownloadedTable.containsKey(fileToFind)) {

						// System.out.println(linkStr);
						String gpgFileURL = AWS_URL + dir + "/" + linkStr;

						// printFileSize(gpgFileURL);

						String commandWget = "wget -nc -q -P " + localDir + dir + "/ " + gpgFileURL;

						// String commandDecrypytHDFS = "wget -O - "
						// + gpgStr
						// +
						// " |   gpg --no-permission-warning --trust-model always  "
						// + " | hdfs dfs "
						// + localDir + dir + "/"
						// + linkStr.substring(0, linkStr.length() - 4);

						String command = commandWget;
						System.out.println(command);
						FileProcessor.runBinaryShellCommand(command);
					} else {
						System.out.println("Already Downloaded " + fileToFind);
					}
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		S3CorpusDownloader sp = new S3CorpusDownloader();
		sp.Execute();
	}
}
