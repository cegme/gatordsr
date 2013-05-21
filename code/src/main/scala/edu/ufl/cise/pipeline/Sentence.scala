package edu.ufl.cise.pipeline

import java.util.ArrayList

class Sentence {
  //TODO: the middle-level representation of a sentence 
  val token_list = new ArrayList[String]()
  val pos_list = new ArrayList[String]()
  val loc_list = new ArrayList[java.lang.Integer]()
  
  // from these 3 lists construct a new string containing all these information
  // and can be matched by patterns with the same format
  override def toString():String = {
    
    val comp = new Array[String](token_list.size())
    for(i <- 0 until  token_list.size())
      comp(i) = token_list.get(i) + "\\" + pos_list.get(i) + "\\" + loc_list.get(i)
    // make string
    comp.mkString(" ")
  }
}