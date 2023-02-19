package xyz.cofe.jtfm.bg

import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class SyncQATest extends munit.FunSuite:
  test("sync test") {
    val sa = SyncQA[String,String]
    val childTh = Thread(()=>{
      println(s"[${Thread.currentThread().getId()}] question 1")
      sa.question("question 1 ?") match
        case Left(value) =>  println(s"[${Thread.currentThread().getId()}] err: $value")
        case Right(value) => println(s"[${Thread.currentThread().getId()}] answer: $value")      

      println(s"[${Thread.currentThread().getId()}] question 2")
      sa.question("question 2 ?") match
        case Left(value) =>  println(s"[${Thread.currentThread().getId()}] err: $value")
        case Right(value) => println(s"[${Thread.currentThread().getId()}] answer: $value")      

      println(s"[${Thread.currentThread().getId()}] question 3")
      sa.question("question 3 ?") match
        case Left(value) =>  println(s"[${Thread.currentThread().getId()}] err: $value")
        case Right(value) => println(s"[${Thread.currentThread().getId()}] answer: $value")      
    })
    childTh.setDaemon(true)
    childTh.start()
    
    val num = new AtomicInteger(0)
    val lsTh = sa.listen(Duration.ofSeconds(5), Duration.ofSeconds(1)) { question => 
      println(s"[${Thread.currentThread().getId()}] listen accept: $question")

      val n = num.incrementAndGet() % 3
      println(s"[${Thread.currentThread().getId()}] n=$n")
      if n==0 then Some(s"response of $question") else None
    }

    println(s"wait childTh")
    childTh.join()

    println(s"wait ls")
    lsTh.interrupt()
    lsTh.join()

    println("finish")
  }
