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
      //println(eName)
      val url = new URL(
        "http://en.wikipedia.org/w/api.php?action=query&list=backlinks&bltitle=" + eName + "&blfilterredir=redirects&bllimit=max&format=json");
      val is = url.openStream(); // throws an IOException

      val br = new BufferedReader(new InputStreamReader(is));
      var line = "";
      while (!finished) {
        line = br.readLine();
        if (line == null)
          finished = true;
        else
          pageLines += line;
        //println(line)
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
        //  println(alias)
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

      // println(aliasList)

      Searcher.searchEntity(aliasList)

      //   println("---------------------------------")

    })

    //    entities.foreach(e => {
    //      // println("Hi")
    //      var finished = false;
    //
    //      val pageLines = new ListBuffer[String]();
    //      val url = new URL(e.topic_id)
    //      // "http://en.wikipedia.org/w/api.php?action=query&list=backlinks&bltitle=Boris_Berezovsky_%28businessman%29&blfilterredir=redirects&bllimit=max&format=json");
    //      val is = url.openStream(); // throws an IOException
    //
    //      val br = new BufferedReader(new InputStreamReader(is));
    //      var line = "";
    //      while (!finished) {
    //        line = br.readLine();
    //
    //        if (line == null)
    //          finished = true;
    //        else
    //          pageLines += line;
    //
    //      }
    //      br.close();
    //      is.close();
    //
    //      val jsonStr = pageLines.apply(0)
    //
    //      val json = JSON.parseFull(jsonStr)
    //      //    println(json)
    //
    //      val map: Map[String, Any] = json.get.asInstanceOf[Map[String, Any]]
    //      val query = map.get("query").get.asInstanceOf[Map[String, Any]]
    //      //   println(query)
    //      val backlinks = query.get("backlinks").get.asInstanceOf[List[Any]]
    //      // println(backlinks)
    //      //    val entities: List[Any] = map.get("query").get.asInstanceOf[List[Any]]
    //      backlinks.foreach(target => {
    //        val entity: Map[String, Any] = target.asInstanceOf[Map[String, Any]]
    //        val alias = (entity.get("title").get.asInstanceOf[String])
    //        println(alias)
    //        ////      val enType = entity.get("entity_type").asInstanceOf[Some[Any]].get.toString
    //        ////      val enGroup = entity.get("group").asInstanceOf[Some[Any]].get.toString
    //        ////      val enTargetId = entity.get("target_id").asInstanceOf[Some[Any]].get.toString
    //        //
    //      })
    //    })

    //	for (String tempLine : pageLines) {
  }

}