package edu.ufl.cise.pipeline

import edu.ufl.cise.RemoteGPGRetrieval
import java.io.PrintWriter

object SimpleJob extends App{
  
  val pw1 = new PrintWriter("resources/test/file1.txt")
  val pw2 = new PrintWriter("resources/test/file2.txt")
  
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
  
}