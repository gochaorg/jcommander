package xyz.cofe.jtfm.store.json

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

extension ( tok:Token )
  def nextPtr:Ptr = Ptr(tok.end.value, tok.end.source)
  def text:String = tok.begin.source.substring(tok.begin.value, tok.end.value)
