package edu.ufl.cise.benchmark;

import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import streamcorpus.Sentence;
import streamcorpus.StreamItem;
import edu.ufl.cise.DirList;
import edu.ufl.cise.FileProcessor;
import edu.ufl.cise.RemoteGPGRetrieval;
import edu.ufl.cise.util.StreamItemWrapper;

/**
 * The purpose of this class is to measure the processing performance of the system as per MB/s of
 * processing time using bare processing of StreamItems
 * 
 * @author morteza
 * 
 */
public class Benchmark {

	public static final DateFormat	format					= new SimpleDateFormat("yyyy-MM-dd-HH");
	public static final DateFormat	logTimeFormat		= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	long														fileCount				= 0;
	long														siCount					= 0;
	long														siFilteredCount	= 0;
	long														processedSize		= 0;

	public static void main(String[] args) {
		Benchmark bm = new Benchmark();
		bm.processSingleThreaded();
	}

	private void processSingleThreaded() {

		System.out.println("Benchmark Single Threads");

		final Calendar cStart = Calendar.getInstance();
		final Calendar cEnd = Calendar.getInstance();

		System.out.println("Server run.");
		try {
			cStart.setTime(format.parse("2011-10-05-00"));
			cEnd.setTime(format.parse("2013-02-13-23"));
		} catch (ParseException e2) {
			e2.printStackTrace();
		}

		final AtomicInteger finishedThreadTracker = new AtomicInteger(0);

		final Calendar cTemp = Calendar.getInstance();
		cTemp.setTime(cStart.getTime());
		cTemp.add(Calendar.HOUR, 1);

		PrintWriter pw = null;

		while (!(cTemp.getTime().compareTo(cEnd.getTime()) > 0)) {
			try {
				final String date = format.format(cTemp.getTime());
				List<String> tempFileList = null;

				tempFileList = DirList.getFileList(
						"/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/"
								+ date, null);
				if (tempFileList == null)
					throw new RuntimeException("Corpus not found");

				for (final String fileStr : tempFileList) {
					final int hour = cTemp.get(Calendar.HOUR_OF_DAY);
					final String fileName = fileStr.substring(fileStr.lastIndexOf('/') + 1);
					String dateFile = date + "/" + fileName;

					System.out.println("# " + fileStr);
					try {
						String command = "gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt "
								+ fileStr;
						InputStream is = FileProcessor.runBinaryShellCommand(command);

						getStreams(pw, date, hour, fileName, is);
						is.close();

						fileCount++;
						long size = FileProcessor.getLocalFileSize(fileStr);
						processedSize = processedSize + size;
						// report(logTimeFormat, "Thread(" + threadIndex + ")" +
						// date
						// + "/" + fileName);
						System.out.println(logTimeFormat.format(new Date()) + " Total " + fileCount + " Files "
								+ FileProcessor.fileSizeToStr(processedSize, "MB") + " SIs: " + siCount + " +SIs: " + siFilteredCount
								+ " " + date + "/" + fileName);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// }
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			cTemp.add(Calendar.HOUR, 1);
			// System.out.println(threadIndex + ": " +
			// format.format(cTemp.getTime()) + " "
			// + format.format(cEnd.getTime()));
		}

		finishedThreadTracker.incrementAndGet();
		if (pw != null)
			pw.close();

	}

	private void getStreams(PrintWriter pw, String day, int hour, String fileName, InputStream is) throws Exception {
		XZCompressorInputStream bais = new XZCompressorInputStream(is);
		TIOStreamTransport transport = new TIOStreamTransport(bais);
		transport.open();
		TBinaryProtocol protocol = new TBinaryProtocol(transport);

		int index = 0;
		boolean exception = false;
		while (!exception) {
			try {
				StreamItem si = new StreamItem();
				if (protocol.getTransport().isOpen())
					si.read(protocol);
				siCount++;

				if (si.getBody() != null) {
					List<Sentence> listSentence = si.getBody().getSentences().get("lingpipe");
					if (listSentence == null) {
						si.getBody().getClean_visible();
						si.getBody().getRaw();
					}
				}

				index = index + 1;
			} catch (TTransportException e) {
				RemoteGPGRetrieval.tTransportExceptionPrintString(e);
				exception = true;
			}
		}
		transport.close();
	}

}
