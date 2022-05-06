package xyz.cofe.jtfm.wid.wc

import java.util.concurrent.ConcurrentLinkedQueue
import xyz.cofe.jtfm.wid.WidgetCycle

/**
 * Задачи обрабатываемые в цикле обработки
 */
class Jobs {
  private val queue = new ConcurrentLinkedQueue[() => Unit]()

  /**
   * Добавление задачи в очередь
   */
  def add( job: ()=>Unit ):Unit = {
    queue.add(job)
  }
  
  /** Обработка очереди задач */
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

/**
 * Добавление задач в очередь исполнения
 */
object Jobs {
  /**
   * Добавление задачи в очередь
   * @param job задача
   */
  def add( job: => Unit ):Unit = {
    WidgetCycle.tryGet match {
      case None =>
      case Some(wc) => wc.jobs match {
        case None =>
        case Some(jobs) =>
          jobs.add( ()=>{ job } )
      }
    }
  }
}