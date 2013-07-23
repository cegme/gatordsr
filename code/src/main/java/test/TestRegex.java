package test;

public class TestRegex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String s = "/media/sde/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-08-18-14/"
				+ "arxiv-3-e085f6cabc55117393a0af0cf29210ce-7185580bba8b4c627eb29f8483997c22.sc.xz.gpg";

		int lastSlash = s.lastIndexOf('/');
		String sArr[] = s.split("/");
		String tempFileStr = sArr[sArr.length - 2] + "/" + sArr[sArr.length - 1];
		System.out.println();
	}

}
