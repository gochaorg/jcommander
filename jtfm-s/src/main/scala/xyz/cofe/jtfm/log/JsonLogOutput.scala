package xyz.cofe.jtfm.log

import java.io.Closeable
import xyz.cofe.json4s3.derv._
import xyz.cofe.json4s3.stream.ast.AST
import java.time.Instant
import xyz.cofe.jtfm.json.given
import JsonLogOutput._

class JsonLogOutput[A:ToJson]( out:Appendable ) extends Closeable with Function[A,Unit]:
  def apply(v1: A): Unit = write(v1)

  def write( entry:A ):Unit = {
    out.synchronized {
      val entryExtra = new Event[A](
        entry,
        Instant.now,
        ThreadInfo(
          Thread.currentThread().getId().toString(),
          Thread.currentThread().getName()
        )
      )
      out.append( entryExtra.json ).append("\n")
    }
  }

  def close(): Unit = 
    out.synchronized {
      if out.isInstanceOf[AutoCloseable] then out.asInstanceOf[AutoCloseable].close()
    }

object JsonLogOutput:
  case class Event[A](
    data: A,
    time: Instant,
    thread: ThreadInfo
  )
  case class ThreadInfo(
    id: String,
    name: String
  )

  