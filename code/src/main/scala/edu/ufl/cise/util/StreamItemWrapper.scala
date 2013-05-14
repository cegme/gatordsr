package edu.ufl.cise.util

import kba.StreamItem

class StreamItemWrapper(val date: String, val hour: Int, val fileName: String, val index: Int, val streamItem: StreamItem) extends Serializable{

  override def toString = date + hour + "/" + fileName + "/" + index //+ " :::: " + new String(streamItem.body.cleansed.array(), "UTF-8")

}


