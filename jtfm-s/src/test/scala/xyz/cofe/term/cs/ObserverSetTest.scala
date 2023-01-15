package xyz.cofe.term.cs

class ObserverSetTest extends munit.FunSuite:
  test("test") {
    val obSet = ObserverSet.sorted[Int]
    obSet.onInsert(v => println(s"ins $v"))
    obSet.onDelete(v => println(s"del $v"))

    obSet.include(1)
    obSet.include(List(8,9,1,2,3,4,4,5,6,7))

    println(obSet)

    obSet.exclude(1)
    obSet.exclude(List(3,3,4))

    println(obSet)
  }
