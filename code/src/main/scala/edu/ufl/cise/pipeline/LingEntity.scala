package edu.ufl.cise.pipeline

import java.lang._
import streamcorpus.Sentence
import streamcorpus.Token
import java.util.ArrayList

class LingEntity(etype : String, mid : Float, eqid : Integer) {
  val entity_type = etype
  val mention_id = mid
  val equiv_id = eqid
  var topic_id : String = null
  var group : String = null
  var sentence_pos = -1
  var begin = -1
  var end = -1
  var content : String = null
  
  override def toString = content
  
}

class LingSentence( sentence : Sentence){
  val entity_list = new ArrayList[LingEntity]()
  def extractEntities() = {
    val entity_list = new ArrayList[LingEntity]()
    val tokens = sentence.getTokens().toArray(Array[Token]())
    val flags = new Array[Integer](tokens.size)
    // generate flags for tokens
    for(i <- 0 until tokens.size){
      val token = tokens(i)
      if(token.entity_type == null || token.equiv_id == -1) flags(i) = -1
      else
      {
        if ( (i != 0) && token.getEntity_type().equals(tokens(i-1).getEntity_type()) && 
            (token.getMention_id() == tokens(i-1).getMention_id()) && (token.getEquiv_id() == tokens(i -1 ).getEquiv_id()))
            flags(i) = 1
        else
            flags(i) = 0 
      }
    }
    // generate entities 
    var i = 0
    while(i < tokens.size){
      if(flags(i) == 0) {
        val entity = new LingEntity(tokens(i).entity_type.toString(), tokens(i).mention_id, tokens(i).equiv_id)
        entity.begin = i
        var j = i + 1
        while (j < tokens.size && flags(j) == 1)
          j = j + 1
        entity.end = j - 1
        entity.content = tokensToString(tokens.slice(i, j))
        entity_list.add(entity)
        // move i to position j
        i = j
      }
      else i = i + 1
    }
      
    def tokensToString(tokens : Array[Token]) : String = {
      val sb = new java.lang.StringBuilder
      tokens.foreach(token => {
        sb.append(token.token).append(" ")
      })
      //println(sb)
      sb.toString()
    }
    entity_list
  }
  
}