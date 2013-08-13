package edu.ufl.cise.benchmark;

// Java imports
 import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;
// Bouncy castle imports
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
//
public class SingleSignOnTest {
      private static File publicKeyFile = new File("/Development/Java/Single Sign On with Encryption(PGP)/PGP1D0.pkr");
      private static File privateKeyFile = new File("/home/morteza/key");
      private static String privateKeyPassword = "";
 
       //
      // Public class method decrypt
      //
      public static String decrypt(byte[] encdata) {
              System.out.println("decrypt(): data length=" + encdata.length);
              // ----- Decrypt the file
              try {
                      ByteArrayInputStream bais = new ByteArrayInputStream(encdata);
                      FileInputStream privKey = new FileInputStream(privateKeyFile);
                      return _decrypt(bais, privKey, privateKeyPassword.toCharArray());
              } catch (Exception e) {
                      System.out.println(e.getMessage());
                      e.printStackTrace();
              }
              return null;
      }
      //
      // Public class method encrypt
      //
      public static byte[] encrypt(byte[] data) {
              try
              {
                      // ----- Read in the public key
                      PGPPublicKey key = readPublicKeyFromCol(new FileInputStream(publicKeyFile));
                      System.out.println("Creating a temp file...");
                      // create a file and write the string to it
                      File tempfile = File.createTempFile("pgp", null);
                      FileOutputStream fos = new FileOutputStream(tempfile);
                      fos.write(data);
                      fos.close();
                      System.out.println("Temp file created at ");
                      System.out.println(tempfile.getAbsolutePath());
                      System.out.println("Reading the temp file to make sure that the bits were written\n--------------");
                      BufferedReader isr = new BufferedReader(new FileReader(tempfile));
                      String line = "";
                      while ( (line = isr.readLine())!= null )
                      {
                              System.out.println(line + "\n");
                      }
                      // find out a little about the keys in the public key ring
                      System.out.println("Key Strength = " + key.getBitStrength());
                      System.out.println("Algorithm = " + key.getAlgorithm());
                      System.out.println("Bit strength = " + key.getBitStrength());
                      System.out.println("Version = " + key.getVersion());
                      System.out.println("Encryption key = " + key.isEncryptionKey()+ ", Master key = " + key.isMasterKey());
                      int count = 0;
                      for ( java.util.Iterator iterator = key.getUserIDs(); iterator.hasNext(); )
                      {
                              count++;
                              System.out.println((String) iterator.next());
                      }
                      System.out.println("Key Count = " + count);
                      // create an armored ascii file
                      // FileOutputStream out = new FileOutputStream(outputfile);
                      // encrypt the file
                      // encryptFile(tempfile.getAbsolutePath(), out, key);
                      // Encrypt the data
                      ByteArrayOutputStream baos = new ByteArrayOutputStream();
                      _encrypt(tempfile.getAbsolutePath(), baos, key);
                      System.out.println("encrypted text length=" + baos.size());			
                      tempfile.delete();
                      return baos.toByteArray();
              }
              catch (PGPException e)
              {
                      // System.out.println(e.toString());
                      System.out.println(e.getUnderlyingException().toString());
                      e.printStackTrace();
              }
              catch (Exception e)
              {
                      e.printStackTrace();
              }
              return null;
      }
      //
      // Private class method readPublicKeyFromCol
      //
      private static PGPPublicKey readPublicKeyFromCol(InputStream in)
                     throws Exception {
              PGPPublicKeyRing pkRing = null;
              PGPPublicKeyRingCollection pkCol = new PGPPublicKeyRingCollection(in);
              System.out.println("key ring size=" + pkCol.size());
              Iterator it = pkCol.getKeyRings();
              while (it.hasNext()) {
                      pkRing = (PGPPublicKeyRing) it.next();
                      Iterator pkIt = pkRing.getPublicKeys();
                      while (pkIt.hasNext()) {
                              PGPPublicKey key = (PGPPublicKey) pkIt.next();
                              System.out.println("Encryption key = " + key.isEncryptionKey() + ", Master key = " + 
                                                 key.isMasterKey());
                              if (key.isEncryptionKey())
                                      return key;
                      }
              }
              return null;
      }
      //
      // Private class method _encrypt
      //
      private static void _encrypt(String fileName, OutputStream out, PGPPublicKey encKey)
                          throws IOException, NoSuchProviderException, PGPException
      {
              out = new DataOutputStream(out);
              ByteArrayOutputStream bOut = new ByteArrayOutputStream();
              System.out.println("creating comData...");
              // get the data from the original file
              PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedDataGenerator.ZIP);
              PGPUtil.writeFileToLiteralData(comData.open(bOut), PGPLiteralData.BINARY, new File(fileName));
              comData.close();
              System.out.println("comData created...");
              System.out.println("using PGPEncryptedDataGenerator...");
              // object that encrypts the data
              PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(PGPEncryptedDataGenerator.CAST5, 
                                              new SecureRandom(), "BC");
              cPk.addMethod(encKey);
              System.out.println("used PGPEncryptedDataGenerator...");
              // take the outputstream of the original file and turn it into a byte
              // array
              byte[] bytes = bOut.toByteArray();
              System.out.println("wrote bOut to byte array...");
              // write the plain text bytes to the armored outputstream
              OutputStream cOut = cPk.open(out, bytes.length);
              cOut.write(bytes);
              cPk.close();
              out.close();
      }
      //
      // Private class method _decrypt
      //
      private static String _decrypt(InputStream in, InputStream keyIn,
                      char[] passwd) throws Exception {
              in = PGPUtil.getDecoderStream(in);
              try {
                      PGPObjectFactory pgpF = new PGPObjectFactory(in);
                      PGPEncryptedDataList enc;
                      Object o = pgpF.nextObject();
                      //
                      // the first object might be a PGP marker packet.
                      //
                      if (o instanceof PGPEncryptedDataList) {
                              enc = (PGPEncryptedDataList) o;
                      } else {
                              enc = (PGPEncryptedDataList) pgpF.nextObject();
                      }
                      //
                      // find the secret key
                      //
                      Iterator it = enc.getEncryptedDataObjects();
                      PGPPrivateKey sKey = null;
                      PGPPublicKeyEncryptedData pbe = null;
                      while (sKey == null && it.hasNext()) {
                              pbe = (PGPPublicKeyEncryptedData) it.next();
                              System.out.println("pbe id=" + pbe.getKeyID());
                              sKey = findSecretKey(keyIn, pbe.getKeyID(), passwd);
                      }
                      if (sKey == null) {
                             throw new IllegalArgumentException("secret key for message not found.");
                      }
                      InputStream clear = pbe.getDataStream(sKey, "BC");
                      PGPObjectFactory plainFact = new PGPObjectFactory(clear);
                      Object message = plainFact.nextObject();
                      if (message instanceof PGPCompressedData) {
                              PGPCompressedData cData = (PGPCompressedData) message;
                              PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream());
                              message = pgpFact.nextObject();
                      }
                      ByteArrayOutputStream baos = new ByteArrayOutputStream();
                      if (message instanceof PGPLiteralData) {
                              PGPLiteralData ld = (PGPLiteralData) message;
                              InputStream unc = ld.getInputStream();
                              int ch;
                              while ((ch = unc.read()) >= 0) {
                                      baos.write(ch);
                              }
                      } else if (message instanceof PGPOnePassSignatureList) {
                              throw new PGPException("encrypted message contains a signed message - not literal data.");
                      } else {
                              throw new PGPException("message is not a simple encrypted file - type unknown.");
                      }
                      if (pbe.isIntegrityProtected()) {
                              if (!pbe.verify()) {
                                      System.err.println("message failed integrity check");
                              } else {
                                      System.err.println("message integrity check passed");
                              }
                      } else {
                              System.err.println("no message integrity check");
                      }
                      return baos.toString();
              } catch (PGPException e) {
                      System.err.println(e);
                      if (e.getUnderlyingException()!= null) {
                              e.getUnderlyingException().printStackTrace();
                      }
              }
              return null;
      }
      //
      // Private class method findSecretKey
      //
      private static PGPPrivateKey findSecretKey(InputStream keyIn, long keyID,
                      char[] pass) throws IOException, PGPException,
                      NoSuchProviderException {
              PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn));
              PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);
              if (pgpSecKey == null) {
                      return null;
              }
              return pgpSecKey.extractPrivateKey(pass, "BC");
      }
      //
      // Public class method readFile
      //
      public byte[] readFile(File file) throws IOException {
              FileInputStream fis = new FileInputStream(file);
              byte[] buf = new byte[4096];
              int numRead = 0;
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              while ((numRead = fis.read(buf)) > 0) {
                      baos.write(buf, 0, numRead);
              }
              fis.close();
              byte[] returnVal = baos.toByteArray();
              baos.close();
              return returnVal;
      }
      //
      // Public main method
      //
      public static void main(String[] args) {
              Security.addProvider(new BouncyCastleProvider());
         //     String TOKEN = "aamine";
              // ----- Encrypt the message to a file
              // them
           //   byte[] encdata = encrypt(TOKEN.getBytes());
             // System.out.println("Encrypted: " + encdata);
              // Encode the byte array to a string
         //     BASE64Encoder en = new BASE64Encoder();		
           //   String temp = en.encode(encdata);
            //  System.out.println("Temp: " + temp);
              // us
              
              File f  = new File("");
              byte[] newB=null;		
              BASE64Decoder en1 = new BASE64Decoder();
              try {
                     // newB = en1.decodeBuffer(temp);
              	FileInputStream is = new FileInputStream(new File("/home/morteza/zproject/FORUM-161-9fd2150f9ae1eb9f0d44bdf093f86b60-4442a5d96c0ce98f4bb78977a0c15b7d.sc.xz.gpg"));
            //  	OutputStream os = new o
              //	 newB = en1.decodeBuffer(is, );
              } catch (Exception e) {
                      System.out.println("Exception: " + e);
              }
              System.out.println("byte array" + newB.length);
              // ----- Decrypt the token that
              String result = decrypt(newB);
              System.out.println("Decrypted: " + result);
      }
}