package xyz.cofe.jtfm.gr

import org.junit.Test

class TextBlockTest:
  def show(tb:TextBlock):Unit =
    tb.lines.foreach { line =>
      //print( " ".repeat(line.offset) );
      val pref = " ".repeat(line.offset)
      print(pref)
      println(line.line)
    }

  @Test
  def test01():Unit =
    println("="*40)
    println("test01")
    println("-"*30)
    val tb = TextBlock("some\nline and\nwith other line")
    show(tb)

    println("-"*30)
    show(tb.alignRight)

    println("-"*30)
    show(tb.alignRight.cropLeft(7))

    println("-"*30)
    show(tb.alignCenter)

    println("-"*30)
    show(tb.alignCenter(tb.widthMinMax.get._2+8))

    println("-"*30)
    show(tb.alignLeft.cropRight(7))

    println("-"*30)    

  @Test
  def test02():Unit =    
    println("="*40)
    println("test02")
    println("-"*30)

    val tb = TextBlock("some")
    show(tb)

    println("-"*30)
    show(tb.cropLeft(10).expandLeft(10))

    println("-"*40)
