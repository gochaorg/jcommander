package xyz.cofe.jtfm

import com.googlecode.lanterna.terminal.Terminal
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Менеджер сессий
 * @tparam S сессия
 */
class SessionManager[S](){
  private var _sessions: List[S] = List()
  
  /**
   * Регистрация сессии
   * @param ses сессия
   */
  def registry( ses:S ):Unit =
    _sessions = ses :: _sessions
  
  /**
   * Удаление сессии
   * @param ses сессия
   */
  def remove( ses:S ):Unit =
    _sessions = _sessions.filter { s => s!=ses }
  
  /**
   * Удаление сессии
   * @param ses сессии
   */
  def remove( ses:Seq[S] ):Unit =
    _sessions = _sessions.filter { s => !ses.contains(s) }
  
  /**
   * Удаление сессиий
   * @param sesForRemove фильтр-функция, какие сессии удалять
   */
  def remove( sesForRemove:S=>Boolean ):Unit =
    _sessions = _sessions.filterNot(sesForRemove)
  
  /**
   * Возвращает список сессий
   * @return сессии
   */
  def sessions:List[S] = _sessions
  
  /**
   * Остановака сессии
   * @param ses сессия
   * @param t прерываетль сессии
   */
  def terminate( ses:S )(using t:Terminable[S]):Unit = {
    val terminated = _sessions
      .filter { _ == ses }
      .map { ses => t.terminate(ses); ses }
    _sessions = _sessions.filter { s => !terminated.contains(s) }
  }
  
  /**
   * Остановка всех сессий
   * @param t прерываетль сессии
   */
  def terminateAll(using t:Terminable[S]):Unit = {
    _sessions.foreach { t.terminate(_) }
  }
  
  /**
   * Прием входящих соединений
   * @param accept функция приема сообщений
   * @return Слушатель (thread)
   */
  def listen( accept:()=>Option[S] ):Listener2[S] = {
    val ls = Listener2(this,accept)
    ls.setDaemon(true)
    ls.setName("session listener")
    ls.start()
    ls
  }
}

/**
 * Слушаетель (thread), принимает входящие запросы сессий
 * @param sessMgr менеджер сессий
 * @param accept функция приема сессии
 * @tparam S сессия
 */
class Listener2[S]( val sessMgr:SessionManager[S], accept: ()=>Option[S] ) extends Thread {
  private val stopFlag:AtomicBoolean = AtomicBoolean(false)
  
  /**
   * Остановка прослушиания, асинхронно
   */
  def stopDeffered():Unit = {      
    stopFlag.set(true)
  }
  
  /**
   * Остановка прослушиания, синхронно
   */
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
