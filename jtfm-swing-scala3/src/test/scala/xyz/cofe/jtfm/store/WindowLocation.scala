package xyz.cofe.jtfm.store

import java.text.Normalizer

enum WindowState:
  case Normal
  case Minimized
  case Maximized

case class WindowLocation(state:WindowState, left:Int, top:Int, width:Int, height:Int)

trait ToJson[T] {
  def toJson( t:T ):Json
}

trait FromJson[T,E] {
  def fromJson(json:Json):Either[E,T]
}

class WindowLocTest extends munit.FunSuite {
  test("json encode") {
    import Json._

    val jsObj = JsObject( 
      List(
        JsField("str",JsString("strValue")),
        JsField("num",JsNumber(15.2)),
        JsField("bo",JsBool(true)),
        JsField("arr",JsArray(
          List(
            JsString("strValue"),
            JsNumber(15.2),
            JsBool(true),
            JsObject()
          )
        )),
      )
    )

    println(jsObj)
    println(summon[ToString[Json]].toString(jsObj))
  }
}
