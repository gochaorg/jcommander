package xyz.cofe.jtfm.store

object JsonParser {
  enum Token( val begin:Ptr, val end:Ptr ):
    case Undefined(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)

    case Str(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)

    case Number(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)

    case OpenSuqare(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)
    
    case CloseSuqare(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)
    
    case OpenBrace(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)
    
    case CloseBrace(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)
    
    case Comma(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)
    
    case Colon(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)

    case WhiteSpace(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)

    case Litteral(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)

    case SLComment(
      begin0:Ptr, end0:Ptr
    ) extends Token(begin0,end0)

  trait Show[T]:
    def apply(value:T):String

  object Show:
    import Token._
    given Show[Undefined] with { def apply(value:Undefined) = s"Undefined(${value.begin.source.substring(value.begin.value, value.end.value)})" }
    given Show[Str] with { def apply(value: Str): String = s"Str(${value.begin.source.substring(value.begin.value, value.end.value)})" }
    given Show[SLComment] with { def apply(value: SLComment): String = s"SLComment(${value.begin.source.substring(value.begin.value, value.end.value)})" }
    given Show[Litteral] with { def apply(value: Litteral): String = s"Litteral(${value.begin.source.substring(value.begin.value, value.end.value)})" }
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
        case v:Litteral => summon[Show[Litteral]].apply(v)
        case v:WhiteSpace => summon[Show[WhiteSpace]].apply(v)
        case v:Number => summon[Show[Number]].apply(v)
        case v:OpenSuqare => summon[Show[OpenSuqare]].apply(v)
        case v:CloseSuqare => summon[Show[CloseSuqare]].apply(v)
        case v:OpenBrace => summon[Show[OpenBrace]].apply(v)
        case v:CloseBrace => summon[Show[CloseBrace]].apply(v)
        case v:Comma => summon[Show[Comma]].apply(v)
        case v:Colon => summon[Show[Colon]].apply(v)

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
      
  extension ( tok:Token )
    def nextPtr:Ptr = Ptr(tok.end.value, tok.end.source)
    
  extension ( optChr:Option[Char] )
    def in( chars:String ):Boolean = optChr.map { chr => chars.indexOf(chr)>=0 }.getOrElse(false)
    def isWhiteSpace:Boolean = optChr.map { chr => chr.isWhitespace }.getOrElse( false )
    def isDigit = optChr.map { chr => chr.isDigit }.getOrElse( false )
    def isLetter = optChr.map { chr => chr.isLetter }.getOrElse( false )
    def is( check:Char=>Boolean ):Boolean = optChr.map { chr => check(chr) }.getOrElse( false )

  trait Lexer[T <: Token]:
    def apply(ptr:Ptr):Option[T]

  object Lexer {
    import Token._

    given Lexer[SLComment] with
      def apply(ptr:Ptr) =
        if !ptr.lookup(2).equals("//") then
          None
        else
          var p = ptr + 2
          while !p.empty && !(p(0).in("\\n\\r")) do
            p = p + 1
          p.lookup(2) match
            case "\r\n" => Some(SLComment(ptr, p+2))
            case _ => p.lookup(1) match
              case "\n" => Some(SLComment(ptr, p+1))
              case "\r" => Some(SLComment(ptr, p+1))
              case _ => Some(SLComment(ptr, p))

    given Lexer[Comma] with { def apply(ptr: Ptr) = ptr(0).map { c => c==',' }.flatMap { if _ then Some(Comma(ptr, ptr+1)) else None } }
    given Lexer[Colon] with { def apply(ptr: Ptr) = ptr(0).map { c => c==':' }.flatMap { if _ then Some(Colon(ptr, ptr+1)) else None } }
    given Lexer[OpenBrace] with { def apply(ptr: Ptr) = ptr(0).map { c => c=='{' }.flatMap { if _ then Some(OpenBrace(ptr, ptr+1)) else None } }
    given Lexer[CloseBrace] with { def apply(ptr: Ptr) = ptr(0).map { c => c=='}' }.flatMap { if _ then Some(CloseBrace(ptr, ptr+1)) else None } }
    given Lexer[OpenSuqare] with { def apply(ptr: Ptr) = ptr(0).map { c => c=='[' }.flatMap { if _ then Some(OpenSuqare(ptr, ptr+1)) else None } }
    given Lexer[CloseSuqare] with { def apply(ptr: Ptr) = ptr(0).map { c => c==']' }.flatMap { if _ then Some(CloseSuqare(ptr, ptr+1)) else None } }

