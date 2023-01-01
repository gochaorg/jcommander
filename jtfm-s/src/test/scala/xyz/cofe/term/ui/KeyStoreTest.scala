package xyz.cofe.term.ui

import xyz.cofe.term.common.ev.InputKeyEventBase
import xyz.cofe.term.common.KeyName
import xyz.cofe.term.common.ev.InputCharEventBase

class KeyStoreTest extends munit.FunSuite:
  val keyEv1 = new InputKeyEventBase(KeyName.Backspace,false,false,false) {}
  val keyEv2 = new InputKeyEventBase(KeyName.Backspace,true,false,false) {}

  val chrEv1 = new InputCharEventBase('a',true,false,false) {}

  test("match KeyEvent") {
    assert(KeyStroke.KeyEvent(
      keyEv1.getKey(),
      keyEv1.isAltDown(),keyEv1.isControlDown(),keyEv1.isShiftDown()
    ).matchLeft(List(keyEv1)))

    assert(!KeyStroke.KeyEvent(
      keyEv1.getKey(),
      keyEv1.isAltDown(),keyEv1.isControlDown(),keyEv1.isShiftDown()
    ).matchLeft(List(keyEv2)))

    assert(KeyStroke.KeyEvent(
      keyEv2.getKey(),
      keyEv2.isAltDown(),keyEv2.isControlDown(),keyEv2.isShiftDown()
    ).matchLeft(List(keyEv2)))

    assert(KeyStroke.CharEvent(
      chrEv1.getChar(),
      chrEv1.isAltDown(),chrEv1.isControlDown(),chrEv1.isShiftDown()
    ).matchLeft(List(chrEv1)))
  }

  test("match Seq"){
    assert(
      KeyStroke.Sequence(
        List(
          KeyStroke.KeyEvent( keyEv1.getKey(), keyEv1.isAltDown(),keyEv1.isControlDown(),keyEv1.isShiftDown() ),
          KeyStroke.KeyEvent( keyEv2.getKey(), keyEv2.isAltDown(),keyEv2.isControlDown(),keyEv2.isShiftDown() ),
        )
      ).matchLeft(List(
        keyEv1, keyEv2
      ))
    )
  }

  test("match Seq non"){
    assert(
      ! KeyStroke.Sequence(
        List(
          KeyStroke.KeyEvent( keyEv1.getKey(), keyEv1.isAltDown(),keyEv1.isControlDown(),keyEv1.isShiftDown() ),
          KeyStroke.KeyEvent( keyEv2.getKey(), keyEv2.isAltDown(),keyEv2.isControlDown(),keyEv2.isShiftDown() ),
        )
      ).matchLeft(List(
        keyEv1, chrEv1
      ))
    )
  }

  test("parse KeyStroke"){
    // println( KeyStroke.parse("F1+A") )
    // assert( KeyStroke.parse("F1+A") == Some(KeyStroke.KeyEvent(KeyName.F1,true,false,false)) )
    // assert( KeyStroke.parse("F1") == Some(KeyStroke.KeyEvent(KeyName.F1,false,false,false)) )
    // assert( KeyStroke.parse("a") == Some(KeyStroke.CharEvent('a',false,false,false)) )

    val f1 = KeyStroke.KeyEvent(KeyName.F1,false,false,false)
    val f2 = KeyStroke.KeyEvent(KeyName.F2,false,false,false)

    val str = "F1,F2"
    val ksseq = KeyStroke.Sequence(List( f2,f1 ))
    assert( KeyStroke.parse(str) == Some(ksseq) )
    assert(ksseq.toString() == str)
  }
