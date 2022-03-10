package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.input.KeyStroke

trait InputProcess {
  def input( state:State.Work, ks:KeyStroke ):State
}

object InputProcess {
  def dummy:InputProcess = new InputProcess {
    override def input(state: State.Work, ks: KeyStroke): State = state
  }
  
  def dummy( exitIf: KeyStroke=>Boolean ):InputProcess = new InputProcess {
    override def input(state: State.Work, ks: KeyStroke): State = {
      if( exitIf(ks) ){
        state.finish()
      }else{
        state
      }
    }
  }
}
