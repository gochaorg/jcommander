package xyz.cofe.jtfm.store.json

object Parser {
  def parse(jsonString:String):Option[AST] =
    val tokens = Lexer
      .parse(jsonString)
      .dropWhitespaces
    expression(
      LPtr(
        0,
        Lexer.parse(jsonString).dropWhitespaces
      )
    ).map( _._1 )

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
