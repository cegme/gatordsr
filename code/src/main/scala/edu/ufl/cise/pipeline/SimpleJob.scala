package edu.ufl.cise.pipeline

import edu.ufl.cise.RemoteGPGRetrieval
import java.io.PrintWriter
import scala.io.Source
import scala.util.parsing.json.JSON
import edu.ufl.cise.Logging
import java.util.ArrayList
import streamcorpus.StreamItem
import streamcorpus.Token
import java.io.FileOutputStream
import java.io.File
import java.lang.Integer
import streamcorpus.Sentence

import scala.collection.mutable.WeakHashMap
import scala.ref.WeakReference
import java.util.LinkedList

object SimpleJob extends Logging {
  // method library
  def main(args: Array[String]) {
  }

  def getPOS(tokens: Array[Token]): String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
      sb.append(token.pos).append(" ")
    })
    sb.toString()
  }

  def getNER(tokens: Array[Token]): String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
      sb.append(token.entity_type).append(" ")
    })
    //println(sb)
    sb.toString()
  }

  def getMentionID(tokens: Array[Token]): String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
      sb.append(token.mention_id).append(" ")
    })
    //println(sb)
    sb.toString()
  }
  def getEquivID(tokens: Array[Token]): String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
      sb.append(token.equiv_id).append(" ")
    })
    //println(sb)
    sb.toString()
  }

  def transform(tokens: Array[Token]): String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
      sb.append(token.token).append(" ")
    })
    //println(sb)
    sb.toString()
  }

  def transform2(tokens: Array[Token]): String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
      if (token.getEntity_type() != null)
        sb.append(token.token).append(" ")
    })
    //println(sb)
    sb.toString()
  }

  def getStreamItem(filePath: String) {

  }

  def filterSentences(n: Integer, filePath: String) = {
    // read from a file to get all the streamitems, use the filtered entity to filter
    var num = 1
    //  val pw = new PrintWriter("resources/test/ss.txt")   
    val lines = Source.fromFile(filePath).getLines //.take(100)//.slice(0, n)
    lines.foreach(line => {
      // parse one line to get parameters
      val array = line.split(" \\| ")
      val date_hour = array(0).split(">")(1)
      val filename = array(1)
      val si_num = array(2)
      val topics = line.split("\\|\\| ")(1).split(", ")
      println(num + " " + date_hour + " " + filename + " " + si_num)
      //println(topics.mkString(" "))
      val ens = findEntity(topics)
      //val list = RemoteGPGRetrieval.getStreams(date_hour, filename)
      //val si = list.get(Integer.parseInt(si_num))
      val si = getLocalStreamItem(date_hour, filename, Integer.parseInt(si_num))
      //val si = getLocalStreamItem(date_hour, filename, Integer.parseInt(si_num))
      val ss = si.body.sentences.get("lingpipe")
      // process all the sentences
      for (i <- 0 until ss.size()) {
        val s = SimpleJob.transform2(ss.get(i).getTokens().toArray(Array[Token]()))
        //println(s.toLowerCase())
        //println(s)
        ens.foreach(e => {
          Pipeline.entities(e).names.toArray(Array[String]()).foreach(name => {
            //println(name.toLowerCase())
            if (s.toLowerCase().contains(name.toLowerCase())) { //take to annotate. morteza
              // print line to the file, containing date_hour, filename, si_num, sentence i and entity index e
              //println(s)
              // println(name)
              //println(num + " " + date_hour + " " + filename + " " + si_num + " " + i + " " + e + " " + Pipeline.entities(e).topic_id)

              val tempStr = date_hour + ", " + filename + ", " + si_num + ", " + i + ", " + e + ", " + name + ", " +
                si.stream_id + ", " + Pipeline.entities(e).topic_id
              //Pipeline.annotate(ss.get(i), tempStr, e, name)
              println("Hello! " + i)

              //pw.println(date_hour + ", " + filename + ", " + si_num + ", " + i + ", " + e + ", " + name + ", " +
              //   si.stream_id  + ", " +  Pipeline.entities(e).topic_id)
              //pw.flush()
              // store sentences into files, too             
            }
          })
        })
      }
      num = num + 1
    })
    // improve the performance
    //  pw.close()
    // store the filtered sentences into a file
  }

  def filterSentencesCoref(n: Integer, filePath: String) = {
    var num = 1
    val lines = Source.fromFile(filePath).getLines //.take(100)//.slice(0, n)
    lines.foreach(line => {
      // parse one line to get parameters
      val array = line.split(" \\| ")
      val date_hour = array(0).split(">")(1)
      val filename = array(1)
      val si_num = array(2)
      val topics = line.split("\\|\\| ")(1).split(", ")
      println(num + " " + date_hour + " " + filename + " " + si_num)

      val ens = findEntity(topics)
      val si = getLocalStreamItem(date_hour, filename, Integer.parseInt(si_num))
      val ss = si.body.sentences.get("lingpipe")
      // process all the sentences
      for (i <- 0 until ss.size()) {

        val tokens = ss.get(i).getTokens();
        val sb = new StringBuilder("")
        val list = new LinkedList[LingEntity]()
        var find = false
        for (j <- 0 until tokens.size() if !find) {
          val token = tokens.get(j)
          sb.append(token.token + " ")
          ens.foreach(e => {
            Pipeline.entities(e).names.toArray(Array[String]()).foreach(name => {
              //println(name.toLowerCase())
              if (sb.toString.toLowerCase().contains(name.toLowerCase()) && token != null) {
                val le = new LingEntity(token.entity_type.toString(), token.mention_id, token.equiv_id)
                le.entityIndex = e
                list.add(le)
                find = true
              }
            })
          })
        }

        for (i <- 0 until ss.size()) {

          val tokens = ss.get(i).getTokens();
          for (j <- 0 until tokens.size()) {
            for (k <- 0 until list.size()) {
              val token = tokens.get(j)
              val lingentity = list.get(k)
              if (token.equiv_id == lingentity.equiv_id) {
                val e = lingentity.entityIndex
                val tempStr = date_hour + ", " + filename + ", " + si_num + ", " + i + ", " + lingentity.entityIndex + ", NA, " +
                  si.stream_id + ", " + Pipeline.entities(e).topic_id
                Pipeline.annotate(ss.get(i), tempStr, e, lingentity)
              }
            }
          }
        }
      }
    })
  }

  // find corresponding entities by name
  def findEntity(topics: Array[String]) = {
    // get the indexes of the entities contained in one document
    val ens = new Array[Integer](topics.size)
    var index = 0
    topics.foreach(topic => {
      for (i <- 0 until Pipeline.entities.size) {
        if (Pipeline.entities(i).topic_id.toLowerCase().equals(topic.toLowerCase())) {
          ens(index) = i
          index = index + 1
        }
      }
    })
    ens
  }

  // from sentences create entities
  def filterEntities(entities: Array[Entity]) = {
    val pw = new PrintWriter("resources/test/ee.txt")
    val lines = Source.fromFile("resources/test/ss.txt").getLines()
    lines.foreach(line => {
      val array = line.split(" ")
      val sentence = getLocalSentence(array(0), array(1), Integer.parseInt(array(2)), Integer.parseInt(array(3)))
      //val sentence = getLocalSentence(array(0), array(1), Integer.parseInt(array(2)), Integer.parseInt(array(3)))
      val entity_list = extractEntities(sentence)
      val tokens = sentence.getTokens().toArray(Array[Token]())
      val target = entities(Integer.parseInt(array(4)))
      var index = 0
      pw.print(target.entity_type + "-" + target.group + "---")
      for (i <- 0 until entity_list.size()) {
        val entity = entity_list.get(i)
        if (index < entity.begin) pw.print(SimpleJob.transform(tokens.slice(index, entity.begin)) + "- ")
        if (entity.entity_type.equals(target.entity_type) && entity.content.contains(array(5))) {
          // find the target entity
          pw.print("{" + entity.content + "} - ")
        } else pw.print("[" + entity.content + "] - ")
        index = entity.end + 1
      }
      pw.print("\n")
      pw.flush()
    })
    pw.close()
  }

  // A cached "last accessed files"
  private val fileCache: WeakHashMap[(String, String), LinkedList[StreamItem]] = new WeakHashMap[(String, String), LinkedList[StreamItem]]()
  //private val fileCache:HashMap[(String,String), LinkedList[StreamItem]] = new HashMap[(String,String),LinkedList[StreamItem]]()

  // get the specified stream item
  def getRemoteStreamItem(date_hour: String, filename: String, num: Integer): StreamItem = {

    fileCache.get(date_hour, filename) match {
      case Some(z) => z.get(num)
      case None =>
        logInfo("Cache Miss (rsi) %s".format((date_hour, filename)))
        val newList = RemoteGPGRetrieval.getStreams(date_hour, filename)
        //fileCache.clear
        fileCache.put((date_hour, filename), newList)
        newList.get(num)
    }

  }

  // get the specified sentence
  def getRemoteSentence(date_hour: String, filename: String, num: Integer, sid: Integer): Sentence = {

    fileCache.get(date_hour, filename) match {
      case Some(z) => z.get(num).body.sentences.get("lingpipe").get(sid)
      case None =>
        logInfo("Cache Miss (rs) %s".format((date_hour, filename)))
        val newList = RemoteGPGRetrieval.getStreams(date_hour, filename)
        //fileCache.clear
        fileCache.put((date_hour, filename), newList)
        newList.get(num).body.sentences.get("lingpipe").get(sid)
    }
  }

  def getLocalSentence(date_hour: String, filename: String, num: Integer, sid: Integer): Sentence = {
    fileCache.get(date_hour, filename) match {
      case Some(z) => z.get(num).body.sentences.get("lingpipe").get(sid)
      case None =>
        logInfo("Cache Miss (ls) %s".format((date_hour, filename)))
        val newList = RemoteGPGRetrieval.getLocalStreams(date_hour, filename)
        //fileCache.clear
        fileCache.put((date_hour, filename), newList)
        newList.get(num).body.sentences.get("lingpipe").get(sid)
    }

  }

  // get the specified stream item
  def getLocalStreamItem(date_hour: String, filename: String, num: Integer): StreamItem = {
    fileCache.get(date_hour, filename) match {
      case Some(z) => z.get(num)
      case None =>
        logInfo("Cache Miss (lsi) %s".format((date_hour, filename)))
        val newList = RemoteGPGRetrieval.getLocalStreams(date_hour, filename)
        //fileCache.clear
        fileCache.put((date_hour, filename), newList)
        newList.get(num)
    }
  }

  def extractEntities(sentence: Sentence) = {
    val entity_list = new ArrayList[LingEntity]()
    val tokens = sentence.getTokens().toArray(Array[Token]())
    val flags = new Array[Integer](tokens.size)
    // generate flags for tokens
    for (i <- 0 until tokens.size) {
      val token = tokens(i)
      if (token.entity_type == null || token.equiv_id == -1) flags(i) = -1
      else {
        if ((i != 0) && token.getEntity_type().equals(tokens(i - 1).getEntity_type()) &&
          (token.getMention_id() == tokens(i - 1).getMention_id()) && (token.getEquiv_id() == tokens(i - 1).getEquiv_id()))
          flags(i) = 1
        else
          flags(i) = 0
      }
    }
    // generate entities 
    var i = 0
    while (i < tokens.size) {
      if (flags(i) == 0) {
        val entity = new LingEntity(tokens(i).entity_type.toString(), tokens(i).mention_id, tokens(i).equiv_id)
        entity.begin = i
        var j = i + 1
        while (j < tokens.size && flags(j) == 1)
          j = j + 1
        entity.end = j - 1
        entity.content = tokensToString(tokens.slice(i, j))
        entity_list.add(entity)
        // move i to position j
        i = j
      } else i = i + 1
    }

    entity_list
  }

  def tokensToString(tokens: Array[Token]): String = {
    val sb = new java.lang.StringBuilder
    tokens.foreach(token => {
      sb.append(token.token).append(" ")
    })
    //println(sb)
    sb.toString()
  }
}

