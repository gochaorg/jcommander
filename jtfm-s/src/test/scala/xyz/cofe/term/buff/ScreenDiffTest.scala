package xyz.cofe.term.buff

import xyz.cofe.term.common.Color

class ScreenDiffTest extends munit.FunSuite:
  test("diff - equals") {
    val buf1 = Buffer()
    assert( buf1.resize(5,5).isRight )

    val buf2 = buf1.copy
    assert( buf1.diff(buf2).size==0 )

    buf2.set(0,0,ScreenChar('a', Color.Black, Color.White))
    buf2.set(1,1,ScreenChar('a', Color.Black, Color.White))

    val diffs = buf1.diff(buf2)
    println(diffs)
  }

  test("diff changes") {
    val buf1 = Buffer()
    assert( buf1.resize(5,5).isRight )

    val buf2 = buf1.copy

    val chr = ScreenChar('a', Color.Black, Color.White)
    
    buf2.set(0,0,chr)
    
    assert( buf2.get(0,0)==Some(chr) )
    assert( buf1.get(0,0)!=buf2.get(0,0) )

    val diffs = buf1.diff(buf2)
    assert(diffs.size == 1)
  }

  test("get/set") {
    val buf1 = Buffer()
    assert( buf1.resize(5,5).isRight )
    assert( buf1.width == 5, "expect width 5" )
    assert( buf1.height == 5, "expect height 5" )

    val chr = ScreenChar('a', Color.Black, Color.White)
    buf1.set(0,0,chr)

    assert( buf1.get(0,0) == Some(chr) )
  }

  test("diffs count") {
    val buf1 = Buffer()
    assert( buf1.resize(5,5).isRight )

    val buf2 = ChangeMetricBuffer(buf1)
    val chr = ScreenChar('a', Color.Black, Color.White)
    buf2.set(0,0,chr)

    assert(buf2.changeCount == 1)

    buf2.set(0,0,chr)
    assert(buf2.changeCount == 1)

    buf2.set(1,0,chr)
    assert(buf2.changeCount == 2)
  }
