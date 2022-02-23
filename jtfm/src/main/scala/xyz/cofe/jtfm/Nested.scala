package xyz.cofe.jtfm

trait Nested[SELF,N] {
  lazy val nested: CollectionWithNotify[SELF,N]
}
