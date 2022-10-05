package xyz.cofe.jtfm

import xyz.cofe.jtfm.ObserverList

class ObserverListTest extends munit.FunSuite {
  test("check") {
    val list = ObserverList[String]()
    list.listen {println}

    list.insert("abc")
    list.insert("bcd")
    list.insert("cde")
    list.delete(0)
    list.update(0,"def")
  }
}