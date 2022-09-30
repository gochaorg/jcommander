package xyz.cofe.jtfm.store

import java.text.Normalizer
import Json._

enum WindowState:
  case Normal
  case Minimized
  case Maximized

object WindowState:
  given ToJson[WindowState] with
    def toJson(s:WindowState) = s match
      case WindowState.Maximized => Right(JS.Str("max"))
      case WindowState.Minimized => Right(JS.Str("min"))
      case WindowState.Normal => Right(JS.Str("norm"))
  given FromJson[WindowState] with
    def fromJson(j:JS) = j match
      case JS.Str("max") => Right(WindowState.Maximized)
      case JS.Str("min") => Right(WindowState.Minimized)
      case JS.Str("norm") => Right(WindowState.Normal)
      case _ => Left(s"undefined state for $j")

extension[T] (optValue:Option[T])
  def lift:Either[String,T] = optValue match
    case Some(v) => Right(v)
    case None => Left("value not exists")
  def lift(errMessage:String):Either[String,T] = optValue match
    case Some(v) => Right(v)
    case None => Left(errMessage)

case class WindowLocation(state:WindowState, left:Int, top:Int, width:Int, height:Int)
object WindowLocation:
  given ToJson[WindowLocation] with
    def toJson(loc:WindowLocation) =
      for
        j_s <- summon[ToJson[WindowState]].toJson(loc.state)
        j_l <- summon[ToJson[Int]].toJson(loc.left)
        j_t <- summon[ToJson[Int]].toJson(loc.top)
        j_w <- summon[ToJson[Int]].toJson(loc.width)
        j_h <- summon[ToJson[Int]].toJson(loc.height)
      yield
        JS.Obj(Map(
          "state" -> j_s,
          "left" -> j_l,
          "top" -> j_t,
          "width" -> j_w,
          "height" -> j_h
        ))
  given FromJson[WindowLocation] with
    def fromJson(j:JS) = j match
      case JS.Obj(fields) => 
        for
          st_j <- fields.get("state").lift("state not readed")
          st_v <- summon[FromJson[WindowState]].fromJson(st_j)

          l_j <- fields.get("left").lift("left not readed")
          l_v <- summon[FromJson[Int]].fromJson(l_j)

          t_j <- fields.get("top").lift("top not readed")
          t_v <- summon[FromJson[Int]].fromJson(t_j)

          w_j <- fields.get("width").lift("width not readed")
          w_v <- summon[FromJson[Int]].fromJson(w_j)

          h_j <- fields.get("height").lift("height not readed")
          h_v <- summon[FromJson[Int]].fromJson(h_j)
        yield
          WindowLocation(st_v, l_v, t_v, w_v, h_v)
      case _ => Left(s"expect obj in $j")

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

class WindowLocTest extends munit.FunSuite {
  test("json encode") {
    import Json._

    val jsObj = JS.Obj( 
      Map(
        "str" -> JS.Str("strValue"),
        "num" -> JS.Num(15.2),
        "bo" -> JS.Bool(true),
        "arr" -> JS.Arr(
          List(
            JS.Str("strValue"),
            JS.Num(15.2),
            JS.Bool(true),
            JS.Obj()
          )
        )
      )
    )

    println(jsObj.json)
  }

  test("encode by given") {
    println("="*40)
    println("encode by given")
    
    val loc = WindowLocation(WindowState.Normal, 1, 2, 3, 4)
    val js = summon[ToJson[WindowLocation]].toJson(loc)
    println(js)

    val loc_r = summon[FromJson[WindowLocation]].fromJson(js.getOrElse(null)).getOrElse(null)
    println(loc_r)
    println(loc)
    println(loc_r == loc)

    println( summon[ToJson[Pos]].toJson(Pos(1,2)) )
  }

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
