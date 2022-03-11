package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.input.{KeyStroke, KeyType}

trait InputProcess {
  def input( state:State.Work, ks:KeyStroke ):State
}

class InputDummy extends InputProcess {
  var exitIf: KeyStroke=>Boolean = _ => false
  
  def exitIf( e:KeyStroke=>Boolean ):InputDummy = {
    exitIf = e
    this
  }
  
  var handlers = List[KeyStroke => Unit]()
  
  def handler( kt: KeyType, ls: KeyStroke => Unit ):InputDummy = {
    handlers = handlers :+ (ks => {
      if( ks.getKeyType == kt ){
        ls(ks)
      }
    })
    this
  }
  
  override def input(state: State.Work, ks: KeyStroke): State = {
    handlers.foreach( h => h(ks) )
    if( exitIf(ks) ){
      state.finish()
    }else{
      state
    }
  }
}

object InputProcess {
  def dummy:InputProcess = new InputProcess {
    override def input(state: State.Work, ks: KeyStroke): State = state
  }
  
  def dummy( exitIf: KeyStroke=>Boolean ):InputDummy = {
    new InputDummy().exitIf(exitIf)
  }
}
