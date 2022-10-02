package xyz.cofe.jtfm.store.json

import scala.deriving.*
import scala.compiletime.{erasedValue, summonInline, constValue}
import scala.CanEqual.derived

// https://dotty.epfl.ch/docs/reference/contextual/derivation.html
inline def summonAllToJson[T <: Tuple]: List[ToJson[_]] =
  inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (t *: ts) => summonInline[ToJson[t]] :: summonAllToJson[ts]

trait ToJson[T]:
  def toJson(t:T):Either[String,JS]

object ToJson:
  given ToJson[Double] with
    def toJson(n:Double) = Right(JS.Num(n))
  given ToJson[Int] with
    def toJson(n:Int) = Right(JS.Num(n.toDouble))
  given ToJson[Boolean] with
    def toJson(n:Boolean) = Right(JS.Bool(n))      
  given ToJson[String] with
    def toJson(n:String) = Right(JS.Str(n))
  given [T](using ToJson[T]): ToJson[List[T]] with
    def toJson(list:List[T]):Either[String,JS] =
      val t2js = summon[ToJson[T]]
      list.foldLeft( Right(List[JS]()):Either[String,List[JS]] ){ case(a,e) =>
        t2js.toJson(e).flatMap { js => 
          a.map { ls => js :: ls }
        }
      }.map { ls => JS.Arr(ls.reverse) }

  // https://dotty.epfl.ch/docs/reference/contextual/derivation.html
  def iterator[T](p: T) = p.asInstanceOf[Product].productIterator

  inline given derived[T](using m: scala.deriving.Mirror.Of[T]): ToJson[T] = 
    val elems = summonAllToJson[m.MirroredElemTypes]
    inline m match
      case s: Mirror.SumOf[T] => toJsonSum(s, elems)
      case p: Mirror.ProductOf[T] => toJsonProduct(p, elems)
  
  def toJsonSum[T](s: Mirror.SumOf[T], elems: List[ToJson[_]]):ToJson[T] = 
    new ToJson[T]:
      def toJson(t:T):Either[String,JS] =          
        Right(JS.Num(s.ordinal(t).toDouble))
        val rr = elems.map { tjs => tjs.asInstanceOf[ToJson[Any]].toJson(t) }
        Left(s"toJsonSum s:$s elems:$elems t:$t rr:$rr")

  def toJsonProduct[T](p: Mirror.ProductOf[T], elems: List[ToJson[_]]):ToJson[T] = 
    new ToJson[T]:
      def toJson(t:T):Either[String,JS] =
        val jsons = t.asInstanceOf[Product].productIterator.zip(elems).map { case (v,tjs) => tjs.asInstanceOf[ToJson[Any]].toJson(v) }
        val names = t.asInstanceOf[Product].productElementNames
        val str = names.zip(jsons).foldLeft( Right(Map[String,JS]()):Either[String,Map[String,JS]] ){ case(m_e, (name,v_e)) => 
          v_e.flatMap( js => m_e.map( m => m + (name -> js) ) )
        }.map( m => JS.Obj(m) )
        str
