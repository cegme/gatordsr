package edu.ufl.cise;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

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

	private static void LoadEntityStreamItemsPartitioner(final String filePath) throws Exception {
		Scanner sc = new Scanner(new File(filePath));
		List<String> lines = new LinkedList<String>();
		while (sc.hasNext()) {
			lines.add(sc.nextLine());
		}

		final int threadCount = 15;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		final AtomicInteger finishedThreadTracker = new AtomicInteger(0);
		final AtomicInteger fileCount = new AtomicInteger(0);
		final AtomicLong processedSize = new AtomicLong(0);
		final int count = 200;// SI per file.

		for (int k = 0; k < lines.size(); k = k + count) {
			final List<String> tempList = lines.subList(k, Math.min(k + count, lines.size() - 1));

			final int index = k;

			Thread worker = new Thread() {// one thread per hour then add index
				public void run() {
					List<StreamItem> listSI = new LinkedList<StreamItem>();
					Iterator<String> it = tempList.iterator();
					while (it.hasNext()) {
						String s = it.next();
						String[] sArr = s.split("\\|");
						String date = sArr[0].substring(1).trim();

						String fileName = sArr[1].trim();
						int index = Integer.parseInt(sArr[2].trim());
					//	System.out.println(fileName);
						String fileStr = RemoteGPGRetrieval.SDD_BASE_PATH + date + "/" + fileName;
						try {
							listSI.add(RemoteGPGRetrieval.getLocalStreams(RemoteGPGRetrieval.SDD_BASE_PATH, date,
									fileName).get(index));
							// fileCount.incrementAndGet();
							long size = FileProcessor.getLocalFileSize(fileStr);
							processedSize.addAndGet(size);
							fileCount.incrementAndGet();
							System.out.println();
							System.out.println(CorpusBatchProcessor.logTimeFormat.format(new Date()) + " Total "
									+ fileCount + " Files " + FileProcessor.fileSizeToStr(processedSize.get(), "MB")
									///+ "Thread("+index + ")"
									+ date + "/" + fileName);
						} catch (Exception e) {
							e.printStackTrace();
						}
						// listSI.clear();
					}
					FileOutputStream fout;
					try {
						fout = new FileOutputStream(tempFilePath + "." + index / count + "."
								+ filePath.substring(filePath.lastIndexOf('/') + 1));
						XZCompressorOutputStream xzos = new XZCompressorOutputStream(fout);
						ObjectOutputStream oos = new ObjectOutputStream(xzos);
						oos.writeObject(listSI);
						oos.close();
						xzos.close();
						fout.close();

					} catch (Exception e) {
						e.printStackTrace();
					}
					finishedThreadTracker.incrementAndGet();
				}
			};
			// worker.start();
			executor.execute(worker);
		}
		// while (finishedThreadTracker.get() < threadCount) {
		// try {
		// Thread.sleep(500);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }

		// //
		executor.shutdown();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

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

		LoadEntityStreamItemsPartitioner("/media/sde/backupFinal/totalEntitiesSIs.txt.sorted.2011");
		LoadEntityStreamItemsPartitioner("/media/sde/backupFinal/totalEntitiesSIs.txt.sorted.2012");
		LoadEntityStreamItemsPartitioner("/media/sde/backupFinal/totalEntitiesSIs.txt.sorted.2013");

		// SIs.txt.sorted.2012 StopWatch timer = new StopWatch();
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
