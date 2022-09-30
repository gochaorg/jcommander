package xyz.cofe.jtfm.store

import java.text.Normalizer

enum WindowState:
  case Normal
  case Minimized
  case Maximized

case class WindowLocation(state:WindowState, left:Int, top:Int, width:Int, height:Int)

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

    //println(jsObj.json)
    
  }
}
