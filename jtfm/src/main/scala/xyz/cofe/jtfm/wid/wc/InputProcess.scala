package xyz.cofe.jtfm.wid.wc

import xyz.cofe.jtfm.wid.Widget
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import xyz.cofe.jtfm.Navigate

trait InputProcess {
  def focusOwner:Option[Widget[_]] = None
  def input( state:State.Work, ks:KeyStroke ):State
}

object InputProcess {
  sealed trait DummyBuilder {
    def build():InputDummy
  }
  
  case class DummyBuilder0( val exitIf: KeyStroke=>Boolean ) extends DummyBuilder {
    override def build(): InputDummy = {
      new InputDummy().exitIf(exitIf)
    }
    
    def focusManager[W <: Widget[_]]( fm:FocusManager[W] ):DummyBuilder1[W] = DummyBuilder1(this, fm)
    def focusManager[W <: Widget[_]]( root:W, navigate:Navigate[W] ):DummyBuilder1[W] = DummyBuilder1(this, new FocusManager[W](root, navigate))
  }
  
  case class DummyBuilder1[W <: Widget[_]]( val prnt: DummyBuilder0, val fm:FocusManager[W] ) extends DummyBuilder {
    override def build(): InputDummy2[W] = {
      new InputDummy2(fm).exitIf(prnt.exitIf)
    }
  }
  
  def dummy( exitIf: KeyStroke=>Boolean ):DummyBuilder0 = {
    DummyBuilder0( exitIf )
  }
}
