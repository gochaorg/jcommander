package xyz.cofe.jtfm.store.json

class LexerTest extends munit.FunSuite {
  test("Lexer test") {
    val showTok = summon[Show[Token]]
    Lexer.parse(
      "10 0 12.3 -14 -0 -0.34 true false null 'single \\' quoted' \"double qouted\""
    ).foreach(t => println(showTok(t)))
  }
}