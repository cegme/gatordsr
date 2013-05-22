package edu.ufl.cise;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

public class FileProcessor {

	public static String fileSizeToStr(long bytesLong) {
		int BINARY_KILO = 1024;
		long bytes = bytesLong;
		long kilobytes = Math.round(bytes / BINARY_KILO);
		long megabytes = Math.round(kilobytes / BINARY_KILO);
		long gigabytes = Math.round(megabytes / BINARY_KILO);
		long terabytes = Math.round(gigabytes / BINARY_KILO);
		long petabytes = Math.round(terabytes / BINARY_KILO);
		long exabytes = Math.round(petabytes / BINARY_KILO);
		long zettabytes = Math.round(exabytes / BINARY_KILO);
		long yottabytes = Math.round(zettabytes / BINARY_KILO);

		if (yottabytes > 1)
			return yottabytes + "YB";
		else if (zettabytes > 1)
			return zettabytes + "ZB";
		else if (exabytes > 1)
			return exabytes + "EB";
		else if (petabytes > 1)
			return petabytes + "PB";
		else if (terabytes > 1)
			return terabytes + "TB";
		else if (gigabytes > 1)
			return gigabytes + "GB";
		else if (megabytes > 1)
			return megabytes + "MB";
		else if (kilobytes > 1)
			return kilobytes + "KB";
		else if (bytes > 1)
			return bytes + "B";
		return null;
	}

	// private boolean isAlreadyDownloaded(String localDir, String file, String
	// dir){
	//
	// File f = new File(localDir + localDirPrefix);
	// if(f.exists()) { /* do something */ }
	// }

	/**
	 * run shell command with string output
	 * 
	 * @param command
	 * @return
	 */
	public static List<String> runStringShellCommand(String command) {
		LinkedList<String> list = new LinkedList<String>();
		String line;
		String[] cmd = { "/bin/sh", "-c", command };
		// System.out.println(command);
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd);

			// System.out.println(process.exitValue());
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			// BufferedReader stdErr = new BufferedReader(new InputStreamReader(
			// process.getErrorStream()));

			// return IOUtils.toByteArray(process.getInputStream());
			// return process.getInputStream();
			while ((line = stdOut.readLine()) != null) {
				list.add(line);
			}
			// System.out.println("");
			// while ((line = stdErr.readLine()) != null) {
			// System.out.println(line);
			// }
			process.destroy();
		} catch (IOException e) {
			System.err.println(command);
			e.printStackTrace();
		}
		return list;

	}

	/**
	 * run shell command with binary data output
	 * 
	 * @param command
	 * @return
	 */
	public static InputStream runBinaryShellCommand(String command) {
		String[] cmd = { "/bin/sh", "-c", command };
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

	public static long getLocalFileSize(String gpgFileAddress) throws Exception {
		File f = new File(gpgFileAddress);
		if (f.exists())
			return f.length();
		else
			throw new Exception("File size err.");
	}

	public static int getFileSize(String gpgFileURL) throws Exception {
		URL url = new URL(gpgFileURL);
		URLConnection conn = url.openConnection();
		int size = conn.getContentLength();
		if (size < 0)
			System.out.println(gpgFileURL + " Could not determine file size.");
		else
			System.out.println(gpgFileURL + " Size: " + size);
		conn.getInputStream().close();
		return size;
	}

	public static void main(String[] args) {
		System.out.println(Long.MAX_VALUE);
	}
}
