package edu.ufl.cise.pipeline

import java.util.ArrayList

class Entity(val entity_type: String, val group: String, val topic_id: String) {

  def this(entity_type: String, group: String, topic_id: String, human_readable: String) = {
    this(entity_type, group, topic_id)
    add(human_readable)
  }

  val names = new ArrayList[String] // the list of alias names for Entity

  def add(name: String) = names.add(name) // add one more alias name for the entity

}