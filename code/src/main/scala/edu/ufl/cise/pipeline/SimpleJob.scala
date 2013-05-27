package edu.ufl.cise.pipeline

import edu.ufl.cise.RemoteGPGRetrieval
import java.io.PrintWriter
import scala.io.Source

object SimpleJob extends App{
  val pipe = Pipeline.getPipeline(Pipeline.patterns, Pipeline.queries, Pipeline.dirs)
  readFile("resources/test/entities.psv")
/*  val pw1 = new PrintWriter("resources/test/file1.txt")
  val pw2 = new PrintWriter("resources/test/file2.txt")
  2011-10-13-15 | social-300-e99d3abf7c96d7c44a09b0fc304d73b7-272b08fb7cfc5c0e99dcd0486d5cdbcc.sc.xz.gpg | 98 | a751c9e5da7d8a597902814a2afcfc67 | https://twitter.com/BobStovall
  val file1 = "social-292-bba11a194150414d9f683164d0dd05ee-1c8a01976ae9fd0b448605d9902fb0f7.sc.xz.gpg";
  val list1 = RemoteGPGRetrieval.getStreams("2011-10-08-15", file1);
  
  pw1.write(list1.get(154).body.clean_visible)
  //pw1.write(list1.get(154).body.raw.toString())
  pw1.close()
  
  val file2 = "social-246-89d6eaabc52882cb12b944fbb13e490d-b41540fec629ce7af67cde6c948012c5.sc.xz.gpg";
  val list2 = RemoteGPGRetrieval.getStreams("2011-10-09-12", file2);
  
  pw2.write(list2.get(205).body.clean_visible)
  //pw2.write(list2.get(205).body.raw.toString)
  pw2.close()
  */
  
  
  def readFile(filename: String){
    //val pw = new PrintWriter("resources/test/file2.txt")
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