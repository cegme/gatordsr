package edu.ufl.cise.pipeline

import scala.util.parsing.json.JSON
import java.util.ArrayList
import scala.io.Source
import scala.collection.immutable.List
import java.net.URL

object Preprocessor {
  // TODO: initialize the entity list from entity file containing all the 150 entities
  // TODO: create new entity json files
  // TODO: initialize the slot list from slot files, 13 files
  // TODO: figure out all these file formats
  // TODO: with the entity_list and the slot_list, initialize all the possible patterns and store them into the pattern list
  val entity_list = new ArrayList[Entity]
  val slot_list = new ArrayList[Slot]
  val pattern_list = new ArrayList[Pattern]

  def initEntityList(filename: String) {
    // ($schema,http://trec-kba.org/schemas/v1.1/filter-topics.json)
    val json = JSON.parseFull(Source.fromFile(filename).mkString)
    val map: Map[String, Any] = json.get.asInstanceOf[Map[String, Any]]
    //println(map.iterator.next)
    val entities: List[Any] = map.get("targets").get.asInstanceOf[List[Any]]
    entities.foreach(target => {
      val entity: Map[String, Any] = target.asInstanceOf[Map[String, Any]]
      val alias = (entity.get("alias").get.asInstanceOf[List[String]]).map(s => s.toLowerCase())
      val enType = entity.get("entity_type").asInstanceOf[Some[Any]].get.toString
      val enGroup = entity.get("group").asInstanceOf[Some[Any]].get.toString
      val enTargetId = entity.get("target_id").asInstanceOf[Some[Any]].get.toString

      entity_list.add(new Entity(enType, enGroup, enTargetId, alias))
    })
    // call extractWiki
  }
  
    def initEntityList(filename: String, entity_list:ArrayList[Entity]) {
    // ($schema,http://trec-kba.org/schemas/v1.1/filter-topics.json)
    val json = JSON.parseFull(Source.fromFile(filename).mkString)
    val map: Map[String, Any] = json.get.asInstanceOf[Map[String, Any]]
    //println(map.iterator.next)
    val entities: List[Any] = map.get("targets").get.asInstanceOf[List[Any]]
    entities.foreach(target => {
      val entity: Map[String, Any] = target.asInstanceOf[Map[String, Any]]
      val alias = (entity.get("alias").get.asInstanceOf[List[String]])
      val enType = entity.get("entity_type").asInstanceOf[Some[Any]].get.toString
      val enGroup = entity.get("group").asInstanceOf[Some[Any]].get.toString
      val enTargetId = entity.get("target_id").asInstanceOf[Some[Any]].get.toString

      entity_list.add(new Entity(enType, enGroup, enTargetId, alias))
    })
    // call extractWiki
  }

  def extractWiki() {
    // TODO: use media wiki api to extract alias name information for entities
    // TODO: store all these inforamtion into one json file
    val url = "http://en.wikipedia.org/w/api.php?format=json&action=query&prop=revisions&titles=Benjamin_Bronfman&rvprop=timestamp|user|comment|content&rvend=20130201000000"
    val json = JSON.parseFull(Source.fromURL(new URL(url)).mkString)
    println(json)
    // TODO: find name information and store into json files

  }

  def initPatternList(filename : String, pattern_list : ArrayList[Pattern]){
    // TODO: initialize a pattern list from a file
    val lines = Source.fromFile(filename).getLines()
    lines.foreach(line => {
      val array = line.split(", ")
      pattern_list.add(new Pattern(array(0), array(1), array(2), array(3), array(4)))
    })
  }

  // test a single string using a single pattern
  def test() {
    // create patterns from the entity and slot
    val bm = new Entity("PER", "bronfman", "http://en.wikipedia.org/wiki/Benjamin_Bronfman")
    bm.add("Benjamin Bronfman")
    bm.add("Bronfman")
    val pa = new Slot("PER", "Affiliate")
    pa.names.toArray().foreach(s => {
      bm.names.toArray().foreach(e => {
        //pattern_list.add(new Pattern(e.asInstanceOf[String], s.asInstanceOf[String]))
      })
    })
    // extract some wikipedia file
    val s = Source.fromFile("resources/test/bm.txt").mkString
    // for each pattern in the pattern list, match for some string
    pattern_list.toArray().foreach(p => {
      //if(p.asInstanceOf[Pattern].matches(s)) println("match")
    })
    // TODO: how to efficiently match all that many patterns
    // Solution: one pattern for each list
  }

  def main(args: Array[String]) {
    initEntityList("resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json")
    // initSlot("affiliate")
  }

}