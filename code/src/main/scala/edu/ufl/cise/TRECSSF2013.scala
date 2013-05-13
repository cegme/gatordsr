package edu.ufl.cise

import edu.mit.jwi.item.POS
import edu.ufl.cise.util.WordnetUtil
import edu.ufl.cise.util.OntologySynonymGenerator
import java.util.ArrayList

object TRECSSF2013 extends Logging {

  val query = new SSFQuery("roosevelt", "president")
  lazy val pipeline = Pipeline.getPipeline(query)

  def main(args: Array[String]): Unit = {
    //runFaucet()
    runCachedFaucet()
  }

  /**
   * This will execute the program using Faucet.scala
   */
  def runFaucet() {
    val z = StreamFaucet.getStreams("2011-10-08", 0, 23)
      //.take(100)
      .map(si => new String(si.body.cleansed.array, "UTF-8"))
      //.map(_.take(964))
      .map(pipeline.run(_).toArray())
      .flatMap(x => x)
      .filter(p =>
        {
          val e0 = p.asInstanceOf[Triple].entity0.toLowerCase
          val e0DicArr = WordnetUtil.getSynonyms(e0, POS.NOUN)

          e0.equalsIgnoreCase(query.entity.toLowerCase) //||
          //query.entity.toLowerCase.contains(p.entity0.toLowerCase()) ||
          //query.slotName.toLowerCase.contains(p.slot.toLowerCase()) ||
          //p.slot.toLowerCase.equalsIgnoreCase(query.slotName.toLowerCase)
        })
      .foreach(t => logInfo("Answer: %s".format(t.toString)))
  }

  /**
   * This will execute the program using CashedFaucet.scala
   */
  def runCachedFaucet() {

    val sr = new StreamRange
    sr.addFromDate("2011-10-07")
    sr.addFromHour(14)
    sr.addToDate("2011-10-07")
    sr.addToHour(14)
    val z = new CachedFaucet(SparkIntegrator.sc, sr)

    lazy val z1 = z.iterator.reduce(_ union _) // Combine RDDS

//    val filtered = z1.map(si =>
//      new String(si.body.cleansed.array(), "UTF-8").toLowerCase()).
//      filter(s => s.contains("roosevelt"))
//    println(filtered.count)
    
      val a = (new ArrayList[Triple] { new Triple("", "", "") }).toArray()
    z1.map(p =>
      {
        logInfo("Current doc: %s".format(p.doc_id))
        if (p.body != null && p.body.cleansed != null) {
          val bb = p.body.cleansed.array
          if (bb.length > 0) {
            val str = new String(bb, "UTF-16")
            val b = pipeline.run(str)
            b.toArray
          } else {
            a
          }
        } else
          a
      }).flatMap(x => x)
      .filter(p =>
        {
          val t = p.asInstanceOf[Triple]
          //  val e0DicArr = WordnetUtil.getSynonyms(e0, POS.NOUN)

          t.entity0.equals(query.entity) //||
          //query.entity.toLowerCase.contains(p.entity0.toLowerCase()) ||
          //query.slotName.toLowerCase.contains(p.slot.toLowerCase()) ||
          //p.slot.toLowerCase.equalsIgnoreCase(query.slotName.toLowerCase)
        })
      .foreach(t => logInfo("Answer: %s".format(t.toString)))

  }

   //      .
    //      flatMap(s => pipeline.run(s).toArray()).
    //      filter(o => {
    //        val t = o.asInstanceOf[Triple]
    //        OntologySynonymGenerator.getSynonyms(PER.Affiliate).
    //          filter(s => s.contains(t.slot)).length > 0
    //      })
}
