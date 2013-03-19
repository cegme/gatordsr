package edu.ufl.cise.util

import java.io.{BufferedReader, FileNotFoundException, InputStreamReader}
import java.net.URL

import edu.ufl.cise.Logging

/**
 * This class reads a URL line by line
 */
class URLLineReader(url: String) extends Iterator[String] with Logging {
  private lazy val reader = new BufferedReader(
                              new InputStreamReader(
                                new URL(url).openStream))

  def hasNext = {
    try {
      reader.ready
    }
    catch {
      case e:java.io.FileNotFoundException => logError("File Not Found: %s".format(url)); false
    }

  }

  def next = {
    reader.readLine
  }
}
