package xyz.cofe.jtfm

import com.googlecode.lanterna.terminal.Terminal
import java.util.concurrent.atomic.AtomicBoolean

class SessionManager[S](){
  private var _sessions: List[S] = List()

  def registry( ses:S ):Unit =
    _sessions = ses :: _sessions

  def sessions:List[S] = _sessions
  
  def terminateAll(using t:Terminable[S]):Unit = {
    _sessions.foreach { t.terminate(_) }
  }

  def listen( accept:()=>Option[S] ):Listener2[S] = {
    val ls = Listener2(this,accept)
    ls.setDaemon(true)
    ls.setName("session listener")
    ls.start()
    ls
  }
}

class Listener2[S]( val sessMgr:SessionManager[S], accept: ()=>Option[S] ) extends Thread {
  private val stopFlag:AtomicBoolean = AtomicBoolean(false)

  def stopDeffered():Unit = {      
    stopFlag.set(true)
  }

  def stopNow():Unit = {
    if( Thread.currentThread.getId == getId ){
      throw new IllegalThreadStateException("can't call stopNow from same thread")
    }
    stopDeffered()
    join()
  }

  override def run():Unit = {
    println("Listener started")
    while(!stopFlag.get){
      accept() match {
        case Some(ses) => sessMgr.registry(ses)
        case _ =>
      }
    }
    println("Listener stopped")
  }
}
