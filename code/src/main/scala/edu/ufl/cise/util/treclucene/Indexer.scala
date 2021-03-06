package edu.ufl.cise.util.treclucene

import java.io.ByteArrayOutputStream 
import java.io.ByteArrayInputStream 
import java.io.File
import java.util.concurrent.TimeUnit

import scala.actors.Actor
import scala.collection.JavaConversions._
import scala.sys.process._
import scala.sys.process.ProcessLogger

import edu.ufl.cise.Logging

import streamcorpus.Sentence
import streamcorpus.StreamItem
import streamcorpus.Token

import com.google.common.base.Stopwatch

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.lucene.analysis.core.SimpleAnalyzer
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


/**
  * Example usage:
  *  time find /media/sd{d,e}/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/????-??-??-?? -type d | parallel -j5 --results /media/sdc/lucenelog -u --progress --files "java -Done-jar.verbose=false -Done-jar.silent=true -jar /home/cgrant/projects/gatordsr/code/target/scala-2.9.2/gatordsr_2.9.2-0.01-one-jar.jar {}"
  */



object Indexer extends Logging {
  val decrypt_file = "gpg --no-permission-warning --trust-model always --output - --decrypt %s"

  /** Use linux 'stat' to get a file size */
  def getFileSize(fileName:String):Int = {
    (("stat -c%s %s".format("%s",fileName)).!!).trim.toInt
  }


  def main(args: Array[String]) {
    if (args.length < 1 || args.length > 2) {
      //println("Usage: run /media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-01-01-00/social-256-44919e3ea5b47f8b947974af769214b6-3a661cfbb2ef6b72090b3f441e31eefb.sc.xz.gpg")
      println("Usage: run /media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-01-01-00/")
      System.exit(1)
    }

    //logInfo("param1: %s".format(args(0)))

    val indexer = new Indexer(args(0)) // TODO decide where to puf all these index files, probably same type of hierarchy

    //indexer.start()
    indexer.act
    
  }
}

/** Index a directory of gpg files */
class Indexer(val gpgdir:String, val indexdir:String = "/media/sdc/kbaindex/")  extends Logging /*with Actor*/ {
  
  // TODO Use a custom analyzer!!!
  val analyzer = new StandardAnalyzer(Version.LUCENE_43)


  // Lucene fields 
  val si_docid = new StringField("si_docid", "", Field.Store.YES);
  val int_field = new IntField("si_index", -1, Field.Store.YES);
  val gpg_field = new StringField("gpgfile", "", Field.Store.YES);
  val bdy_field = new TextField("clean_visible", "", Field.Store.YES);
  lazy val doc = { 
    val d = new Document
    d.add(int_field);
    d.add(si_docid);
    d.add(gpg_field);
    d.add(bdy_field);
    d
  }

  val newindexdir = "%s%s".format(indexdir,gpgdir)

  /** This is a fake actor*/
  def act () {
   val toc = new Stopwatch 
   toc.start

    // If the index directory does not exist, create it
    if (!new File(newindexdir).exists) {
      new File(newindexdir).mkdir // Make the directory
    }
  
    // Directory for the index 
    // XXX Check if I can use NIOFSDirectory with an actor
    val directory = new NIOFSDirectory(new File(newindexdir))
    val iwc = new IndexWriterConfig(Version.LUCENE_43, analyzer)
    val writer = new IndexWriter(directory, iwc)

    // Warmup
    toc.stop
    logInfo("Warmup|%s|%s|nanoseconds".format(directory, toc.elapsed(TimeUnit.NANOSECONDS)))
    toc.start // File Timer

    val file_clock = new Stopwatch

    // Get all the gpg files in the gpgdirectory
    var file_counter = 0
    var total_size = 0
    for (gpgFile <- new File(gpgdir).listFiles.withFilter(_.getName.endsWith(".gpg"))) {
      file_clock.reset
      file_clock.start

      // Set the new file name for the index
      gpg_field.setStringValue(gpgFile.getPath)
    
      // Create decryption file
      val decrypt_cmd = Indexer.decrypt_file.format(gpgFile.getPath)

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
          int_field.setIntValue(si_counter)
          si_docid.setStringValue(s.doc_id)
          bdy_field.setStringValue(s.body.getClean_visible)

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
      // End stopwatch
      file_clock.stop
      val sz =  Indexer.getFileSize(gpgFile.getPath)
      logInfo("GPGFile|%s|%d|%s|nanoseconds".format(gpgFile.getPath, sz, file_clock.elapsed(TimeUnit.NANOSECONDS)))
      file_counter += 1
      total_size += sz
    }
    
    // End the writer
    writer.commit
    writer.close

    toc.stop
    logInfo("TotalDir|%s|%d|%d|bytes|%s|nanoseconds".format(gpgdir, file_counter, total_size, toc.elapsed(TimeUnit.NANOSECONDS)))


  }
}
