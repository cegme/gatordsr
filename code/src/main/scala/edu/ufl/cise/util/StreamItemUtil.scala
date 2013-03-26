package edu.ufl.cise.util

import kba.StreamItem
import edu.ufl.cise.Faucet
import java.io.PrintWriter
import java.io.File

object StreamItemUtil {

  def toString(si: StreamItem): String = {

    // return a string from an array of Bytes
    def getString(array: Array[Byte]): String = new String(array, "UTF-8")

    val raw_body = getString(si.body.raw.array)
    val cleansed_body = getString(si.body.cleansed.array)

    // write two strings into files 
    val pwRaw = new PrintWriter(new File("raw.html"))
    pwRaw.write(raw_body)
    pwRaw.close()
    val pwCleansed = new PrintWriter(new File("cleansed.html"))
    pwCleansed.write(cleansed_body)
    pwCleansed.close()
    return ""
  }

  def main(args: Array[String]) {
    //val a = si.body.cleansed
    val z = Faucet.getStreams("2012-05-01")
    val stream = z.next 
    //TODO: It looks like we are merging all the documents in one 
    //streamitem. needs to be fixed. The file contains several html files and its size is
    //about 50MB. The raw and the cleansed versions do not differ much
    toString(stream);
  }
}