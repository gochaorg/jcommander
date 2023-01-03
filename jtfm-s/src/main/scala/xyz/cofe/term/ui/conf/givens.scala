package xyz.cofe.term.ui.conf

import xyz.cofe.json4s3.derv.ToJson
import xyz.cofe.term.common.Color
import xyz.cofe.json4s3.stream.ast.AST
import xyz.cofe.json4s3.derv.FromJson
import xyz.cofe.json4s3.derv.errors.DervError
import xyz.cofe.json4s3.derv.errors.TypeCastFail

given colorToJson:ToJson[Color] with
  override def toJson(color: Color): Option[AST] = 
    color match
      case Color.Black => Some(AST.JsStr("Black"))
      case Color.BlackBright => Some(AST.JsStr("BlackBright"))
      case Color.Red => Some(AST.JsStr("Red"))
      case Color.RedBright => Some(AST.JsStr("RedBright"))
      case Color.Green => Some(AST.JsStr("Green"))
      case Color.GreenBright => Some(AST.JsStr("GreenBright"))
      case Color.Yellow => Some(AST.JsStr("Yellow"))
      case Color.YellowBright => Some(AST.JsStr("YellowBright"))
      case Color.Blue => Some(AST.JsStr("Blue"))
      case Color.BlueBright => Some(AST.JsStr("BlueBright"))
      case Color.Magenta => Some(AST.JsStr("Magenta"))
      case Color.MagentaBright => Some(AST.JsStr("MagentaBright"))
      case Color.Cyan => Some(AST.JsStr("Cyan"))
      case Color.CyanBright => Some(AST.JsStr("CyanBright"))
      case Color.White => Some(AST.JsStr("White")) 
      case Color.WhiteBright => Some(AST.JsStr("WhiteBright"))
    
given colorFromJson:FromJson[Color] with
  override def fromJson(j: AST): Either[DervError, Color] = 
    summon[FromJson[String]].fromJson(j).flatMap { 
      case str if str.matches("(?is)Black") => Right(Color.Black)
      case str if str.matches("(?is)Black-?(Bright|\\+)") => Right(Color.BlackBright)
      case str if str.matches("(?is)Red") => Right(Color.Red)
      case str if str.matches("(?is)Red-?(Bright|\\+)") => Right(Color.RedBright)
      case str if str.matches("(?is)Green") => Right(Color.Green)
      case str if str.matches("(?is)Green-?(Bright|\\+)") => Right(Color.GreenBright)
      case str if str.matches("(?is)Yellow") => Right(Color.Yellow)
      case str if str.matches("(?is)Yellow-?(Bright|\\+)") => Right(Color.YellowBright)
      case str if str.matches("(?is)Blue") => Right(Color.Blue)
      case str if str.matches("(?is)Blue-?(Bright|\\+)") => Right(Color.BlueBright)
      case str if str.matches("(?is)Magenta") => Right(Color.Magenta)
      case str if str.matches("(?is)Magenta-?(Bright|\\+)") => Right(Color.MagentaBright)
      case str if str.matches("(?is)Cyan") => Right(Color.Cyan)
      case str if str.matches("(?is)Cyan-?(Bright|\\+)") => Right(Color.CyanBright)
      case str if str.matches("(?is)White") => Right(Color.White)
      case str if str.matches("(?is)White-?(Bright|\\+)") => Right(Color.WhiteBright)
      case str => Left(TypeCastFail(s"can't cast str($str) to color"))
    }