package edu.ufl.cise

object TRECSSF2013 extends Logging {

  def main(args: Array[String]): Unit = {

    Pipeline.init()
    
    val query = new SSFQuery("Richard Radcliffe", "topped")    
    logInfo("SSFQuery : %s".format(query)) 
    
    //val z = Faucet.getStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
    val z = Faucet.getStreams("2012-05-02",0,1)
      //.take(100)
      .map(si => new String(si.body.cleansed.array, "UTF-8"))
      //.map(_.take(964))
      .map(Pipeline.run(_))
      .flatMap(x => x)
      .filter(p => 
        p.entity0.toLowerCase.equalsIgnoreCase(query.entity.toLowerCase) //||
        //query.entity.toLowerCase.contains(p.entity0.toLowerCase()) ||
        //query.slotName.toLowerCase.contains(p.slot.toLowerCase()) ||
        //p.slot.toLowerCase.equalsIgnoreCase(query.slotName.toLowerCase)
      )
      .foreach(t => logInfo("Answer: %s".format(t.toString)))
 
  }

}
