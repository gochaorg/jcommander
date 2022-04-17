package xyz.cofe.jtfm.wid.wc

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Задачи обрабатываемые в цикле обработки
 */
class Jobs {
  private val queue = new ConcurrentLinkedQueue[() => Unit]()

  def add( job: ()=>Unit ):Unit = {
    queue.add(job)
  }
  
  def run( state: State.Work ):State = {
    var cnt = 0
    var stop = false
    while( !stop ) {
      val e = queue.poll()
      if (e == null) {
        stop = true
      } else {
        try {
          e()
        } catch {
          case e:Throwable =>
            println(e)
        }
        cnt += 1
      }
    }
    state
  }
}
