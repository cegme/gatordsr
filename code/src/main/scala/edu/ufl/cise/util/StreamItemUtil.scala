package edu.ufl.cise.util

import kba.StreamItem
import edu.ufl.cise.Faucet
import java.io.PrintWriter
import java.io.File
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream

/**
 * This class provides utility functions for StreamItem. For example 
 * proper unicode conversions, etc.
 */
object StreamItemUtil {

  def toString(si: StreamItem): String = {

    // return a string from an array of Bytes
    def getString(array: Array[Byte]): String = new String(array, "UTF-8")

    val raw_body = new String(si.body.raw.array, "UTF-8")
    val cleansed_body = getString(si.body.cleansed.array)

    // write two strings into files 
    val pwRaw = new PrintWriter(new File("raw.html"), "UTF-8")
    pwRaw.write(raw_body)
    pwRaw.close()
    val pwCleansed = new PrintWriter(new File("cleansed.html"), "UTF-8")
    pwCleansed.write(cleansed_body)
    pwCleansed.close()

    //test as some unicode characters don't show properly and will be 
    //eliminated from cleansed version although we get the english text 
    //in the document
    val out = new BufferedWriter(new OutputStreamWriter(
      new FileOutputStream("rawutf8.html"), "UTF-8"));
    try {
      out.write(raw_body);
    } finally {
      out.close();
    }

    return raw_body
  }

  def main(args: Array[String]) {
    //val a = si.body.cleansed
    val z = Faucet.getStreams("2011-12-13", 19)
    val si = z.next.get
    //TODO: It looks like we are merging all the documents in one 
    //streamitem. needs to be fixed. The file contains several html files and its size is
    //about 50MB. The raw and the cleansed versions do not differ much
    //println(new String(si.body.raw.array(), "UTF-8"))
    println(toString(si))
  }
}