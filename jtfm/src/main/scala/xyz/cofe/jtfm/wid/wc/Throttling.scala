package xyz.cofe.jtfm.wid.wc

/** Дросселирование тактов (тротлинг) */
sealed trait Throttling {
  def apply( state:State ):State
}
object Throttling {
  case class Sleep(time:Long) extends Throttling {
    override def apply( state:State ):State = {
      if( time>0 ){
        try {
          Thread.sleep(time)
          state
        } catch {
          case e:java.lang.InterruptedException => 
            state match {
              case s:State.Work => s.stop()
              case _ => state
            }
        }
      }else{
        state
      }
    }
  }
}
