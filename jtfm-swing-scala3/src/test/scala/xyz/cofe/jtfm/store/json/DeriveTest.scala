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
    val js = jsEt.getOrElse(null)
    println(js)

    val res = summon[FromJson[ColorPos]].fromJson(js)
    println(res)

    assert(res.isRight)
    val restored = res.getOrElse(null)

    assert(cpos == restored)
  }
}
