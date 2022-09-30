package xyz.cofe.jtfm.store.json

case class LPtr( val value:Int, val source:Seq[Token] ):
  import scala.reflect._
  def token:Option[Token]=
    val t = value
    if t>=0 && t<source.size then
      Some(source(value))
    else
      None
  def beginPtr:Ptr = token.map(_.begin).getOrElse(Ptr(-1,""))
  def endPtr:Ptr =  token match
    case Some(existsToken) => existsToken.end
    case None => (this + (-1)).token match
      case Some(preEndToken) => 
        preEndToken.end
      case None => throw new 
        RuntimeException(s"can't compute end ptr of ${this}")
      
  def token(off:Int):Option[Token]=
    val t = value+off
    if t>=0 && t<source.size then
      Some(source(value+off))
    else
      None
  def fetch[T<:Token:ClassTag](off:Int):Option[T]=
    val ct = summon[ClassTag[T]]
    val t = value+off
    if t>=0 && t<source.size then
      val x = source(value+off)
      ct.unapply(x)
    else
      None
  def inside: Boolean = !empty
  def empty: Boolean = value < 0 || value >= source.size
  def +( off:Int ):LPtr = copy( value = value+off )
  def isIdentifier(off:Int,txtPred:String=>Boolean):Option[Token.Identifier]=
    fetch[Token.Identifier](off).flatMap(t => if txtPred(t.text) then Some(t) else None )
  def isNull(off:Int)  = isIdentifier(off, _=="null")
  def isFalse(off:Int) = isIdentifier(off, _=="false")
  def isTrue(off:Int)  = isIdentifier(off, _=="true")

