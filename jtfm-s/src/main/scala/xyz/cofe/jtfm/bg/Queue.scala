package xyz.cofe.jtfm.bg

import java.time.Duration

trait Queue[A]:
  def put(a:A):Unit
  def poll:Option[A]
  def read(timeout:Duration, throttle:Duration):Option[A]
  def peek:Option[A]
  def size:Int

object Queue:
  def build[A]:Queue[A] = Simple()

  class Simple[A] extends Queue[A]:
    @volatile private var _size : Int = 0
    @volatile private var events : List[A] = List.empty
    def size:Int = _size
    def poll: Option[A] = 
      this.synchronized {
        val res = events.headOption
        if events.nonEmpty then events = events.tail
        res
      }    
    def peek: Option[A] = {
      this.synchronized {
        events.headOption
      }
    }
    def read(timeout:Duration, throttle:Duration):Option[A] = 
      this.synchronized {
        if events.nonEmpty
        then 
          val res = events.headOption
          events = events.tail
          res
        else
          val started = System.currentTimeMillis()
          val limitTime = started + timeout.toMillis()
          var result : Option[A] = None
          while (System.currentTimeMillis() < limitTime) && result==None do
            this.wait(throttle.toMillis())
            if events.nonEmpty
            then
              result = events.headOption
              events = events.tail
          result
      }
    def put(a:A):Unit =
      this.synchronized {
        events = events :+ a
        _size = events.size
        this.notifyAll()
      }
    