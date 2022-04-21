package xyz.cofe.jtfm.wid.wc

import xyz.cofe.jtfm.Navigate
import xyz.cofe.jtfm.wid.{FocusProperty, Widget}
import xyz.cofe.jtfm.wid.Widget.*
import xyz.cofe.jtfm.*
import xyz.cofe.jtfm.wid.WidgetCycle
import scala.ref.WeakReference

/**
 * Менеджер фокуса ввода
 */
class FocusManager[W <: Widget[_]]
(
  /**
   * корневой виджет
   */
  val root: W,

  /** навигация по дереву */
  val navigate: Navigate[W]
) {
  private var focus_owner:Option[W] = None

  /** возвращает владельца фокуса */
  def focusOwner:Option[Widget[_]] = focus_owner
  
  private val focusableFilter: W=>Boolean = w => w.isInstanceOf[FocusProperty[_]] // && w.asInstanceOf[Focusable].focusable
  private def findInitialFocus:Option[W] =
    navigate.forwardIterator(root).find( focusableFilter )
  
  /** находит следующий элемент принимающий фокус */
  def next( wid:W ):Option[W] =
    navigate.forwardIterator(wid).drop(1).find( focusableFilter )

  /** находит предыдущий элемент принимающий фокус */
  def prev( wid:W ):Option[W] =
    navigate.backwardIterator(wid).drop(1).find( focusableFilter )
  
  private def visible( w:W ):Boolean = !w.widgetPath.map( _.visible.value ).contains( false )

  private var listeners:List[(FocusManager.Switched[W])=>Unit] = List()
  def onChange( ls:(FocusManager.Switched[W])=>Unit ):()=>Unit = {
    listeners = ls :: listeners
    val wr = WeakReference(ls)
    () => {
      wr.get match {
        case Some(r) =>
          listeners = listeners.filterNot( l => l==r )
          wr.clear
        case None =>
      }      
    }
  }

  /** Переключение фокуса */
  def switchTo( w:W ):Either[String,FocusManager.Switched[W]] = {
    (visible(w) && focusableFilter(w)) match {
      case false => Left("target is not visible or not focusable")
      case true =>
        val old_focus = focus_owner
        focus_owner = Some(w)
        
        val focusable = w.asInstanceOf[FocusProperty[_]]
        old_focus match {
          case Some(old_w) =>
            old_w match {
              case old_w_f:FocusProperty[_] =>
                old_w_f.focus.onLost( focus_owner )
            }
          case None =>
        }
        focusable.focus.onGain(old_focus)
        
        val event = FocusManager.Switched(old_focus,focus_owner)
        listeners.foreach( ls => ls(event) )

        Right(event)
    }
  }
  
  /** цикл смены фокуса */
  class Cycled( val from:W, val move:W=>Option[W], val continue:()=>Option[W] ) extends Iterator[W] {
    private var continueCalled = false
    private var cur:Option[W] = move(from) match {
        case Some(w) if w!=from => Some(w)
        case _ =>
          continueCalled = true
          continue()
      }
    
    def hasNext:Boolean = cur.isDefined
    def next():W = {
      val r = cur
      cur = move(r.get) match {
        case Some(w) if w!=from =>
          Some(w)
        case _ if !continueCalled =>
          continueCalled = true
          continue()
        case _ =>
          None
      }
      r.get
    }
  }
  
  def nextCycle( from:W ):Cycled = Cycled( from, next, ()=>{
    next(root) match {
      case Some(first) if first!=from =>
        Some(first)
      case _ => None
    }
  })
  def prevCycle( from:W ):Cycled = Cycled( from, prev, ()=>{
    navigate.last(root) match {
      case Some(last) =>
        last match {
          case _:FocusProperty[_] =>
            if( from!=last ){
              Some(last)
            }else{
              prev(last)
            }
        }
      case None => None
    }
  })
}

object FocusManager {
  /** Событие смены фокуса */
  case class Switched[W <: Widget[_]]( from:Option[W], to:Option[W] )
  def tryGet:Option[FocusManager[_]] = {
    for {
      wc <- WidgetCycle.tryGet
      ws <- wc.workState
      in1 <- ws.inputProcess match {
        case e:InputDummy2 =>  Some(e.fm)
        case _ => None
      }
    } yield (in1)
  }
}