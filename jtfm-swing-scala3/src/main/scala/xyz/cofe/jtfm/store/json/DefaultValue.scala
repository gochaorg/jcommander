package xyz.cofe.jtfm.store.json

trait DefaultValue[T]:
  def defaultValue:Option[T]

object DefaultValue:
  given [T]: DefaultValue[T] with
    def defaultValue: Option[T] = None