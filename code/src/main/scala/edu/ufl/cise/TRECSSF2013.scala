package edu.ufl.cise

object TRECSSF2013 extends Logging {

  def main(args: Array[String]): Unit = {

    val streams = Faucet.getStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg")
    val si = streams.next.get
    val cleansedStr = new String(si.body.cleansed.array(), "UTF-8")

    logInfo("Data displayed properly.")

    Pipeline.init()
    
    val query = new SSFQuery("Abraham Linon", "president of")    
    
    println("QUERY:::")
    
    val z = streams
      .map(si => new String(si.get.body.cleansed.array, "UTF-8"))
      .map(Pipeline.run(_))
      .flatMap(x => x.toArray())
      .filter(p => {
        val triple = p.asInstanceOf[Triple]
        triple.entity0.toLowerCase().contains(query.entity.toLowerCase()) ||
          query.entity.toLowerCase().contains(triple.entity0.toLowerCase()) ||
          query.slotName.toLowerCase().contains(triple.slot.toLowerCase()) ||
          triple.slot.toLowerCase().contains(query.slotName.toLowerCase())
      }).foreach(println)
  }

}