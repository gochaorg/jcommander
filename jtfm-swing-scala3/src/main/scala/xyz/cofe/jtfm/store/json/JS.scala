package xyz.cofe.jtfm.store.json

enum JS:
  case Null()
  case Bool(val value:Boolean)
  case Str(val value:String)
  case Num(val value:Double)
  case Arr(val value:Seq[JS]=List())
  case Obj(val fields:Map[String,JS]=Map())
  def json:String =
    summon[ToJsonString[JS]].toJsonString(this)

