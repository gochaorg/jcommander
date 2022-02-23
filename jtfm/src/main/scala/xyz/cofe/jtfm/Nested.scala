package xyz.cofe.jtfm

import xyz.cofe.jtfm.ev.CollectionWithNotify
import xyz.cofe.jtfm.ev.BasicCollection

trait Nested[SELF,N] {
  lazy val nested = BasicCollection[N]()
}
