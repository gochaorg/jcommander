package xyz.cofe.term.ui

import xyz.cofe.term.common.Size
import xyz.cofe.term.ui.prop._
import xyz.cofe.term.ui.prop.color._
import xyz.cofe.term.ui.paint._

class Label extends Widget 
  with LocationRWProp
  with SizeRWProp
  with FillBackground
  with TextProperty
  with ForegroundColor
  with PaintText:
    def this(text:String) = {
      this()
      this.text = text
      this.size = Size(text.length(),1)
    }