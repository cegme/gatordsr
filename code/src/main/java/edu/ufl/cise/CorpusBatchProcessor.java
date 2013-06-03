package edu.ufl.cise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import streamcorpus.Sentence;
import streamcorpus.StreamItem;
import streamcorpus.Token;
import edu.ufl.cise.pipeline.Entity;
import edu.ufl.cise.pipeline.Pipeline;
import edu.ufl.cise.pipeline.Preprocessor;

/**
 * check http://sourceforge.net/projects/faststringutil/ structured graph
 * learning sgml icml, online lda, stremaing
 * 
 * Some issues: <br>
 * 
 * memory usage is ok, not much io<br>
 * Time: <br>
 * decrypt nonblocking<br>
 * split streamitem body and add it via softrefernce. in O(1) do comparison via
 * cache. ORRRR use the tokens they already provide
 * 
 * single machine single thread profiling
 * 
 * separate file path from file name by interning
 * 
 * 
 * 
 * @author morteza
 * 
 */
public class CorpusBatchProcessor {

	public final static String				CORPUS_DIR_SERVER					= "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	public final static String				CORPUS_DIR_LOCAL					= "/home/morteza/2013Corpus/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	public final static String				LOG_DIR_SERVER						= "/media/sde/runs/";
	public final static String				LOG_DIR_SERVER_OLD				= "/media/sde/backupFinal/";
	public final static String				LOG_DIR_LOCAL							= "/home/morteza/trec/runs/";
	public final static String				LOG_DIR_LOCAL_OLD					= "/home/morteza/trec/backup/";
	public final static String				LOG_DIR_LOCAL_TO_PROCESS	= "/home/morteza/trec/toProcess/";
	public final static String				LOG_DIR_SERVER_TO_PROCESS	= "/media/sde/toProcess/";
	final String											FILTER										= "";
	final String											query											= "president";
	AtomicLong												fileCount									= new AtomicLong(0);
	AtomicLong												siCount										= new AtomicLong(0);
	AtomicLong												siFilteredCount						= new AtomicLong(0);
	AtomicLong												processedSize							= new AtomicLong(0);

	public static final DateFormat		format										= new SimpleDateFormat(
																																	"yyyy-MM-dd-HH");
	public static final DateFormat		logTimeFormat							= new SimpleDateFormat(
																																	"yyyy-MM-dd HH:mm:ss");
	final int													indexOfThisProcess;
	final int													totalNumProcesses;

	
	final Pattern											pattern										= Pattern.compile(query);

	DecimalFormat											numberFormatter						= new DecimalFormat("00");

	List<Entity>											listEntity;

	final boolean											localRun;

	final Hashtable<String, Boolean>	alreadyProcessedGPGFileHashTable;
	final Hashtable<String, Boolean>	toBeProcessedGPGFileHashTable;

	/**
	 * gets the index of thus process and total # of processes that this process
	 * is a member of to avoid duplicate process of corpus files.
	 * 
	 * @throws FileNotFoundException
	 * 
	 */
	public CorpusBatchProcessor(int indexOfThisProcess, int totalNumProcesses)
			throws FileNotFoundException {
		this.indexOfThisProcess = indexOfThisProcess;
		this.totalNumProcesses = totalNumProcesses;
		File f = new File(CORPUS_DIR_LOCAL);
		localRun = f.exists();
		alreadyProcessedGPGFileHashTable = localRun ? LogReader.getPreLoggedFileList(LOG_DIR_LOCAL_OLD)
				: LogReader.getPreLoggedFileList(LOG_DIR_SERVER_OLD);
		toBeProcessedGPGFileHashTable = localRun ? LogReader
				.getToProcessFileList(LOG_DIR_LOCAL_TO_PROCESS) : LogReader
				.getToProcessFileList(LOG_DIR_SERVER_TO_PROCESS);
	}

	public CorpusBatchProcessor() throws FileNotFoundException {
		indexOfThisProcess = -1;
		this.totalNumProcesses = -1;
		File f = new File(CORPUS_DIR_LOCAL);
		localRun = f.exists();
		alreadyProcessedGPGFileHashTable = localRun ? LogReader.getPreLoggedFileList(LOG_DIR_LOCAL_OLD)
				: LogReader.getPreLoggedFileList(LOG_DIR_SERVER_OLD);
		toBeProcessedGPGFileHashTable = localRun ? LogReader
				.getToProcessFileList(LOG_DIR_LOCAL_TO_PROCESS) : LogReader
				.getToProcessFileList(LOG_DIR_SERVER_TO_PROCESS);

	}

