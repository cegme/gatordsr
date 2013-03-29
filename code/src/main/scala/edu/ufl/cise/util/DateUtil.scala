package edu.ufl.cise.util
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar

object DateUtil {

  /**
   * function created to test the functionality of ScalaTest in the project.
   */
  def shoutMyMessage(message: String) = message.toUpperCase()

  def toDate(dt: String): String = {
   /// String dt = "2008-12-31"; // Start date
    val sdf = new SimpleDateFormat("yyyy-MM-dd");
    val c = Calendar.getInstance();
    
      c.setTime(sdf.parse(dt));
    
    c.add(Calendar.DATE, 1); // number of days to add
    val dtNew = sdf.format(c.getTime()); // dt is now the new date
    System.out.println(dtNew);
    return dtNew
  }
}