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
  var entityIndex = -1
  override def toString = content
  
}