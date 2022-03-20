package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.graphics.TextGraphics

/**
 * Корень для виджетов
 */
final case class VirtualWidgetRoot() 
extends Widget[VirtualWidgetRoot] 
with BackgroundProperty[VirtualWidgetRoot]
with OpaqueProperty[VirtualWidgetRoot]
{
  override def render(gr:TextGraphics):Unit = {
    this.renderOpaque(gr)
  }
}