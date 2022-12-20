package xyz.cofe.term.cs

class ObserverListTest extends munit.FunSuite:
  test("check events") {
    val lst = ObserverList.empty[String]
    lst.onInsert { itm => println(s"insert $itm") }
    lst.onDelete { itm => println(s"delete $itm") }
    lst.onChange { println(s"changed") }

    lst.insert(0,"a")
    lst.insert(0,"b")
    lst.insert(0,"a")
    println(lst)

    lst.delete("a")
    println(lst)

    lst.insert(100,"c")
    println(lst)

    lst.insert(1,"a")
    println(lst)
  }
