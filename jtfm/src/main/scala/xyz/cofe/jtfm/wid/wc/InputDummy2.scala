package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.input.{KeyStroke, KeyType, MouseAction}
import xyz.cofe.jtfm.wid.{FocusProperty, Widget}
import xyz.cofe.jtfm.wid.Widget.*
import xyz.cofe.jtfm.gr.Point

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
    ks match {
      case ma: MouseAction =>
        fm.navigate.last(fm.root) match {
          case None =>
          case Some(last_w) =>
            fm.navigate.backwardIterator(last_w).find { wid =>
              val mma : MouseAction = ma
              val abs : Point = Point(mma.getPosition)
              val local : Point = abs toLocal wid
              val x = wid.rect.value.size.include(local)
              x
            } match {
              case None =>
              case Some( wid ) =>
                val mma : MouseAction = ma
                val abs : Point = Point(mma.getPosition)
                val local : Point = abs toLocal wid
                val local_ma = new MouseAction(mma.getActionType, mma.getButton, local)
                wid.input(local_ma) match {
                  case false =>
                  case true =>
                    wid match {
                      case f:FocusProperty[_] =>
                        fm.switchTo(wid) match {
                          case Some(sw) =>
                          case None =>
                        }
                      case _ =>
                    }
                }
            }
        }
      case _ =>
    }
    super.input(state, ks)
  }
}
