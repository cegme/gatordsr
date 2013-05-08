package edu.ufl.cise

import java.util.ArrayList
import java.io.PrintWriter
import java.io.File

object KBAOutput {

  var row_num = 0 // the num of rows added into the row list

  val rows = new ArrayList[Row]() // the list of the rows
	

  // add one row into the row list

  def add(triple:Triple){
	rows.add(new Row(triple))
	row_num = rows.size()
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
	add(new Triple("aa", "bb", "cc"))
	add(new Triple("dd", "ee", "ff"))
	writeToFile("temp.txt")
  }
	
}

case class Row(triple:Triple) {
	
  val team_id = "gatordsr"; // first column: team_id
  val system_id ="gatordsr" // second column: system_id
  val doc_id ="" //third column: stream_id in the kba-stream-corpus-2013
  val topic_id = "" + triple.entity0 // fourth column: urlname of the entity
  val confidence = 0 // fifth column: confidence ∈ (0, 1000] and confidence ∈ 𝐙
  val relevance = -2 // sixth column: ccr relevance rating level integer in [-1, 0, 1, 2]
  val mention = -1 // seventh column: contains mention integer in [0, 1]
  val date_hour = "0000-00-00" // eighth column: date-hour string corresponds to the directory name, e.g. '2012-04-04-04'
  val slot_name = "" + triple.slot // ninth column: slot name from the TAC KBP slot ontology. Optionally, this field may contain a second string separated from the first by a colon ":"
  val slot_value = -1 // tenth column: slot value equivalence class name generated by system
  val byte_range = "0-0 " + triple.entity1 // eleventh column: inclusive byte range, e.g. "23-27" specifies five bytes. Byte numbering is zero-based

	
  override def toString(): String = team_id + " " + system_id + " " + doc_id + " " + topic_id + " " + confidence + " " + relevance + " " + mention + " " + date_hour + " " + slot_name + " " + slot_value + " " + byte_range
	
	  
}