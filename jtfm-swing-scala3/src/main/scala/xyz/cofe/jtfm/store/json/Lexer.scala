package xyz.cofe.jtfm.store.json

trait Lexer[T <: Token]:
  def apply(ptr:Ptr):Option[T]

extension ( optChr:Option[Char] )
  def in( chars:String ):Boolean = optChr.map { chr => chars.indexOf(chr)>=0 }.getOrElse(false)
  def isWhiteSpace:Boolean = optChr.map { chr => chr.isWhitespace }.getOrElse( false )
  def isDigit = optChr.map { chr => chr.isDigit }.getOrElse( false )
  def isLetter = optChr.map { chr => chr.isLetter }.getOrElse( false )
  def is( check:Char=>Boolean ):Boolean = optChr.map { chr => check(chr) }.getOrElse( false )

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

