package edu.ufl.cise.pipeline



import edu.ufl.cise.Logging
import streamcorpus.StreamItem
import streamcorpus.Sentence
import java.util.ArrayList
import edu.ufl.cise.KBAOutput
import edu.ufl.cise.RemoteGPGRetrieval
import streamcorpus.Token
import java.lang.Integer
import scala.io.Source
import java.io.PrintWriter
import org.apache.thrift.protocol.TProtocol
import streamcorpus.OffsetType
import com.google.common.base.Stopwatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.{MILLISECONDS, NANOSECONDS, SECONDS}
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import java.io.FileInputStream
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.postag.POSModel
import opennlp.tools.chunker.ChunkerME
import opennlp.tools.chunker.ChunkerModel
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.TokenNameFinderModel


object Pipeline extends Logging {

  /** This keeps track of how many times run is called. */
  val num = new java.util.concurrent.atomic.AtomicInteger

  // load entities and patterns from files
  // kba entities
  val entity_list = new ArrayList[Entity]
  Preprocessor.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08-wiki-alias.json", entity_list)
  lazy val entities = entity_list.toArray(Array[Entity]())
  
  // patterns
  val pattern_list = new ArrayList[Pattern]
  Preprocessor.initPatternList("resources/test/pattern.txt", pattern_list)
  lazy val patterns = pattern_list.toArray(Array[Pattern]())

  // stop list of names
  val stop_list = new ArrayList[String]
  Preprocessor.initStopList("resources/test/stop_list", stop_list)
  lazy val stops = stop_list.toArray(Array[String]())
  
  // np patterns
  val titles = "is | was | be | been"
  val causeOfDeath = "died of | pass away of"
  val awardsWon = "awarded | honored | award | prize | honor"
    
    
  logInfo("entities and patterns are loaded")
  
  
  val tokenizer = new TokenizerME(new TokenizerModel(this.getClass().getClassLoader().getResourceAsStream("en-token.bin")))
  val tagger = new POSTaggerME(new POSModel(this.getClass().getClassLoader().getResourceAsStream("en-pos-maxent.bin")))
  val chunker = new ChunkerME(new ChunkerModel(this.getClass().getClassLoader().getResourceAsStream("en-chunker.bin")))
  val finder = new NameFinderME(new TokenNameFinderModel(new FileInputStream("resources/en-ner-person.bin")))
  
  logInfo("opennlp tokenizer, pos tagger, chunker, finder are loaded")
  
  // preprocessing, to generate indexes for sentences from indexes for stream items
  //SimpleJob.filterSentences(3000) // FIXME SLOOOWWW


  //logInfo("start to generate results")
  // the main logic, used to generate KBA outputs

  def main(args : Array[String]){
    //println(testNP("Bronfman was unable to bid higher than Universal"))
    //println(testNP("Corbato was born on July 1, 1926, in Oakland"))
    
    //val array = Array(1, 2)
    //println(array.slice(0, 0).mkString(" ") + "a")
    // args(0) -- input file
    // args(1) -- output prefix name

    //annotate()
    
    //modified with Chris and Milenko when Yang was in China to include coreference 
    if(args.size > 1)
      KBAOutput.outputPrefix = args(1)
//    if(args.size > 2)
//      SimpleJob.filterUsingStreamFiles(args(0))
    SimpleJob.filterSentencesCoref(args(0))//,args(1)) 
}

  def annotate(sentence: streamcorpus.Sentence, sentenceStr: String, targetIndex: Int, le: LingEntity) = {
    // get the token array of that sentence
    val tokens = sentence.getTokens().toArray(Array[Token]())
    val array = sentenceStr.split(", ")
    val entity_list = SimpleJob.extractEntities(sentence)
    val target = entities(targetIndex)

    val index = getCorresEntity(target, entity_list, le)
    if (index != -1) { // when finding the target index in the list of Ling Entities, try to match the patterns in that sentence
      // start to try to find all the patterns fit for that entity
      val entity = entity_list.get(index)
      closePatternMatch(entity, index, tokens, entity_list, array)
      
      // pattern matching with NP
      // val s = SimpleJob.transform(tokens)
      // extractNPList(s)
      patternMatchNP(entity, index, tokens, entity_list, array)
    }
   
  }

