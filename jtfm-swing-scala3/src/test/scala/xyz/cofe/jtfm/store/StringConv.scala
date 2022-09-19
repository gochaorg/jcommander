package xyz.cofe.jtfm.store

trait ToString[T] {
  def toString(t:T):String
}

trait FromString[T,E] {
  def fromString(str:String):Either[E,T]
}
