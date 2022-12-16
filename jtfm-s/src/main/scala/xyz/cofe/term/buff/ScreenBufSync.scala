package xyz.cofe.term.buff

import xyz.cofe.term.common.Console
import xyz.cofe.term.common.Position
import xyz.cofe.term.common.Color
import xyz.cofe.term.common.Size
object ScreenBufSync:
  def sync(console:Console, buff:ScreenBuffer) =
    batching(buff, console.getSize()).foreach( cmd => batch.apply(console, cmd) )

  def batching(buff:ScreenBuffer, consoleSize:Size, fullSync:Boolean=true):Seq[BatchCmd] =
    val rect = consoleSize.leftUpRect(0,0)
    val chars0 = writeChars(buff).filter { pchr => rect.contains(pchr.pos) }
    val chars = 
      if !fullSync && buff.isInstanceOf[ChangeMetricBuffer]
      then 
        val cmBuff = buff.asInstanceOf[ChangeMetricBuffer]
        chars0.filter { pchr => cmBuff.changed(pchr.pos) }
      else chars0

    reduce(batching(chars))

  def writeChars(buff: ScreenBuffer):IndexedSeq[PosScreenChar] =
    (0 until buff.width).flatMap { y => 
      (0 until buff.height).map { x =>
        buff.get(x,y).map { chr => 
          PosScreenChar(chr, Position(x,y))
        }
      }
    }.flatMap { 
      case Some(value) => List(value)
      case None => List()
    }

  enum BatchCmd:
    case SetCursor(x:Int, y:Int)
    case SetForeground(color:Color)
    case SetBackground(color:Color)
    case WriteChar(char:Char)
    case WriteStr(string:String)

  def batching( chars:IndexedSeq[PosScreenChar] ):Seq[BatchCmd] =
    if chars.isEmpty
    then List()
    else
      if chars.size == 1
      then batch.single(chars.head)
      else
        val tailBatch =
          chars.zip(chars.tail).map { case (prvChr,curChr) => 
            if prvChr==curChr 
            then List()
            else 
              val setCursor =
                if prvChr.pos.y != curChr.pos.y
                  then List(batch.cursor(curChr.pos))
                  else if prvChr.pos.x != (curChr.pos.x - 1)
                    then List(batch.cursor(curChr.pos))
                    else List()
              val setForeground =
                if prvChr.char.foreground != curChr.char.foreground
                  then List(batch.foreground(curChr.char.foreground))
                  else List()
              val setBackground =
                if prvChr.char.background != curChr.char.background
                  then List(batch.background(curChr.char.background))
                  else List()
              val setChar = List(batch.char(curChr.char.char))
              setCursor ++ setForeground ++ setBackground ++ setChar
          }.flatten
        batch.single(chars.head) ++ tailBatch

  def reduce( batchCommands:Seq[BatchCmd] ):Seq[BatchCmd] =
    if batchCommands.size < 2
    then batchCommands
    else
      batchCommands.foldLeft( List[BatchCmd]() ){ case (sum, itm) => 
        if sum.isEmpty
        then List(itm)
        else itm match
          case BatchCmd.SetCursor(x, y) => itm :: sum
          case BatchCmd.SetForeground(color) => itm :: sum
          case BatchCmd.SetBackground(color) => itm :: sum
          case BatchCmd.WriteChar(curChar) =>
            sum.head match
              case BatchCmd.WriteChar(char) =>
                BatchCmd.WriteStr("" + char + curChar) :: sum.tail
              case BatchCmd.WriteStr(string) =>
                BatchCmd.WriteStr(string + curChar) :: sum.tail
              case _ => itm :: sum
          case BatchCmd.WriteStr(curString) =>
            sum.head match
              case BatchCmd.WriteChar(char) =>
                BatchCmd.WriteStr("" + char + curString) :: sum.tail
              case BatchCmd.WriteStr(string) =>
                BatchCmd.WriteStr("" + string + curString) :: sum.tail
              case _ => itm :: sum
        
      }.reverse

  object batch:
    def single(chr:PosScreenChar):List[BatchCmd] = 
      List(
        cursor(chr.pos), 
        foreground(chr.char.foreground), 
        background(chr.char.background),
        char(chr.char.char)
      )
    def cursor(x:Int,y:Int):BatchCmd = BatchCmd.SetCursor(x,y)
    def cursor(p:Position):BatchCmd = cursor(p.x, p.y)
    def foreground(c:Color):BatchCmd = BatchCmd.SetForeground(c)
    def background(c:Color):BatchCmd = BatchCmd.SetBackground(c)
    def char(chr:Char):BatchCmd = BatchCmd.WriteChar(chr)
    def apply( console:Console, cmd:BatchCmd ):Unit =
      cmd match
        case BatchCmd.SetCursor(x, y) => console.setCursorPosition(x,y)
        case BatchCmd.SetForeground(color) => console.setForeground(color)
        case BatchCmd.SetBackground(color) => console.setBackground(color)
        case BatchCmd.WriteChar(char) => console.write(""+char)
        case BatchCmd.WriteStr(string) => console.write(string)
      