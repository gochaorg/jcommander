package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.input.{KeyStroke, KeyType, MouseAction}
import xyz.cofe.jtfm.wid.{FocusProperty, Widget}
import xyz.cofe.jtfm.wid.Widget.*
import xyz.cofe.jtfm.gr.Point
import xyz.cofe.jtfm._
import xyz.cofe.jtfm.LikeTreeOps

class InputDummy2( val fm:FocusManager[Widget[_]] ) extends InputDummy {
  override def focusOwner: Option[Widget[_]] = fm.focusOwner
  
  override def exitIf( e:KeyStroke=>Boolean ):InputDummy2 = {
    super.exitIf(e)
    this
  }
  
  override def handler( kt: KeyType, ls: KeyStroke => Unit ):InputDummy2 = {
    super.handler(kt,ls)
    this
  }
  
  override def input(state: State.Work, ks: KeyStroke): State = {
    ks match {
      case ma: MouseAction =>
        // обход в обратном порядке рендере
        fm.navigate.last(fm.root) match {
          case None =>
          case Some(last_w) =>
            val widInputProcessed = fm.navigate.backwardIterator(last_w).map { wid =>
              val mma : MouseAction = ma
              val abs : Point = Point(mma.getPosition)
              val local : Point = abs toLocal wid
              val x = wid.rect.value.size.include(local)
              // вычисление локальных координат
              (x, wid, local)
            }.filter { case(matched,wid,local) =>
              matched
            }.map { case(matched,wid,local) =>
              val mma : MouseAction = ma
              val local_ma = new MouseAction(mma.getActionType, mma.getButton, local)
              // посылка сигнала в виджет
              val x = wid.input(local_ma)
              (x, wid)
            }.find( _._1 ).map( _._2 )

            // смена фокуса, если владелец != виджет получивший сигнал
            val w = (widInputProcessed match {
              case Some(wid) =>
                if( fm.focusOwner.map( fo => fo == wid ).getOrElse( false ) ) {
                  List()
                }else{
                  wid.widgetPath
                }
              case None => List()
            }).reverse
              .find( _.isInstanceOf[FocusProperty[_]] )
              .map( x => (x.asInstanceOf[FocusProperty[_]], x) )
              .foreach{ case(f,wid)=>fm.switchTo(wid) }
        }
        super.input(state, ks)
      case _ =>
        ks.getKeyType match {
          // Смена фокуса
          case KeyType.Tab | KeyType.ReverseTab =>
            fm.focusOwner match {
              case None => super.input(state, ks)
              case Some(fo) =>
                if( !fo.input(ks) ){
                  
                  val from = fm.focusOwner match {
                    case None => fm.root
                    case Some(fo) => fo
                  }
//                  val findNext = if ks.getKeyType == KeyType.Tab then fm.next else fm.prev
//                  findNext(from) match {
//                    case None =>
//                    case Some(next) =>
//                      fm.switchTo(next)
//                  }
                  
                  val fnext = if ks.getKeyType == KeyType.Tab then fm.nextCycle else fm.prevCycle
                  fnext(from).take(1).foreach { next =>
                    fm.switchTo(next)
                  }
                  
                  super.input(state, ks)
                }else{
                  super.input(state, ks)
                }
            }
          case _ =>
            fm.focusOwner match {
              case None =>
              case Some(fo) =>
                fo.input(ks)
            }
            super.input(state, ks)
        }
    }
  }
}
