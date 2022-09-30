package xyz.cofe.jtfm.store

import scala.deriving.*
import scala.compiletime.{erasedValue, summonInline, constValue}
import scala.CanEqual.derived

object Json {
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

    case Identifier(
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
    def text:String = tok.begin.source.substring(tok.begin.value, tok.end.value)

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

    given Lexer[Identifier] with
      def apply( ptr:Ptr ) =
        ptr.inside() match
          case false => None
          case true => ptr(0).is { c => c.isLetter || c=='_' } match
            case false => None
            case true =>
              var p = ptr + 1
              while !p.empty && p(0).is { c => c.isLetter || c=='_' || c.isDigit } do
                p = p + 1
              Some(Identifier(ptr,p))

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
        summon[Lexer[Identifier]],
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

  extension ( str:String )
    def decodeLitteral:String =
      def unescape( strContent:String ):String = {
        val sb = new StringBuilder()
        var skip = 0
        for 
          idx <- (0 until strContent.length)
          ch0 = strContent.charAt(idx)
          ch1opt = 
            if idx<(strContent.length-1) then 
              Some(strContent.charAt(idx+1)) 
            else 
              None
        do
          if skip>0 then skip -= 1 
          else
            (ch0, ch1opt) match
              case ('\\', Some('\n')) => sb += '\n'; skip=1
              case ('\\', Some('\r')) => sb += '\r'; skip=1
              case ('\\', Some('\t')) => sb += '\t'; skip=1
              case ('\\', Some(ch1)) => sb += ch1; skip=1
              case _ => sb += ch0
        sb.toString()
      }
      if str.startsWith("'") && str.endsWith("'") then
        unescape(str.substring(1,str.length()-1))
      else if str.startsWith("\"") && str.endsWith("\"") then
        unescape(str.substring(1,str.length()-1))
      else
        throw new IllegalArgumentException(s"can't decode js string litteral: $str")
    def encodeLitteral:String =
     str match
      case null => "null"
      case _ => 
        "\"" + str.flatMap( chr => chr match
          case '"'  => "\\\""
          case '\\' => "\\\\"
          case '\n' => "\\n"
          case '\r' => "\\r"
          case '\t' => "\\t"
          case _ => chr.toString
        ) + "\""

  enum JS:
    case Null()
    case Bool(val value:Boolean)
    case Str(val value:String)
    case Num(val value:Double)
    case Arr(val value:Seq[JS]=List())
    case Obj(val fields:Map[String,JS]=Map())
    def json:String =
      summon[ToJsonString[JS]].toJsonString(this)

  trait ToJsonString[T]:
    def toJsonString(t:T):String

  object ToJsonString:
    given ToJsonString[JS.Null] with
      def toJsonString(n:JS.Null) = "null"
    given ToJsonString[JS.Bool] with
      def toJsonString(b:JS.Bool) = b.value match 
        case true => "true"
        case false => "false"
    given ToJsonString[JS.Str] with
      def toJsonString(s:JS.Str) = s.value match
        case null => "null"
        case _ => s.value.encodeLitteral
    given ToJsonString[JS.Num] with
      def toJsonString(n:JS.Num) = n.value.toString()
    given ToJsonString[JS.Arr] with
      def toJsonString(a:JS.Arr) = a.value.map { it => summon[ToJsonString[JS]].toJsonString(it) }.mkString("[", ",", "]")
    given ToJsonString[JS.Obj] with
      def toJsonString(o:JS.Obj) = o.fields.map { (k,v) => 
        k.encodeLitteral+":"+summon[ToJsonString[JS]].toJsonString(v) 
        }.mkString("{",",","}")
    given ToJsonString[JS] with
      def toJsonString(j:JS) = j match
        case n:JS.Null => summon[ToJsonString[JS.Null]].toJsonString(n)
        case n:JS.Bool => summon[ToJsonString[JS.Bool]].toJsonString(n)
        case n:JS.Str => summon[ToJsonString[JS.Str]].toJsonString(n)
        case n:JS.Num => summon[ToJsonString[JS.Num]].toJsonString(n)
        case n:JS.Arr => summon[ToJsonString[JS.Arr]].toJsonString(n)
        case n:JS.Obj => summon[ToJsonString[JS.Obj]].toJsonString(n)
      
  enum AST( val begin:Ptr, val end:Ptr ):
    case Id(val tok:Token.Identifier, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case True(val tok:Token.Identifier, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case False(val tok:Token.Identifier, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case Null(val tok:Token.Identifier, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case Str(val decode:String, val tok:Token.Str, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case Num(val tok:Token.Number, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
    case Arr(items:Seq[AST], begin0:Ptr, end0:Ptr) extends AST(begin0,end0)
    case Field(
      val tok:Token.Str|Token.Identifier,
      val name:String,
      val value:AST, 
      begin0:Ptr,
      end0:Ptr,
    ) extends AST(begin0,end0)
    case Obj(
      val body:Seq[AST],
      begin0:Ptr,end0:Ptr,
    ) extends AST(begin0,end0), ObjOpt
    case Comment(val tok:Token.SLComment,begin0:Ptr,end0:Ptr) extends AST(begin0,end0)

  trait ObjOpt {
    self: AST.Obj =>
      lazy val fields: Map[String, AST] =
        self.body
          .filter { e => e.isInstanceOf[AST.Field] }
          .map { e => e.asInstanceOf[AST.Field] }
          .map { f => (f.name, f.value) }
          .toMap
  }

  object AST:
    given ToJsonString[Id] with
      def toJsonString(a:Id) = a.tok.text.encodeLitteral
    given ToJsonString[True] with
      def toJsonString(a:True) = a.tok.text
    given ToJsonString[False] with
      def toJsonString(a:False) = a.tok.text
    given ToJsonString[Null] with
      def toJsonString(a:Null) = "null"
    given ToJsonString[Str] with
      def toJsonString(a:Str) = a.tok.text
    given ToJsonString[Num] with
      def toJsonString(a:Num) = a.tok.text
    given ToJsonString[Arr] with
      def toJsonString(a:Arr) = a.items.map { el => 
        summon[ToJsonString[AST]].toJsonString(el) 
      }.mkString("[",",","]")
    given ToJsonString[Obj] with
      def toJsonString(a:Obj) = a.fields.map { (k,v) => 
        k.encodeLitteral+":"+summon[ToJsonString[AST]].toJsonString(v)
      }.mkString("{",",","}")
    given ToJsonString[AST] with
      def toJsonString(a:AST) = a match
        case n:Id => summon[ToJsonString[Id]].toJsonString(n)
        case n:True => summon[ToJsonString[True]].toJsonString(n)
        case n:False => summon[ToJsonString[False]].toJsonString(n)
        case n:Null => summon[ToJsonString[Null]].toJsonString(n)
        case n:Str => summon[ToJsonString[Str]].toJsonString(n)
        case n:Num => summon[ToJsonString[Num]].toJsonString(n)
        case n:Arr => summon[ToJsonString[Arr]].toJsonString(n)
        case n:Obj => summon[ToJsonString[Obj]].toJsonString(n)
        case _ => s"/* $a */"

  extension( ast:AST )
    def id:Option[String] = ast match
      case AST.Id(t,_,_) => Some(t.text)
      case _ => None
    def bool:Option[Boolean] = ast match
      case _:AST.True => Some(true)
      case _:AST.False => Some(false)
      case _ => None
    def isNull:Boolean = ast match
      case _:AST.Null => true
      case _ => false
    def str:Option[String] = ast match
      case AST.Str(s,_,_,_) => Some(s)
      case _ => None
    def num:Option[Double] = ast match
      case AST.Num(t,_,_) => t.text.toDoubleOption
      case _ => None
    def list:Option[List[AST]] = ast match
      case AST.Arr(elems,_,_) => Some(elems.toList)
      case _ => None
    def obj:Option[Map[String,AST]] = ast match
      case ob:AST.Obj => Some(ob.fields)
      case _ => None
    def json:String = summon[ToJsonString[AST]].toJsonString(ast)
    def toJson: Either[String,JS] = ast match
      case _:AST.True => Right(JS.Bool(true))
      case _:AST.False => Right(JS.Bool(false))
      case _:AST.Null => Right(JS.Null())
      case s:AST.Str => Right(JS.Str(s.decode))
      case n:AST.Num => n.num.map { x => Right(JS.Num(x)) }.getOrElse( Left("can't fetch num") )
      case n:AST.Arr => n.items.foldLeft( Right(List[JS]()):Either[String,List[JS]] ){ 
        case (a,i) =>
          i.toJson.flatMap { j => 
            a.map { l => j :: l }
          }
        }.map { l => JS.Arr(l.reverse) }
      case n:AST.Obj => n.fields.foldLeft( 
        Right(Map[String,JS]()):Either[String,Map[String,JS]] 
      ){ case (a,i) =>
        i._2.toJson.flatMap { j =>
          a.map { m => m + (i._1 -> j) }
        }
      }.map { m => JS.Obj(m) }
      case _:AST.Id => Left("Not supported for AST.Id")
      case _:AST.Field => Left("Not supported for AST.Field")
      case _:AST.Comment => Left("Not supported for AST.Comment")
    

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

  extension ( tokens:Seq[Token] )
    def dropWhitespaces:Seq[Token] = 
      tokens.filterNot( t => 
        t.isInstanceOf[Token.WhiteSpace] || 
        t.isInstanceOf[Token.SLComment] )

  object Parser {
    // expression ::= object | array | atom
    def expression ( ptr:LPtr ):Option[(AST,LPtr)] = 
      _object(ptr).orElse( array(ptr) ).orElse( atom(ptr) )

    // object ::= '{' [ field { ',' field } [ ',' ] ]  '}' 
    def _object    ( ptr:LPtr ):Option[(AST,LPtr)] = 
      ptr.fetch[Token.OpenBrace](0).flatMap { openBrace =>
        val show = summon[Show[Token]]
        var p = ptr+1
        var stop = false
        var fields = List[AST.Field]()
        var lastTok : Token = null
        while(!stop) {
          field(p) match {
            case Some(fld, fld_ptr) => (fld_ptr.token(0), fld_ptr.token(1)) match 
              case (Some(_:Token.Comma), Some(lastTok3:Token.CloseBrace)) =>
                fields = fld :: fields
                lastTok = lastTok3
                p = fld_ptr + 2
                stop = true
              case (Some(_:Token.Comma), _) =>
                fields = fld :: fields
                p = fld_ptr + 1
              case (Some(lastTok4:Token.CloseBrace), _) =>
                fields = fld :: fields
                lastTok = lastTok4
                p = fld_ptr + 1
                stop = true
              case e => 
                throw new RuntimeException("expect , or ] at "+p)
            case _ => (p.token(0), p.token(1)) match {
              case (Some(_:Token.Comma), Some(lastTok1:Token.CloseBrace)) =>
                p = p + 2
                stop = true
              case (Some(lastTok2:Token.CloseBrace),_) =>
                p = p + 1
                stop = true
              case e => 
                throw new RuntimeException("expect , or ] at "+p)
            }
          }
        }

        Some((AST.Obj(fields.reverse, ptr.beginPtr, p.endPtr), p))
      }

    // field ::= ( str | id ) ':' expression
    def field      ( ptr:LPtr ):Option[(AST.Field,LPtr)] = {
      def fieldName(ptr2:LPtr):Option[(AST.Id|AST.Str,LPtr)] =
        val idName:Option[(AST.Id,LPtr)] = ptr2.fetch[Token.Identifier](0).flatMap { t =>
          Some( (AST.Id(t,ptr2.beginPtr,ptr2.endPtr), ptr2+1) )
          }
        val strName:Option[(AST.Str,LPtr)] = ptr2.fetch[Token.Str](0).flatMap { t =>
          Some( (AST.Str(t.text.decodeLitteral, t,ptr2.beginPtr,ptr2.endPtr), ptr2+1) )
          }
        idName.orElse(strName)

      fieldName(ptr).flatMap { (fname,next_ptr) => 
        next_ptr.fetch[Token.Colon](0).flatMap { colon => 
          expression(next_ptr + 1) match 
            case Some( (exp,exp_ptr) ) =>
              fname match 
                case idName: AST.Id =>                  
                  Some(AST.Field( 
                    idName.tok, 
                    idName.tok.text,
                    exp, ptr.beginPtr, exp_ptr.endPtr ), 
                    exp_ptr)
                case strName: AST.Str =>
                  Some(AST.Field( 
                    strName.tok, 
                    strName.tok.text.decodeLitteral,
                    exp, ptr.beginPtr, exp_ptr.endPtr ), 
                    exp_ptr)
            case _ =>
              throw new RuntimeException(s"expect expression at "+(next_ptr + 1))
        }
      }
    }

    // array ::= '[' [ expression { ',' expression } [ ',' ] ] ']'
    def array      ( ptr:LPtr ):Option[(AST.Arr,LPtr)] = 
      ptr.fetch[Token.OpenSuqare](0).flatMap { openBrace =>
        var expList = List[AST]()
        var p = ptr + 1
        var stop = false
        var latTok : Token = null
        while (!stop) {
          // [ expression { ',' expression } [ ',' ] ]
          expression(p) match
            case Some(ex1, next_p) =>
              next_p.fetch[Token.Comma](0) match
                case Some(_) => next_p.fetch[Token.CloseSuqare](1) match
                  case Some(et) =>  // expression , ] 
                                    //              ▲ you here
                    p = next_p + 1
                    expList = ex1 :: expList
                    stop = true
                    latTok = et
                  case _ =>        // expression , ?
                                   //              ▲ you here
                    p = next_p + 1
                    expList = ex1 :: expList
                case _ => // expression ?
                          //            ▲ you here
                  next_p.fetch[Token.CloseSuqare](0) match
                    case Some(et) =>
                      p = next_p + 1
                      expList = ex1 :: expList
                      stop = true
                      latTok = et
                    case _ =>
                      throw new RuntimeException("expect ] at "+p)
            case _ => // expect ] or , ]
              ( p.token(0)
              , p.token(1)
              ) match
                case (Some(et:Token.CloseSuqare), _) =>
                  p = p + 1
                  stop = true
                  latTok = et
                case (Some(_:Token.Comma), Some(et:Token.CloseSuqare)) =>
                  p = p + 2
                  stop = true
                  latTok = et
                case _ =>
                  throw new RuntimeException("expect ] or , ] at "+p)
        }
        val x = p.token.map { _.begin }.orElse( (p+(-1)).token.map(_.end) )
        Some((AST.Arr(
            expList.reverse,
            ptr.beginPtr,
            latTok.end),
            p))
        }

    // atom ::= str | num | predef_id
    def atom       ( ptr:LPtr ):Option[(AST,LPtr)] = 
      str(ptr).orElse(num(ptr)).orElse(predef_id(ptr))

    // str ::= ...
    def str        ( ptr:LPtr ):Option[(AST.Str,LPtr)] = 
      ptr.fetch[Token.Str](0).map { t => (AST.Str(t.text.decodeLitteral, t,ptr.beginPtr,ptr.endPtr),ptr+1)}

    // num ::= ...
    def num        ( ptr:LPtr ):Option[(AST.Num,LPtr)] = 
      ptr.fetch[Token.Number](0).map { t => (AST.Num(t,ptr.beginPtr,ptr.endPtr),ptr+1)}

    // predef_id ::= 'false' | 'true' | 'null'
    def predef_id  ( ptr:LPtr ):Option[(AST,LPtr)] = 
      _false(ptr).orElse(_true(ptr)).orElse(_null(ptr))

    def _false     ( ptr:LPtr ):Option[(AST.False,LPtr)] =
      ptr.isFalse(0).map(t => (AST.False(t, ptr.token.get.begin, ptr.token.get.end), ptr+1) )
    def _true      ( ptr:LPtr ):Option[(AST.True,LPtr)] = 
      ptr.isTrue(0).map(t => (AST.True(t, ptr.token.get.begin, ptr.token.get.end), ptr+1) )
    def _null      ( ptr:LPtr ):Option[(AST.Null,LPtr)] = 
      ptr.isNull(0).map(t => (AST.Null(t, ptr.token.get.begin, ptr.token.get.end), ptr+1) )
  }

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
      def toJson(n:Boolean) = Right(JS.Str(n match 
        case true => "true"
        case false => "false"
      ))      
    given ToJson[String] with
      def toJson(n:String) = Right(JS.Str(n))

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
}
