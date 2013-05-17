package fileproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import edu.ufl.cise.util.StreamItemWrapper;

import streamcorpus.StreamItem;

public class CorpusBatchProcessor {

	private static InputStream grabGPGLocal(String day, int hour,
			String fileName) {
		System.out.println(day + "/" + hour + "/" + fileName);
		String command = "gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt "
				+ fileName + "| xz --decompress";
		return FileProcessor.runBinaryShellCommand(command);
	}

	private static List<StreamItemWrapper> getStreams(String day, int hour,
			String fileName, InputStream is) {
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
				System.err.println(e.getMessage());
				exception = true;
			}

			list.add(new StreamItemWrapper(day, hour, fileName, index, si));
			index = index + 1;
		}
		transport.close();
		return list;
	}
	
	

	private void job() {
		 
int threadCount = 32;
		 ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		String DIRECTORY = "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
				String FILTER = "";

		    List<String> fileList = DirList.getFileList(DIRECTORY, FILTER);
		    System.out.println("total file count on disk sdd is: " + fileList.size());

		    AtomicInteger siCount = new AtomicInteger(0);
		    AtomicInteger siFilteredCount = new AtomicInteger(0);
			int i = 0;
			boolean finished = false;
			String line;
			String dayHourFileNamePatternStr = ".*language/([^/]+)-(.+)/(.+)";
			final Pattern dayHourFileNamePattern = Pattern.compile(dayHourFileNamePatternStr);
			
			while (!finished) {
				

				try {
					//line = br.readLine();

					//if (line == null)
				//		finished = true;
					//else {
					//	final Matcher m1 = p.matcher(line);

					//	if (m1.find()) {
							//String linkStr =  m1.group(1);
							// System.out.println(linkStr);
							final String fileStr = fileList.get(i);//linkStr.substring(0, linkStr.indexOf('/'));
							System.out.println(fileStr);

							Runnable worker = new Thread(i++ + " " + fileStr) {
								public void run() {
									int size;
									try {
										size = FileProcessor.getFileSize(fileStr);
										Matcher m = dayHourFileNamePattern.matcher(fileStr);
										String day = m.group(1);
										int hour = Integer.parseInt(m.group(2));
										String fileName = m.group(3);
										
										InputStream is = grabGPGLocal(day, hour, fileName);
										List<StreamItemWrapper> list = getStreams(day, hour, fileName, is);
										processThriftFile(fileStr);
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
				i++;
			}

			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			System.out.println("Finished all threads");		    
		    
		    

		    fileList.foreach(p => {
		      val temp = p.asInstanceOf[String]
		      val pattern = """.*language/([^/]+)-(.+)/(.+)""".r

		      val dayHourFileList = pattern.findAllIn(temp).matchData.toArray
		      val day = dayHourFileList.apply(0).group(1)
		      val hour = new Integer(dayHourFileList.apply(0).group(2))
		      val fileName = dayHourFileList.apply(0).group(3)
		      val data = grabGPGLocal(day, hour, temp)
		      // val data = grabGPGSSH("/media/sde/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-11-03-05/WEBLOG-89-15957f5baef21e2cda6dca887b96e23e-e3bb3adf7504546644d4bc2d62108064.sc.xz.gpg")
		      val tempArr = data.toByteArray()
		      val sis = getStreams(day, hour, fileName, data)
		      siCount.addAndGet(sis.size)

		      sis.foreach(p => {
		        var res = false

		        if (p.streamItem.body != null) {
		          val document = p.streamItem.body.getClean_visible()
		          if (document != null) {
		            val strEnglish = document.toLowerCase().replaceAll("[^A-Za-z0-9\\p{Punct}]", " ").replaceAll("\\s+", " ")
		              .replaceAll("(\r\n)+", "\r\n").replaceAll("(\n)+", "\n").replaceAll("(\r)+", "\r").toLowerCase()
		            res = strEnglish.contains(query)
		          } else
		            res = false
		        }
		        if (res == true) {
		          // println("Found")
		          val str = p.toString
		          println(str)
		          siFilteredCount.incrementAndGet()
		        }
		      })
		    })

		    println("total file count on disk" + DIRECTORY + " before filter is: " + siCount.get())
		    println("total file count on disk " + DIRECTORY + "after filter is: " + siFilteredCount.get())
		    //    tempFilter.foreach(p => {
		    //      logInfo(p.toString())
		    //    })
		  }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		process();
	}

}
