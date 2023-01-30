package xyz.cofe.term.ui.ses

trait SessionLog:
  def apply(string:String):Unit

object SessionLog:
  given defaultLog:SessionLog with
    override def apply(string: String): Unit = ()

  def simple( out:Appendable ):SessionLog =
    new SessionLog {
      override def apply(string: String): Unit = 
        out.append(string).append("\n")
    }
