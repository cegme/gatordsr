package edu.ufl.cise

// used to represent the entity-slot-entity triple during the sentence relation extraction
class Triple (val entity0:String, val slot:String, val entity1:String) {

	override def toString():String = entity0 + " - " + slot + " - " + entity1

	def toArray(): Array[String] = Array(entity0, slot, entity1)
}