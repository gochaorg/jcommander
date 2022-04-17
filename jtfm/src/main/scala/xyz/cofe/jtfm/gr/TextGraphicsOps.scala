package xyz.cofe.jtfm.gr

import com.googlecode.lanterna.graphics.TextGraphics

implicit class TextGraphicsOps( gr:TextGraphics ):
  def draw( rect:Rect, str:String, halign:Align, valign:Align ):Unit = {
    val lines = rect.size.align(str,halign, valign)
    (0 until rect.height).zip(lines).foreach { (y,line) => gr.putString(rect.left, rect.top+y, line) }
  }

  def draw( rect:Rect, str:String, halign:Align ):Unit = {
    val lines = rect.size.align(str,halign)
    (0 until rect.height).zip(lines).foreach { (y,line) => gr.putString(rect.left, rect.top+y, line) }
  }
