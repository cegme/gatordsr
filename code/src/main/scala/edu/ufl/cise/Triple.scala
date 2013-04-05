package edu.ufl.cise

// used to represent the entity-slot-entity triple during the sentence relation extraction
class Triple (entity0:String, slot:String, entity1:String) {
  override def toString():String = 
	{
		val s = entity0 + " - " + slot + " - " + entity1
		return s
	}
}
