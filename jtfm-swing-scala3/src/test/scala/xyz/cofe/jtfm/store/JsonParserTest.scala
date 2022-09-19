package xyz.cofe.jtfm.store

class JsonParserTest extends munit.FunSuite {
  import JsonParser._

  test("Lexer test") {
    val showTok = summon[Show[Token]]
    JsonParser.Lexer.parse(
      "10 0 12.3 -14 -0 -0.34 true false null 'single \\' quoted' \"double qouted\""
    ).foreach(t => println(showTok(t)))
  }
}
