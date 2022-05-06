package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.graphics.TextGraphics

trait Render {
  def render( gr:TextGraphics ):Unit = ()
}
