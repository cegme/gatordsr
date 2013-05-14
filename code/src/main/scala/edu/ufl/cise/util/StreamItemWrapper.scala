package edu.ufl.cise.util

import kba.StreamItem

class StreamItemWrapper(val date: String, val hour: Int, val fileName: String, val index: Int, val streamItem: StreamItem) extends Serializable {

  override def toString = {
    val str = date + hour + "/" + fileName + "/" + index + " :::: "
    if (streamItem.title != null)
      str + (new String(streamItem.title.cleansed.array(), "UTF-8")).substring(0, 30)
    else
      str
  }
}


