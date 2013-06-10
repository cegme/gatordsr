package edu.ufl.cise

import java.util.ArrayList
import java.io.PrintWriter
import java.io.File
import java.lang.Integer
import java.io.BufferedWriter
import java.io.FileWriter

object KBAOutput {

  var row_num = 0 // the num of rows added into the row list

  val rows = new ArrayList[Row]() // the list of the rows
  
  val pwr = new PrintWriter("resources/test/kbaoutput/full_result.txt")
  val firstLine = "#{\"run_type\": \"automatic\", \"poc_email\": \"trec-kba@googlegroups.com\", " +
  		"\"team_id\": \"gatordsr\", \"topic_set_id\": \"sample-trec-kba-topics-2013\", \"corpus_id\": " +
  		"\"tiny-corpus\", \"$schema\": \"http://trec-kba.org/schemas/v1.1/filter-run.json\", " +
  		"\"team_name\": \"gatordsr\", \"system_description_short\": \"rating=2,contains_mention=1,confidence=1000(default)\", " +"\"system_description\": \"Pattern matching\"" +
  		" with default confidence 1000\", \"task_id\": \"kba-ssf-2013\", " +
  		"\"poc_name\": \"TREC KBA Organizers\", \"run_info\": {\"num_entities\": 170, \"num_stream_hours\": 11948}, \"system_id\": \"gatordsr\"}"
  pwr.println(firstLine)
  pwr.flush()
  pwr.close()
  
  var outputPrefix = "resources/test/kbaoutput/"

  lazy val pw = {
    val _pw = new PrintWriter(new File(outputPrefix + "result.txt"))
    _pw.println("#{'team_id', 'system_id', 'stream_id', 'topic_id', 'confidence', 'relevance', 'mention', " +
  		"'date_hour', 'slot_name', 'equiv_id', 'byte_range'}")
    _pw.flush()
    _pw
  }
  
  
  def main(args : Array[String]){
    println(firstLine)
  }
  
  // add one row into the row list

  def add(stream_id:String, topic_id : String, confidence : Integer, 
    date_hour : String, slot_name : String, slot_value : Integer, byte_range : String, comment : String){
    val pw = new PrintWriter(new BufferedWriter(new FileWriter(outputPrefix + slot_name, true)));
    val row = new Row(stream_id:String, topic_id : String, confidence : Integer, 
    date_hour : String, slot_name : String, slot_value : Integer, byte_range : String)
    pw.println(row.toString)
    row_num = row_num + 1
    pw.println(comment)
    pw.close
/*    pwr.synchronized({
      pwr.write(row.toString + "\n")
      pwr.flush()
      })*/
  }
	
  // write the rows into one file
  def writeToFile(filename:String)
  {
	val writer = new PrintWriter(new File(filename))
	// write some auxiliary information here
	rows.toArray().foreach(row => writer.println(row.toString()))
	writer.close()
  }
	
}

case class Row(stream_id:String, topic_id : String, confidence : Integer, 
    date_hour : String, slot_name : String, equiv_id : Integer, byte_range : String) {
  //this(triple:Triple)
  val team_id = "gatordsr"; // first column: team_id
  val system_id ="gatordsr" // second column: system_id
  val relevance = 2 // sixth column: ccr relevance rating level integer in [-1, 0, 1, 2]
  val mention = 1 // seventh column: contains mention integer in [0, 1]
 
	
  override def toString(): String = team_id + " " + system_id + " " + stream_id + " " + topic_id + " " + confidence + " " + 
    relevance + " " + mention + " " + date_hour + " " + slot_name + " " + equiv_id + " " + byte_range
	
	  
}
