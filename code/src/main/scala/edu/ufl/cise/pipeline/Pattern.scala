package edu.ufl.cise.pipeline

import edu.ufl.cise.Triple


class Pattern(entity:String,id:Int, slot:String, sid:Int){
  var relation:Triple = null // generate the corresponding result relation triple
  val regex = ("(?!"+ entity + ")" + entity + "[a-zA-Z0-9 ]*" + slot + "([a-zA-Z0-9 ]*)").r
  //println(regex)
  
  def this(entity:String, slot:String) = this(entity,-1, slot, -1)
  
  def matches(s:String):Boolean = {
    // TODO: return whether the target string matches the pattern
	// TODO: generate corresponding triple result for this matched pattern
    // regex.findFirstIn(s).
    s match {
      case regex => println("matches")
    }
    println(regex.findAllIn(s).matchData.foreach(m=> println(m.group(1))))
    return !regex.findAllIn(s).isEmpty
  }
     
}