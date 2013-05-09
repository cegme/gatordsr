
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

public class FileProcessor {

	int threadCount = 32;
	static String localDirSDD = "/media/sdd/corpus/";
	static String localDirSDE = "/media/sde/corpus/";
	String AWS_URL = "http://s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";

	private boolean isStorableInSDD(int size) {
		String s = "/home/morteza";// "/media/sdd";
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
									// System.out.println(dir + " Size:" + size
									// /
									// 1024
									// + " KB");
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
					conn.getInputStream().close();
				}
			}
		}
		return size;
	}

	void downloadDir(String localDir, String dir) throws Exception {
		String line;

		URL url = new URL(AWS_URL + dir + "/index.html");
		InputStream is = url.openStream(); // throws an IOException
		BufferedReader br2 = new BufferedReader(new InputStreamReader(is));

		Pattern p = Pattern.compile("a href=\"([^\"]+)\"");
		while ((line = br2.readLine()) != null) {
			Matcher m = p.matcher(line);
			if (m.find()) {
				String linkStr = m.group(1);
				if (linkStr.contains("gpg")) {

					// System.out.println(linkStr);
					String gpgFileURL = AWS_URL + dir + "/" + linkStr;

					url = new URL(gpgFileURL);
					URLConnection conn = url.openConnection();
					int size = conn.getContentLength();
					if (size < 0)
						System.out.println(dir + "/" + linkStr
								+ " Could not determine file size.");
					// else
					// System.out.println(dir + "/" + linkStr + " Size: "
					// + size);
					conn.getInputStream().close();

					String commandWget = "wget -q -p " + localDir + dir + "/ "
							+ gpgFileURL;

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

	private void runShellCommand(String command) throws Exception {
		String line;
		String[] cmd = { "/bin/sh", "-c", command };
		System.out.println(command);
		Process process = Runtime.getRuntime().exec(cmd);
		// System.out.println(process.exitValue());
		BufferedReader stdOut = new BufferedReader(new InputStreamReader(
				process.getInputStream()));
		BufferedReader stdErr = new BufferedReader(new InputStreamReader(
				process.getErrorStream()));
		while ((line = stdOut.readLine()) != null) {
			System.out.println(line);
		}
		System.out.println("");
		while ((line = stdErr.readLine()) != null) {
			System.out.println(line);
		}
		process.destroy();
	}

	public static void main(String[] args) {
		FileProcessor fp = new FileProcessor();
		fp.Execute();
	}
}
