package edu.ufl.cise

object TRECSSFQuery {

  def main(args: Array[String]): Unit = {
    
    Pipeline.init()

    class Relation(entity0: String, slot: String, entity1: String) {
      override def toString(): String =
        {
          val s = entity0 + " - " + slot + " - " + entity1
          return s
        }
    }

    val query = new Relation("Abraham Linon", "is president of", "United States")
    val z = Faucet.getStreams("2012-05-02-00", "news.f451b42043f1f387a36083ad0b089bfd.xz.gpg").
      map(si => new String(si.get.body.cleansed.array(), "UTF-8")).
      map(Pipeline.run(_)).
      flatMap(x => x.toArray()).
      filter(p => {
        val triple = p.asInstanceOf[Triple]
        triple.entity0
      })

  }

}