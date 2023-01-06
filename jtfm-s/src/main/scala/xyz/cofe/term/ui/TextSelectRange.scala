package xyz.cofe.term.ui

case class TextSelectRange(from:Int, to:Int):
  require(from <= to)
  def resetTo(pos:Int):TextSelectRange = 
    new TextSelectRange(pos,pos)

  def size:Int = Math.abs(from - to)

  def extendTo( pos:Int ):TextSelectRange =
    if pos<from 
    then TextSelectRange( pos, to )
    else 
      if pos>to
      then TextSelectRange( from, pos )
      else
        this

object TextSelectRange:
  def apply(from:Int, to:Int):TextSelectRange =
    new TextSelectRange(from min to, from max to)