  // find the possible results by looking at two nearest entities
  def closePatternMatch(entity : LingEntity, index : Integer, 
    tokens : Array[Token], entities : ArrayList[LingEntity], array:Array[String]){
    // the first entity
    if(index == 0 && index != entities.size() -1){
      //println("As the first entity")
      val target = entities.get(index + 1)
      // match right nearest patterns         
      getKBAOutput(entity, target, tokens, 1, array)
    }      
    // the last entity
    if(index != 0 && index == entities.size() - 1){
      //println("As the last entity")
      val target = entities.get(index - 1)
      // match right nearest patterns
      getKBAOutput(entity, target, tokens, 0, array)
    }
    // the entity in the middle
    if(index != 0 && index != entities.size() -1 ){         
      //println("entity in the middle")
      val target1 = entities.get(index + 1)
      // match right nearest patterns
      getKBAOutput(entity, target1, tokens, 1, array)

      val target0 = entities.get(index - 1)
      // match right nearest patterns
      getKBAOutput(entity, target0, tokens, 0, array)      
    }        
  }   

  // for the entity and the target, judge whether there is a pattern matched, if so, generating results.
  def getKBAOutput(entity:LingEntity, target:LingEntity, tokens : Array[Token], direction : Integer, array: Array[String]){
    if (direction == 0){
      // find the patterns that fit with the two entities
      val pats = findClosePattern(entity, target, "left")
      //println("patterns: " + pats)
      val s = SimpleJob.transform(tokens.slice(target.end + 1, entity.begin))
      //println("To be matched: " + s)
      pats.toArray(Array[Pattern]()).foreach(pattern => {
          // match each pattern here
          if (s.toLowerCase().contains(pattern.pattern)){
            // match, create KBA Output
            val comment = "# " + target.content + " " + s + " " + entity.content + " --- " + SimpleJob.transform(tokens)
            KBAOutput.add(array(6), entity.topic_id, 1000, array(0), pattern.slot, target.equiv_id, getByteRange(target, tokens), comment)
          }
        })
  
    }
    else{
      // find the patterns that fit with the two entities
      val pats = findClosePattern(entity, target, "right")
      //println("patterns: " + pats)
      val s = SimpleJob.transform(tokens.slice(entity.end + 1, target.begin))
      //println(s + ": to be matched")
      pats.toArray(Array[Pattern]()).foreach(pattern => {
          // match each pattern here
          if (s.toLowerCase().contains(pattern.pattern)){
            // match, create KBA Output
            val comment = "# " + entity.content + " " + s + " " + target.content + " --- " + SimpleJob.transform(tokens)
            KBAOutput.add(array(6), entity.topic_id, 1000, array(0), pattern.slot, target.equiv_id, getByteRange(target, tokens), comment)
          }
        })
      // generate these results for the DateOfDeath
      if (entity.entity_type.equals("PER") && target.entity_type.equals("DATE") && entity.end + 1 == target.begin){
         val comment = "# " + entity.content + " " + s + " " + target.content + " --- " + SimpleJob.transform(tokens)
         KBAOutput.add(array(6), entity.topic_id, 1000, array(0), "DateOfDeath", target.equiv_id, getByteRange(target, tokens), comment)
      }
      
      if (entity.entity_type.equals("PER") && s.toLowerCase().contains("award")){
        val comment = "# " + entity.content + " " + s + " " + target.content + " --- " + SimpleJob.transform(tokens)
        KBAOutput.add(array(6), entity.topic_id, 1000, array(0), "AwardsWon", target.equiv_id, getByteRange(target, tokens), comment)
      }
      
    }
  }

  // get the byte range of one LingPipe Entity
  def getByteRange(target : LingEntity, tokens:Array[Token]) : String = {
    val first = tokens(target.begin).getOffsets().get(OffsetType.findByValue(1)).first
    val last = tokens(target.end).getOffsets().get(OffsetType.findByValue(1)).first + 
    tokens(target.end).getOffsets().get(OffsetType.findByValue(1)).length - 1
    first + "-" + last
  }

  // find the corresponding patterns for two entities
  def findClosePattern(entity : LingEntity, target : LingEntity, direction : String) = {
    val pats = new ArrayList[Pattern]
    //TODO: find the corresponding patterns that fits the entity
    patterns.foreach(pattern => {
        if (pattern.entity_type.equals(entity.entity_type) && 
          target.entity_type.equals(pattern.target_type) && pattern.direction.equals(direction))
        pats.add(pattern)
      })
    pats
  }

  // get the corresponding entity for the name that is matched in the sentence
  def getCorresEntity(target : Entity, entity_list: ArrayList[LingEntity], le : LingEntity) = {
    var index = -1
    for(i <- 0 until entity_list.size()){
      val entity = entity_list.get(i)
      if (entity.equiv_id == le.equiv_id){
        index = i
        entity.topic_id = target.target_id
        entity.group = target.group
      }
    }
    index
  }
  
