package impl

class ImplTest extends munit.FunSuite:
  test("") {
    println("1")
  }

  trait H:
    def msg:String

  def hello(implicit h:H):Unit = {
    println(h.msg)
  }

  class Prov(m:String):
    given h:H with
      override def msg: String = m

  class Call(implicit p:Prov):
    given H = p.h

    def call:Unit =
      hello