package xyz.cofe.jtfm.bg.copy

trait TextOf[R]:
  def textOf(r:R):String

object TextOf:
  given TextOf[String] with
    override def textOf(r: String): String = r
