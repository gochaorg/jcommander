package xyz.cofe.jtfm.ev

trait ReadProp[T] {
  def get:T
}

trait WriteProp[T] extends ReadProp[T] {
  def set(v:T):Unit
}

object Prop {  
  var computing:List[EvProp[_]] = List()

  class RWProp[T] (
    private var value:T
  ) extends WriteProp[T] {
    def get:T = {
      computing.headOption.foreach { _.addRef(this) }
      value
    }
    def set(v:T):Unit = { value:T }

    private var name:Option[String] = None
    def withName(name:String):RWProp[T] = {
      this.name = Some(name)
      this
    }

    override def toString():String = {
      name match {
        case None => super.toString
        case Some(n) => s"var($n)"
      }
    }
  }

  def writeable[T]( v:T ):RWProp[T] = RWProp(v)

  class EvProp[T] ( compution: =>T )
  extends ReadProp[T] {
    private var _refs:Option[List[ReadProp[_]]] = None
    def refs:List[ReadProp[_]] = if(_refs.isDefined)_refs.get else List()

    private var collected:List[ReadProp[_]] = List()

    def get:T = {
      if( _refs.isEmpty ){
        computing = this :: computing
      }
      try {
        println("compute")
        compution
      } finally {
        if( _refs.isEmpty ){
          computing = computing.drop(1)
          _refs = Some(collected)
        }
      }
    }

    def addRef( prop:ReadProp[_] ):Unit = {      
      println(s"add ref ${prop}")
      collected = (prop :: collected).distinct
    }
  }
  def compute[T]( compution: =>T ):EvProp[T] = EvProp(compution)

  implicit class IntProp( a:ReadProp[Int] ) {
    def + ( b:ReadProp[Int] ):Int = a.get + b.get
    def + ( b:Int ):Int = a.get + b
  }

  implicit class IntEx( a:Int ) {
    def + ( b:ReadProp[Int] ):Int = a + b.get
  }
}