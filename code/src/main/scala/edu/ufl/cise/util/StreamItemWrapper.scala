package edu.ufl.cise.util

import streamcorpus.StreamItem
import edu.ufl.cise.EmbededFaucet

class StreamItemWrapper(val day: String, val hour: Int, val fileName: String, val index: Int, val streamItem: StreamItem) extends Serializable {

   def getDirectoryName(day: String, hour: Int): String = {
    val hourStr = EmbededFaucet.numberFormatter.format(hour)
    "%s-%s".format(day, hourStr)
  }
  
  override def toString = {
    val str = getDirectoryName(day, hour) + "/" + fileName + "/" + index 
//    if (streamItem.getBody != null && streamItem.getBody.getClean_visible() != null)
//      str + ":" + streamItem.getBody.getClean_visible.substring(0, 30)
//    else
      str
  }
}


