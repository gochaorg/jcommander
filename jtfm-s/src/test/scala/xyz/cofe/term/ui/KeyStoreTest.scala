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

  def keyEvent(keyName:KeyName, alt:Boolean=false, shift:Boolean=false, ctrl:Boolean=false ) =
     new InputKeyEventBase(keyName,alt,shift,ctrl) {}

  test("nested seq") {
    println("="*80)
    println("nested seq")
    val ef1 = keyEvent(KeyName.F1)
    val ef2 = keyEvent(KeyName.F2)
    val ef3 = keyEvent(KeyName.F3)
    val ef4 = keyEvent(KeyName.F4)

    val f1 = KeyStroke.KeyEvent(KeyName.F1,false,false,false)
    val f2 = KeyStroke.KeyEvent(KeyName.F2,false,false,false)
    val f3 = KeyStroke.KeyEvent(KeyName.F3,false,false,false)
    val f4 = KeyStroke.KeyEvent(KeyName.F4,false,false,false)

    val seq1 = KeyStroke.Sequence(List(f2,f1))
    val seq2 = KeyStroke.Sequence(List(f4,f3))
    val seq  = KeyStroke.Sequence(List(seq2,seq1))

    val inpEvents = List(ef4,ef3,ef2,ef1)
    assert( seq.matchLeft(inpEvents) )
  }

  test("parse/toString") {
    assert( List(
      KeyStroke.KeyEvent(KeyName.F1,false,false,false),
      KeyStroke.KeyEvent(KeyName.F12,false,false,false),
      KeyStroke.CharEvent('a', false,false,false),
      KeyStroke.CharEvent('a', true,false,false),
      KeyStroke.CharEvent('a', false,true,false),
      KeyStroke.CharEvent('a', false,false,false),
      KeyStroke.CharEvent(' ', false,false,false),
      KeyStroke.CharEvent('Я', false,false,false),
      KeyStroke.CharEvent('\t',false,false,false),
      KeyStroke.CharEvent('\n',false,false,false),
      KeyStroke.CharEvent('\r',false,false,false),
      KeyStroke.CharEvent('+', false,false,false),
      KeyStroke.CharEvent('-', false,false,false),
      KeyStroke.CharEvent(',', false,false,false),
      KeyStroke.CharEvent('%', false,false,false),
      KeyStroke.CharEvent('<', false,false,false),
      KeyStroke.CharEvent('>', false,false,false),
      KeyStroke.CharEvent('(', false,false,false),
      KeyStroke.CharEvent(')', false,false,false),
      KeyStroke.CharEvent('{', false,false,false),
      KeyStroke.CharEvent('}', false,false,false),
      KeyStroke.CharEvent('[', false,false,false),
      KeyStroke.CharEvent(']', false,false,false),
      KeyStroke.CharEvent('=', false,false,false),
      KeyStroke.CharEvent('!', false,false,false),
      KeyStroke.CharEvent('/', false,false,false),
      KeyStroke.CharEvent('\\',false,false,false),
      KeyStroke.CharEvent('@', false,false,false),
      KeyStroke.CharEvent('\'',false,false,false),
      KeyStroke.CharEvent('\"',false,false,false),
      KeyStroke.CharEvent('_', false,false,false),
      KeyStroke.CharEvent('|', false,false,false),
      KeyStroke.CharEvent('#', false,false,false),
      KeyStroke.CharEvent(':', false,false,false),
      KeyStroke.CharEvent(';', false,false,false),
      KeyStroke.CharEvent('^', false,false,false),
      KeyStroke.CharEvent('?', false,false,false),
      KeyStroke.CharEvent('&', false,false,false),
      KeyStroke.CharEvent('*', false,false,false),
      KeyStroke.CharEvent('$', false,false,false),
      KeyStroke.CharEvent('~', false,false,false),
      KeyStroke.CharEvent('.', false,false,false),
      KeyStroke.CharEvent('`', false,false,false),
    ).foldLeft( true ){ case (res,ks) => 
      print(s"ks $ks")
      if res then
        val res1 = KeyStroke.parse(ks.toString()).map(k => k==ks).getOrElse(false)
        println(" "+(if res1 then "matched" else ""))
        res1
      else        
        false
    })
  }