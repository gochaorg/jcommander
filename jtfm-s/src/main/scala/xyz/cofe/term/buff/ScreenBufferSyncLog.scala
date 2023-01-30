package xyz.cofe.term.buff

trait ScreenBufferSyncLog:
  def apply(string:String):Unit

object ScreenBufferSyncLog:
  given defaultInstance:ScreenBufferSyncLog with
    override def apply(string: String): Unit = ()

  def simple(out:Appendable):ScreenBufferSyncLog =
    new ScreenBufferSyncLog {
      override def apply(string: String): Unit = 
        out.append(string).append("\n")
    }
