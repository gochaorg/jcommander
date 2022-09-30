package xyz.cofe.jtfm.store

import java.text.Normalizer
import Json._

enum WindowState:
  case Normal
  case Minimized
  case Maximized

object WindowState
  given ToJson[WindowState] with
    def toJson(s:WindowState) = s match
      case WindowState.Maximized => Right(JS.Str("max"))
      case WindowState.Minimized => Right(JS.Str("min"))
      case WindowState.Normal => Right(JS.Str("norm"))
  given FromJson[WindowState] with
    def fromJson(j:JS) = j match
      case JS.Str("max") => Right(WindowState.Maximized)
      case JS.Str("min") => Right(WindowState.Maximized)
      case JS.Str("norm") => Right(WindowState.Maximized)
      case _ => Left(s"undefined state for $j")

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
  }
}
