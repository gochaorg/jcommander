package xyz.cofe.jtfm

import xyz.cofe.jtfm.ev.CollectionWithNotify

trait Nested[SELF,N] {
  lazy val nested: CollectionWithNotify[SELF,N]
}
