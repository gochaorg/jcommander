package xyz.cofe.term.ui.log

import xyz.cofe.term.common.Size
import xyz.cofe.json4s3.derv.ToJson
import xyz.cofe.json4s3.stream.ast.AST
import xyz.cofe.json4s3.derv.FromJson
import xyz.cofe.json4s3.derv.errors.DervError
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.common.MouseButton
import scala.util.Try
import xyz.cofe.json4s3.derv.errors.TypeCastFail

/* #region Size store to json */

case class SizeValue( width:Int, height:Int ):
  def toSize:Size = new Size(width, height)

object SizeValue:
  def apply( size:Size ):SizeValue = new SizeValue(size.width(), size.height())

given ToJson[Size] with
  override def toJson(v: Size): Option[AST] = 
    summon[ToJson[SizeValue]].toJson(SizeValue(v))

given FromJson[Size] with
  override def fromJson(j: AST): Either[DervError, Size] = 
    summon[FromJson[SizeValue]].fromJson(j).map(_.toSize)

/* #endregion */

/* #region Position store to json */

case class PositionValue( x:Int, y:Int ):
  def toPosition:Position = new Position(x,y)
object PositionValue:
  def apply( pos:Position ):PositionValue = new PositionValue(pos.x, pos.y)

given ToJson[Position] with
  override def toJson(v: Position): Option[AST] = 
    summon[ToJson[PositionValue]].toJson(PositionValue(v))

given FromJson[Position] with
  override def fromJson(j: AST): Either[DervError, Position] = 
    summon[FromJson[PositionValue]].fromJson(j).map(_.toPosition)

/* #endregion */

given ToJson[KeyName] with
  override def toJson(v: KeyName): Option[AST] = 
    summon[ToJson[String]].toJson(v.name())

given FromJson[KeyName] with
  override def fromJson(j: AST): Either[DervError, KeyName] = 
    summon[FromJson[String]].fromJson(j).flatMap { str =>
      Try(KeyName.valueOf(str)).toEither.left.map(err => TypeCastFail(err.getMessage())).flatMap( value =>
        if value==null then Left(TypeCastFail(s"can't cast from $str to KeyName")) else Right(value)
      )
    }

given ToJson[MouseButton] with
  override def toJson(v: MouseButton): Option[AST] = 
    summon[ToJson[String]].toJson(v.name())

given FromJson[MouseButton] with
  override def fromJson(j: AST): Either[DervError, MouseButton] = 
    summon[FromJson[String]].fromJson(j).flatMap { str =>
      Try(MouseButton.valueOf(str)).toEither.left.map(err => TypeCastFail(err.getMessage())).flatMap( value =>
        if value==null then Left(TypeCastFail(s"can't cast from $str to MouseButton")) else Right(value)
      )
    }
