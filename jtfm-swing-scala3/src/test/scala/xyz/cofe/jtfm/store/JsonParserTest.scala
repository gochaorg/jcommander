package xyz.cofe.jtfm.store

class JsonParserTest extends munit.FunSuite {
  import Json._

  test("Lexer test") {
    val showTok = summon[Show[Token]]
    Json.Lexer.parse(
      "10 0 12.3 -14 -0 -0.34 true false null 'single \\' quoted' \"double qouted\""
    ).foreach(t => println(showTok(t)))
  }

  test("LPtr fetch") {
    import Json._
    import Json.Token._
    println("=== LPtr fetch =====================")

    val tokens = Json.Lexer.parse("10 true 'single'")
    val showTok = summon[Show[Token]]
    tokens.zipWithIndex.foreach((t,i) => println(s"$i "+showTok(t)))

    val ptr = Json.LPtr(0,tokens)
    println("fetch 0 Number ? "+ptr.fetch[Number](0))
    println("fetch 0 WhiteSpace ? "+ptr.fetch[WhiteSpace](0))
  }

  test("Parse atom") {
    println("=== Parse atom =====================")

    import Json._
    import Json.Token._

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

  test("Parse array") {
    println("=== Parse array =====================")

    import Json._
    import Json.Token._

    val tokens = Lexer
      .parse("[ 1, true, 'str' ]")
      .dropWhitespaces

    val showTok = summon[Show[Token]]
    tokens.zipWithIndex.foreach((t,i) => println(s"$i "+showTok(t)))

    val ptr = LPtr(0,tokens)
    val arr = Parser.array(ptr)
    println(arr)
  }

  test("str decode") {
    println("=== str decode  =====================")

    import Json._
    import Json.Token._

    println("'key'".decodeLitteral)
    assert("'key'".decodeLitteral == "key")
    assert("'ke\\'y'".decodeLitteral == "ke'y")
  }

  test("Field parse"){
    println("=== Field parse =====================")

    import Json._
    import Json.Token._

    val tokens = Lexer
      .parse("'key' : 1")
      .dropWhitespaces

    val showTok = summon[Show[Token]]
    tokens.zipWithIndex.foreach((t,i) => println(s"$i "+showTok(t)))

    val ptr = LPtr(0,tokens)
    val optExp = Parser.field(ptr)
    println(optExp)

    assert(optExp.isDefined)
    assert(optExp.get._1.isInstanceOf[AST.Field])
    
    val fld = optExp.get._1
    println(s"fld begin ${fld.begin}")
    println(s"fld end ${fld.end}")

    assert(fld.name == "key")
    assert(fld.begin.value==0)
    assert(fld.end.value==9)
  }

  test("Field parse 2"){
    println("=== Field parse 2 =====================")

    import Json._
    import Json.Token._

    val tokens = Lexer
      .parse("key : 12")
      .dropWhitespaces

    val showTok = summon[Show[Token]]
    tokens.zipWithIndex.foreach((t,i) => println(s"$i "+showTok(t)))

    val ptr = LPtr(0,tokens)
    val optExp = Parser.field(ptr)
    println(optExp)

    assert(optExp.isDefined)
    assert(optExp.get._1.isInstanceOf[AST.Field])
    
    val fld = optExp.get._1
    println(s"fld begin ${fld.begin}")
    println(s"fld end ${fld.end}")

    assert(fld.name == "key")
    assert(fld.begin.value==0)
    assert(fld.end.value==8)
  }

  test("Parse obj") {
    println("=== Parse obj =====================")

    import Json._
    import Json.Token._

    val tokens = Lexer
      .parse("{ 'key' : 1, 'keyb' : true }")
      .dropWhitespaces

    val showTok = summon[Show[Token]]
    tokens.zipWithIndex.foreach((t,i) => println(s"$i "+showTok(t)))

    val ptr = LPtr(0,tokens)
    val optExp = Parser.expression(ptr)
    val obj = optExp.get._1.asInstanceOf[AST.Obj]

    println( s"body ${obj.body.size}" )
    println( s"fields, ${obj.fields.size}" )
    println( obj.fields("key") )
    println( obj.fields("key").num )

    println( obj.json )
  }

  test("Parse obj 2") {
    println("=== Parse obj 2 =====================")

    import Json._
    import Json.Token._

    val tokens = Lexer
      .parse("{ 'key' : 1, keyb : true, ar: [1,2,3] }")
      .dropWhitespaces

    val showTok = summon[Show[Token]]
    tokens.zipWithIndex.foreach((t,i) => println(s"$i "+showTok(t)))

    val ptr = LPtr(0,tokens)
    val optExp = Parser.expression(ptr)
    val obj = optExp.get._1.asInstanceOf[AST.Obj]

    println( s"body ${obj.body.size}" )
    println( s"fields, ${obj.fields.size}" )
    println( obj.fields("key") )
    println( obj.fields("key").num )

    println( obj.json )
  }
}
