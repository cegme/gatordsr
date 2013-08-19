package edu.ufl.cise.benchmark;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PGPFileProcessor {

	private String passphrase;

	private String keyFile;

	private String inputFile;

	private String outputFile;

	private boolean asciiArmored = false;

	private boolean integrityCheck = true;
	
	public static void main(String[] args) {
		PGPFileProcessor pgp = new PGPFileProcessor();
		
		pgp.setPassphrase("");
//		pgp.setAsciiArmored(!true);
		pgp.setKeyFile("/home/morteza/trec-kba-rsa.txt");
		pgp.setInputFile("/home/morteza/zproject/FORUM-161-9fd2150f9ae1eb9f0d44bdf093f86b60-4442a5d96c0ce98f4bb78977a0c15b7d.sc.xz.gpg");
		pgp.setOutputFile("/home/morteza/bc");
		try {
			pgp.decrypt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean encrypt() throws Exception {
		FileInputStream keyIn = new FileInputStream(keyFile);
        FileOutputStream out = new FileOutputStream(outputFile);
        PGPUtil.encryptFile(out, inputFile, PGPUtil.readPublicKey(keyIn),
        	asciiArmored, integrityCheck);
        out.close();
        keyIn.close();
        return true;
	}

	public boolean decrypt() throws Exception {
		 FileInputStream in = new FileInputStream(inputFile);
         FileInputStream keyIn = new FileInputStream(keyFile);
         FileOutputStream out = new FileOutputStream(outputFile);
         PGPUtil.decryptFile(in, out, keyIn, passphrase.toCharArray());
         in.close();
         out.close();
         keyIn.close();
         return true;
	}

	public boolean isAsciiArmored() {
		return asciiArmored;
	}

	public void setAsciiArmored(boolean asciiArmored) {
		this.asciiArmored = asciiArmored;
	}

	public boolean isIntegrityCheck() {
		return integrityCheck;
	}

	public void setIntegrityCheck(boolean integrityCheck) {
		this.integrityCheck = integrityCheck;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public String getKeyFile() {
		return keyFile;
	}

	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

}
