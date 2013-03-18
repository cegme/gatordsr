package edu.ufl.cise.util

/**
 * This class reads a URL line by line
 */
class URLLineReader(url: String) extends Iterator[String] {
  val reader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.net.URL(url).openStream()))
  var line: String = null;

  def hasNext = {
    line = reader.readLine()
    line != null
  }

  def next = line
}
