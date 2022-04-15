package xyz.cofe.jtfm.wid

import com.googlecode.lanterna.input.KeyType
import java.util.regex.Pattern
import com.googlecode.lanterna.input.KeyStroke

/** клавиатурные комбинации */
sealed trait Shortcut
object Shortcut {
  case class FunShortcut( keyType:KeyType, ctrl:Boolean=false, alt:Boolean=false, shift:Boolean=false ) extends Shortcut:
    def test( ke:KeyStroke ):Boolean =
      ke.getKeyType() == keyType && 
      ke.isCtrlDown() == ctrl && ke.isAltDown() == alt && ke.isShiftDown() == shift
    override def toString():String = {
      val sb = new java.lang.StringBuilder()
      if( ctrl )sb.append("C+")
      if( alt )sb.append("A+")
      if( shift )sb.append("S+")
      sb.append(keyType2Name(keyType))
      sb.toString
    }

  case class ChrShortcut( chr:Char, ctrl:Boolean=false, alt:Boolean=false, shift:Boolean=false ) extends Shortcut:
    def test( ke:KeyStroke ):Boolean =
      ke.getKeyType() == KeyType.Character && ke.getCharacter() == chr && 
      ke.isCtrlDown() == ctrl && ke.isAltDown() == alt && ke.isShiftDown() == shift
    override def toString():String = {
      val sb = new java.lang.StringBuilder()
      if( ctrl )sb.append("C+")
      if( alt )sb.append("A+")
      if( shift )sb.append("S+")
      sb.append(letter2String(chr))
      sb.toString
    }


  case class SeqShortcut( seq:Seq[FunShortcut|ChrShortcut] ) extends Shortcut:
    def matchBegin(kss:Seq[KeyStroke]):Boolean =
      kss.size >= seq.size &&
      seq.zip(kss).map {
        (sh,ks) => sh match {
          case fSh:FunShortcut => fSh.test(ks)
          case fSh:ChrShortcut => fSh.test(ks)
          //case _ => falseif seq.size<1 then
        }
      }.foldLeft(true)( (r,a) => r && a )
    override def toString():String = {
      seq.size match {
        case 0 => ""
        case 1 => seq(0).toString
        case _ => seq.map(_.toString).mkString(",")
      }
    }

  lazy val keyType2Name : Map[KeyType,String] = Map() ++ name2KeyType.map(_.swap)
  val name2KeyType = Map(
    "left" -> KeyType.ArrowLeft,
    "right" -> KeyType.ArrowRight,
    "up" -> KeyType.ArrowUp,
    "down" -> KeyType.ArrowDown,
    
    "ins" -> KeyType.Insert,
    "del" -> KeyType.Delete,
    "home" -> KeyType.Home,
    "end" -> KeyType.End,
    "p_up" -> KeyType.PageUp,
    "p_down" -> KeyType.PageDown,

    "tab" -> KeyType.Tab,
    "r_tab" -> KeyType.ReverseTab,

    "enter" -> KeyType.Enter,
    
    "F1" -> KeyType.F1, "F2" -> KeyType.F2, "F3" -> KeyType.F3,
    "F4" -> KeyType.F4, "F5" -> KeyType.F5, "F6" -> KeyType.F6,
    "F7" -> KeyType.F7, "F8" -> KeyType.F8, "F9" -> KeyType.F9,
    "F10" -> KeyType.F10, "F11" -> KeyType.F11, "F12" -> KeyType.F12,
    "F13" -> KeyType.F13, "F14" -> KeyType.F14, "F15" -> KeyType.F15,
    "F16" -> KeyType.F16, "F17" -> KeyType.F17, "F18" -> KeyType.F18,
    "F19" -> KeyType.F19,

    "bs" -> KeyType.Backspace,
    "esc" -> KeyType.Escape,
    "eof" -> KeyType.EOF
  )

