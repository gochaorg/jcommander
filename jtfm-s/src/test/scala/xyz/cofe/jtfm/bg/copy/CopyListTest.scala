package xyz.cofe.jtfm.bg.copy

class CopyListTest extends munit.FunSuite:
  test("test copy list"){
    var log = List.empty[(String,String)]
    CopyList(
      (what:String, state:String) => {
        log = log :+ (what,state)
        state match
          case "s0" => Some("s1")
          case "s1" => Some("s2")
          case _ => None
      }
    ).copy(
      List("a","b","c","d"),
      "s0"
    )
    assert(log.size==3)
    assert(log(0) == ("a","s0"))
    assert(log(1) == ("b","s1"))
    assert(log(2) == ("c","s2"))
  }
