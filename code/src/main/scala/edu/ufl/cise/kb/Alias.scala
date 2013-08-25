package edu.ufl.cise.kb

import scala.util.parsing.json.JSON
import java.util.ArrayList
import scala.io.Source
import scala.collection.immutable.List
import java.net.URL
import java.io.PrintWriter

import edu.ufl.cise.Logging

object Alias extends Logging {

  def main(args:Array[String]){
    //extractWiki()
    GetAliases("http://en.wikipedia.org/wiki/Aharon_Barak")
    GetAliases("http://en.wikipedia.org/wiki/Bill_Coen")
  }

  val URL = "http://en.wikipedia.org/w/api.php?format=json&action=query&prop=revisions&titles=%s&rvprop=timestamp|user|comment|content&rvend=20120104000000"

  def extractWiki() {
    // read wikipedia entity address from a file
    //println("beginning")
    val lines = Source.fromFile("wikiaddr.txt").getLines
    lines.foreach(line => {
      val titles = line.split("/")(4)
      print(line + ": ")
      if(!titles.contains("Basic_Element_(music_group)")) 
      {
        val url = "http://en.wikipedia.org/w/api.php?format=json&action=query&prop=revisions&titles=" + 
    		  	titles + "&rvprop=timestamp|user|comment|content&rvend=20120104000000"
        //println(url)
        getNames(url)
      }
      else println()
    })
   
  }

  // The WikiAPI.scala calls this code to get the 
  // Example: getAliases ("http://en.wikipedia.org/wiki/Shafi_Goldwasser")
  def GetAliases(target_id: String): ArrayList[String] = {
    // Get the title out of the full url
    val title = target_id.substring(target_id.lastIndexOf("/")+1)
    val wiki_url = URL.format(title)

    getNames(wiki_url)
  }
  
  def getNames(url : String)={
    val json = JSON.parseFull(Source.fromURL(new URL(url)).mkString)
    val contents = json.toString().split("\n")
    val names = new ArrayList[String]()
    contents.foreach(line => {
      if(line.toLowerCase().contains("|name") ||line.toLowerCase().contains("|birth_name") ||
          line.toLowerCase().contains("|other_names") || line.toLowerCase().contains("| name") 
          ||line.toLowerCase().contains("| birth_name") 
          ||line.toLowerCase().contains("| other_names")){
        val array = line.split("=")
        if (array.size >= 2)
        { val name = line.split("=")(1) trim()
         if(!names.contains(name))
          names.add(name.trim())
        }
      }
    })
    logInfo("Aliases: %s".format(names.toArray().mkString(";")))
    names
  }
  
}
