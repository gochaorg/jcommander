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
    import JsonParser.Token._
    println("=== LPtr fetch =====================")

    val tokens = JsonParser.Lexer.parse("10 true 'single'")
    val showTok = summon[Show[Token]]
    tokens.zipWithIndex.foreach((t,i) => println(s"$i "+showTok(t)))

    val ptr = JsonParser.LPtr(0,tokens)
    println("fetch 0 Number ? "+ptr.fetch[Number](0))
    println("fetch 0 WhiteSpace ? "+ptr.fetch[WhiteSpace](0))
  }
}
