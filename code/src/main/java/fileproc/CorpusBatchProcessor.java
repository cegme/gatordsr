package fileproc;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;

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

	final String DIRECTORY = "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	final String FILTER = "";
	final String query = "president";
	AtomicLong siCount = new AtomicLong(0);
	AtomicLong siFilteredCount = new AtomicLong(0);
	AtomicLong processedSize = new AtomicLong(0);

	private static InputStream grabGPGLocal(String day, int hour,
			String fileName) {

		System.out.println(day + "/" + hour + "/" + fileName);
		String command = "gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt "
				+ fileName + "| xz --decompress";
		return FileProcessor.runBinaryShellCommand(command);
	}

	private static List<StreamItemWrapper> getStreams(String day, int hour,
			String fileName, InputStream is) throws Exception {
		TIOStreamTransport transport = new TIOStreamTransport(is);
		transport.open();
		TBinaryProtocol protocol = new TBinaryProtocol(transport);

		List<StreamItemWrapper> list = new LinkedList<StreamItemWrapper>();

		int index = 0;
		boolean exception = false;
		while (!exception) {
			StreamItem si = new StreamItem();
			try {
				si.read(protocol);
			} catch (Exception e) {
				System.err
						.println("Error reading StreamItem " + e.getMessage());
				exception = true;
			}

			list.add(new StreamItemWrapper(day, hour, fileName, index, si));
			index = index + 1;
		}
		transport.close();
		return list;
	}

	private void process(StreamItemWrapper siw) {
		boolean res = false;
		if (siw.streamItem().getBody() != null) {
			String document = siw.streamItem().getBody().getClean_visible();
			if (document != null) {
				String strEnglish = document.toLowerCase()
						.replaceAll("[^A-Za-z0-9\\p{Punct}]", " ")
						.replaceAll("\\s+", " ").replaceAll("(\r\n)+", "\r\n")
						.replaceAll("(\n)+", "\n").replaceAll("(\r)+", "\r")
						.toLowerCase();
				res = strEnglish.contains(query);
			} else
				res = false;
		}
		if (res == true) {
			System.out.println(siw);
			siFilteredCount.incrementAndGet();
		}
	}

	private void process() {
		int threadCount = 32;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		List<String> fileList = DirList.getFileList(DIRECTORY, FILTER);
		System.out.println("total file count on disk sdd is: "
				+ fileList.size());

		int fileNumber = 0;
		boolean finished = false;
		String dayHourFileNamePatternStr = ".*language/([^/]+)-(.+)/(.+)";
		final Pattern dayHourFileNamePattern = Pattern
				.compile(dayHourFileNamePatternStr);

		while (!finished) {

			try {
				final String fileStr = fileList.get(fileNumber);
				Matcher m = dayHourFileNamePattern.matcher(fileStr);
				final String day = m.group(1);
				final int hour = Integer.parseInt(m.group(2));
				final String fileName = m.group(3);
				System.out.println(fileStr);

				// TODO filnamewrapper class to do file name splitting
				// efficiently stribnuilder
				Runnable worker = new Thread(fileNumber + " " + day + "/"
						+ hour + "/" + fileName) {
					public void run() {

						try {
							int size = FileProcessor
									.getFileSize(fileStr);
							processedSize.addAndGet(size);
							System.out.print(FileProcessor
									.fileSizeToStr(size));

							InputStream is = grabGPGLocal(day, hour, fileName);
							List<StreamItemWrapper> list = getStreams(day,
									hour, fileName, is);
							for (StreamItemWrapper siw : list) {
								process(siw);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					};
				};
				executor.execute(worker);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			fileNumber++;
		}

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

		System.out.println("total file count on disk" + DIRECTORY
				+ " before filter is: " + siCount.get());
		System.out.println("total file count on disk " + DIRECTORY
				+ "after filter is: " + siFilteredCount.get());

		System.out.println("Finished all threads");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CorpusBatchProcessor cps = new CorpusBatchProcessor();

		cps.process();
	}

}
