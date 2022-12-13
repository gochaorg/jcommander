package xyz.cofe.term.buff

import xyz.cofe.term.common.Position

case class CharDifference(
  pos: Position,
  left:Option[ScreenChar],
  right:Option[ScreenChar]
)

