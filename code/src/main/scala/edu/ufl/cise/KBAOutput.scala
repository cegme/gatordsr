package edu.ufl.cise

import java.util.ArrayList
import java.io.PrintWriter
import java.io.File
import java.lang.Integer

object KBAOutput {

  var row_num = 0 // the num of rows added into the row list

  val rows = new ArrayList[Row]() // the list of the rows
	
  val pw = new PrintWriter(new File("resources/test/result.txt"))
  pw.println("#{'team_id', 'system_id', 'doc_id', 'topic_id', 'confidence', 'relevance', 'mention', " +
  		"'date_hour', 'slot_name', 'equiv_id', 'byte_range'}")
  pw.flush()
  		
  // add one row into the row list

  def add(doc_id:String, topic_id : String, confidence : Integer, 
    date_hour : String, slot_name : String, slot_value : Integer, byte_range : String){
    pw.println(new Row(doc_id:String, topic_id : String, confidence : Integer, 
    date_hour : String, slot_name : String, slot_value : Integer, byte_range : String).toString)
    row_num = row_num + 1
    pw.flush()
  }
	
  // write the rows into one file
  def writeToFile(filename:String)
  {
	val writer = new PrintWriter(new File(filename))
	// write some auxiliary information here
	rows.toArray().foreach(row => writer.println(row.toString()))
	writer.close()
  }
	
  def main(args:Array[String]){
    // empty
  }
	
}

case class Row(doc_id:String, topic_id : String, confidence : Integer, 
    date_hour : String, slot_name : String, equiv_id : Integer, byte_range : String) {
  //this(triple:Triple)
  val team_id = "gatordsr"; // first column: team_id
  val system_id ="gatordsr" // second column: system_id
  val relevance = -2 // sixth column: ccr relevance rating level integer in [-1, 0, 1, 2]
  val mention = -1 // seventh column: contains mention integer in [0, 1]
 
	
  override def toString(): String = team_id + " " + system_id + " " + doc_id + " " + topic_id + " " + confidence + " " + 
    relevance + " " + mention + " " + date_hour + " " + slot_name + " " + equiv_id + " " + byte_range
	
	  
}