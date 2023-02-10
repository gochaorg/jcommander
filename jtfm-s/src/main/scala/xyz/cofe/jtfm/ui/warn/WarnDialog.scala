package xyz.cofe.jtfm.ui
package warn

import xyz.cofe.term.ui.Dialog
import xyz.cofe.term.ui.Label
import xyz.cofe.term.ui.prop.bind
import xyz.cofe.term.geom.Rect
import xyz.cofe.term.ui.conf.DialogColorConf
import xyz.cofe.term.common.Color

object WarnDialog:
  def message(message:String):Message = Message(message,None)
  
  case class Message(message:String, title:Option[String]):
    def title(string:String):Message = copy(title=Some(string))
    def show:Unit =
      implicit val color = DialogColorConf.defaultConf.copy(background = Color.Red, contentBg = Color.Red, titleFg = Color.WhiteBright, foreground=Color.WhiteBright)

      Dialog
        .title(title.getOrElse("warning"))
        .size(36,15)
        .content { (pnl,hdl) => 
          val label = Label(message)
          label.foregroundColor = color.foreground
          label.backgroundColor = color.background

          pnl.children.append(label)
          label.bind(pnl){ b => Rect(0,0,b.width,b.height)}
        }
        .open()
