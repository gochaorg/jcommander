package xyz.cofe.jtfm.store

class JsonParserTest extends munit.FunSuite {
  import JsonParser._

  test("Lexer test") {
    val showTok = summon[Show[Token]]
    JsonParser.Lexer.parse(
      "10 0 12.3 -14 -0 -0.34 true false null 'single \\' quoted' \"double qouted\""
    ).foreach(t => println(showTok(t)))
  }

  test("LPtr fetch") {
    import JsonParser._
    import JsonParser.Token._
    println("=== LPtr fetch =====================")

    val tokens = JsonParser.Lexer.parse("10 true 'single'")
    val showTok = summon[Show[Token]]
    tokens.zipWithIndex.foreach((t,i) => println(s"$i "+showTok(t)))

    val ptr = JsonParser.LPtr(0,tokens)
    println("fetch 0 Number ? "+ptr.fetch[Number](0))
    println("fetch 0 WhiteSpace ? "+ptr.fetch[WhiteSpace](0))
  }

  test("Parse atom") {
    println("=== Parse atom =====================")

    import JsonParser._
    import JsonParser.Token._

    val tokens = Lexer
      .parse("10 true false null 'str'")
      .dropWhitespaces

    val showTok = summon[Show[Token]]
    tokens.zipWithIndex.foreach((t,i) => println(s"$i "+showTok(t)))

    val ptr = LPtr(0,tokens)
    assert( Parser.atom(ptr  ).get._1.isInstanceOf[AST.Num] )
    assert( Parser.atom(ptr+1).get._1.isInstanceOf[AST.True] )
    assert( Parser.atom(ptr+2).get._1.isInstanceOf[AST.False] )
    assert( Parser.atom(ptr+3).get._1.isInstanceOf[AST.Null] )
    assert( Parser.atom(ptr+4).get._1.isInstanceOf[AST.Str] )
  }
}
