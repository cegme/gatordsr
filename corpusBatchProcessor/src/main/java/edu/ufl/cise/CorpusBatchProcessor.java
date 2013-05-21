package edu.ufl.cise;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import streamcorpus.StreamItem;

/**
 * check http://sourceforge.net/projects/faststringutil/ structured graph
 * learning sgml icml, online lda, stremaing
 * 
 * Time: get file size decrypt
 * 
 * 
 * @author morteza
 * 
 */
public class CorpusBatchProcessor {

	String													DIR_SERVER			= "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	String													DIR_LOCAL				= "/home/morteza/2013Corpus/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	final String										FILTER					= "";
	final String										query						= "president";
	long														fileCount				= 0;
	AtomicLong											siCount					= new AtomicLong(0);
	AtomicLong											siFilteredCount	= new AtomicLong(0);
	AtomicLong											processedSize		= new AtomicLong(0);
	public static final DateFormat	format					= new SimpleDateFormat("yyyy-MM-dd-HH");
	public static final DateFormat	logTimeFormat		= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static InputStream grabGPGLocal(String date, String fileName, String fileStr) {
		System.out.println(date + "/" + fileName);
		String command = "gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt "
				+ fileStr + " | xz --decompress";
		return FileProcessor.runBinaryShellCommand(command);
	}

	private static List<SIWrapper> getStreams(String day, int hour, String fileName, InputStream is)
			throws Exception {
		TIOStreamTransport transport = new TIOStreamTransport(is);
		transport.open();
		TBinaryProtocol protocol = new TBinaryProtocol(transport);

		List<SIWrapper> list = new LinkedList<SIWrapper>();

		int index = 0;
		boolean exception = false;
		while (!exception) {
			try {
				StreamItem si = new StreamItem();
				if (protocol.getTransport().isOpen())
					si.read(protocol);
				list.add(new SIWrapper(day, hour, fileName, index, si));
				index = index + 1;
			} catch (TTransportException e) {
				processException(e);
				exception = true;
			}
		}
		transport.close();
		return list;
	}

	private static void processException(TTransportException e) {
		switch (e.getType()) {
		case TTransportException.ALREADY_OPEN:
			System.err.println("Error reading StreamItem: ALREADY_OPEN");
			break;
		case TTransportException.END_OF_FILE:
			// System.err.println("Error reading StreamItem: END_OF_FILE");
			break;
		case TTransportException.NOT_OPEN:
			System.err.println("Error reading StreamItem: NOT_OPEN");
			break;
		case TTransportException.TIMED_OUT:
			System.err.println("Error reading StreamItem: TIMED_OUT");
			break;
		case TTransportException.UNKNOWN:
			System.err.println("Error reading StreamItem: UNKNOWN");
			break;
		}
	}

	private void process(SIWrapper siw) {
		boolean res = false;
		if (siw.getStreamItem().getBody() != null) {
			String document = siw.getStreamItem().getBody().getClean_visible();
			if (document != null) {
				String strEnglish = document.toLowerCase().replaceAll("[^A-Za-z0-9\\p{Punct}]", " ")
						.replaceAll("\\s+", " ").replaceAll("(\r\n)+", "\r\n").replaceAll("(\n)+", "\n")
						.replaceAll("(\r)+", "\r").toLowerCase();
				res = strEnglish.contains(query);
			} else
				res = false;
		}
		if (res == true) {
			// System.out.println(siw);
			siFilteredCount.incrementAndGet();
		}
	}

	/**
	 * 
	 * @throws ParseException
	 */
	private void process() throws ParseException {

		int threadCount;

		Calendar c = Calendar.getInstance();
		c.setTime(format.parse("2011-10-05-00"));
		Calendar cEnd = Calendar.getInstance();

		File f = new File(DIR_LOCAL);
		boolean localRun = f.exists();
		final String DIRECTORY = (localRun) ? DIR_LOCAL : DIR_SERVER;
		if (localRun) {
			System.out.println("Local run.");
			cEnd.setTime(format.parse("2011-10-07-14"));
			threadCount = 2;
		} else {
			System.out.println("Server run.");
			cEnd.setTime(format.parse("2013-02-13-23"));
			threadCount = 31;
		}

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		while (!(c.getTime().compareTo(cEnd.getTime()) > 0)) {
			try {// WHATEVER HAPPENS DON'T TERMINATE!
				final String date = format.format(c.getTime());
				List<String> fileList = DirList.getFileList(DIRECTORY + date, FILTER);
				// System.gc();// one gc per directory
				for (final String fileStr : fileList) {
					fileCount++;
					final int hour = c.get(Calendar.HOUR);
					final String fileName = fileStr.substring(fileStr.lastIndexOf('/') + 1);

					// TODO filnamewrapper class to do file name splitting
					// efficiently stribnuilder
					Runnable worker = new Thread(fileCount + " " + date + "/" + fileName) {
						public void run() {

							try {
								InputStream is = grabGPGLocal(date, fileName, fileStr);
								List<SIWrapper> list = getStreams(date, hour, fileName, is);
								is.close();
								siCount.addAndGet(list.size());
								for (SIWrapper siw : list) {
									process(siw);
								}

								long size = FileProcessor.getLocalFileSize(fileStr);
								System.out.print(FileProcessor.fileSizeToStr(size));
								processedSize.addAndGet(size);
								report(logTimeFormat);

							} catch (Exception e) {
								e.printStackTrace();
							}
						};
					};
					executor.execute(worker);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			c.add(Calendar.HOUR, 1);
		}

		executor.shutdown();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		report(logTimeFormat);
		System.out.println("Finished all threads");
	}

	private void report(DateFormat df) {
		System.out.println(df.format(new Date()) + " Total " + fileCount + " Files "
				+ FileProcessor.fileSizeToStr(processedSize.get()) + " SIs: " + siCount.get() + " +SIs:"
				+ siFilteredCount);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws ParseException {
		CorpusBatchProcessor cps = new CorpusBatchProcessor();
		cps.process();
	}

}
