package xyz.cofe.jtfm.gr

import com.googlecode.lanterna.TerminalSize

final case class Size2D(width: Int, height:Int) {
  require(width>=0)
  require(height>=0)
}

object Size2D {
  def apply(rect:Rect):Size2D = Size2D(rect.width, rect.height)
  implicit def terminalSize2Size2D( termSize:TerminalSize ):Size2D = Size2D(termSize.getColumns, termSize.getRows)
  implicit def size2D2TerminalSize( size2D:Size2D ):TerminalSize = new TerminalSize(size2D.width, size2D.height)
}