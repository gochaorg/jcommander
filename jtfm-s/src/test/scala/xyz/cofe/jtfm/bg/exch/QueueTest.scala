package xyz.cofe.jtfm.bg.exch

import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue

class QueueTest extends munit.FunSuite:
  test("sync read") {
    val log = ConcurrentLinkedQueue[(Long,String)]()

    val queue = Queue.build[String]
    val thWriter = new Thread() { override def run(): Unit = {
      log.add((Thread.currentThread().getId(), "hello"))
      queue.put("hello")      
      Thread.sleep(500)

      log.add((Thread.currentThread().getId(), "second"))
      queue.put("second")
      Thread.sleep(3500)

      log.add((Thread.currentThread().getId(), "exit"))
      queue.put("exit")
    }}
    val thReader = new Thread() { override def run(): Unit = {
      var stop = false
      while ! stop do
        queue.read(Duration.ofSeconds(2), Duration.ofMillis(100)) match
          case Some(value) => 
            log.add((Thread.currentThread().getId(), s"accept $value"))
            println(s"accept $value")
            value match
              case "exit" => stop = true
              case _ => ()
          case None => 
            log.add((Thread.currentThread().getId(), s"no response"))
            println("no response")
    }}

    thReader.start()
    thWriter.start()

    thReader.join()
    thWriter.join()

    var lst = List.empty[(Long,String)]
    log.forEach((id,str)=> lst = lst :+ (id,str))

    assert( lst.contains((thReader.getId(), "no response")) )
    assert( lst.contains((thReader.getId(), "accept exit")) )
  }
