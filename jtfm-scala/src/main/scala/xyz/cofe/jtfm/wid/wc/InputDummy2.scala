package xyz.cofe.jtfm.wid.wc

import com.googlecode.lanterna.input.{KeyStroke, KeyType, MouseAction}
import xyz.cofe.jtfm.wid.{FocusProperty, Widget}
import xyz.cofe.jtfm.wid.Widget.*
import xyz.cofe.jtfm.gr.Point
import xyz.cofe.jtfm._
import xyz.cofe.jtfm.tree._
import xyz.cofe.jtfm.wid.wc.FocusManager.Switched
import xyz.cofe.jtfm.wid.cmpt.Dialog
import xyz.cofe.jtfm.wid.VirtualWidgetRoot
import org.slf4j.LoggerFactory

/**
 * Обработка ввода
 */
class InputDummy2( val fm:FocusManager[Widget[_]] ) extends InputDummy {
  protected val log = LoggerFactory.getLogger(classOf[InputDummy2])

  override def focusOwner: Option[Widget[_]] = fm.focusOwner
  
  override def focusRequest( target:Widget[_] ):Either[String,Option[Widget[_]]] = {
    dialogHolder match {
      case None => fm.switchTo(target).map(_.from)
      case Some(dlg) =>
        if( !target.widgetPath.exists( w => w==target ) ){
          Left("can't switch outside dialog")
        }else{
          fm.switchTo(target).map(_.from)
        }
    }
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
        processKeyboard(state, ks)
    }
  }

  private def isVisible(widget:Widget[_]):Boolean = {
    val wpath = widget.widgetPath
    val allVisible = wpath.map(_.visible.value).foldLeft(true)((a,b)=>a&&b)
    val prntIsRoot = wpath.headOption.map { w => w.isInstanceOf[VirtualWidgetRoot] }.getOrElse( false )
    allVisible && prntIsRoot
  }

  /** Текущий диалог */
  private def dialogHolder:Option[Dialog] = 
    fm.focusOwner.flatMap { fowner =>
      fowner.widgetPath.reverse.find( w => w.isInstanceOf[Dialog] && isVisible(w) )
      .map( _.asInstanceOf[Dialog] )      
    }

  private def visibleWidgets:List[Widget[_]] = 
    fm.root.widgetTree.toList.reverse.filter(isVisible)

  /** 
   * Обработка события мыши.
   * 
   * - События мыши должны попадать в видимые элементы
   * - Если есть диалог сожержащий фокус ввода, 
   *     то события не должны попадать в элементы которые вне этого диалога
   */
  private def processMouseAction( state:State.Work, ma:MouseAction ):State = {
    val dlgHolder = dialogHolder
    log.info(s"processMouseAction( x=${ma.getPosition.getColumn} y=${ma.getPosition.getRow} but=${ma.getButton} atype=${ma.getActionType} )")

    if( log.isTraceEnabled ){
      log.trace("tree")
      fm.root.widgetTree.foreach { w =>       
        log.trace( 
          "..".repeat(w.widgetPath.length-1) +
          w+":"+w.getClass.getName +
          " "+w.rect.value+
          " visible="+w.visible.value+
          " parent="+w.parent.value
        )
      }
    }

    // обход в обратном порядке рендере
    fm.navigate.last(fm.root) match {
      case None =>
      case Some(last_w) =>
        val widInputProcessed = visibleWidgets.map { wid =>
          val mma : MouseAction = ma
          val abs : Point = Point(mma.getPosition)
          val local : Point = abs toLocal wid          
          val mouseAtWidget = wid.rect.value.size.include(local)
          val dialogMatch = dlgHolder match {
            case Some(dlg) if dlg.visible.value =>
              wid.widgetPath.exists(w => w==dlg)
            case _ => true
          }
          if( log.isTraceEnabled )log.trace( s"$wid mouseAtWidget=$mouseAtWidget dialogMatch=$dialogMatch abs=$abs local=$local rect=${wid.rect}" ) 
          // вычисление локальных координат
          (mouseAtWidget && dialogMatch, wid, local)
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

  /** Переключение фокуса на следующий элемент */
  def focusSwitchNext():Either[String,FocusManager.Switched[_]] = {
    log.info("focusSwitchNext()")
    fm.nextCycle( fm.focusOwner.getOrElse( fm.root ) ).take(1).nextOption match {
      case None => Left("can't take focus owner")
      case Some(w) => 
        dialogHolder match {
          case Some(dlg) => w.widgetPath.exists(w2 => w2==dlg) match {
            case true => fm.switchTo(w)
            case false => Left("can't switch outside border")
          }
          case _ => fm.switchTo(w)
        }
    }
  }

  /** Переключение фокуса на следующий элемент */
  def focusSwitchPrev():Either[String,FocusManager.Switched[_]] = {
    log.info("focusSwitchPrev()")
    fm.prevCycle( fm.focusOwner.getOrElse( fm.root ) ).take(1).nextOption match {
      case None => Left("can't take focus owner")
      case Some(w) => 
        dialogHolder match {
          case Some(dlg) => w.widgetPath.exists(w2 => w2==dlg) match {
            case true => fm.switchTo(w)
            case false => Left("can't switch outside border")
          }
          case _ => fm.switchTo(w)
        }
    }
  }

  /**
   * обработка события клавиатуры
   */
  private def processKeyboard( state:State.Work, ks:KeyStroke ):State = {
    log.info("processKeyboard ks={}", ks)
    val defProc: ()=> State = ()=>{ 
      InputDummy2.super.input(state,ks) 
    }
    val proc : ()=>State = ()=>{
      ks.getKeyType match {
        // Смена фокуса
        case KeyType.Tab | KeyType.ReverseTab =>
          fm.focusOwner match {
            case None => 
              focusSwitchNext()
              defProc()
            case Some(fo) =>
              if( !fo.input(ks) ){

                if ks.getKeyType == KeyType.Tab then
                  focusSwitchNext()
                else
                  focusSwitchPrev()
                
                defProc()
              }else{
                defProc()
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
          defProc()
      }
    }

    val hist = collect(ks)    
    if hist.nonEmpty then
      state.keyInterceptor.accept( state, hist ) match {
        case Some(newState) => newState
        case None =>
          proc()
      }
    else
      proc()
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
