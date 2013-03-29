package edu.ufl.cise.util


import edu.ufl.cise.Faucet
import kba.StreamItem

/**
 * This class provides utility functions for StreamItem. For example
 * proper unicode conversions, etc.
 */
object StreamItemUtil {

  def toString(si: StreamItem): String = {
    val raw_body = new String(si.body.raw.array, "UTF-8")
    val cleansed_body = new String(si.body.cleansed.array, "UTF-8")
    return raw_body
  }

  def main(args: Array[String]) {
    val z = Faucet.getStreams("2011-12-13", 19)
    val si = z.next.get
    println(toString(si))
  }
}