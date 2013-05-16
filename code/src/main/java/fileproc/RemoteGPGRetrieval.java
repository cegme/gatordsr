package fileproc;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransportException;

import streamcorpus.StreamItem;

public class RemoteGPGRetrieval {

	public static void main(String[] args) {
		String fileName = "WEBLOG-89-15957f5baef21e2cda6dca887b96e23e-e3bb3adf7504546644d4bc2d62108064.sc.xz.gpg";
		getStreams("2012-11-03-05", fileName);
	}

	public static List<StreamItem> getStreams(String date, String fileName) {

		String command = "sshpass -p 'trecGuest' ssh trecGuest@sm321-01.cise.ufl.edu 'cat /media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/"
				+ date
				+ "/"
				+ fileName
				+ "' | gpg -q --no-verbose --no-permission-warning --trust-model always --output - --decrypt - | xz --decompress";

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
				if (si.getBody() != null
						&& si.getBody().getClean_visible() != null) {
					System.out.println(si.getBody().getClean_visible()
							.substring(0, 100));
				}
			} catch (Exception e) {
				exception = true;
				System.err.println(e);
			}
			list.add(si);
			index = index + 1;
		}
		transport.close();
		return list;
	}
}