  private def parseLetter( str:String,offset:Int ):Option[(Char,Int)] = {
    if( str.startsWith("spc",offset) )
      Some((' ',offset+3))
    else if( str.startsWith("space",offset) )
      Some((' ',offset+5))
    else if( str.startsWith("\\\\",offset) )
      Some(('\\',offset+2))
    else if( str.startsWith("\\\"",offset) )
      Some(('"',offset+2))
    else if( str.startsWith("\\,",offset) )
      Some((',',offset+2))
    else if( str.startsWith("\\;",offset) )
      Some((';',offset+2))
    else
      if( str.startsWith("\\",offset) && (offset+2)<str.length )
        Some((str.charAt(offset),offset+2))
      else if( Character.isLetterOrDigit(str.charAt(offset)) )
        Some((str.charAt(offset),offset+1))
      else
        None
  }

  private def letter2String( ltr:Char ):String = {
    if( ltr==' ' )
      "space"
    else if( ltr=='\\' )
      "\\\\"
    else if( ltr=='\'' )
      "\\\'"
    else if( ltr=='"' )
      "\\\""
    else if( ltr==',' )
      "\\,"
    else if( ltr==';' )
      "\\;"
    else if( Character.isLetterOrDigit(ltr) )
      ""+ltr
    else
      "\\"+ltr
  }
  
  private def parseWhiteSpace( str:String, off:Int ):Option[(String,Int)] = {
    var ptr = off
    if( off>=str.length || !Character.isWhitespace(str.charAt(off)) )
      None
    else
      while( ptr<str.length && Character.isWhitespace(str.charAt(ptr)) ){
        ptr += 1
      }
      Some( (str.substring(off,ptr), ptr) )
  }

  enum Mod:
    case Ctrl,Shift,Alt
    def apply( ch:ChrShortcut ):ChrShortcut = this match {
      case Ctrl => ch.copy(ctrl = true)
      case Shift => ch.copy(shift = true)
      case Alt => ch.copy(alt = true)
    }
    def apply( ch:FunShortcut ):FunShortcut = this match {
      case Ctrl => ch.copy(ctrl = true)
      case Shift => ch.copy(shift = true)
      case Alt => ch.copy(alt = true)
    }

  private def parseMod0( str:String, off:Int ):Option[(Mod,Int)] = {
    if str.startsWith("ctrl",off) then
      Some( (Mod.Ctrl, off+4) )
    else if str.startsWith("shift",off) then
      Some( (Mod.Shift, off+5) )
    else if str.startsWith("alt",off) then
      Some( (Mod.Alt, off+3) )
    else if str.startsWith("C",off) then
      Some( (Mod.Ctrl, off+1) )
    else if str.startsWith("A",off) then
      Some( (Mod.Alt, off+1) )
    else if str.startsWith("S",off) then
      Some( (Mod.Shift, off+1) )
    else
      None
  }

  private def parseExpect( str:String, off:Int, alt:String* ):Option[(String,Int)] = {
    alt.flatMap { a =>
      if str.startsWith(a,off) then
        Some(a)
      else
        None
    }.headOption.flatMap( s => Some((s, off+s.length)) )
  }

  def parseMod1( str:String,off:Int ):Option[(Mod,Int)] = {
    (
      for {
        mod <- parseMod0(str,off)
        ws <- parseWhiteSpace(str, mod._2)
        pl <- parseExpect(str, ws._2, "+")
      } yield( (mod._1, pl._2) )
    ).orElse {
      for {
        mod <- parseMod0(str,off)
        pl <- parseExpect(str, mod._2, "+")
      } yield( (mod._1, pl._2) )
    }
  }

  def parseSkipWS[R]( str:String,off:Int,parser:(String,Int)=>Option[(R,Int)] ):Option[(R,Int)] = {
    (for {
      ws <- parseWhiteSpace(str,off)
      r <- parser(str,ws._2)
    } yield( (r._1, r._2) )).orElse {
      parser(str,off)
    }
  }

