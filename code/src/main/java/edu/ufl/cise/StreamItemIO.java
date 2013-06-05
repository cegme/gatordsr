package edu.ufl.cise;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.lang.time.StopWatch;

import streamcorpus.StreamItem;

public class StreamItemIO {

	private static String	baseDir				= "/media/sde/entitySIs/";
	// "/home/morteza/trec/";
	static String					tempFilePath	= baseDir + "totalSIs.o";

	private static void testObjectIO() throws Exception {

		String date = "2011-12-13-05";
		String fileName = "social-245-df86bb840942922df57102b2060596ac-1225d8c09e1aec09b874f170254c2f39.sc.xz.gpg";
		// "news-293-099809a9e1767f888c2de0f05854031f-d086232c92638c47455208582c2c86ca.sc.xz.gpg";

		List<StreamItem> list = RemoteGPGRetrieval.getLocalStreams(baseDir, date, fileName);
		for (StreamItem si : list) {
			System.out.println(si.body.clean_visible);
			System.out.println("----------------------------------");
		}

		List<StreamItem> listOutput = new LinkedList<StreamItem>();
		listOutput.add(list.get(0));
		listOutput.add(list.get(1));

		String tempFilePath = baseDir + "tempSIs";
		FileOutputStream fout = new FileOutputStream(tempFilePath);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(listOutput);
		oos.close();
		System.out.println("--");

		FileInputStream fin = new FileInputStream(tempFilePath);
		ObjectInputStream ois = new ObjectInputStream(fin);
		LinkedList<StreamItem> o = (LinkedList<StreamItem>) ois.readObject();
		System.out.println(o.get(0).doc_id);
		System.out.println(o.get(1).doc_id);
		ois.close();

	}

	private static List<StreamItem> LoadEntityStreamItems(String filePath) throws Exception {
		List<StreamItem> listSI = new LinkedList<StreamItem>();
		Scanner sc = new Scanner(new File(filePath));

		int count = 10;
		int i = 0;
		while (sc.hasNext() && i < count) {
			String s = sc.nextLine();
			String[] sArr = s.split("\\|");
			String date = sArr[0].substring(1).trim();
			if (date.compareTo("2012") < 0) {
				String fileName = sArr[1].trim();
				int index = Integer.parseInt(sArr[2].trim());
				System.out.println(fileName);
				listSI.add(RemoteGPGRetrieval.getStreams(date, fileName).get(index));
				i++;
			}
		}
		return listSI;
	}

	private static List<StreamItem> LoadEntityStreamItemsPartitioner(String filePath)
			throws Exception {
		List<StreamItem> listSI = new LinkedList<StreamItem>();
		Scanner sc = new Scanner(new File(filePath));

		int count = 5000;
		int i = 0;
		while (sc.hasNext()) {
			String s = sc.nextLine();
			String[] sArr = s.split("\\|");
			String date = sArr[0].substring(1).trim();

			// if (date.compareTo("2012") < 0) {
			String fileName = sArr[1].trim();
			int index = Integer.parseInt(sArr[2].trim());
			System.out.println(fileName);
			listSI.add(RemoteGPGRetrieval.getStreams(date, fileName).get(index));
			i++;
			// }
			if (i != 0 && i % count == 0) {
				FileOutputStream fout = new FileOutputStream(tempFilePath + "." + i / count + "."
						+ filePath.substring(filePath.lastIndexOf('/') + 1));
				XZCompressorOutputStream xzos = new XZCompressorOutputStream(fout);
				ObjectOutputStream oos = new ObjectOutputStream(xzos);
				oos.writeObject(listSI);
				oos.close();
				xzos.close();
				fout.close();
				listSI.clear();
			}
		}
		return listSI;
	}

	private static LinkedList<StreamItem> loadBulkSIs(String filePath) throws Exception {
		FileInputStream fin = new FileInputStream(filePath);
		XZCompressorInputStream xzis = new XZCompressorInputStream(fin);
		ObjectInputStream ois = new ObjectInputStream(xzis);
		LinkedList<StreamItem> listSI = (LinkedList<StreamItem>) ois.readObject();

		for (StreamItem si : listSI) {
			String s = si.getDoc_id();
			System.out.println(s);
		}
		fin.close();
		return listSI;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		// testObjectIO();
		String localPath = "/home/morteza/zproject/gatordsr/code/resources/entity/totalEntityList.txt.sorted.2011";

		LoadEntityStreamItemsPartitioner("/home/morteza/zproject/gatordsr/code/resources/entity/totalEntityList.txt.sorted.2011");
		LoadEntityStreamItemsPartitioner("/home/morteza/zproject/gatordsr/code/resources/entity/totalEntityList.txt.sorted.2012");
		LoadEntityStreamItemsPartitioner("/home/morteza/zproject/gatordsr/code/resources/entity/totalEntityList.txt.sorted.2013");

		// StopWatch timer = new StopWatch();
		//
		// timer.start();
		// List<StreamItem> listSI = LoadEntityStreamItems(localPath);
		// timer.stop();
		//
		// System.out.println(timer.toString());
		//
		// FileOutputStream fout = new FileOutputStream(tempFilePath);
		// XZCompressorOutputStream xzos = new XZCompressorOutputStream(fout);
		// ObjectOutputStream oos = new ObjectOutputStream(xzos);
		//
		// oos.writeObject(listSI);
		// oos.close();
		//
		// timer.reset();
		//
		// timer.start();
		// loadBulkSIs(tempFilePath);
		// timer.stop();
		//
		// System.out.println(timer.toString());

	}

}
