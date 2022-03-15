package xyz.cofe.jtfm.wid.wc

import xyz.cofe.jtfm.Navigate
import xyz.cofe.jtfm.wid.{FocusProperty, Widget}
import xyz.cofe.jtfm.wid.Widget.*
import xyz.cofe.jtfm.*

class FocusManager[W <: Widget[_]]
(
  val root: W,
  val navigate: Navigate[W]
) {
  private var focus_owner:Option[W] = None
  def focusOwner:Option[Widget[_]] = focus_owner
  
  private val focusableFilter: W=>Boolean = w => w.isInstanceOf[FocusProperty[_]] // && w.asInstanceOf[Focusable].focusable
  private def findInitialFocus:Option[W] =
    navigate.forwardIterator(root).find( focusableFilter )
  
  def next( wid:W ):Option[W] =
    navigate.forwardIterator(wid).drop(1).find( focusableFilter )
  def prev( wid:W ):Option[W] =
    navigate.backwardIterator(wid).drop(1).find( focusableFilter )
  
  private def visible( w:W ):Boolean = !w.widgetPath.map( _.visible.value ).contains( false )
  
  case class Switched( from:Option[W], to:Option[W] )
  def switchTo( w:W ):Option[Switched] = {
    (visible(w) && focusableFilter(w)) match {
      case false => None
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
        
        Some(Switched(old_focus,focus_owner))
    }
  }
}