  def patternMatchNP(entity : LingEntity, index : Integer, 
    tokens : Array[Token], entities : ArrayList[LingEntity], array:Array[String]){
    // the right noun phrase list
    val list = extractNPList(tokens.slice(entity.end + 1, tokens.size))    
    // the left noun phrase list
    val left_list = extractNPList(tokens.slice(0, entity.begin))
    
    //println(list)
    if (list.size() > 0){
      val np = list.get(0)
      val text = SimpleJob.transform(tokens.slice(entity.end + 1, entity.end + np.begin + 1))
      
      if(entity.entity_type.equals("PER"))
      {
        //match titles
        titles.split(" \\| ").foreach(title => {
          if (text.contains(title) && finder.find(tokenizer.tokenize(np.content)).isEmpty) {
            // TODO: output the result
            val comment = "# " + entity.content + " " + text + " " + np.content + " --- " + SimpleJob.transform(tokens)
            val byte_range = getByteRangeNP(tokens, entity.end + np.begin + 1, entity.end + np.end + 1)
            KBAOutput.add(array(6), entity.topic_id, 1000, array(0), "Titles", tokens(entity.end + np.begin + 1).equiv_id, byte_range, comment)
            //println(array(6), entity.topic_id, 1000, array(0), "Titles", tokens(entity.end + np.begin + 1).equiv_id, byte_range, comment)
          }
        })
        
        if ((text.matches(", ") || text.matches(",") || text.matches(" , ") || text.matches(", ")) && finder.find(tokenizer.tokenize(np.content)).isEmpty){
          val comment = "# " + entity.content + " " + text + " " + np.content + " --- " + SimpleJob.transform(tokens)
          //var end = entity.end + np.end + 1; if (end >= tokens.size) end = tokens.size - 1
          val byte_range = getByteRangeNP(tokens, entity.end + np.begin + 1, entity.end + np.end + 1)
          KBAOutput.add(array(6), entity.topic_id, 1000, array(0), "Titles", tokens(entity.end + np.begin + 1).equiv_id, byte_range, comment)
        }
        
        
        // match causeOfDeath
        causeOfDeath.split(" \\| ").foreach(cause => {
          if (text.contains(cause) && finder.find(tokenizer.tokenize(np.content)).isEmpty) {
            // TODO: output the result
            val comment = "# " + entity.content + " " + text + " " + np.content + " --- " + SimpleJob.transform(tokens)
            //var end = entity.end + np.end + 1; if (end >= tokens.size) end = tokens.size - 1
            val byte_range = getByteRangeNP(tokens, entity.end + np.begin + 1, entity.end + np.end + 1)
            KBAOutput.add(array(6), entity.topic_id, 1000, array(0), "CauseOfDeath", tokens(entity.end + np.begin + 1).equiv_id, byte_range, comment)
            //println(array(6), entity.topic_id, 1000, array(0), "CauseOfDeath", tokens(entity.end + np.begin + 1).equiv_id, byte_range, comment)
          }
        })
        
        // match awardsWon
        awardsWon.split(" \\| ").foreach(award => {
          if (text.contains(award) && finder.find(tokenizer.tokenize(np.content)).isEmpty) {
            val comment = "# " + entity.content + " " + text + " " + np.content + " --- " + SimpleJob.transform(tokens)
            //var end = entity.end + np.end + 1; if (end >= tokens.size) end = tokens.size - 1
            val byte_range = getByteRangeNP(tokens, entity.end + np.begin + 1, entity.end + np.end + 1)
            KBAOutput.add(array(6), entity.topic_id, 1000, array(0), "AwardsWon", tokens(entity.end + np.begin + 1).equiv_id, byte_range, comment)
          }
        })

        // generate samples
        val comment = "# " + entity.content + " " + text + " " + np.content + " --- " + SimpleJob.transform(tokens)
        var end = entity.end + np.end + 1; if (end >= tokens.size) end = tokens.size -1
        val byte_range = getByteRangeNP(tokens, entity.end + np.begin + 1, entity.end + np.end + 1)
        KBAOutput.add(array(6), entity.topic_id, 1000, array(0), "Samples", tokens(entity.end + np.begin + 1).equiv_id, byte_range, comment)
      }
     }

    if (left_list.size() > 0) {
      val np = left_list.get(left_list.size() - 1)
      val text = SimpleJob.transform(tokens.slice(np.end + 1, entity.begin))
      if (entity.entity_type.equals("PER")) {
        // match awardsWon
        awardsWon.split(" \\| ").foreach(award => {
          if (text.contains(award) && finder.find(tokenizer.tokenize(np.content)).isEmpty) {
            val comment = "# " + np.content + " " + text + " " + entity.content + " --- " + SimpleJob.transform(tokens)
            //var end = np.end; if (np.end >= tokens.size) end = tokens.size - 1
            val byte_range = getByteRangeNP(tokens, np.begin, np.end)
            KBAOutput.add(array(6), entity.topic_id, 1000, array(0), "AwardsWon", tokens(np.begin).equiv_id, byte_range, comment)
          }
        })
        
        // match titles
        if (text.matches(" ") && finder.find(tokenizer.tokenize(np.content)).isEmpty){
          val comment = "# " + np.content + " " + text + " " + entity.content + " --- " + SimpleJob.transform(tokens)
          //var end = np.end; if (np.end >= entity.begin) end = entity.begin - 1
          val byte_range = getByteRangeNP(tokens, np.begin, np.end)
          KBAOutput.add(array(6), entity.topic_id, 1000, array(0), "Titles", tokens(np.begin).equiv_id, byte_range, comment)
        }
      }
      // generate samples
      val comment = "# " + np.content + " " + text + " " + entity.content + " --- " + SimpleJob.transform(tokens)
      //var end = np.end; if (np.end >= entity.begin) end = entity.begin - 1
      //println(np.begin + " " + np.end + " " + tokens.size)
      val byte_range = getByteRangeNP(tokens, np.begin, np.end)
      KBAOutput.add(array(6), entity.topic_id, 1000, array(0), "Samples", tokens(np.begin).equiv_id, byte_range, comment)
    }
    
  }
  
