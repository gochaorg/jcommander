package xyz.cofe.term.ui

import xyz.cofe.term.common.KeyName
import xyz.cofe.term.common.InputEvent
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.InputCharEvent
import java.util.regex.Pattern

enum KeyStroke( val sequenceSize:Int ):
  case KeyEvent( keyName:KeyName, altDown:Boolean, ctrlDown:Boolean, shiftDown:Boolean ) extends KeyStroke(1)
  case CharEvent( char:Char, altDown:Boolean, ctrlDown:Boolean, shiftDown:Boolean ) extends KeyStroke(1)
  case Sequence( events:Seq[KeyStroke] ) extends KeyStroke( if events.isEmpty then 0 else events.map(_.sequenceSize).sum )

  override def toString():String =
    this match
      case e @ KeyStroke.KeyEvent(keyName, altDown, ctrlDown, shiftDown) => 
        val mod = KeyStroke.Modifiers(e)
        KeyStroke.keyName2str(keyName)+mod.parsedString
      case e @ KeyStroke.CharEvent(char, altDown, ctrlDown, shiftDown) =>
        val mod = KeyStroke.Modifiers(e)
        KeyStroke.char2str(char)+mod.parsedString
      case KeyStroke.Sequence(events) =>
        events.map(_.toString()).reverse.mkString(",")

  private def matchLeft0( events:Seq[InputEvent] ):(Boolean,Seq[InputEvent]) = 
    if events.isEmpty
    then (false,events)
    else
      this match
        case KeyStroke.KeyEvent(keyName, altDown, ctrlDown, shiftDown) => 
          events.head match
            case ke:InputKeyEvent =>
              ( ke.getKey() == keyName && ke.isAltDown() == altDown && ke.isControlDown() == ctrlDown && ke.isShiftDown() == shiftDown,
                events.drop(1)
              )
            case _ => (false,events)
        case KeyStroke.CharEvent(char, altDown, ctrlDown, shiftDown) =>
          events.head match
            case ce:InputCharEvent =>
              ( ce.getChar() == char && ce.isAltDown() == altDown && ce.isControlDown() == ctrlDown && ce.isShiftDown() == shiftDown,
                events.drop(1)
              )
            case _ => (false,events)
        case KeyStroke.Sequence(ksEvents) =>
          val (succOpt,tail,_) = ksEvents.foldLeft( (None:Option[Boolean],events,ksEvents) ){ case ((res, eventsTail, ksEventsRest), ks) => 
            res match
              case None =>
                val (m,t) = ks.matchLeft0(eventsTail)
                if m 
                then (
                  if ksEventsRest.tail.isEmpty then Some(true) else None
                  ,t,ksEventsRest.tail)
                else (
                  Some(false)
                  ,t,ksEventsRest.tail)
              case _ => 
                (res,eventsTail,ksEvents.tail)
          }
          succOpt match
            case None => (false,events)
            case Some(false) => (false,events)
            case Some(true)  => (true,tail)
          
  def matchLeft(events:Seq[InputEvent]):Boolean =
    val (res,tail) = matchLeft0(events)
    res

