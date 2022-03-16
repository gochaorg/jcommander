package xyz.cofe.jtfm.wid.wc

import xyz.cofe.jtfm.wid.{FocusProperty, Widget}
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import xyz.cofe.jtfm.Navigate

trait InputProcess {
  def focusOwner:Option[Widget[_]] = None
  def focusRequest( target:Widget[_] ):Either[String,Option[Widget[_]]] = Left("Not implement")
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
    
    def focusManager( fm:FocusManager[Widget[_]] ):DummyBuilder1 = DummyBuilder1(this, fm)
    def focusManager( root:Widget[_], navigate:Navigate[Widget[_]] ):DummyBuilder1 = DummyBuilder1(this, new FocusManager(root, navigate))
  }
  
  case class DummyBuilder1( val prnt: DummyBuilder0, val fm:FocusManager[Widget[_]] ) extends DummyBuilder {
    override def build(): InputDummy2 = {
      new InputDummy2(fm).exitIf(prnt.exitIf)
    }
  }
  
  def dummy( exitIf: KeyStroke=>Boolean ):DummyBuilder0 = {
    DummyBuilder0( exitIf )
  }
}
