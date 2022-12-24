package xyz.cofe.term.geom

import xyz.cofe.term.common.Position

extension (pos:Position)
  def diff(other:Position):Position =
    Position(pos.x - other.x, pos.y - other.y)
