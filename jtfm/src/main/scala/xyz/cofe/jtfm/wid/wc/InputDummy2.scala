package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.input.{KeyStroke, KeyType, MouseAction}
import xyz.cofe.jtfm.wid.{FocusProperty, Widget}
import xyz.cofe.jtfm.wid.Widget.*
import xyz.cofe.jtfm.gr.Point
import xyz.cofe.jtfm._
import xyz.cofe.jtfm.tree._

/**
 * Обработка ввода
 */
class InputDummy2( val fm:FocusManager[Widget[_]] ) extends InputDummy {
  override def focusOwner: Option[Widget[_]] = fm.focusOwner
  
  override def focusRequest( target:Widget[_] ):Either[String,Option[Widget[_]]] = {
    fm.switchTo(target).map(_.from)
  }
  
  override def exitIf( e:KeyStroke=>Boolean ):InputDummy2 = {
    super.exitIf(e)
    this
  }
  
  override def handler( kt: KeyType, ls: KeyStroke => Unit ):InputDummy2 = {
    super.handler(kt,ls)
    this
  }

  private var historyLimit = 10
  private var history:List[KeyStroke] = List()

  /** 
   * Сохранение истории нажатий комбинации клавиатуры 
   * @return история, самы свежие в начале
   */
  private def collect( ks:KeyStroke ):List[KeyStroke] = {
    if historyLimit>0 then
      history = ks :: history
      if history.size > historyLimit then
        history = history.take(historyLimit)
    history
  }
  
  override def input(state: State.Work, ks: KeyStroke): State = {
    ks match {
      case ma: MouseAction =>
        processMouseAction(state,ma)
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
            // посылка события нажатия в виджет содержащий фокус
            fm.focusOwner match {
              case None =>
                broadcast(ks)
              case Some(fo) =>
                var w:Widget[?] = fo
                var stop = false
                var consumed = false
                while( !stop ){
                  if w.input(ks) then
                    consumed = true
                    stop = true
                  else
                    w.parent.value match {
                      case Some(prt) =>
                        w = prt
                        stop = false                  
                      case None =>
                        stop = true
                    }
                }
                if !consumed then {
                  broadcast(ks)
                }
            }
            super.input(state, ks)
        }
    }
  }

  /** 
   * обработка события мыши
   */
  private def processMouseAction( state:State.Work, ma:MouseAction ):State = {
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
    super.input(state, ma)
  }

  /**
  */
  private def processKeyboard( state:State.Work, ks:KeyStroke ):State = {

  }

  /** Рассылка необработанного нажатия клавиш во все видимые и принимающие Broadcast */
  private def broadcast( ks:KeyStroke ):Unit = {
    var ws:List[Widget[?]] = List(fm.root)
    while( !ws.isEmpty ){
      val w = ws.head
      ws = ws.tail
      ws = w.nested.toList ::: ws
      w match {
        case rcv:BroadcastReciver => rcv.reciveBroadcast( ks )
        case _ =>
      }
    }
  }
}
