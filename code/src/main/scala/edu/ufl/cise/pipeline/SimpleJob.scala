package edu.ufl.cise.pipeline

import edu.ufl.cise.RemoteGPGRetrieval
import java.io.PrintWriter
import scala.io.Source
import scala.util.parsing.json.JSON

object SimpleJob extends App{
  //readFile("resources/test/entities.psv")
  writeJSON
  
  // write a json to a file
  def writeJSON()
  {
    val json = JSON.parseFull("""{
    "FAC": [
        {"group": "danville"},
    	{"group": "deeplearning"},
        {"group": "fargo"},
        {"group": "hoboken"},
        {"group": "mining"},
        {"group": "ocala"}  
      ],
    "ORG": [
        {"group": "danville"},
        {"group": "fargo"},
        {"group": "hoboken"},
        {"group": "kba2012"},
        {"group": "mining"},
        {"group": "startups"}
      ],
    "PER": [
        {"group": "bronfman"},
        {"group": "commedians"},
        {"group": "danville"},
        {"group": "deeplearning"},
        {"group": "fargo"},
        {"group": "french"},
        {"group": "hep"},
        {"group": "hoboken"},
        {"group": "kba2012"},
        {"group": "ocala"},
        {"group": "screenwriters"},
        {"group": "turing"}
      ]                                                                                                                                  
}
""")
    println(json.isEmpty)
    val map: Map[String, Any] = json.get.asInstanceOf[Map[String, Any]]
    val pers: List[Any] = map.get("PER").get.asInstanceOf[List[Any]]
    pers.foreach(p => {println(p)})
    
  }
  
  def readFile(filename: String){
    //val pw = new PrintWriter("resources/test/file2.txt")
    val pipe = Pipeline.getPipeline(Pipeline.patterns, Pipeline.queries, Pipeline.dirs)
    var i = 0
    Source.fromFile(filename).getLines.foreach(line => {
      val array = line.split("\\|") 
      println(array(0) + " " + array(1) + " " + array(2))
      val list = RemoteGPGRetrieval.getStreams(array(0), array(1))
      val si = list.get(Integer.parseInt(array(2)))
      val fn = "resources/test/file" + i + ".txt"
      val pw = new PrintWriter(fn)
      pw.write(si.body.clean_visible)
      pw.close()
      pipe.run(si)
      i = i + 1
    })
  }
}