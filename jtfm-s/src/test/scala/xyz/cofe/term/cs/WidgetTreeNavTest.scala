package xyz.cofe.term.cs

import xyz.cofe.term.ui.Panel
import xyz.cofe.term.ui.SesInput
import xyz.cofe.term.ui.Widget
import xyz.cofe.jtfm.FocPanel
import xyz.cofe.term.ui.WidgetInput

class WidgetTreeNavTest extends munit.FunSuite:
  test("widget navigate") {
    println(s"widget navigate")
    println(s"="*40)

    val p0 = FocPanel()
    val p1 = FocPanel()
    val p2 = FocPanel()
    val p3 = FocPanel()
    p0.children.get.append(p1)
    p0.children.get.append(p2)
    p2.children.get.append(p3)

    val names = Map[Widget,String](p0->"p0", p1->"p1", p2->"p2", p3->"p3")

    val nav = SesInput.NavigateFrom(p0).forward.typed[WidgetInput]
    val f0 = nav.next()
    println(s"f0 ${names.get(f0)}")

    val f1 = nav.next()
    println(s"f1 ${names.get(f1)}")

    val f2 = nav.next()
    println(s"f2 ${names.get(f2)}")
  }