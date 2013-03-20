package edu.ufl.cise



/**
 * This class represents the queries from the for the SSF task.
 * The class as two main properties, entity and slotName.
 *
 * An example is: (James_McCartney, founderOf, Beatles2) 
 * where James_McCartney is the entity and founderOf is the slot name.
 * We will not be given the final entity Beatles2, this will be inferred.
 */

class SSFQuery(val entity:String, val slotName:String) extends Logging {

  // TODO : See if we are given a "time" for this query, if so add it to the constructor
  // TODO : Add a mutable dictionary of synonyms for the entity
  // TODO : Add a dictionary of synonyms for the slot name
  // TODO : Add functions to compare strings with entities and the entity synonyms and return a ranked list with probabilities
  // TODO : Add functions to compare strings with the slotName and the slotName synonyms and return a ranked list with probabilities

}
