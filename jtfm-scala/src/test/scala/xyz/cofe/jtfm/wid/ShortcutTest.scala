package xyz.cofe.jtfm.wid

import org.junit.Test

class ShortcutTest {
  @Test
  def test01():Unit = {
    println( Shortcut.parseMod1("ctrl +",0) )
    println( Shortcut.parseMod1("ctrl+",0) )
    println( Shortcut.parseMod1("  ctrl+",0) )
    println( Shortcut.parseSkipWS("  ctrl+",0, Shortcut.parseMod1) )
    println( Shortcut.parseChrShortcut("ctrl + a",0) )
    println( Shortcut.parseChrShortcut("ctrl + alt + a",0) )
    println( Shortcut.parseChrShortcut("ctrl+alt+a",0) )
    println( Shortcut.parseChrShortcut("a",0) )

    println( Shortcut.parseFunShortcut("F1",0) )
    println( Shortcut.parseFunShortcut("ctrl+F1",0) )
    println( Shortcut.parseFunShortcut("C+F1",0) )
    println( Shortcut.parseFunShortcut("C + F1",0) )

    println( Shortcut.parseShortcutOne("C + F2",0) )
    println( Shortcut.parseShortcutOne("C + a",0) )

    println( Shortcut.parseShortcutSeq("C+F3", 0) )
    println( Shortcut.parseShortcut("C+F3,C+F4", 0) )
    println( Shortcut.parse("C+F3,S+F5") )
  }
}
