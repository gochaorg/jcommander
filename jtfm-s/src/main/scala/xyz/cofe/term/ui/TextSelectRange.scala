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

  def select(string:String):String =
    if string.length()<1
    then ""
    else
      if from>=string.length()
      then ""
      else string.substring(from).take(size)

extension (string:String)
  def before(range:TextSelectRange):String =
    if range.from<1 
    then ""
    else
      string.substring(0, range.from min string.length() )

  def after(range:TextSelectRange):String =
    if range.to >= string.length()
    then ""
    else
      string.substring(range.to)

object TextSelectRange:
  def apply(from:Int, to:Int):TextSelectRange =
    new TextSelectRange(from min to, from max to)
