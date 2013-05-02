package edu.ufl.cise

import java.util.Date
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

import scala.collection.mutable.LinkedList

import edu.ufl.cise.util.URLLineReader

/**
 * StreamRange class is used to create a builder that specifies a
 * time range for the queries.
 * Some Rules/Options:
 * 1) If a file is specified is mucs have an hour and date
 * 2) If only a dateFrom and `hour` are specified, it considers only that hour
 * 3) A dateFrom can also have dateTo, hours are optional with either of those.
 * 4) No entry means all to get all the files
 */
class StreamRange extends Logging {

  private val TODATE = "TODATE"
  private val TOHOUR = "TOHOUR"
  private val FROMDATE = "FROMDATE"
  private val FROMHOUR = "FROMHOUR"
  private val FILE = "FILE"
  private val settingsMap = new scala.collection.mutable.HashMap[String,String]

  def addToDate(date:String):Unit = settingsMap += (TODATE -> date)
  def addFromDate(date:String):Unit = settingsMap += (FROMDATE -> date)
  def addToHour(hour:Integer):Unit = settingsMap += (TOHOUR -> hour.toString)
  def addFromHour(hour:Integer):Unit = settingsMap += (FROMHOUR -> hour.toString)
  def addFile(file:String):Unit = settingsMap += (FILE -> file)

  def apply(fromDate:String, fromHour:Int, toDate:String, toHour:Int):StreamRange = {

    // TODO check if any of these are empty string or None, if so dont put them in the map
    val sr = new StreamRange
    
    sr.addToDate(toDate)
    sr.addFromDate(fromDate)
    sr.addToHour(fromHour)
    sr.addFromHour(toHour)

    sr
  }

  def getFileList:List[(String,String)] = {
    logInfo("Getting the File List with the settingsMap: %s".format(settingsMap.toString))

    // Get all the
    if (settingsMap.isEmpty) {
      logInfo("Getting all the files in the dataset.")
      StreamRange.getAllFiles
    }
    else if(settingsMap contains FILE) {
      assert(settingsMap contains FROMDATE)
      assert(settingsMap contains FROMHOUR)
      logInfo("Getting a particular file.")
      // date, hour, file
      StreamRange.getFiles(settingsMap(FROMDATE), 
              settingsMap(FROMHOUR).toInt,
              settingsMap(FILE))
    }
    else if(settingsMap.contains(FROMHOUR)&& settingsMap.contains(FROMDATE)&& settingsMap.contains(TODATE)){
      if (settingsMap contains TOHOUR) {
        logInfo("Getting a range with a from hour and a to hour.")
        assert(settingsMap contains TODATE)
        StreamRange.getFiles(settingsMap(FROMDATE), 
                settingsMap(FROMHOUR).toInt,
                settingsMap(TODATE), 
                settingsMap(TOHOUR).toInt)
      }
      else {
        logInfo("Getting a range with a from hour and we just choose a to hour of 24.")
        StreamRange.getFiles(settingsMap(FROMDATE), 
                settingsMap(FROMHOUR).toInt,
                settingsMap(TODATE), 
                24)
      }
    }
    else if(settingsMap contains FROMDATE) {
      if(settingsMap contains TODATE) {
        logInfo("Getting a range of files from a fromDate to a todate.")
        StreamRange.getFiles(settingsMap(FROMDATE), settingsMap(TODATE))
      }
      else {
        logInfo("Getting a range of files from a fromDate to the end of the list of files.")
        StreamRange.getFilesBeginingWith(settingsMap(FROMDATE))
      }
    }
    else {
      logError("Unsupported settingsMap: %s".format(settingsMap.toString))
      throw new java.lang.UnsupportedOperationException
    }

  }

}




/**
 * This class deifnes a range of dates and hours for the stream of data.
 * It has several methods that return all the files from a particular time frame.
 */
object StreamRange extends Logging {

  val hourFormatter = new DecimalFormat("00")
  val KBA_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  val BASE_URL = "http://neo.cise.ufl.edu/trec-kba/aws-publicdatasets/trec/kba/kba-stream-corpus-2012/"
  val MIN_DATE = "2011-10-07"
  val MIN_HOUR = 14
  val MAX_DATE = "2012-05-02"
  val MAX_HOUR = 0

  def getAllFiles:List[(String,String)] = getFiles(MIN_DATE, MIN_HOUR, MAX_DATE, MAX_HOUR)

  def getFiles(date:String, hour:Integer, file:String):List[(String,String)] = {
    List(("%s-%s".format(date, hourFormatter.format(hour)), file))
  }

  def getDates(fromDate:String):LinkedList[String] = getDates(fromDate, MAX_DATE)

  def getDates(fromDate: String, 
              toDate: String): LinkedList[String] = {

    val dFrom = KBA_DATE_FORMAT.parse(fromDate)
    val dTo = KBA_DATE_FORMAT.parse(toDate)
    
    var runnerDate = dFrom
    val c = Calendar.getInstance

    var it = new LinkedList[String]()
    while(runnerDate.before(dTo) || runnerDate.equals(dTo)) {
      val nextDate = KBA_DATE_FORMAT.format(runnerDate)
      it = it :+ nextDate

      c.setTime(runnerDate)
      c.add(Calendar.DATE, 1);
      runnerDate = c.getTime
    }
    
    it.flatMap{d => (0 to 23).map(t => "%s-%s".format(d,hourFormatter.format(t)))}
  }
     

  def getDates(fromDate: String,
              fromHour: Int,
              toDate: String,
              toHour: Int): LinkedList[String] = {

    getDates(fromDate, toDate)
      .dropWhile(!_.endsWith(hourFormatter.format(fromHour)))
      .reverse
      .dropWhile(!_.endsWith(hourFormatter.format(toHour)))
      .reverse
  }

  def getFiles(date:String):List[(String,String)] = {

    val directory = "%s%s".format(BASE_URL, date)
    val reader = new URLLineReader(directory)
    val html = reader.toList.mkString
    val pattern = """a href="([^"]+.gpg)""".r

    pattern
      .findAllIn(html)
      .matchData
      .map( x => (date, x.group(1))) // TODO maybe remove this
      .toList
  }
  

  def getFilesBeginingWith(fromDate:String): List[(String,String)] = {
    getDates(fromDate)
      .par
      .flatMap(date => getFiles(date))
      //.flatMap(date => getFiles(date).map((date,_)))
      .toList
    }

  def getFiles(fromDate: String, 
              toDate: String): List[(String,String)] = {

    getDates(fromDate,toDate)
      .par
      //.flatMap(date => getFiles(date).map((date,_)))
      .flatMap(date => getFiles(date))
      .toList
  }
     
  def getFiles(fromDate: String,
              fromHour: Int,
              toDate: String,
              toHour: Int): List[(String,String)] = {

    getDates(fromDate, fromHour, toDate, toHour)
      .par
      //.flatMap(date => getFiles(date).map((date,_)))
      .flatMap(date => getFiles(date))
      .toList
  }

  def main(args: Array[String]) {
    logInfo("Total file count: %d".format(getFiles(MIN_DATE, MIN_HOUR, MAX_DATE, MAX_HOUR).size))

    getAllFiles.take(5).foreach(x => logInfo(x.toString))

  }
            
}



