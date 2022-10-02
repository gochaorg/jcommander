package xyz.cofe.jtfm.store.json

class DeriveTest extends munit.FunSuite {
  case class Pos(x:Int, y:Int)

  enum Color:
    case Red, Green, Blue

  object Color:
    given ToJson[Color] with
      def toJson(col:Color):Either[String,JS] =
        col match
          case Red => Right(JS.Str("red"))
          case Green => Right(JS.Str("green"))
          case Blue => Right(JS.Str("blue"))
    given FromJson[Color] with
      def fromJson(js:JS):Either[String,Color] =
        js match
          case JS.Str("red") => Right(Red)
          case JS.Str("green") => Right(Green)
          case JS.Str("blue") => Right(Blue)
          case _ => Left(s"can't read color from $js")

  case class ColorPos(x:Int, y:Int, color:Color)

  test("encode by derive") {
    println("="*30)
    println("encode by derive")

    val cpos = ColorPos(1,2,Color.Green)
    println(cpos)
    println(summon[ToJson[ColorPos]].toJson(cpos))
  }

  test("decode by derive") {
    println("="*30)
    println("decode by derive")

    val cpos = ColorPos(1,2,Color.Green)
    println(cpos)

    val jsEt = summon[ToJson[ColorPos]].toJson(cpos)
    val jsTree = jsEt.getOrElse(null)
    println(jsTree)

    val jsStr = jsTree.json
    println(jsStr)

    val res = summon[FromJson[ColorPos]].fromJson(jsTree)
    println(res)

    assert(res.isRight)
    val restored = res.getOrElse(null)

    assert(cpos == restored)
  }

  test("decode by derive - json") {
    println("="*40)
    println("decode by derive - json")

    val jsonStr = "{\"x\":1.0,\"y\":2.0,\"color\":\"green\"}"
    val result = for 
      astTree <- Parser.parse(jsonStr).lift(s"can't parse ast of $jsonStr")
      jsTree <- astTree.toJson
      obj <- summon[FromJson[ColorPos]].fromJson(jsTree)
    yield
      obj

    println( result )
    assert( result.isRight )
    assert( result.map( o => o == ColorPos(1, 2, Color.Green) ).getOrElse(false) )
  }

  test("syntax, from json") {
    import syntax._

    println("="*40)
    println("syntax, from json")

    val result = "{\"x\":1.0,\"y\":2.0,\"color\":\"green\"}".parseJson[ColorPos]
    println( result )
    assert( result.isRight )
    assert( result.map( o => o == ColorPos(1, 2, Color.Green) ).getOrElse(false) )
  }

  test("syntax, to json") {
    import syntax._

    println("="*40)
    println("syntax, to json")

    val result = ColorPos(1,2,Color.Red).asJson
    println( result )
    assert( result.isRight )
  }

  case class Append(value:String)
  object Append:
    given DefaultValue[Append] with
      override def defaultValue: Option[Append] = Some(Append("def_value"))

  case class ColorPos2(x:Int, y:Int, color:Color, append:Append)

  test("syntax, from json + defaultValue") {
    import syntax._

    println("="*40)
    println("syntax, to json")

    val result = "{\"x\":1.0,\"y\":2.0,\"color\":\"green\"}".parseJson[ColorPos2]

    println( result )
    assert( result.isRight )
  }
}
