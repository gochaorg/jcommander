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
        var p = ptr+1
        var stop = false
        var fields = List[AST.Field]()
        var lastTok : Token = null
        while(!stop) {
          println(s"ptr at ${ptr.value}")
          field(p) match {
            case Some(fld, fld_ptr) => (fld_ptr.token(0), fld_ptr.token(1)) match 
              case (Some(_:Token.Comma), Some(lastTok3:Token.CloseBrace)) =>
                fields = fld :: fields
                println(s"1 fields ${fields}")
                lastTok = lastTok3
                p = fld_ptr + 2
                stop = true
              case (Some(_:Token.Comma), _) =>
                fields = fld :: fields
                println(s"2 fields ${fields}")
                p = fld_ptr + 1
              case _ => throw new RuntimeException("expect , or ] at "+p)
            case _ => (p.token(0), p.token(1)) match {
              case (Some(_:Token.Comma), Some(lastTok1:Token.CloseBrace)) =>
                println("3 case")
                p = p + 2
                stop = true
              case (Some(lastTok2:Token.CloseBrace),_) =>
                println("4 case")
                p = p + 1
                stop = true
              case _ => throw new RuntimeException("expect , or ] at "+p)
            }
          }
        }

        println(s"fields result ${fields}")
        Some((AST.Obj(fields.reverse, ptr.beginPtr, p.endPtr), p))
      }

    // field ::= ( str | id ) ':' expression
    def field      ( ptr:LPtr ):Option[(AST.Field,LPtr)] = {
      def fieldName(ptr:LPtr):Option[(AST.Id|AST.Str,LPtr)] =
        val idName:Option[(AST.Id,LPtr)] = ptr.fetch[Token.Identifier](0).flatMap { t =>
          Some( (AST.Id(t,ptr.beginPtr,ptr.endPtr), ptr+1) )
          }
        val strName:Option[(AST.Str,LPtr)] = ptr.fetch[Token.Str](0).flatMap { t =>
          Some( (AST.Str(t.text.decodeLitteral, t,ptr.beginPtr,ptr.endPtr), ptr+1) )
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
                  println(s"exp_ptr $exp_ptr")
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
}
