package edu.ufl.cise.util

import streamcorpus.StreamItem

class StreamItemWrapper(val day: String, val hour: Int, val fileName: String, val index: Int, val streamItem: StreamItem) extends Serializable {

  override def toString = {
    val str = "day: " + day + "hour: " + hour + "/" + fileName + "/" + index + " :::: "
    if (streamItem.getBody.clean_html != null)
      str + streamItem.getBody.getClean_visible.substring(0, 30)
    else
      str
  }
}


