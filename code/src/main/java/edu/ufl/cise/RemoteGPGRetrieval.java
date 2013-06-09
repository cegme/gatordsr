package edu.ufl.cise;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import streamcorpus.StreamItem;

public class RemoteGPGRetrieval {

	public static final String	SDD_BASE_PATH	= "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";
	public static final String	SDE_BASE_PATH	= "/media/sde/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/";

	public static void main(String[] args) {
		 String fileName =
		 "social-458-b51e990263a58e94a88d22a8be8502d1-d71caa2571e6e6aa16da0cdae2a4dfc7.sc.xz.gpg";
		 getStreams("2011-11-03-05", fileName);

		try {
			// List<StreamItem> l =
			// getLocalStreams("/home/morteza/Downloads/social-222-fc6ce593d5a66a74da58358cfd87c9e1-5aa3991c8ea528a275238355aabc9d8c.sc.xz.gpg");
			List<StreamItem> l = getLocalStreams("2011-10-05-03",
					"arxiv-5-1432f036a5768d8e2f16f56770b2b13b-aae9af08ed49d35c0810f3c8fac1db00.sc.xz.gpg ");
			for (StreamItem si : l) {
				System.out.println(si.doc_id);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<StreamItem> getLocalStreams(String date, String fileName) throws IOException {
		return getLocalStreams(SDD_BASE_PATH, date, fileName);
	}

	public static List<StreamItem> getLocalStreams(String basePath, String date, String fileName)
			throws IOException {
		String command = "gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt "
				+ basePath + date + "/" + fileName;
//		System.out.println(command);
		InputStream is = FileProcessor.runBinaryShellCommand(command);
		XZCompressorInputStream xzis = new XZCompressorInputStream(is);
		TIOStreamTransport transport = new TIOStreamTransport(xzis);

		List<StreamItem> list = new LinkedList<StreamItem>();

		boolean exception = false;
		while (!exception) {
			try {
				transport.open();
				TBinaryProtocol protocol = new TBinaryProtocol(transport);

				int index = 0;

				StreamItem si = new StreamItem();
				if (protocol.getTransport().isOpen())
					si.read(protocol);
				list.add(si);
				// SIWrapper siw = new SIWrapper(day, hour, fileName, index, si);
				index = index + 1;
			} catch (TTransportException e) {
				tTransportExceptionPrintString(e);
				exception = true;
			} catch (TException e) {
				e.printStackTrace();
			}
		}
		transport.close();
		return list;
	}

	/**
	 * Get the appropirate cause of exception string for TTransportException
	 * 
	 * @param e
	 */
	public static void tTransportExceptionPrintString(TTransportException e) {
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

	public static List<StreamItem> getStreams(String date, String fileName) {

		String command = "sshpass -p 'trecGuest' ssh trecGuest@sm321-01.cise.ufl.edu 'cat /media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/"
				+ date
				+ "/"
				+ fileName
				+ "' | gpg  --no-permission-warning --trust-model always --output - --decrypt - | xz --decompress";
		// System.out.println(command);

		InputStream is = FileProcessor.runBinaryShellCommand(command);
		TIOStreamTransport transport = new TIOStreamTransport(is);
		try {
			transport.open();
		} catch (TTransportException e1) {
			e1.printStackTrace();
		}
		TBinaryProtocol protocol = new TBinaryProtocol(transport);

		LinkedList<StreamItem> list = new LinkedList<StreamItem>();

		int index = 0;
		boolean exception = false;
		while (!exception) {
			StreamItem si = new StreamItem();
			try {
				si.read(protocol);
				if (si.getBody() != null && si.getBody().getClean_visible() != null) {
					// System.out.println(si.getBody().getClean_visible()
					// .substring(0, 5));
				}
			} catch (Exception e) {
				exception = true;
				//System.err.println(e);
			}
			list.add(si);
			index = index + 1;
		}
		transport.close();
		return list;
	}

    /**
     * Reads non encrypted si files and returns a list of them.
     */
    public static List<StreamItem> readNonEncrypted(String fileName) throws IOException, TTransportException {
        InputStream is = new java.io.FileInputStream(new java.io.File(fileName));
        XZCompressorInputStream xzis = new XZCompressorInputStream(is);
        TIOStreamTransport transport = new TIOStreamTransport(xzis);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        System.err.println("readNonEncrypted: " + fileName);
        transport.open();
        LinkedList<StreamItem> listSI = new LinkedList<StreamItem>();
        boolean exception = false;
        while (!exception) {
            try {
                StreamItem si = new StreamItem();
                si.read(protocol);
                listSI.add(si);
                System.out.println(si.getBody().getSentences().get("lingpipe").get(0).getTokens().get(0));
            } catch (TTransportException e) {
                RemoteGPGRetrieval.tTransportExceptionPrintString(e);
//                e.printStackTrace();
                exception = true;
            } catch (TException e) {
                e.printStackTrace();
            }
        }
        transport.close();
        return listSI;
            
    }
}
