package fileproc;

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
import edu.ufl.cise.util.StreamItemWrapper;

/**
 * check http://sourceforge.net/projects/faststringutil/ structured graph
 * learning sgml icml, online lda, stremaing
 * 
 * @author morteza
 * 
 */
public class CorpusBatchProcessor {

	final String	DIRECTORY				= "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	//final String	DIRECTORY				= "/home/morteza/2013Corpus/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	final String	FILTER					= "";
	final String	query						= "book";
	AtomicLong		fileCount				= new AtomicLong(0);
	AtomicLong		siCount					= new AtomicLong(0);
	AtomicLong		siFilteredCount	= new AtomicLong(0);
	AtomicLong		processedSize		= new AtomicLong(0);

	private static InputStream grabGPGLocal(String date, String fileName, String fileStr) {
		// System.out.println(date + "/" + fileName);
		String command = "gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt "
				+ fileStr + " | xz --decompress";
		return FileProcessor.runBinaryShellCommand(command);
	}

	private static List<StreamItemWrapper> getStreams(String day, int hour, String fileName,
			InputStream is) throws Exception {
		TIOStreamTransport transport = new TIOStreamTransport(is);
		transport.open();
		TBinaryProtocol protocol = new TBinaryProtocol(transport);

		List<StreamItemWrapper> list = new LinkedList<StreamItemWrapper>();

		int index = 0;
		boolean exception = false;
		while (!exception) {
			StreamItem si = new StreamItem();
			try {
				if (protocol.getTransport().isOpen())
					si.read(protocol);
				list.add(new StreamItemWrapper(day, hour, fileName, index, si));
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

	private void process(StreamItemWrapper siw) {
		boolean res = false;
		if (siw.streamItem().getBody() != null) {
			String document = siw.streamItem().getBody().getClean_visible();
			if (document != null) {
				String strEnglish = document.toLowerCase().replaceAll("[^A-Za-z0-9\\p{Punct}]", " ")
						.replaceAll("\\s+", " ").replaceAll("(\r\n)+", "\r\n").replaceAll("(\n)+", "\n")
						.replaceAll("(\r)+", "\r").toLowerCase();
				res = strEnglish.contains(query);
			} else
				res = false;
		}
		if (res == true) {
			System.out.println(siw);
			siFilteredCount.incrementAndGet();
		}
	}

	/**
	 * 
	 * @throws ParseException
	 */
	private void process() throws ParseException {
		int threadCount = 2;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		final DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH");
		final DateFormat logTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		c.setTime(format.parse("2011-10-05-00"));
		Calendar cEnd = Calendar.getInstance();
		cEnd.setTime(format.parse("2011-10-05-02"));

		// cEnd.setTime(format.parse("2013-02-13-23"));

		while (!(c.getTime().compareTo(cEnd.getTime()) > 0)) {
			final String date = format.format(c.getTime());
			try {
				List<String> fileList = DirList.getFileList(DIRECTORY + date, FILTER);
				for (final String fileStr : fileList) {
					fileCount.incrementAndGet();
					final int hour = c.get(Calendar.HOUR);
					final String fileName = fileStr.substring(fileStr.lastIndexOf('/') + 1);

					// TODO filnamewrapper class to do file name splitting
					// efficiently stribnuilder
					Runnable worker = new Thread(fileCount.incrementAndGet() + " " + date + "/" + fileName) {
						public void run() {

							try {
								InputStream is = grabGPGLocal(date, fileName, fileStr);
								List<StreamItemWrapper> list = getStreams(date, hour, fileName, is);
								siCount.addAndGet(list.size());
								for (StreamItemWrapper siw : list) {
									process(siw);
								}

								processedSize.addAndGet(FileProcessor.getLocalFileSize(fileStr));
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
		System.out.println(df.format(new Date()) + " Total " + fileCount.get() + " Files "
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
