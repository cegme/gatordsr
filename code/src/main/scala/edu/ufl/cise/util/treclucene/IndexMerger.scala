package edu.ufl.cise.util.treclucene

import scala.collection.JavaConversions._

import java.io.File

import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer 
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.IntField
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.store.Directory
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.store.SimpleFSDirectory
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.util.Version
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TIOStreamTransport
import org.apache.thrift.transport.TTransportException

import edu.ufl.cise.Logging




object IndexMerger extends Logging {


  val OLD_INDEXES = "resources/lucene/indexdirs.txt"
  val INDEX_DIR = "/media/sdc/optimizedindex"
  
  /** Command to find all index directories */
  val cmd = """find /media/sdc/kbaindex/media/sd{d,e}/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/????-??-??-??/ -type d """


  def main (args: Array[String]) {
    
    // Get all the index files 
    val analyzer = new StandardAnalyzer(Version.LUCENE_43)

    logInfo("Fetching indexdirs to merge...")
    val dirs = io.Source.fromFile(OLD_INDEXES).getLines.map(dirPath => new SimpleFSDirectory(new File(dirPath))).toList
    val dirCount = dirs.size * 1.0
    logInfo("Merging dirs: %f".format(dirCount))

    val directory = new NIOFSDirectory(new File(INDEX_DIR))
    val iwc = new IndexWriterConfig(Version.LUCENE_43, analyzer)
    iwc.setRAMBufferSizeMB(30000)
    
    val writer = new IndexWriter(directory, iwc)

    logInfo("Merging Added Indexes...\n ")
    for ((dir,i) <- dirs.zipWithIndex) {
      try {
        writer.addIndexes(dir)
      }
      catch {
        case e: org.apache.lucene.index.IndexNotFoundException => e.printStackTrace
      }
      //logInfo("%d| %s".format(i,dir))
      System.err.print("\r%f percent complete".format(i/dirCount))
    }
    //writer.addIndexes(dirs)
    //writer.addIndexes(java.util.Arrays.asList(dirs).toArray)
    logInfo("now waiting for merges...")
    writer.waitForMerges()
    logInfo("\nComplete!")

    logInfo("Optimizing Index with forceMerge(1)")
    writer.forceMerge(1)
    logInfo("Optimized!")

    logInfo("Commiting and closing the index.")
    // Close and wait for the merge to complete
    writer.commit()
    writer.close(true)
    logInfo("Done.")
  }


}
