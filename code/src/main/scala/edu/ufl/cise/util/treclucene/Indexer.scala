package edu.cise.ufl.util

import java.io.ByteArrayOutputStream 
import java.io.ByteArrayInputStream 

import scala.sys.process._
import scala.sys.process.ProcessLogger

import edu.ufl.cise.Logging

import streamcorpus.Sentence
import streamcorpus.StreamItem
import streamcorpus.Token

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.lucene.analysis.standard.StandardAnalyzer 
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.IntField
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.util.Version
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TIOStreamTransport
import org.apache.thrift.transport.TTransportException


object Indexer extends Logging {
  val decrypt_file = "gpg --no-permission-warning --trust-model always --output - --decrypt %s"

  val analyzer = new StandardAnalyzer(Version.LUCENE_43)
  var filedir = new java.io.File("/var/tmp/lucene")
  val directory = new NIOFSDirectory(filedir)
  var gpgFile = ""

  val int_field = new IntField("si_index", -1, Field.Store.YES);
  val gpg_field = new StringField("gpgfile", "", Field.Store.YES);
  val bdy_field = new TextField("clean_visible", "", Field.Store.YES);
  lazy val doc = { 
    val d = new Document
    d.add(int_field);
    d.add(gpg_field);
    d.add(bdy_field);
    d
  }

  // XXX Not thread safe, but we do this for efficency
  def makeDoc(si:StreamItem, /*gpgFile:String = "",*/ si_index:Int = -1):Document = {
    //val doc = new Document

    //doc.add(new StringField("gpgfile", gpgFile, Field.Store.YES))
    int_field.setIntValue(si_index)
    //gpg_field.setStringValue(gpgFile)
    bdy_field.setStringValue(si.body.getClean_visible)
      
    doc
  }


  def main(args: Array[String]) {
    if (args.length < 1 || args.length > 2) {
      println("Usage: run /media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-01-01-00/social-256-44919e3ea5b47f8b947974af769214b6-3a661cfbb2ef6b72090b3f441e31eefb.sc.xz.gpg")
      System.exit(1)
    }

    logInfo(args(0))

    // Location to write the index 
    if (!filedir.exists) {
      filedir.mkdir
    }

    // Set the GPG file to the index document
    gpg_field.setStringValue(args(0))

    // Create the index writer
    val writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_43, analyzer))

    // Create decryption file
    val decrypt_cmd = decrypt_file.format(args(0))

    // Read and extract the gpg file
    val xzGPG = new ByteArrayOutputStream
    (decrypt_cmd #> xzGPG) ! ProcessLogger(line => ())
    val is = new ByteArrayInputStream(xzGPG.toByteArray)
    val bais = new XZCompressorInputStream(is)
    val transport = new TIOStreamTransport(bais)
    transport.open
    val protocol = new TBinaryProtocol(transport)
    
    // For each StreamItem
    var si_counter = 0
    var isFinished = false
    while (!isFinished) {
      val s = new StreamItem
      try {
        
        s.read(protocol)
        val doc = makeDoc(s, si_counter)

        writer.addDocument(doc)

        si_counter += 1
      } 
      catch {
        case e:java.lang.OutOfMemoryError => logError("OOM Error: %s".format(e.getStackTrace.mkString("\n"))); isFinished = true
        case e:TTransportException => e.getType match { 
          case TTransportException.END_OF_FILE => logDebug("mkstream Finished."); isFinished = true
          case TTransportException.ALREADY_OPEN => logError("mkstream already opened."); isFinished = true
          case TTransportException.NOT_OPEN => logError("mkstream not open."); isFinished = true
          case TTransportException.TIMED_OUT => logError("mkstream timed out."); isFinished = true
          case TTransportException.UNKNOWN => logError("mkstream unknown."); isFinished = true
          case e => logError("Error in mkStreamItem: %s".format(e.toString)); isFinished = true
        }
        case e: Exception => logDebug("Error in mkStreamItem"); isFinished = true
      }
    }

    // End the writer
    writer.commit
    writer.close


  }
}


class Indexer extends Logging {

}
