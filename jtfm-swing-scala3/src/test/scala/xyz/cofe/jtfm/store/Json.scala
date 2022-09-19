package xyz.cofe.jtfm.store

enum Json:
  case JsSLComment(comment:String) extends Json
  case JsString(string:String) extends Json
  case JsBool(bool:Boolean) extends Json
  case JsNumber(num:Double) extends Json
  case JsArray(array:Seq[Json]=List[Json]()) extends Json
  case JsObject(fields:Seq[JsField]=List[JsField]()) extends Json
case class JsField(name:String, value:Json)

object Json {
  def encodeJsString(str:String):String = 
    str match
      case null => "null"
      case _ => 
        "\"" + str.flatMap( chr => chr match
          case '"'  => "\\\""
          case '\\' => "\\\\"
          case '\n' => "\\n"
          case '\r' => "\\r"
          case '\t' => "\\t"
          case _ => chr.toString
        ) + "\""

  def encodeJsSLComment(str:String):String = "//" + str

  extension (str:String)
    def toJsLetteral:String = encodeJsString(str)
    def toJsSLComment:String = encodeJsSLComment(str)

  given ToString[Json] with
    def toString(value:Json):String =
      value match
        case e: JsSLComment => summon[ToString[JsSLComment]].toString(e)
        case e: JsString    => summon[ToString[JsString   ]].toString(e)
        case e: JsBool      => summon[ToString[JsBool     ]].toString(e)
        case e: JsNumber    => summon[ToString[JsNumber   ]].toString(e)
        case e: JsArray     => summon[ToString[JsArray    ]].toString(e)
        case e: JsObject    => summon[ToString[JsObject   ]].toString(e)

  given ToString[JsSLComment] with
    def toString(value:JsSLComment):String = value.comment.toJsSLComment

  given ToString[JsString] with
    def toString(t: JsString): String = t.string.toJsLetteral
  
  
  given ToString[JsBool] with
    def toString(b:JsBool):String = b match {
      case JsBool(true) => "true"
      case JsBool(false) => "false"
    }
  
  given ToString[JsNumber] with
    def toString(num:JsNumber) = num.num.toString()
  
  given ToString[JsArray] with
    def toString(arr:JsArray):String =
      "[" +
      arr.array.foldLeft("")( (acc,itm) => {
        val pref = acc.length() match
          case 0 => ""
          case _ => acc + ","
        pref + summon[ToString[Json]].toString(itm)
      }) +
      "]"
  
  given ToString[JsObject] with
    def toString(obj:JsObject):String =
      val sb = new java.lang.StringBuilder()
      "{" +
      obj.fields.foldLeft("")( (acc,field) => {
        val pref = acc.length() match
          case 0 => ""
          case _ => acc + ","
        pref + field.name.toJsLetteral + ":" + summon[ToString[Json]].toString(field.value)
      }) + 
      "}"
}
