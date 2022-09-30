package xyz.cofe.jtfm.store.json

object syntax {
  extension (json:String)
    def parseJson[T <: Product : FromJson]:Either[String,T] =
      for 
        astTree <- Parser.parse(json).lift(s"can't parse ast of $json")
        jsTree <- astTree.toJson
        obj <- summon[FromJson[T]].fromJson(jsTree)
      yield
        obj

  extension [T <: Product : ToJson]( t:T )
    def asJson:Either[String,String] =
      summon[ToJson[T]].toJson(t).map(_.json)
      
}