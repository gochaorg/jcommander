package xyz.cofe.jtfm.bg.exch

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object SenderTest: 
  case class Req(string:String) extends AnyVal
  case class Res(string:String) extends AnyVal

class SenderTest extends munit.FunSuite:
  import SenderTest._

  test("test") {
    val stopFlag = new AtomicBoolean(false)
    val idSeq = new AtomicInteger(0)

    val input  : Queue[(Long,Res)] = Queue.build
    val output : Queue[(Long,Req)] = Queue.build

    val sender : SyncSender[Req,Res,Long] = SyncSender(output, input)

    val reciver : AsyncReciver[Req,Res,Long] = AsyncReciver(output, input, (event, respone) => {
      println(s"[${Thread.currentThread().getName()}] accept $event")
      respone(Res(event.string))
      event.string match
        case "exit" => stopFlag.set(true)
        case _ => ()
    })

    val recieverThread = new Thread(){
      override def run(): Unit = {
        while ! stopFlag.get() do
          reciver.poll
          Thread.sleep(25)
      }
    }
    recieverThread.setDaemon(true)
    recieverThread.setName("reciever")
    recieverThread.start()

    val senderThread = new Thread(){
      def send(msg:String):Unit = {
        val req = Req(msg)
        println(s"[${Thread.currentThread().getName()}] send $req")
        
        val res = sender(req)
        println(s"[${Thread.currentThread().getName()}] res $res")
      }
      override def run(): Unit = {
        send("hello")
        send("next")
        send("exit")
      }
    }
    senderThread.setDaemon(true)
    senderThread.setName("sender")
    senderThread.start()

    recieverThread.join()
    senderThread.join()
  }