	/**
	 * Grab content of a local GPG file.
	 * 
	 * @param date
	 * @param fileName
	 * @param fileStr
	 * @return
	 */
	private static InputStream grabGPGLocal(String date, String fileName, String fileStr) {
		// System.out.println(date + "/" + fileName);
		String command = "gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt "
				+ fileStr;
		// + fileStr + " | xz --decompress";
		// System.out.println(command);
		return FileProcessor.runBinaryShellCommand(command);
	}

	/**
	 * Get Streams of a specific file name in a day-hour directory.
	 * 
	 * @param day
	 * @param hour
	 * @param fileName
	 * @param is
	 * @throws Exception
	 */
	private void getStreams(PrintWriter pw, String day, int hour, String fileName, InputStream is)
			throws Exception {
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
				siCount.incrementAndGet();
				// si.getBody().unsetRaw();

				// processTokens(si);
				// pipeline runs on the stream item

				// pipe.run(si);

				// System.out.println(day + "|" + hour+ "|" + fileName+ "|" + index);
				SIWrapper siw = new SIWrapper(day, hour, fileName, index, si);
				process(pw, siw);

				// si.clear();
				index = index + 1;
			} catch (TTransportException e) {
				tTransportExceptionPrintString(e);
				exception = true;
			}
		}
		transport.close();
	}

	/**
	 * Get the appropirate cause of exception string for TTransportException
	 * 
	 * @param e
	 */
	private static void tTransportExceptionPrintString(TTransportException e) {
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

	/**
	 * Process StreamItemWrapper by going through tokens and concatenating tehm to
	 * make sure we can handle multi word entities.
	 * 
	 * @param siw
	 */
	private void process(PrintWriter pw, SIWrapper siw) {
		boolean printedFileName = false;
		if (siw.getStreamItem().getBody() != null) {

			List<Sentence> listSentence = siw.getStreamItem().getBody().getSentences().get("lingpipe");

			if (listSentence == null) {
				System.out.println("lingpipe = Null: " + siw);
			} else {
				// /TODO a map from entoty to boolean true false takes less memory than
				// initiing all sentences.
				List<String> listStr = new LinkedList<String>();
				for (Sentence sentence : listSentence) {
					StringBuilder sentenceStr = new StringBuilder();
					for (Token t : sentence.getTokens()) {
						if (t.entity_type != null)
							sentenceStr.append(t.token.toLowerCase() + " ");
					}
					// / pipe.transform(sentence.tokens.toArray(new
					// Token[sentence.tokens.size()]));
					listStr.add(sentenceStr.toString());
				}

				// match all entities
				for (Entity entity : listEntity) {
					boolean matchedEntity = false;
					// match all aliases of entity
					for (int ientity = 0; ientity < entity.names().size(); ientity++) {
						// for all sentences
						for (int isentence = 0; isentence < listStr.size() && !matchedEntity; isentence++) {
							String s = listStr.get(isentence);

							String alias = entity.names().get(ientity);
							if (s.contains(alias)) {
								if (!printedFileName) {
									pw.print(">" + siw.day + " | " + siw.fileName + " | " + siw.getIndex() + " | "
											+ siw.getStreamItem().getDoc_id() + " || ");

									printedFileName = true;
								}
								matchedEntity = true;

								pw.print(entity.topic_id() + ", ");
								siFilteredCount.incrementAndGet();
							}
						}
					}
				}
				// }
				// }
				if (printedFileName)
					pw.println(); // final new line
			}
		}

		// .replaceAll("[^A-Za-z0-9\\p{Punct}]", " ")
		// .replaceAll("\\s+", " ").replaceAll("(\r\n)+",
		// "\r\n").replaceAll("(\n)+", "\n")
		// .replaceAll("(\r)+", "\r").toLowerCase();
		pw.flush();
	}

	private boolean isAlreadyProcessed(String fileName) {
		boolean contains = alreadyProcessedGPGFileHashTable.containsKey(fileName);
		return contains;
	}

	private boolean isToBeProcessed(String fileName) {
		boolean contains = toBeProcessedGPGFileHashTable.containsKey(fileName);
		return contains;
	}

	/**
	 * Process StreamItem and go through tokens.
	 * 
	 * @param si
	 */
	private void process(StreamItem si) {
		Map<String, List<Sentence>> sentencesMap = si.getBody().getSentences();
		Set<String> sentenceSetKeys = sentencesMap.keySet();
		for (Iterator iterator = sentenceSetKeys.iterator(); iterator.hasNext();) {
			String sentenceKey = (String) iterator.next();

			List<Sentence> listSentence = sentencesMap.get(sentenceKey);
			for (Sentence s : listSentence) {
				List<Token> listToken = s.tokens;
				for (Token t : listToken) {
					// System.out.println(si.getBody().getClean_visible());
					// if (si.getBody().getClean_visible().toLowerCase().contains(query))
					// System.out.println(t.getLemma());
					if (t.getToken() != null) {

						// t.setLemma(t.getLemma().toLowerCase());
						if (t.getToken().toLowerCase().contains(query)) {
							// siFilteredCount.incrementAndGet();
							return;
						}
					}
				}
			}
		}
	}

	/**
	 * Process using java ExecutorService
	 * 
	 * @throws ParseException
	 */
	// private void process() throws ParseException {
	//
	// int threadCount;
	//
	// Calendar c = Calendar.getInstance();
	// Calendar cEnd = Calendar.getInstance();
	//
	// File f = new File(CORPUS_DIR_LOCAL);
	// boolean localRun = f.exists();
	// final String DIRECTORY = (localRun) ? CORPUS_DIR_LOCAL : CORPUS_DIR_SERVER;
	// if (localRun) {
	// System.out.println("Local run.");
	// // c.setTime(format.parse("2011-10-05-00"));
	// c.setTime(format.parse("2011-10-07-13"));
	// cEnd.setTime(format.parse("2011-10-07-14"));
	// threadCount = 2;
	// } else {
	// System.out.println("Server run.");
	// // c.setTime(format.parse("2011-10-05-00"));
	// c.setTime(format.parse("2012-08-18-01"));
	// cEnd.setTime(format.parse("2012-08-18-01"));
	// threadCount = 32;
	// }
	//
	// ExecutorService executor = Executors.newFixedThreadPool(threadCount);
	// while (!(c.getTime().compareTo(cEnd.getTime()) > 0)) {
	// try {
	// final String date = format.format(c.getTime());
	//
	// final List<String> fileList = DirList.getFileList(DIRECTORY + date,
	// FILTER);
	// for (final String fileStr : fileList) {
	// final int hour = c.get(Calendar.HOUR_OF_DAY);
	// final String fileName = fileStr.substring(fileStr.lastIndexOf('/') + 1);
	//
	// //
	// // Runnable worker = new Thread(fileCount + " " + date + "/" +
	// // fileName) {
	// // public void run() {
	// //
	//
	// try {
	// InputStream is = grabGPGLocal(date, fileName, fileStr);
	// getStreams(date, hour, fileName, is);
	// is.close();
	//
	// fileCount.incrementAndGet();
	// long size = FileProcessor.getLocalFileSize(fileStr);
	// processedSize.addAndGet(size);
	// report(logTimeFormat, date + "/" + fileName);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// //
	// //
	// // };
	// // };
	// // executor.execute(worker);
	// //
	// //
	//
	// }
	// } catch (Exception e1) {
	// e1.printStackTrace();
	// }
	// if (totalNumProcesses == -1)
	// c.add(Calendar.HOUR, 1);
	// else
	// c.add(Calendar.HOUR, totalNumProcesses);
	// }
	//
	// //
	// executor.shutdown();
	// while (!executor.isTerminated()) {
	// try {
	// Thread.sleep(500);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	// //
	//
	// report(logTimeFormat, "Finished all threads");
	// }

	/**
	 * Process in multithreaded fashion.
	 * 
	 * @throws ParseException
	 */
	private void processMultiThreads() throws ParseException {
		System.out.println("processMultiThreads");

		final Calendar cStart = Calendar.getInstance();
		final Calendar cEnd = Calendar.getInstance();

		final int threadCount = (localRun) ? 1 : 64;
		final String CORPUS_DIRECTORY = (localRun) ? CORPUS_DIR_LOCAL : CORPUS_DIR_SERVER;
		final String LOG_DIRECTORY = (localRun) ? LOG_DIR_LOCAL : LOG_DIR_SERVER;
		if (localRun) {
			System.out.println("Local run.");
			cStart.setTime(format.parse("2011-10-05-00"));
			// cStart.setTime(format.parse("2011-10-07-13"));
			cEnd.setTime(format.parse("2011-10-07-14"));
		} else {
			System.out.println("Server run.");
			cStart.setTime(format.parse("2011-10-05-00"));
			// cStart.setTime(format.parse("2012-08-18-01"));
			cEnd.setTime(format.parse("2012-08-18-01"));
		}

		final AtomicInteger finishedThreadTracker = new AtomicInteger(0);
		for (int i = 0; i < threadCount; i++) {
			final int threadIndex = i;
			final Calendar cTemp = Calendar.getInstance();
			cTemp.setTime(cStart.getTime());
			cTemp.add(Calendar.HOUR, threadIndex);

			Thread worker = new Thread() {// one thread per hour then add index
				public void run() {

					PrintWriter pw = null;
					try {
						pw = new PrintWriter(new File(LOG_DIRECTORY + "run" + threadIndex + "Log.txt"));

						// System.out.println(threadIndex + ": " +
						// format.format(cTemp.getTime()) + " "
						// + format.format(cEnd.getTime()));
						while (!(cTemp.getTime().compareTo(cEnd.getTime()) > 0)) {
							try {
								final String date = format.format(cTemp.getTime());

								final List<String> fileList = DirList.getFileList(CORPUS_DIRECTORY + date, FILTER);
								for (final String fileStr : fileList) {
									final int hour = cTemp.get(Calendar.HOUR_OF_DAY);
									final String fileName = fileStr.substring(fileStr.lastIndexOf('/') + 1);
									String dateFile = date + "/" + fileName;
									if (isAlreadyProcessed(dateFile)) {
										System.out.println("@ " + dateFile);
										// Object o =
										// alreadyProcessedGPGFileHashTable.remove(dateFile);
										// if(o == null)
										// throw new Exception("Exception");
									} else if (isToBeProcessed(fileStr)) {
										try {
											InputStream is = grabGPGLocal(date, fileName, fileStr);
											getStreams(pw, date, hour, fileName, is);
											is.close();

											fileCount.incrementAndGet();
											long size = FileProcessor.getLocalFileSize(fileStr);
											processedSize.addAndGet(size);
											// report(logTimeFormat, "Thread(" + threadIndex + ")" +
											// date
											// + "/" + fileName);
											pw.println(logTimeFormat.format(new Date()) + " Total " + fileCount
													+ " Files " + FileProcessor.fileSizeToStr(processedSize.get(), "MB")
													+ " SIs: " + siCount.get() + " +SIs: " + siFilteredCount + " "
													+ "Thread(" + threadIndex + ")" + date + "/" + fileName);
											pw.flush();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							} catch (Exception e1) {
								e1.printStackTrace();
							}

							cTemp.add(Calendar.HOUR, threadCount);
							// System.out.println(threadIndex + ": " +
							// format.format(cTemp.getTime()) + " "
							// + format.format(cEnd.getTime()));
						}

					} catch (FileNotFoundException e2) {
						e2.printStackTrace();
					}

					finishedThreadTracker.incrementAndGet();
					if (pw != null)
						pw.close();
				}
			};
			worker.start();
		}
		while (finishedThreadTracker.get() < threadCount) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		report(logTimeFormat, "Finished all threads");
	}

	/**
	 * Generate a timely statistics of the # of fiels, total file size processed
	 * so far. # of StreamItems, # of Stream Items that contained an entity,
	 * current file name etc.
	 * 
	 * @param df
	 * @param message
	 */
	private void report(DateFormat df, String message) {
		System.out.println(df.format(new Date()) + " Total " + fileCount + " Files "
				+ FileProcessor.fileSizeToStr(processedSize.get(), "MB") + " SIs: " + siCount.get()
				+ " +SIs: " + siFilteredCount + " " + message);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		Preprocessor
				.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json");

		if (args.length == 0) {
			CorpusBatchProcessor cps = new CorpusBatchProcessor();
			cps.listEntity = Preprocessor.entity_list();
			// cps.process();
			cps.processMultiThreads();
		} else if (args.length == 2) {
			// CorpusBatchProcessor cps = new
			// CorpusBatchProcessor(Integer.parseInt(args[0]),
			// Integer.parseInt(args[0]));
			// cps.listEntity = Preprocessor.entity_list();
			// cps.process();
		} else {
			System.err
					.println("Usage: CorpusBatchProcessor indexOfThisProcess totalNumProcesses   OR just   CorpusBatchProcessor");
			System.err
					.println("Where totalNumProcesses is an integer  the total # of processes of CorpusBatchProcessor that run on corpus."
							+ " indexOfThisProcess is an integer that identifies the index of this process in the total # of CorpusBatchProcessor that are running on the corpus at the same time to avoid duplicate process of corpus sections.");
		}

		// String s = new String();
		// s.intern();
	}
}
