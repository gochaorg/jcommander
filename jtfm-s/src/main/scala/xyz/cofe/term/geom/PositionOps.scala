package xyz.cofe.term.geom

import xyz.cofe.term.common.Position

extension (pos:Position)
  def diff(other:Position):Position =
    Position(pos.x - other.x, pos.y - other.y)

  def +(other:Position):Position =
    Position(pos.x + other.x, pos.y + other.y)

  def +(other:(Int,Int)):Position =
    Position(pos.x + other._1, pos.y + other._2)

  def xMirror:Position = Position(-pos.x,  pos.y)
  def yMirror:Position = Position( pos.x, -pos.y)
  def mirror:Position  = Position(-pos.x, -pos.y)