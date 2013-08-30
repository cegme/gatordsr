package edu.ufl.cise.kb

import java.util.LinkedList
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import scala.collection.mutable.ListBuffer
import scala.util.parsing.json.JSON
import edu.ufl.cise.pipeline.Preprocessor
import java.util.ArrayList
import edu.ufl.cise.pipeline.Entity
import edu.cise.ufl.util.treclucene.Searcher
import edu.ufl.cise.util.NameOrderGenerator
import com.codahale.jerkson.Json._
import java.util.HashSet
import java.io.PrintWriter
import java.io.File
import edu.ufl.cise.pipeline.KBAJson
import java.net.URLDecoder
import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode
import scala.util.Properties
import java.util.Properties
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.ling.CoreAnnotations
import scala.collection.JavaConversions._
import java.io.StringReader
import edu.stanford.nlp.process.DocumentPreprocessor
import edu.stanford.nlp.ling.HasWord
import java.util.regex.Pattern

/**
 * Preload entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json then populate
 * trec-kba-ccr-and-ssf-query-topics-2013-04-08-wiki-alias.json
 */
object WikiAPI {

  val htmlCleaner = new HtmlCleaner();
  // creates a StanfordCoreNLP object, with POS tagging, lemmatization
  val props = new Properties();
  props.put("annotators", "tokenize, ssplit, pos, lemma");
  val pipeline = new StanfordCoreNLP(props);

  def main(args: Array[String]): Unit = {

    //   val urlStr = "http://en.wikipedia.org/wiki/Basic_Element_%28company%29"
    //  val inlineAliases = inlineAliasExtractor(urlStr);
    //println(inlineAliases)
    redirectAliasGenerator();
  }

  def redirectAliasGenerator() {

    val entity_list = new ArrayList[Entity]
    Preprocessor.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json", entity_list)

    val kbaJson = new KBAJson(entity_list)

    val entities = entity_list.toArray(Array[Entity]())

    entities.foreach(e => {
      //  println(e.topic_id)
      var finished = false;

      val pageLines = new ListBuffer[String]();
      val eName = e.target_id.substring(e.target_id.lastIndexOf('/') + 1)
      val url = new URL(
        "http://en.wikipedia.org/w/api.php?action=query&list=backlinks&bltitle=" + eName + "&blfilterredir=redirects&bllimit=max&format=json");
      val is = url.openStream();

      val br = new BufferedReader(new InputStreamReader(is));
      var line = "";
      while (!finished) {
        line = br.readLine();
        if (line == null)
          finished = true;
        else
          pageLines += line;
      }
      br.close();
      is.close();
      val jsonStr = pageLines.apply(0)

      val json = JSON.parseFull(jsonStr)

      val map: Map[String, Any] = json.get.asInstanceOf[Map[String, Any]]
      val query = map.get("query").get.asInstanceOf[Map[String, Any]]
      val backlinks = query.get("backlinks").get.asInstanceOf[List[Any]]

      val aliasList = new ArrayList[String]();

      aliasList.add(eName)
      aliasList.add(eName.replaceAll("_", " "))

      aliasList.add(URLDecoder.decode(eName, "UTF-8"))
      aliasList.add(eName.replace("([^)]*)", ""))

      aliasList.add(eName.replace('_', ' '))
      aliasList.add(URLDecoder.decode(eName.replace('_', ' '), "UTF-8"))

      aliasList.add(eName.replaceAll("([a-z])([A-Z])", "$1 $2"))
      aliasList.add(URLDecoder.decode(eName.replaceAll("([a-z])([A-Z])", "$1 $2"), "UTF-8"))

      backlinks.foreach(target => {
        val entity: Map[String, Any] = target.asInstanceOf[Map[String, Any]]
        val alias = (entity.get("title").get.asInstanceOf[String])
        aliasList.add(alias)
        aliasList.add(alias.replaceAll("([a-z])([A-Z])", "$1 $2"))

      })

      try {
        aliasList.addAll(inlineAliasExtractor(e.target_id))
        //aliasList.addAll(edu.ufl.cise.kb.Alias.GetAliases(e.target_id)) // Added here for Yangs code
      } catch {
        case ex: Exception => {
          println(e.target_id + "Missing file exception.")
        }

      }

      //  NameOrderGenerator.
      val size = aliasList.size()
      for (a <- 0 to size) {
        aliasList.addAll(NameOrderGenerator.namePermutation(aliasList.get(a)))
      }

      //update the aliases and search for them in lucene
      if (e.target_id.contains("wikipedia"))
        e.alias.clear()
      e.alias.addAll(aliasList)

      //Skip abbreviated forms.
      val temp = e.alias.filter(x => x.size > 3 && x.split(' ').first.size > 2 && x.split(' ').last.size > 2)
      e.alias.clear()
      e.alias.addAll(temp)
      removeDuplicate(e.alias)
      addWithoutParenthesis(e.alias)
      println(e.alias)
      //      Searcher.searchEntity(e.target_id, e.alias)
    })

    val p = new PrintWriter(new File("./resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08-wiki-alias.json"))
    val json = generate(kbaJson)
    p.print(json)
    p.close()
    //  println(json)

  }
  