    given Lexer[Number] with
      def apply(ptr:Ptr)=
        ptr(0).map { c => c.isDigit || c=='-' } match 
          case None => None
          case Some(false) => None
          case Some(true) =>
            var p = ptr + 1
            var dotCount = 0
            while !p.empty && p(0).is { c => 
              dotCount match 
                case 0 | 1 =>
                  c.isDigit || c=='.'
                case _ =>
                  c.isDigit
            } do
              p = p + 1
              dotCount += (if p(0).in(".") then 1 else 0)
            Some(Number(ptr,p))

    given Lexer[Str] with
      def apply( ptr:Ptr ) =
        def readStrLit(quote:Char):Str =
          var p = ptr+1
          while !p.empty && !(p(0).is { c => c==quote }) do
            p(0) match
              case Some(c) => c match 
                case '\\' => p = p + 2
                case _ => p = p + 1
              case _ => p = p + 1
          Str(ptr, if p.empty then p else p+1)
        ptr(0) match 
          case Some('"') => Some(readStrLit('"'))
          case Some('\'') => Some(readStrLit('\''))
          case _ => None

    given Lexer[Litteral] with
      def apply( ptr:Ptr ) =
        ptr.inside() match
          case false => None
          case true => ptr(0).is { c => c.isLetter || c=='_' } match
            case false => None
            case true =>
              var p = ptr + 1
              while !p.empty && p(0).is { c => c.isLetter || c=='_' || c.isDigit } do
                p = p + 1
              Some(Litteral(ptr,p))

    given Lexer[WhiteSpace] with
      def apply( ptr:Ptr ) =
        ptr.inside() match
          case false => None
          case true => ptr(0).isWhiteSpace match
            case false => None
            case true =>
              var p = ptr + 1
              while !p.empty && p(0).isWhiteSpace do
                p = p + 1
              Some(WhiteSpace(ptr,p))

    def parse( str:String ):List[_ <: Token] =
      val parsers = List[Lexer[_ <: Token]](
        summon[Lexer[SLComment]],
        summon[Lexer[Comma]],
        summon[Lexer[Colon]],
        summon[Lexer[OpenBrace]],
        summon[Lexer[CloseBrace]],
        summon[Lexer[OpenSuqare]],
        summon[Lexer[CloseSuqare]],
        summon[Lexer[Number]],
        summon[Lexer[Str]],
        summon[Lexer[WhiteSpace]],
        summon[Lexer[Litteral]],
      )
      var list = List[Token]()
      var ptr = Ptr(0, str)
      var stop = false
      while( !stop ){
        ptr.inside() match
          case true =>  
            parsers.foldLeft( None:Option[Token] )( (res,parser) => {
                res match
                  case Some(t) => res
                  case None => parser(ptr)
            }) match
              case Some(tok) =>
                list = tok :: list
                ptr = tok.nextPtr
              case None =>
                ptr.toEnd match
                  case Some(endPtr) =>
                    list = Undefined(ptr, endPtr) :: list
                    stop = true
                  case None =>
                    stop = true
          case false =>
            stop = true
      }
      list.reverse
  }

  trait Name {
    def name():String
  }

  enum AST( val begin:Ptr, val end:Ptr ):
    case True(begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case False(begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case Null(begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case Str(val tok:Token.Str, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case Num(val tok:Token.Number, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case Arr(items:Seq[AST], begin0:Ptr, end0:Ptr) extends AST(begin0,end0)
    case Field(val name:Token.Str|Token.Litteral, val value:AST, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case Obj(val body:Seq[AST],begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case Comment(val tok:Token.SLComment,begin0:Ptr,end0:Ptr) extends AST(begin0,end0)

  case class LPtr( val value:Int, val source:Seq[Token] ):
    import scala.reflect._
    def fetch[T<:Token:ClassTag](off:Int):Option[T]=
      val ct = summon[ClassTag[T]]
      val t = value+off
      if t>=0 && t<source.size then
        val x = source(value+off)
        ct.unapply(x)
      else
        None

  trait Parser {
    // expression ::= object | array | atom
    // object ::= '{' [ field { ',' field } [ ',' ] ]  '}'
    // field ::= ( str | id ) ':' expression
    // array ::= '[' [ expression { ',' expression } [ ',' ] ] ']'
    // atom ::= str | num | predef_id
    // str ::= ...
    // num ::= ...
    // predef_id ::= 'false' | 'true' | 'null'
    def expression( ptr:LPtr ):Option[(AST,LPtr)]
  }
}
