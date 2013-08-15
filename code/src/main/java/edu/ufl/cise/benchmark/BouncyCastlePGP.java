//package edu.ufl.cise.benchmark;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.security.NoSuchProviderException;
//import java.security.SignatureException;
//import java.util.Iterator;
//
//import org.bouncycastle.openpgp.PGPCompressedData;
//import org.bouncycastle.openpgp.PGPEncryptedDataList;
//import org.bouncycastle.openpgp.PGPException;
//import org.bouncycastle.openpgp.PGPLiteralData;
//import org.bouncycastle.openpgp.PGPObjectFactory;
//import org.bouncycastle.openpgp.PGPOnePassSignature;
//import org.bouncycastle.openpgp.PGPOnePassSignatureList;
//import org.bouncycastle.openpgp.PGPPrivateKey;
//import org.bouncycastle.openpgp.PGPPublicKey;
//import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
//import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
//import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
//import org.bouncycastle.openpgp.PGPSignature;
//import org.bouncycastle.openpgp.PGPSignatureList;
//import org.bouncycastle.openpgp.PGPUtil;
//import org.bouncycastle.util.io.Streams;
//
//public class BouncyCastlePGP {
//
//	public static void decryptFile(InputStream in, InputStream keyIn, char[] passwd, OutputStream fOut,
//			InputStream publicKeyIn) throws IOException, NoSuchProviderException, SignatureException, PGPException {
//		in = PGPUtil.getDecoderStream(in);
//
//		PGPObjectFactory pgpF = new PGPObjectFactory(in);
//		PGPEncryptedDataList enc;
//
//		Object o = pgpF.nextObject();
//		//
//		// the first object might be a PGP marker packet.
//		//
//		if (o instanceof PGPEncryptedDataList) {
//			enc = (PGPEncryptedDataList) o;
//		} else {
//			enc = (PGPEncryptedDataList) pgpF.nextObject();
//		}
//
//		//
//		// find the secret key
//		//
//		Iterator<?> it = enc.getEncryptedDataObjects();
//		PGPPrivateKey sKey = null;
//		PGPPublicKeyEncryptedData pbe = null;
//		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn));
//
//		while (sKey == null && it.hasNext()) {
//			pbe = (PGPPublicKeyEncryptedData) it.next();
//			sKey = PGPTools.findSecretKey(pgpSec, pbe.getKeyID(), passwd);
//		}
//
//		if (sKey == null) {
//			throw new IllegalArgumentException("secret key for message not found.");
//		}
//
//		InputStream clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(sKey));
//
//		PGPObjectFactory plainFact = new PGPObjectFactory(clear);
//
//		Object message = null;
//
//		PGPOnePassSignatureList onePassSignatureList = null;
//		PGPSignatureList signatureList = null;
//		PGPCompressedData compressedData = null;
//
//		message = plainFact.nextObject();
//		ByteArrayOutputStream actualOutput = new ByteArrayOutputStream();
//
//		while (message != null) {
//			log.trace(message.toString());
//			if (message instanceof PGPCompressedData) {
//				compressedData = (PGPCompressedData) message;
//				plainFact = new PGPObjectFactory(compressedData.getDataStream());
//				message = plainFact.nextObject();
//			}
//
//			if (message instanceof PGPLiteralData) {
//				// have to read it and keep it somewhere.
//				Streams.pipeAll(((PGPLiteralData) message).getInputStream(), actualOutput);
//			} else if (message instanceof PGPOnePassSignatureList) {
//				onePassSignatureList = (PGPOnePassSignatureList) message;
//			} else if (message instanceof PGPSignatureList) {
//				signatureList = (PGPSignatureList) message;
//			} else {
//				throw new PGPException("message unknown message type.");
//			}
//			message = plainFact.nextObject();
//		}
//		actualOutput.close();
//		PGPPublicKey publicKey = null;
//		byte[] output = actualOutput.toByteArray();
//		if (onePassSignatureList == null || signatureList == null) {
//			throw new PGPException("Poor PGP. Signatures not found.");
//		} else {
//
//			for (int i = 0; i < onePassSignatureList.size(); i++) {
//				PGPOnePassSignature ops = onePassSignatureList.get(0);
//				log.trace("verifier : " + ops.getKeyID());
//				PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(publicKeyIn));
//				publicKey = pgpRing.getPublicKey(ops.getKeyID());
//				if (publicKey != null) {
//					ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), publicKey);
//					ops.update(output);
//					PGPSignature signature = signatureList.get(i);
//					if (ops.verify(signature)) {
//						Iterator<?> userIds = publicKey.getUserIDs();
//						while (userIds.hasNext()) {
//							String userId = (String) userIds.next();
//							log.trace("Signed by {}", userId);
//						}
//						log.trace("Signature verified");
//					} else {
//						throw new SignatureException("Signature verification failed");
//					}
//				}
//			}
//
//		}
//
//		if (pbe.isIntegrityProtected() && !pbe.verify()) {
//			throw new PGPException("Data is integrity protected but integrity is lost.");
//		} else if (publicKey == null) {
//			throw new SignatureException("Signature not found");
//		} else {
//			fOut.write(output);
//			fOut.flush();
//			fOut.close();
//		}
//	}
//}
