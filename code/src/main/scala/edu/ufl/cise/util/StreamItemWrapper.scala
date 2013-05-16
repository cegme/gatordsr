package edu.ufl.cise.util

import streamcorpus.StreamItem
import edu.ufl.cise.EmbededFaucet
import scala.collection.mutable.StringBuilder

class StreamItemWrapper(val day: String, val hour: Int, val fileName: String, val index: Int, val streamItem: StreamItem) extends Serializable {

  def getDirectoryName(day: String, hour: Int): String = {
    val hourStr = EmbededFaucet.numberFormatter.format(hour)
    "%s-%s".format(day, hourStr)
  }

  override def toString = {
    var str = new StringBuilder(getDirectoryName(day, hour) + "/" + fileName + "[" + index + "]||")
    if (streamItem.getBody != null && streamItem.getBody.getClean_visible() != null) {

      val doc = streamItem.getBody.getClean_visible()
      val query = EmbededFaucet.query
     // println(query)
     // println(doc)
      var i = -1
      var initIndex = 0
      var endIndex = query.length()

      while ((i = doc.indexOf(query, i + 1)) != -1) {
        initIndex = i - 30
        if (initIndex < 0)
          initIndex = 0
        endIndex = i + query.length + 30
        if (endIndex > doc.length())
          endIndex = doc.length() - 1
        str = str.append(doc.substring(initIndex, endIndex)).append( "||")
      }
    }
    str.toString
  }
}