  def parseChrShortcut( str:String, off:Int ):Option[(ChrShortcut,Int)] = {
    (
      for {
        m1 <- parseSkipWS( str, off, parseMod1 )
        m2 <- parseSkipWS( str, m1._2, parseMod1 )
        m3 <- parseSkipWS( str, m2._2, parseMod1 )
        ch <- parseSkipWS( str, m3._2, parseLetter )
        sc = m3._1( m2._1( m1._1( ChrShortcut(ch._1) ) ) )
      } yield( (sc, ch._2) )
    ).orElse {
      for {
        m1 <- parseSkipWS( str, off, parseMod1 )
        m2 <- parseSkipWS( str, m1._2, parseMod1 )
        ch <- parseSkipWS( str, m2._2, parseLetter )
        sc = m2._1( m1._1( ChrShortcut(ch._1) ) ) 
      } yield( (sc, ch._2) )
    }.orElse {
      for {
        m1 <- parseSkipWS( str, off, parseMod1 )
        ch <- parseSkipWS( str, m1._2, parseLetter )
        sc = m1._1( ChrShortcut(ch._1) )
      } yield( (sc, ch._2) )
    }.orElse {
      for {
        ch <- parseSkipWS( str, off, parseLetter )
        sc = ChrShortcut(ch._1)
      } yield( (sc, ch._2) )
    }
  }

  private def parseKeyType( str:String, off:Int ):Option[(KeyType,Int)] = {
    name2KeyType.flatMap { case(name,kt) =>
      if str.startsWith(name,off) then
        if off+name.length>=str.length then
          Some( (kt, off+name.length) )
        else if " ;,+{}()[]\\\"'.!@#$%^&*=".indexOf( str.charAt(off+name.length) )>=0 then
          Some( (kt,off+name.length) )
        else
          None
      else
        None
    }.headOption
  }

  def parseFunShortcut( str:String, off:Int ):Option[(FunShortcut,Int)] = {
    (
      for {
        m1 <- parseSkipWS( str, off, parseMod1 )
        m2 <- parseSkipWS( str, m1._2, parseMod1 )
        m3 <- parseSkipWS( str, m2._2, parseMod1 )
        ch <- parseSkipWS( str, m3._2, parseKeyType )
        sc = m3._1( m2._1( m1._1( FunShortcut(ch._1) ) ) )
      } yield( (sc, ch._2) )
    ).orElse {
      for {
        m1 <- parseSkipWS( str, off, parseMod1 )
        m2 <- parseSkipWS( str, m1._2, parseMod1 )
        ch <- parseSkipWS( str, m2._2, parseKeyType )
        sc = m2._1( m1._1( FunShortcut(ch._1) ) ) 
      } yield( (sc, ch._2) )
    }.orElse {
      for {
        m1 <- parseSkipWS( str, off, parseMod1 )
        ch <- parseSkipWS( str, m1._2, parseKeyType )
        sc = m1._1( FunShortcut(ch._1) )
      } yield( (sc, ch._2) )
    }.orElse {
      for {
        ch <- parseSkipWS( str, off, parseKeyType )
        sc = FunShortcut(ch._1)
      } yield( (sc, ch._2) )
    }
  }

  def parseShortcutOne( str:String, off:Int ):Option[(ChrShortcut|FunShortcut,Int)] = {
    parseFunShortcut(str,off).orElse {
      parseChrShortcut(str,off)
    }
  }

  def parseShortcutSeq( str:String, off:Int ):Option[(SeqShortcut,Int)] = {
    var lst:List[FunShortcut|ChrShortcut] = List()
    var ptr = off
    var stop = false

    while( !stop ){
      parseShortcutOne(str,ptr) match {
        case Some((sh,nextPtr)) =>
          lst = lst :+ sh
          val prs = (parseExpect(_,_,","))
          parseSkipWS(str,nextPtr,prs) match {
            case Some((_,nextPtr2)) =>
              ptr = nextPtr2
            case None =>
              stop = true
          }
        case None => stop = true
      }      
    }
    if lst.isEmpty then
      None
    else
      Some( (SeqShortcut(lst), ptr) )
  }

  def parseShortcut( str:String, off:Int ):Option[(Shortcut,Int)] = {
    parseShortcutSeq(str,off).flatMap{ (ss,idx) =>
      if ss.seq.size<1 then
        None
      else if ss.seq.size>1 then
        Some( (ss,idx) )
      else 
        Some( (ss.seq(0),idx ) )
    }
  }

  def parse( str:String ):Option[Shortcut] = parseShortcut(str,0).map { (r,_) => r }
}