  def addWithoutParenthesis(arlList: ArrayList[String]){
   var  temp = new ArrayList[String];
   arlList.foreach(f => {
     if(f.contains("(") && f.contains(")")){
            val expr = f.replaceAll("\\(.+?\\)", "").trim();
            temp.add(expr)
     }
       
     
   })
   arlList.addAll(temp)
    
  }

  def removeDuplicate(arlList: ArrayList[String]) {
    val h = new HashSet(arlList);
    arlList.clear();
    arlList.addAll(h);
  }

  def inlineAliasExtractor(urlStr: String): ArrayList[String] = {

    val aliases = new ArrayList[String]
    val pageLines = new ArrayList[String]();

    // val url = new URL("http://en.wikipedia.org/wiki/IDSIA")
    val url = new URL(urlStr)
    val is = url.openStream(); // throws an IOException

    val br = new BufferedReader(new InputStreamReader(is));
    var documentStr = ""
    var line = "";
    var finished = false;
    while (!finished) {

      line = br.readLine();

      if (line == null)
        finished = true;
      else
        documentStr = documentStr.concat(line);

    }
    br.close();
    is.close();

    // println(documentStr)
    val root = htmlCleaner.clean(documentStr);
    boldExtractor(aliases, root);
    //  regexExtractor(aliases, root);
    /////////////////////////////////////////////////
    /////////////////////////////////////////////////
    /////////////////////////////////////////////////

    //      sentences.toList.foreach(s => {
    //        //    println(s)
    //        // val tokens = s.get(classOf[CoreAnnotations.TokensAnnotation])     
    //        //tokens.toList.foreach(t =>{println(t.lemma())})
    //      })

    // println("---------------------Print sentences of first paragraph---------------------------")
    //      val reader = new StringReader(text.toString());
    //      val dp = new DocumentPreprocessor(reader);
    //
    //      val sentenceList = new ArrayList[String]();
    //      val it = dp.iterator();
    //      while (it.hasNext()) {
    //        val sentenceSb = new StringBuilder();
    //        val sentence: Array[HasWord] = it.next().toArray(Array[HasWord]())
    //    //   println( sentence.deepToString)
    //       
    //        sentence.foreach(token => {
    //          if (sentenceSb.length() > 1) {
    //            sentenceSb.append(" ");
    //          }
    //          sentenceSb.append(token.word());
    //        })
    //
    //        sentenceList.add(sentenceSb.toString());
    //      }
    //      sentenceList.foreach(f => println(f))

    //////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////
    /////////////////////////////////////////////////
    /////////////////////////////////////////////////
    aliases
  }

  def boldExtractor(aliases: ArrayList[String], root: TagNode) {

    val boldAlias = root.evaluateXPath("//*[@id=\"mw-content-text\"]/p[1]/b");
    if (boldAlias.length > 0) {
      /*
       * val f0 = boldAlias.apply(0)
      val b = f0.isInstanceOf[TagNode];
      val tagNode = f0.asInstanceOf[TagNode]
      val boldText = tagNode.getText().toString().trim()
      aliases.add(boldText)
      //   println(boldText)
      // if (b) {
      //   tagNode.removeFromTree();
      // }
       * 
       */

      boldAlias.foreach(f0 => {
        val b = f0.isInstanceOf[TagNode];
        val tagNode = f0.asInstanceOf[TagNode]
        val boldText = tagNode.getText().toString().trim()
        println(boldText)
        aliases.add(boldText)
      });
    }
  }

  def regexExtractor(aliases: ArrayList[String], root: TagNode) {

    val firstParagraph = root.evaluateXPath("//*[@id=\"mw-content-text\"]/p[1]");
    if (firstParagraph.length > 0) {
      val f0 = firstParagraph.apply(0)
      val b = f0.isInstanceOf[TagNode];
      val tagNode = f0.asInstanceOf[TagNode]
      val text = tagNode.getText()
      //  println(text)

      // create an empty Annotation just with the given text
      val document = new Annotation(text + "");

      // run all Annotators on this text
      pipeline.annotate(document);

      // these are all the sentences in this document
      // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
      val sentences = document.get(classOf[CoreAnnotations.SentencesAnnotation]);
      val sentence = sentences.apply(0)
      val firstSentenceStr = sentence.get(classOf[CoreAnnotations.TextAnnotation])
      //   println(firstSentenceStr)
      if (firstSentenceStr.contains('(')) {
        val parenthesisDesc = firstSentenceStr.substring(firstSentenceStr.indexOf('(') + 1, firstSentenceStr.indexOf(')'))
        // println(parenthesisDesc)
        val synArray = parenthesisDesc.split("[,;]")
        val mapped = synArray.map(f => {
          if (f.contains(":"))
            f.substring(f.indexOf(":") + 1).trim()
          else if (f.contains("\""))
            f.substring(f.indexOf("\"") + 1, f.lastIndexOf("\"")).trim()
          else if (f.contains("“"))
            f.substring(f.indexOf("“") + 1, f.lastIndexOf("”")).trim()
          else if (f.contains("also known as"))
            f.substring(f.indexOf("also known as") + 1).trim()
          else if (f.contains("also referred to as"))
            f.substring(f.indexOf("also referred to as") + 1).trim()

          else
            f.trim()
        })

        //   mapped.foreach(println)
        aliases.addAll(mapped.toList)
      }
    }
  }
}
