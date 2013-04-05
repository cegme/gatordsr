package edu.ufl.cise

// used to represent the entity-slot-entity triple during the sentence relation extraction
case class Triple (val entity0:String, val slot:String, val entity1:String) {
  override def toString():String = 
	{
		val s = entity0 + " - " + slot + " - " + entity1
		return s
	}
}
