package xyz.cofe.jtfm.store.json

enum AST( val begin:Ptr, val end:Ptr ):
  case Id(val tok:Token.Identifier, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
  case True(val tok:Token.Identifier, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
  case False(val tok:Token.Identifier, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
  case Null(val tok:Token.Identifier, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
  case Str(val decode:String, val tok:Token.Str, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
  case Num(val tok:Token.Number, begin0:Ptr,end0:Ptr) extends AST(begin0,end0)
  case Arr(items:Seq[AST], begin0:Ptr, end0:Ptr) extends AST(begin0,end0)
  case Field(
    val tok:Token.Str|Token.Identifier,
    val name:String,
    val value:AST, 
    begin0:Ptr,
    end0:Ptr,
  ) extends AST(begin0,end0)
  case Obj(
    val body:Seq[AST],
    begin0:Ptr,end0:Ptr,
  ) extends AST(begin0,end0), ObjOpt
  case Comment(val tok:Token.SLComment,begin0:Ptr,end0:Ptr) extends AST(begin0,end0)

trait ObjOpt:
  self: AST.Obj =>
    lazy val fields: Map[String, AST] =
      self.body
        .filter { e => e.isInstanceOf[AST.Field] }
        .map { e => e.asInstanceOf[AST.Field] }
        .map { f => (f.name, f.value) }
        .toMap

object AST:
  given ToJsonString[Id] with
    def toJsonString(a:Id) = a.tok.text.encodeLitteral
  given ToJsonString[True] with
    def toJsonString(a:True) = a.tok.text
  given ToJsonString[False] with
    def toJsonString(a:False) = a.tok.text
  given ToJsonString[Null] with
    def toJsonString(a:Null) = "null"
  given ToJsonString[Str] with
    def toJsonString(a:Str) = a.tok.text
  given ToJsonString[Num] with
    def toJsonString(a:Num) = a.tok.text
  given ToJsonString[Arr] with
    def toJsonString(a:Arr) = a.items.map { el => 
      summon[ToJsonString[AST]].toJsonString(el) 
    }.mkString("[",",","]")
  given ToJsonString[Obj] with
    def toJsonString(a:Obj) = a.fields.map { (k,v) => 
      k.encodeLitteral+":"+summon[ToJsonString[AST]].toJsonString(v)
    }.mkString("{",",","}")
  given ToJsonString[AST] with
    def toJsonString(a:AST) = a match
      case n:Id => summon[ToJsonString[Id]].toJsonString(n)
      case n:True => summon[ToJsonString[True]].toJsonString(n)
      case n:False => summon[ToJsonString[False]].toJsonString(n)
      case n:Null => summon[ToJsonString[Null]].toJsonString(n)
      case n:Str => summon[ToJsonString[Str]].toJsonString(n)
      case n:Num => summon[ToJsonString[Num]].toJsonString(n)
      case n:Arr => summon[ToJsonString[Arr]].toJsonString(n)
      case n:Obj => summon[ToJsonString[Obj]].toJsonString(n)
      case _ => s"/* $a */"

extension( ast:AST )
  def id:Option[String] = ast match
    case AST.Id(t,_,_) => Some(t.text)
    case _ => None
  def bool:Option[Boolean] = ast match
    case _:AST.True => Some(true)
    case _:AST.False => Some(false)
    case _ => None
  def isNull:Boolean = ast match
    case _:AST.Null => true
    case _ => false
  def str:Option[String] = ast match
    case AST.Str(s,_,_,_) => Some(s)
    case _ => None
  def num:Option[Double] = ast match
    case AST.Num(t,_,_) => t.text.toDoubleOption
    case _ => None
  def list:Option[List[AST]] = ast match
    case AST.Arr(elems,_,_) => Some(elems.toList)
    case _ => None
  def obj:Option[Map[String,AST]] = ast match
    case ob:AST.Obj => Some(ob.fields)
    case _ => None
  def json:String = summon[ToJsonString[AST]].toJsonString(ast)
  def toJson: Either[String,JS] = ast match
    case _:AST.True => Right(JS.Bool(true))
    case _:AST.False => Right(JS.Bool(false))
    case _:AST.Null => Right(JS.Null())
    case s:AST.Str => Right(JS.Str(s.decode))
    case n:AST.Num => n.num.map { x => Right(JS.Num(x)) }.getOrElse( Left("can't fetch num") )
    case n:AST.Arr => n.items.foldLeft( Right(List[JS]()):Either[String,List[JS]] ){ 
      case (a,i) =>
        i.toJson.flatMap { j => 
          a.map { l => j :: l }
        }
      }.map { l => JS.Arr(l.reverse) }
    case n:AST.Obj => n.fields.foldLeft( 
      Right(Map[String,JS]()):Either[String,Map[String,JS]] 
    ){ case (a,i) =>
      i._2.toJson.flatMap { j =>
        a.map { m => m + (i._1 -> j) }
      }
    }.map { m => JS.Obj(m) }
    case _:AST.Id => Left("Not supported for AST.Id")
    case _:AST.Field => Left("Not supported for AST.Field")
    case _:AST.Comment => Left("Not supported for AST.Comment")
