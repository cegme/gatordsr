package edu.ufl.cise.util
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar

object DateUtil {

  /**
   * function created to test the functionality of ScalaTest in the project.
   */
  def shoutMyMessage(message: String) = message.toUpperCase()

  /**
   * Used to test incrementing date
   */
  def incDate(dt: String): String = {
    /// String dt = "2008-12-31"; // Start date
    val sdf = new SimpleDateFormat("yyyy-MM-dd");
    val c = Calendar.getInstance();

    c.setTime(sdf.parse(dt));

    c.add(Calendar.DATE, 1); // number of days to add
    val dtNew = sdf.format(c.getTime()); // dt is now the new date
    return dtNew
  }

  /**
   * Get java.util.Date equivalent of a date string, the format is 2012-05-20.
   */
  def getDate(dt: String): Date = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.parse(dt)
//    val c = Calendar.getInstance();
//
//    c.setTime(sdf.parse(dt));
//    c.getTime()
  }
  
  def toString(date: Date): String = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.format(date)    
  }

  /**
   * to be tested if we parse a date properly back and forth
   */
  def dateStringToString(dateStr: String): String = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd");
//    val c = Calendar.getInstance();
//
//    c.setTime(sdf.parse(dateStr));
    sdf.format(sdf.parse(dateStr))
  }

}