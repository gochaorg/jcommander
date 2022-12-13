package xyz.cofe.term.buff

import xyz.cofe.term.common.Color
import xyz.cofe.term.common.Size

class ScreenBufSyncTest extends munit.FunSuite:
  test("3x3") {
    val buff = Buffer()
    buff.resize(3,3)

    buff.set(0,0,ScreenChar('a',Color.Black,Color.White))
    buff.set(1,0,ScreenChar('b',Color.Black,Color.White))
    buff.set(2,0,ScreenChar('c',Color.Black,Color.White))

    buff.set(0,1,ScreenChar('d',Color.Black,Color.White))
    buff.set(1,1,ScreenChar('e',Color.Black,Color.White))
    buff.set(2,1,ScreenChar('f',Color.Black,Color.White))

    buff.set(0,2,ScreenChar('g',Color.Black,Color.White))
    buff.set(1,2,ScreenChar('h',Color.Black,Color.White))
    buff.set(2,2,ScreenChar('i',Color.Black,Color.White))

    ScreenBufSync
      .batching(buff, Size(buff.width, buff.height))
      .zipWithIndex.foreach { case (cmd,idx) => 
        println(s"[${idx.toString().padTo(3,' ')}] $cmd")
      }
  }

  test("3x3 2") {
    println("-"*60)
    val buff = Buffer()
    buff.resize(3,3)

    buff.set(0,0,ScreenChar('a',Color.Black,Color.White))
    buff.set(1,0,ScreenChar('b',Color.Black,Color.White))
    buff.set(2,0,ScreenChar('c',Color.Black,Color.White))

    buff.set(0,1,ScreenChar('d',Color.White,Color.Green))
    buff.set(1,1,ScreenChar('e',Color.Red,Color.Green))
    buff.set(2,1,ScreenChar('f',Color.Black,Color.White))

    buff.set(0,2,ScreenChar('g',Color.Black,Color.White))
    buff.set(1,2,ScreenChar('h',Color.Black,Color.White))
    buff.set(2,2,ScreenChar('i',Color.Black,Color.White))

    ScreenBufSync
      .batching(buff, Size(buff.width, buff.height))
      .zipWithIndex.foreach { case (cmd,idx) => 
        println(s"[${idx.toString().padTo(3,' ')}] $cmd")
      }
  }