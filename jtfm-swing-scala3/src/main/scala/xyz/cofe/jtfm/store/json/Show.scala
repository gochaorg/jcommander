package xyz.cofe.jtfm.store.json

trait Show[T]:
  def apply(value:T):String

object Show:
  import Token._
  given Show[Undefined] with { def apply(value:Undefined) = s"Undefined(${value.begin.source.substring(value.begin.value, value.end.value)})" }
  given Show[Str] with { def apply(value: Str): String = s"Str(${value.begin.source.substring(value.begin.value, value.end.value)})" }
  given Show[SLComment] with { def apply(value: SLComment): String = s"SLComment(${value.begin.source.substring(value.begin.value, value.end.value)})" }
  given Show[Identifier] with { def apply(value: Identifier): String = s"Identifier(${value.begin.source.substring(value.begin.value, value.end.value)})" }
  given Show[WhiteSpace] with { def apply(value: WhiteSpace): String = s"WhiteSpace" }
  given Show[Number] with { def apply(value: Number): String = s"Number(${value.begin.source.substring(value.begin.value, value.end.value)})" }
  given Show[OpenSuqare] with { def apply(value: OpenSuqare): String = s"OpenSuqare" }
  given Show[CloseSuqare] with { def apply(value: CloseSuqare): String = s"CloseSuqare" }
  given Show[OpenBrace] with { def apply(value: OpenBrace): String = s"OpenBrace" }
  given Show[CloseBrace] with { def apply(value: CloseBrace): String = s"CloseBrace" }
  given Show[Comma] with { def apply(value: Comma): String = s"Comma" }
  given Show[Colon] with { def apply(value: Colon): String = s"Colon" }
  given Show[Token] with 
    def apply(value: Token): String = value match
      case v:Undefined => summon[Show[Undefined]].apply(v)
      case v:Str => summon[Show[Str]].apply(v)
      case v:SLComment => summon[Show[SLComment]].apply(v)
      case v:Identifier => summon[Show[Identifier]].apply(v)
      case v:WhiteSpace => summon[Show[WhiteSpace]].apply(v)
      case v:Number => summon[Show[Number]].apply(v)
      case v:OpenSuqare => summon[Show[OpenSuqare]].apply(v)
      case v:CloseSuqare => summon[Show[CloseSuqare]].apply(v)
      case v:OpenBrace => summon[Show[OpenBrace]].apply(v)
      case v:CloseBrace => summon[Show[CloseBrace]].apply(v)
      case v:Comma => summon[Show[Comma]].apply(v)
      case v:Colon => summon[Show[Colon]].apply(v)

