package edu.ufl.cise.util

import streamcorpus.StreamItem
import edu.ufl.cise.EmbededFaucet

class StreamItemWrapper(val day: String, val hour: Int, val fileName: String, val index: Int, val streamItem: StreamItem) extends Serializable {

  def getDirectoryName(day: String, hour: Int): String = {
    val hourStr = EmbededFaucet.numberFormatter.format(hour)
    "%s-%s".format(day, hourStr)
  }

  override def toString = {
    var str = getDirectoryName(day, hour) + "/" + fileName + "[" + index + "]"
    //    if (streamItem.getBody != null && streamItem.getBody.getClean_visible() != null)
    //      str + ":" + streamItem.getBody.getClean_visible.substring(0, 30)
    //    else

    val doc = streamItem.getBody.getClean_visible()
    var i = -1
    var initIndex = 0
    var endIndex = doc.length()
    while ((i = doc.indexOf(EmbededFaucet.query, i + 1)) != -1) {
      initIndex = i - 30
      if (initIndex < 0)
        initIndex = 0
      endIndex = i + EmbededFaucet.query.length
      if (endIndex > doc.length())
        endIndex = doc.length() - 1
      str = str + doc.substring(initIndex, endIndex)
    }
    str
  }
}


