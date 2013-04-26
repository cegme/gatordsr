package edu.ufl.cise

/**
 * This class represents the queries from the for the SSF task.
 * The class as two main properties, entity and slotName.
 *
 * An example is: (James_McCartney, founderOf, Beatles2)
 * where James_McCartney is the entity and founderOf is the slot name.
 * We will not be given the final entity Beatles2, this will be inferred.
 *
 * New query format:
 * Given a slot for each of the target entities, detect changes to the slot value, such as location of next performance or founder of ____.
 * The hourly structured of the KBA stream corpus allows entities to evolve.
 *
 *
 */

trait EntityType {

}

object PER extends Enumeration with EntityType {
  type PER = Value
  val Affiliate, AwardsWon, CauseOfDeath, Contact_Meet_PlaceTime, DateOfDeath, EmployeeOf, FounderOf, Titles = Value
}

object FAC extends Enumeration with EntityType {
  type FAC = Value
  val Affiliate, Contact_Meet_Entity = Value
}

object ORG extends Enumeration with EntityType {
  type ORG = Value
  val Affiliate, FoundedBy, TopMembers  = Value
}

class NewSSFQuery(val entity: String, val slotName: EntityType) extends Logging {

}
class SSFQuery(val entity: String, val slotName: String) extends Logging {

  // TODO : See if we are given a "time" for this query, if so add it to the constructor
  // TODO : Add a mutable dictionary of synonyms for the entity
  // TODO : Add a dictionary of synonyms for the slot name
  // TODO : Add functions to compare strings with entities and the entity synonyms and return a ranked list with probabilities
  // TODO : Add functions to compare strings with the slotName and the slotName synonyms and return a ranked list with probabilities

}
