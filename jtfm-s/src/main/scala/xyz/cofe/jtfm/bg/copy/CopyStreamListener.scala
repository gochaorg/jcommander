package xyz.cofe.jtfm.bg.copy

trait CopyStreamListener:
  def started():Unit
  def progress(writed:Long):Unit
  def stopped():Unit

object CopyStreamListener:
  given defaultListener:CopyStreamListener with
    override def started(): Unit = ()
    override def progress(writed: Long): Unit = ()
    override def stopped(): Unit = ()