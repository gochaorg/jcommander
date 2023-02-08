package xyz.cofe.term.ui

import xyz.cofe.term.common.Size
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.ui.prop.color._
import xyz.cofe.term.ui.paint._
import xyz.cofe.term.ui.conf.LabelColorConf

class Label( using colors: LabelColorConf ) extends Widget 
  with LocationRWProp
  with SizeRWProp
  with FillBackground
  with TextProperty
  with ForegroundColor
  with PaintText:
    def this(text:String)(using colors: LabelColorConf) = {
      this()
      this.text = text
      this.size = Size(text.length(),1)
    }
    foregroundColor = colors.foreground
    backgroundColor = colors.background