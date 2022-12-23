package xyz.cofe.term.ui

import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

trait SesJobs:
  protected val jobs:Queue[()=>Unit] = new ConcurrentLinkedQueue()

  protected def processJobs():Unit = 
    var stop = false
    while !stop do
      val job = jobs.poll()
      if job!=null then
        job()
      else
        stop = true

  def addJob( job:()=>Unit ):Unit = 
    jobs.add(job)

