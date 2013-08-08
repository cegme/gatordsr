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

object WikiAPI {

  def main(args: Array[String]): Unit = {

    val entity_list = new ArrayList[Entity]
    Preprocessor.initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json", entity_list)
    val entities = entity_list.toArray(Array[Entity]())

    entities.foreach(e => {
      //  println(e.topic_id)
      var finished = false;

      val pageLines = new ListBuffer[String]();
      val eName = e.topic_id.substring(e.topic_id.lastIndexOf('/') + 1)
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

      backlinks.foreach(target => {
        val entity: Map[String, Any] = target.asInstanceOf[Map[String, Any]]
        val alias = (entity.get("title").get.asInstanceOf[String])
        aliasList.add(alias)
        aliasList.add(alias.replaceAll("([a-z])([A-Z])", "$1 $2"))

      })

      aliasList.add(eName.replace('_', ' '))
      aliasList.add(eName.replaceAll("([a-z])([A-Z])", "$1 $2"))

      //  NameOrderGenerator.
      val size = aliasList.size()
      for (a <- 0 to size) {
        aliasList.addAll(NameOrderGenerator.namePermutation(aliasList.get(a)))
      }

      if (eName.contains("wikipedia"))
        e.names.clear()
      removeDuplicate(aliasList)
      e.names.addAll(aliasList)
      //  println(generate(aliasList))

      Searcher.searchEntity(e.topic_id, aliasList)
    })

    val p = new PrintWriter(new File("./resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08-wiki-alias.json"))
    val json = generate(entities)
    p.print(json)
    p.close()
    //  println(json)
  }

  def removeDuplicate(arlList: ArrayList[String]) {
    val h = new HashSet(arlList);
    arlList.clear();
    arlList.addAll(h);
  }

}