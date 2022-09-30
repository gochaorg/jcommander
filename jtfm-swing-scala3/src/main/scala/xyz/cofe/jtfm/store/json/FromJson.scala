package xyz.cofe.jtfm.store.json

import scala.deriving.*
import scala.compiletime.{erasedValue, summonInline, constValue}
import scala.CanEqual.derived

trait FromJson[T]:
  def fromJson(j:JS):Either[String,T]

inline def summonAllFromJson[T <: Tuple]: List[FromJson[_]] =
inline erasedValue[T] match
  case _: EmptyTuple => Nil
  case _: (t *: ts) => summonInline[FromJson[t]] :: summonAllFromJson[ts]

inline def labelFromMirror[A](using m:Mirror.Of[A]):String = constValue[m.MirroredLabel]
inline def labelsFrom[A <: Tuple]:List[String] = inline erasedValue[A] match
  case _:EmptyTuple => Nil
  case _:(head *: tail) => 
    constValue[head].toString() :: labelsFrom[tail]
inline def labelsOf[A](using m:Mirror.Of[A]) = labelsFrom[m.MirroredElemLabels]

object FromJson:
  given FromJson[Double] with
    def fromJson(j:JS) = j match
      case JS.Num(n) => Right(n)
      case _ => Left(s"can't get double from $j")      
  given FromJson[Int] with
    def fromJson(j:JS) = j match
      case JS.Num(n) => Right(n.toInt)
      case _ => Left(s"can't get int from $j")      
  given FromJson[Boolean] with
    def fromJson(j:JS) = j match
      case JS.Bool(v) => Right(v)
      case _ => Left(s"can't get bool from $j")      
  given FromJson[String] with
    def fromJson(j:JS) = j match
      case JS.Str(v) => Right(v)
      case _ => Left(s"can't get string from $j") 
  inline given derived[A](using n:Mirror.Of[A]):FromJson[A] =
    val elems = summonAllFromJson[n.MirroredElemTypes]
    val names = labelsFrom[n.MirroredElemLabels]
    inline n match
      case s: Mirror.SumOf[A]     => fromJsonSum(s,elems)
      case p: Mirror.ProductOf[A] => fromJsonPoduct(p,elems,names)
    
  def fromJsonSum[T](s:Mirror.SumOf[T], elems:List[FromJson[_]]):FromJson[T] = 
    new FromJson[T]:
      def fromJson(js:JS):Either[String,T] =
        Left(s"fromJsonSum js:$js")
  def fromJsonPoduct[T](
    p:Mirror.ProductOf[T], 
    elems:List[FromJson[_]], 
    names:List[String],
  ):FromJson[T] = 
    new FromJson[T]:
      def fromJson(js:JS):Either[String,T] =
        js match
          case JS.Obj(fields) => 
            val res = names.zip(elems).map { case (name,restore) => 
              //restore.asInstanceOf[FromJson[Any]].fromJson()
              fields.get(name).lift(s"field $name not found").flatMap { jsFieldValue => 
                restore.asInstanceOf[FromJson[Any]].fromJson(jsFieldValue)
              }
            }.foldLeft(Right(List[Any]()):Either[String,List[Any]]){ case (a,vE) => 
              vE.flatMap { v => 
                a.map { l => v :: l }
              }
            }.map { _.reverse }
            .map { ls => 
              val prod:Product = new Product {
                override def productArity: Int = ls.size
                override def productElement(n: Int): Any = ls(n)
                override def canEqual(that: Any): Boolean = false
              }
              prod
            }
            .map { prod => 
              p.fromProduct(prod)
            }
            //Left(s"fromJsonPoduct \nfields $fields \nnames $names \nelems $elems \nls $ls")
            res
          case _ => Left(s"fromJsonPoduct can't fetch from $js")

