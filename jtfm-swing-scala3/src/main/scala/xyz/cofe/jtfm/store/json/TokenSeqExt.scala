package xyz.cofe.jtfm.store.json

extension ( tokens:Seq[Token] )
  def dropWhitespaces:Seq[Token] = 
    tokens.filterNot( t => 
      t.isInstanceOf[Token.WhiteSpace] || 
      t.isInstanceOf[Token.SLComment] )