object KeyStroke:
  def char2str( char:Char ):String =
    val code = char.toInt
    if Character.isLetterOrDigit(char) 
    then ""+char
    else
      char match
        case ' '  => "<spc>"
        case '\t' => "<tab>"
        case '\n' => "<lf>"
        case '\r' => "<cr>"
        case '+' => "<plus>"
        case '-' => "<minus>"
        case ',' => "<comma>"
        case '%' => "<pct>"
        case '<' => "<less>"
        case '>' => "<more>"
        case '(' => "<(>"
        case ')' => "<)>"
        case '{' => "<{>"
        case '}' => "<}>"
        case '[' => "<[>"
        case ']' => "<]>"
        case '=' => "<eq>"
        case '!' => "<!>"
        case '/' => "</>"
        case '\\' => "<\\>"
        case '@' => "<@>"
        case '"' => "<d-quot>"
        case '\'' => "<apos>"
        case '_' => "<u-line>"
        case '|' => "<v-line>"
        case '#' => "<#>"
        case ':' => "<colon>"
        case ';' => "<s-colon>"
        case '^' => "<^>"
        case '?' => "<?>"
        case '&' => "<&>"
        case '*' => "<*>"
        case '$' => "<$>"
        case '~' => "<tilde>"
        case '.' => "<dot>"
        case '`' => "<grave>"
        case _ =>
          s"<${code.toHexString}>"

  val hexCharPattern = Pattern.compile("(<([a-fA-F0-9]+)>)")

  def str2char( string:String, off:Int ):Option[(Char,Int)] =
    val str = string.substring(off).toLowerCase()

    def hexChar:Option[(Char,Int)]=
      val m = hexCharPattern.matcher(str)
      if m.matches()
      then
        val digits = m.group(2)
        val all = m.group(1)
        val code = Integer.parseInt(digits,16)
        val chr = code.toChar
        Some(chr, off+all.length())
      else
        None

    def letterOrDigit:Option[(Char,Int)]=
      if str.length()>0 && Character.isLetterOrDigit(str.charAt(0))
      then Some(str.charAt(0),off+1)
      else None

    val mapping = List(
      "<spc>"->' ', "<tab>"->'\t', "<lf>"->'\n', "<cr>"->'\r', "<plus>"->'+', "<minus>"->'-', "<comma>"->',', "<pct>"->'%',
      "<less>"->'<', "<more>"->'>', "<(>"->'(', "<(>"->'(', "<)>"->')', "<{>"->'{', "<}>"->'}', "<[>"->'[', "<]>"->']',
      "<eq>"->'=', "<!>"->'!', "</>"->'/', "<\\>"->'\\', "<@>"->'@', "<\">"->'\"', "<d-quot>"->'\"', "<apos>"->'\'', "<u-line>"->'_',
      "<v-line>"->'|', "<#>"->'#', "<colon>"->':', "<s-colon>"->';', "<^>"->'^', "<?>"->'?', "<&>"->'&', "<*>"->'*', "<$>"->'$',
      "<tilde>"->'~', "<dot>"->'.', "<grave>"->'`'
    )
    mapping.foldLeft( None:Option[(Char,Int)] ){ case (opt,(ptrn,kn)) => 
      opt match
        case None => 
          if str.startsWith(ptrn.toLowerCase())
          then Some( (kn,off + ptrn.length()) )
          else opt
        case Some(value) =>
          opt
    }.orElse(hexChar).orElse(letterOrDigit)

  def keyName2str( keyName:KeyName ):String =
    keyName match
      case KeyName.F1 => "F1"
      case KeyName.F2 => "F2"
      case KeyName.F3 => "F3"
      case KeyName.F4 => "F4"
      case KeyName.F5 => "F5"
      case KeyName.F6 => "F6"
      case KeyName.F7 => "F7"
      case KeyName.F8 => "F8"
      case KeyName.F9 => "F9"
      case KeyName.F10 => "F10"
      case KeyName.F11 => "F11"
      case KeyName.F12 => "F12"
      case KeyName.Escape => "Esc"
      case KeyName.Enter => "Enter"
      case KeyName.Left => "Left"
      case KeyName.Right => "Right"
      case KeyName.Up => "Up"
      case KeyName.Down => "Down"
      case KeyName.Insert => "Ins"
      case KeyName.Delete => "Del"
      case KeyName.Backspace => "Back"
      case KeyName.Home => "Home"
      case KeyName.End => "End"
      case KeyName.PageUp => "PgUp"
      case KeyName.PageDown => "PgDn"
      case KeyName.Tab => "Tab"
      case KeyName.ReverseTab => "RTab"

  def str2keyName( string:String, off:Int ):Option[(KeyName,Int)] =
    val str = string.substring(off).toLowerCase()
    val mapping = List(
      "F1"->KeyName.F1
    , "F2"->KeyName.F2
    , "F3"->KeyName.F3
    , "F4"->KeyName.F4
    , "F5"->KeyName.F5
    , "F6"->KeyName.F6
    , "F7"->KeyName.F7
    , "F8"->KeyName.F8
    , "F9"->KeyName.F9
    , "F10"->KeyName.F10
    , "F11"->KeyName.F11
    , "F12"->KeyName.F12
    , "Escape"->KeyName.Escape, "Esc"->KeyName.Escape
    , "Enter"->KeyName.Enter, "Entr"->KeyName.Enter, "Etr"->KeyName.Enter, "Er"->KeyName.Enter
    , "Left"->KeyName.Left, "Lft"->KeyName.Left, "Lt"->KeyName.Left
    , "Right"->KeyName.Right, "Rght"->KeyName.Right, "Rt"->KeyName.Right
    , "Up"->KeyName.Up
    , "Down"->KeyName.Down, "Dwn"->KeyName.Down, "Dn"->KeyName.Down
    , "Insert"->KeyName.Insert
    , "Ins"->KeyName.Insert, "Del"->KeyName.Delete
    , "Back"->KeyName.Backspace, "Bck"->KeyName.Backspace
    , "Home"->KeyName.Home, "End"->KeyName.End
    , "PgUp"->KeyName.PageUp, "PgDn"->KeyName.PageDown
    , "Tab"->KeyName.Tab, "RTab"->KeyName.ReverseTab
    )
    mapping.foldLeft( None:Option[(KeyName,Int)] ){ case (opt,(ptrn,kn)) => 
      opt match
        case None => 
          if str.toLowerCase().startsWith(ptrn.toLowerCase())
          then Some( (kn,off + ptrn.length()) )
          else opt
        case Some(value) =>
          opt
    }
    
  case class Modifiers(alt:Boolean, ctrl:Boolean, shift:Boolean):
    def isEmpty:Boolean = !(alt || ctrl || shift)
    def apply(ks:KeyStroke):KeyStroke =
      ks match
        case ke:KeyStroke.KeyEvent => 
          ke.copy(
            altDown = alt,
            ctrlDown = ctrl,
            shiftDown = shift
          )
        case ce:KeyStroke.CharEvent =>
          ce.copy(
            altDown = alt,
            ctrlDown = ctrl,
            shiftDown = shift
          )
        case s:KeyStroke.Sequence => 
          s
    def parsedString:String =
      if isEmpty 
      then ""
      else s"+${if alt then "A" else ""}${if ctrl then "C" else ""}${if shift then "S" else ""}"

  object Modifiers:
    def apply(ev:KeyStroke.CharEvent):Modifiers=new Modifiers(ev.altDown, ev.ctrlDown, ev.shiftDown)
    def apply(ev:KeyStroke.KeyEvent):Modifiers=new Modifiers(ev.altDown, ev.ctrlDown, ev.shiftDown)
    def apply(ev:KeyStroke):Option[Modifiers]=
      ev match
        case e:KeyStroke.KeyEvent => Some(apply(e))
        case e:KeyStroke.CharEvent => Some(apply(e))
        case e:KeyStroke.Sequence => None
      
  def modifiers2str(mod:Modifiers):String =
    s"${if mod.alt then "A" else ""}${if mod.ctrl then "C" else ""}${if mod.shift then "S" else ""}"

  val modPattern = Pattern.compile("(\\+(?<mod>[ACS]{1,3}))")
  def str2modifiers(string:String, off:Int):Option[(Modifiers,Int)]=
    val str = string.substring(off)
    var alt = false
    var ctrl = false
    var shift = false
    val m = modPattern.matcher(str)
    if m.matches()
    then
      val gr = m.group("mod")
      alt = gr.contains("A")
      ctrl = gr.contains("C")
      shift = gr.contains("S")
      val g1 = m.group(1)
      Some((new Modifiers(alt,ctrl,shift), off+g1.length()))
    else
      None
      
  def parseOne(string:String,off:Int) =
    str2keyName(string,off).flatMap { case(kn,off) => 
      str2modifiers(string,off).map { case(mod,off) => 
        (mod(KeyStroke.KeyEvent(kn,false,false,false)),off)
      }.orElse {
        Some((KeyStroke.KeyEvent(kn,false,false,false),off))
      }
    }.orElse(
      str2char(string,off).flatMap { case(cn,off) => 
        str2modifiers(string,off).map { case(mod,_) => 
          (mod(KeyStroke.CharEvent(cn,false,false,false)),off)
        }.orElse {
          Some((KeyStroke.CharEvent(cn,false,false,false),off))
        }
      }
    )

  def parse0(string:String,off:Int,ks0:Option[KeyStroke]):Option[(KeyStroke,Int)] =
    parseOne(string,off).map { case(ks1,off) => 
      ks0 match
        case Some(k0:KeyStroke.Sequence) => 
          ( KeyStroke.Sequence(ks1 :: k0.events.toList), off )
        case Some(k0) =>
          ( KeyStroke.Sequence(ks1 :: k0 :: Nil), off )
        case None =>
          ( ks1, off )
    }.flatMap { case (ks,off) => 
      if( off < (string.length()-1) && string.charAt(off)==',' ){
        parse0(string,off+1,Some(ks))
      }else{
        Some((ks,off))
      }
    }

  def parse(string:String) = parse0(string,0,None).map(_._1)

  def parse(ev:InputKeyEvent):KeyStroke =
    KeyStroke.KeyEvent( ev.getKey(), ev.isAltDown(), ev.isControlDown(), ev.isShiftDown() )

  def parse(ev:InputCharEvent):KeyStroke =
    KeyStroke.CharEvent( ev.getChar(), ev.isAltDown(), ev.isControlDown(), ev.isShiftDown() )

  def parse(ev:InputEvent):Option[KeyStroke] =
    ev match
      case ke:InputKeyEvent => Some(parse(ke))
      case ce:InputCharEvent => Some(parse(ce))
      case _ => None
