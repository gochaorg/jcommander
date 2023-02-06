package xyz.cofe.term.ui
package ses

import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

import xyz.cofe.metric._

trait SesJobs:
  protected val jobs:Queue[()=>Unit] = new ConcurrentLinkedQueue()

  private val tJobExec = Metrics.tracker("SesJobs.processJobs.exec")

  protected def processJobs():Unit = 
    var stop = false
    while !stop do
      val job = jobs.poll()
      if job!=null then
        tJobExec(job())
      else
        stop = true

  def addJob( job:()=>Unit ):Unit = 
    jobs.add(job)

