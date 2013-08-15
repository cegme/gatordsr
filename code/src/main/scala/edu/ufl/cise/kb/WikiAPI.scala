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


/**
 * Preload entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json then populate
 * trec-kba-ccr-and-ssf-query-topics-2013-04-08-wiki-alias.json
 */
object WikiAPI {

  def main(args: Array[String]): Unit = {

    inlineAliasGenerator();
    // redirectAliasGenerator();
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
      aliasList.add(URLDecoder.decode(eName, "UTF-8"))

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

      //  NameOrderGenerator.
      val size = aliasList.size()
      for (a <- 0 to size) {
        aliasList.addAll(NameOrderGenerator.namePermutation(aliasList.get(a)))
      }

      if (e.target_id.contains("wikipedia"))
        e.alias.clear()
      removeDuplicate(aliasList)
      e.alias.addAll(aliasList)
      //  println(generate(e.alias))

      Searcher.searchEntity(e.target_id, aliasList)
    })

    val p = new PrintWriter(new File("./resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08-wiki-alias.json"))
    val json = generate(kbaJson)
    p.print(json)
    p.close()
    //  println(json)

  }

  def removeDuplicate(arlList: ArrayList[String]) {
    val h = new HashSet(arlList);
    arlList.clear();
    arlList.addAll(h);
  }

  def inlineAliasGenerator() {
    val pageLines = new ArrayList[String]();

    val url = new URL("http://en.wikipedia.org/wiki/IDSIA")
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

    println(documentStr)

    val htmlCleaner = new HtmlCleaner();
    val root = htmlCleaner.clean(documentStr);
    val boldAlias = root.evaluateXPath("//*[@id=\"mw-content-text\"]/p[1]/b");
    if (boldAlias.length > 0) {
      val f0 = boldAlias.apply(0)
      val b = f0.isInstanceOf[TagNode];
      val tagNode = f0.asInstanceOf[TagNode]
      println(tagNode.getText())
     // if (b) {
     //   tagNode.removeFromTree();
     // }
    }
    
    /////////////////////////////////////////////////
    /////////////////////////////////////////////////
    /////////////////////////////////////////////////
    
    val firstParagraph = root.evaluateXPath("//*[@id=\"mw-content-text\"]/p[1]");
    if (firstParagraph.length > 0) {
      val f0 = firstParagraph.apply(0)
      val b = f0.isInstanceOf[TagNode];
      val tagNode = f0.asInstanceOf[TagNode]
      println(tagNode.getText())
    
    
    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
    val props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
    val pipeline = new StanfordCoreNLP(props);
    
    // read some text in the text variable
    val text = tagNode.getText() // Add your text here!
    
    // create an empty Annotation just with the given text
    val document = new Annotation(text+"");
    
    // run all Annotators on this text
    pipeline.annotate(document);
    
    // these are all the sentences in this document
    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
    val sentences = document.get(classOf[CoreAnnotations.SentencesAnnotation]);
    
    sentences.toList.foreach(s => {
      println(s)
      val tokens  = s.get(classOf[CoreAnnotations.TokensAnnotation])
      
      tokens.toList.foreach(t =>{println(t.lemma())})
      })
    
    
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////
    /////////////////////////////////////////////////
    /////////////////////////////////////////////////
    
    documentStr.replaceAll("<script[^>]*>[^<]*</script>", "")
    documentStr = documentStr.replaceAll("<[^>]*>", "")
    documentStr = documentStr.replaceAll("</[^>]*>", "")
    println(documentStr)
  }

}