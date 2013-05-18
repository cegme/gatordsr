package fileproc;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.tools.ant.types.CommandlineJava.SysProperties;

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

//	// final String DIRECTORY =
//	// "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
//	final String DIRECTORY = "/home/morteza/2013Corpus/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
//	final String FILTER = "";
//	final String query = "president";
//	AtomicLong siCount = new AtomicLong(0);
//	AtomicLong siFilteredCount = new AtomicLong(0);
//	AtomicLong processedSize = new AtomicLong(0);
//
//	private static InputStream grabGPGLocal(String day, int hour,
//			String fileName) {
//
//		System.out.println(day + "/" + hour + "/" + fileName);
//		String command = "gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt "
//				+ fileName + "| xz --decompress";
//		return FileProcessor.runBinaryShellCommand(command);
//	}
//
//	private static List<StreamItemWrapper> getStreams(String day, int hour,
//			String fileName, InputStream is) throws Exception {
//		TIOStreamTransport transport = new TIOStreamTransport(is);
//		transport.open();
//		TBinaryProtocol protocol = new TBinaryProtocol(transport);
//
//		List<StreamItemWrapper> list = new LinkedList<StreamItemWrapper>();
//
//		int index = 0;
//		boolean exception = false;
//		while (!exception) {
//			StreamItem si = new StreamItem();
//			try {
//				si.read(protocol);
//			} catch (Exception e) {
//				System.err
//						.println("Error reading StreamItem " + e.getMessage());
//				exception = true;
//			}
//
//			list.add(new StreamItemWrapper(day, hour, fileName, index, si));
//			index = index + 1;
//		}
//		transport.close();
//		return list;
//	}
//
//	private void process(StreamItemWrapper siw) {
//		boolean res = false;
//		if (siw.streamItem().getBody() != null) {
//			String document = siw.streamItem().getBody().getClean_visible();
//			if (document != null) {
//				String strEnglish = document.toLowerCase()
//						.replaceAll("[^A-Za-z0-9\\p{Punct}]", " ")
//						.replaceAll("\\s+", " ").replaceAll("(\r\n)+", "\r\n")
//						.replaceAll("(\n)+", "\n").replaceAll("(\r)+", "\r")
//						.toLowerCase();
//				res = strEnglish.contains(query);
//			} else
//				res = false;
//		}
//		if (res == true) {
//			System.out.println(siw);
//			siFilteredCount.incrementAndGet();
//		}
//	}
//
//	private void process() {
//		int threadCount = 2;
//		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//
//		List<String> fileList = DirList.getFileList(DIRECTORY, FILTER);
//		System.out.println("total compressed file count on disk sdd is: "
//				+ fileList.size());
//
//		int fileNumber = 0;
//		boolean finished = false;
//		String dayHourFileNamePatternStr = ".*language/(....-..-..)-(..)/(.+)";
//		final Pattern dayHourFileNamePattern = Pattern
//				.compile(dayHourFileNamePatternStr);
//
//		Calendar c = new GregorianCalendar(2011, 10, 5, 0, 0);
//		DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh");
//		final DateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh");
//
//		while (!finished) {
//
//			final String day = format.format(c.getTime());
//			try {
//				final String fileStr = fileList.get(fileNumber); // generate
//																	// next day
//				Matcher m = dayHourFileNamePattern.matcher(fileStr);
//				// final String day = m.group(1);
//				final int hour = Integer.parseInt(m.group(2));
//				final String fileName = m.group(3);
//				System.out.println(fileStr);
//
//				// TODO filnamewrapper class to do file name splitting
//				// efficiently stribnuilder
//				Runnable worker = new Thread(fileNumber + " " + day + "/"
//						+ hour + "/" + fileName) {
//					public void run() {
//
//						try {
//							InputStream is = grabGPGLocal(day, hour, fileName);
//							List<StreamItemWrapper> list = getStreams(day,
//									hour, fileName, is);
//							for (StreamItemWrapper siw : list) {
//								process(siw);
//							}
//							int size = FileProcessor.getFileSize(fileStr);
//							processedSize.addAndGet(size);
//							System.out.println(ft.format(new Date())
//									+ " Processed so far: "
//									+ FileProcessor.fileSizeToStr(processedSize
//											.get()) + " SIs: " + siCount.get()
//									+ " " + fileName + " had " + list.size()
//									+ FileProcessor.fileSizeToStr(size));
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					};
//				};
//				executor.execute(worker);
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}
//			fileNumber++;
//		}
//
//		executor.shutdown();
//		while (!executor.isTerminated()) {
//			try {
//				query.wait(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//
//		System.out.println("total file count on disk" + DIRECTORY
//				+ " before filter is: " + siCount.get());
//		System.out.println("total file count on disk " + DIRECTORY
//				+ "after filter is: " + siFilteredCount.get());
//
//		System.out.println("Finished all threads");
//
//	}
//
//	/**
//	 * @param args
//	 */
	public static void main(String[] args) throws ParseException {
		// CorpusBatchProcessor cps = new CorpusBatchProcessor();
		// cps.process();
		String ps = ".*language/(....-..-..)-(..)/(.+)";//
		String s = "/wn-language/2011-13-05-02/arxiv-5-b6c1a8422729eb0346d3d05b07ef0f30-4d1fcd2ae213a5530cc313dc6dfafc31.sc.xz.gpg";
		Pattern p = Pattern.compile(ps);
		Matcher m1 = p.matcher(s);
		if (m1.find()) {
			System.out.println(m1.group(1));
		}

		//Calendar c = new GregorianCalendar(2011, 10, 5, 0, 0);
		Calendar c = Calendar.getInstance();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
			c.setTime(format.parse("2011-10-05 00"));
		
		
		
		// Calendar cEnd = new GregorianCalendar(2013, 2, 13, 23, 0);
	//	Calendar cEnd = new GregorianCalendar(2013, 2, 13, 23, 0);
		Calendar cEnd = Calendar.getInstance();
		cEnd.setTime(format.parse("2013-02-13 23"));
		System.out.println(cEnd.getTimeInMillis());
		while (c.getTime().before(cEnd.getTime())) {
			System.out.print("hi " + format.format(c.getTime()) + "]");
			System.out.println(c.getTimeInMillis() + "][" + cEnd.getTimeInMillis());
			c.add(Calendar.HOUR, 1);
			//2013-02-13 23]1360814400000][
			//
		}

//		Date runnerDate = c.getTime();
//		System.out.println(format.format(c.getTime()));
//
//		// String to be scanned to find the pattern.
//		String line = "This order was places for QT3000! OK?";
//		String pattern = "(.*)(\\d+)(.*)";
//
//		// Create a Pattern object
//		Pattern r = Pattern.compile(pattern);
//
//		// Now create matcher object.
//		Matcher m = r.matcher(line);
//		if (m.find()) {
//			System.out.println("Found value: " + m.group(0));
//			System.out.println("Found value: " + m.group(1));
//			System.out.println("Found value: " + m.group(2));
//		} else {
//			System.out.println("NO MATCH");
//		}

	}

}
