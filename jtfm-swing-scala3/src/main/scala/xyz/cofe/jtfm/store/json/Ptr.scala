package xyz.cofe.jtfm.store.json

case class Ptr(value:Int, source: String):
  def empty:Boolean = !inside()

  def inside():Boolean =
    source match
      case null => false
      case _ => value < 0 match
        case true => false
        case _ => value >= source.length() match
          case true => false
          case false => true

  def lookup(len:Int):String =
    len <= 0 match
      case true => ""
      case _ => inside() match
        case false => ""
        case true => 
          source.substring(value, (value+len) min (source.length()) )

  def apply(off:Int):Option[Char] =
    val trgt = value+off
    if trgt<0 
      then None
      else if trgt>=source.length()
        then None
        else Some(source.charAt(trgt))
      
  def +(len:Int):Ptr = copy(value + len)

  def toEnd: Option[Ptr] =
    inside() match
      case true =>
        Some(Ptr( source.length(), source ))
      case false =>
        None

