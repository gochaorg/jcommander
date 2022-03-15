package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import xyz.cofe.jtfm.wid.Widget

class InputDummy2[W <: Widget[_]]( val fm:FocusManager[W] ) extends InputDummy {
  override def focusOwner: Option[Widget[_]] = fm.focusOwner
  
  override def exitIf( e:KeyStroke=>Boolean ):InputDummy2[W] = {
    super.exitIf(e)
    this
  }
  
  override def handler( kt: KeyType, ls: KeyStroke => Unit ):InputDummy2[W] = {
    super.handler(kt,ls)
    this
  }
  
  override def input(state: State.Work, ks: KeyStroke): State = {
    super.input(state, ks)
  }
}
