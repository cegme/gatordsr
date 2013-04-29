package edu.ufl.cise

import edu.mit.jwi.item.POS
import edu.ufl.cise.util.WordnetUtil
import edu.ufl.cise.util.OntologySynonymGenerator
object TRECSSF2013 extends Logging {

  val text = "Abraham Lincoln was the 16th President of the United States, serving from March 1861 until his assassination in April 1865."
  val query = new SSFQuery("Abraham Lincoln", "president of")
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
    sr.addFromDate("2012-05-01")
    sr.addFromHour(0)
    sr.addToDate("2012-05-01")
    sr.addToHour(23)
    val z = new CachedFaucet(SparkIntegrator.sc, sr)
    val itVal = z.iterator.next
    val filtered = itVal.map(si =>
      new String(si.body.cleansed.array(), "UTF-8")).
      filter(s => s.contains("Atacocha")).
      flatMap(s => pipeline.run(s).toArray()).
      filter(o => {
        val t = o.asInstanceOf[Triple]
        OntologySynonymGenerator.getSynonyms(PER.Affiliate).
        filter(s => s.contains(t.slot)).length > 0

      })

    println(filtered.count)
    //    for(temp <- itVal){
    //      temp.
    //    }
    val z1 = z.iterator.reduce(_ union _)
  }

}
