package xyz.cofe.jtfm.store.json

trait ToJsonString[T]:
  def toJsonString(t:T):String

object ToJsonString:
  given ToJsonString[JS.Null] with
    def toJsonString(n:JS.Null) = "null"
  given ToJsonString[JS.Bool] with
    def toJsonString(b:JS.Bool) = b.value match 
      case true => "true"
      case false => "false"
  given ToJsonString[JS.Str] with
    def toJsonString(s:JS.Str) = s.value match
      case null => "null"
      case _ => s.value.encodeLitteral
  given ToJsonString[JS.Num] with
    def toJsonString(n:JS.Num) = n.value.toString()
  given ToJsonString[JS.Arr] with
    def toJsonString(a:JS.Arr) = a.value.map { it => summon[ToJsonString[JS]].toJsonString(it) }.mkString("[", ",", "]")
  given ToJsonString[JS.Obj] with
    def toJsonString(o:JS.Obj) = o.fields.map { (k,v) => 
      k.encodeLitteral+":"+summon[ToJsonString[JS]].toJsonString(v) 
      }.mkString("{",",","}")
  given ToJsonString[JS] with
    def toJsonString(j:JS) = j match
      case n:JS.Null => summon[ToJsonString[JS.Null]].toJsonString(n)
      case n:JS.Bool => summon[ToJsonString[JS.Bool]].toJsonString(n)
      case n:JS.Str => summon[ToJsonString[JS.Str]].toJsonString(n)
      case n:JS.Num => summon[ToJsonString[JS.Num]].toJsonString(n)
      case n:JS.Arr => summon[ToJsonString[JS.Arr]].toJsonString(n)
      case n:JS.Obj => summon[ToJsonString[JS.Obj]].toJsonString(n)

