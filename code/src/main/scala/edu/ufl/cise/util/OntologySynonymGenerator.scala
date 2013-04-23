package edu.ufl.cise.util

import edu.ufl.cise.EntityType
import edu.ufl.cise.PER
import java.util.Scanner
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.Enumeration
import edu.mit.jwi.item.POS

/**
 * This class will go through our dictionary to generate the synonyms
 * of the ontology specified by TREC, namely:
 * type PER: Affiliate, Contact_Meet_PlaceTime, AwardsWon, DateOfDeath, CauseOfDeath, Titles, FounderOf, EmployeeOf
 * type FAC: Affiliate, Contact_Meet_Entity
 * type ORG: Affiliate, TopMembers, FoundedBy
 * TODO add FAC, ORG
 */
object OntologySynonymGenerator {

  lazy val per_affiliate_dic: Seq[String] = loadDictionary("per_affiliate")
    .flatMap(WordnetUtil.getSynonyms(_, POS.ADJECTIVE)).distinct

  lazy val per_awards_dic: Seq[String] = loadDictionary("per_awards")
    .flatMap(WordnetUtil.getSynonyms(_, POS.NOUN)).distinct

  lazy val per_death_dic: Seq[String] = loadDictionary("per_death")
    .flatMap(WordnetUtil.getSynonyms(_, POS.VERB)).distinct

  lazy val per_place_dic: Seq[String] = loadDictionary("per_place")
    .flatMap(WordnetUtil.getSynonyms(_, POS.ADVERB)).distinct

  lazy val per_time_dic: Seq[String] = loadDictionary("per_time")
    .flatMap(WordnetUtil.getSynonyms(_, POS.ADVERB)).distinct

  lazy val per_founderof_dic: Seq[String] = loadDictionary("per_founderof")
    .flatMap(WordnetUtil.getSynonyms(_, POS.VERB)).distinct

  lazy val per_employeeof_dic: Seq[String] = loadDictionary("per_employeeof")
    .flatMap(WordnetUtil.getSynonyms(_, POS.VERB)).distinct

  lazy val per_title_dic: Seq[String] = loadDictionary("per_title")
    .flatMap(WordnetUtil.getSynonyms(_, POS.NOUN)).distinct

  def main(args: Array[String]) {
    println(getSynonyms(PER.Titles).mkString(", "))
  }

  /**
   * Return the synonyms of a slot type (of ontology)
   */
  def getSynonyms(slotName: PER.Value): Seq[String] = {

    slotName match {
      case PER.Affiliate =>
        per_affiliate_dic
      case PER.AwardsWon =>
        per_awards_dic
      case PER.CauseOfDeath =>
        per_death_dic
      case PER.Contact_Meet_PlaceTime =>
        per_place_dic ++ per_time_dic
      case PER.DateOfDeath =>
        per_death_dic
      case PER.FounderOf =>
        per_founderof_dic
      case PER.EmployeeOf =>
        per_founderof_dic
      case PER.Titles =>
        per_title_dic
    }
  }

  def loadDictionary(dicName: String): Seq[String] = {
    val lb = new ListBuffer[String]()

    val sc = new Scanner(new File("./resources/ontology/" + dicName + ".txt"))
    while (sc.hasNext()) {
      lb.append(sc.nextLine())
    }
    lb
  }
}