  def getByteRangeNP(tokens : Array[Token], begin : Integer, end: Integer) = {
    val first = tokens(begin).getOffsets().get(OffsetType.findByValue(1)).first
    val last = tokens(end).getOffsets().get(OffsetType.findByValue(1)).first + 
    tokens(end).getOffsets().get(OffsetType.findByValue(1)).length -1
    first + "-" + last    
  }

  // extract the NP list in the sentence
  def extractNPList(ts: Array[Token]) = {
    // using chunking to extract NP from a tail string of the orignal string after the matching point   
    val tokens = getTokens(ts)
    val pos = tagger.tag(tokens)
    val tags = chunker.chunk(tokens, pos).toArray

    val list = new ArrayList[NPEntity]
    // generate entities 
    var i = 0
    while (i < tags.size) {
      if (tags(i).equals("B-NP")) {
        val entity = new NPEntity(i)
        var j = i + 1
        while (j < tags.size && tags(j).equals("I-NP"))
          j = j + 1
        entity.end = j - 1
        entity.content = tokens.slice(i, j).mkString(" ")
        list.add(entity)
        // move i to position j
        i = j
      } else i = i + 1
    }
    list
  }
  
  def getTokens(tokens : Array[Token]) = {
    val list = new ArrayList[String]
    tokens.foreach(token => list.add(token.getToken()))
    list.toArray(Array[String]())
  }
  
  
  def testNP(s : String) = {
    val tokens = tokenizer.tokenize(s)
    val ner = finder.find(tokens)
    println(ner)
    val pos = tagger.tag(tokens)
    val tags = chunker.chunk(tokens, pos).toArray

    val list = new ArrayList[NPEntity]
    // generate entities 
    var i = 0
    while (i < tags.size) {
      if (tags(i).equals("B-NP")) {
        val entity = new NPEntity(i)
        var j = i + 1
        while (j < tags.size && tags(j).equals("I-NP"))
          j = j + 1
        entity.end = j - 1
        entity.content = tokens.slice(i, j).mkString(" ")
        list.add(entity)
        // move i to position j
        i = j
      } else i = i + 1
    }
    list
  }
  
  
  
}

class Pipeline() extends Logging with Serializable {



  def transform(tokens:Array[Token]):String = {
    var sb = new java.lang.StringBuilder
    tokens.foreach(token => {
        sb.append(token.token).append(" ")
      })
    //println(sb)
    sb.toString().toLowerCase()
  }


  def run(si:StreamItem) {
    Pipeline.num.incrementAndGet
    // for each sentence, match the pattern
    // TODO: get the sentence string
    si.body.sentences.get("lingpipe").toArray(Array[streamcorpus.Sentence]()).foreach(sentence => {
        //println(sentence.getTokens().toArray().mkString(" "))
        val tokens = sentence.getTokens().toArray(Array[Token]())

      })
  }
}
