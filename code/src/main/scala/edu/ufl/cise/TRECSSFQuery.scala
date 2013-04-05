package edu.ufl.cise

object TRECSSFQuery {

  def main(args: Array[String]): Unit = {

    class Relation(entity0: String, slot: String, entity1: String) {
      override def toString(): String =
        {
          val s = entity0 + " - " + slot + " - " + entity1
          return s
        }
    }

    val query = new Relation("Abraham Linon", "is president of", "United States")
    Faucet.getStreams("2012-05-02", 0).
      map(si => new String(si.get.body.cleansed.array(), "UTF-8")).
      map(Pipeline.run(_)).
      flatMap(x => x.toArray())
     // filter(_.)
      

  }

}