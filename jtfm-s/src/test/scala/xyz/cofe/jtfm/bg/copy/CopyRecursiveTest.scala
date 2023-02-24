package xyz.cofe.jtfm.bg.copy

class CopyRecursiveTest extends munit.FunSuite:
  // d0
  //  d1
  //   b
  //   d3
  //    f
  //   c
  //  d2
  //   _d
  //   e
  //  a
  //
  test("recusive test") {
    CopyRecursive[String,Int](
      (item,state) => 
        item.startsWith("d")
        ,
      (item,state) => 
        item match
          case "d0" => List("d1","d2","a")
          case "d1" => List("b","d3","c")
          case "d2" => List("_d","e")
          case "d3" => List("f")
          case _ => List.empty        
        ,
      (item,state) => 
        println(s"copy $item $state")
        Some(state+1),
      (item,state) => 
        println(s"mkdir $item $state")
        Some(state+1),
    ).copy("d0",0)
  }
