package xyz.cofe.jtfm.bg.exch

import java.time.Duration

class SyncQA[Q,A]():
  enum Err:
    case NoResponse

  @volatile private var question:Option[Q] = None
  @volatile private var answer:Option[A] = None

  def question( q:Q ):Either[Err,A] =
    this.synchronized {
      question = Some(q)
      answer = None
      this.notifyAll()
      this.wait()
      answer match
        case None => Left(Err.NoResponse)
        case Some(value) => Right(value)
    }

  def listen( timeout:Duration, repeatDelay:Duration=Duration.ofMillis(50) )( listener:Q=>Option[A] ):Thread = 
    val th = Thread( ()=>{

      var stopMain = false
      while !stopMain do
        try
          this.synchronized {
            this.wait(2000)
            question.foreach { q => 
              var stop = false
              val limit = timeout.toMillis()
              val started = System.currentTimeMillis()
              while (!stop) && (System.currentTimeMillis() - started) < limit do
                listener(q) match
                  case None => 
                    Thread.sleep(repeatDelay.toMillis())
                  case Some(value) =>
                    answer = Some(value)
                    stop = true
            }
            this.notifyAll()
          }
        catch
          case e:InterruptedException =>
            stopMain = true
    })

    th.setDaemon(true)
    th.start()
    th

