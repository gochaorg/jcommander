package xyz.cofe.term.buff

import xyz.cofe.term.common.Color
import xyz.cofe.term.common.Position

//import xyz.cofe.lazyp
import BuffDiff._

class Buffer extends ScreenBuffer:
  private var lines:List[Array[ScreenChar]] = List.empty
  private var widthValue : Int = 0

  def width:Int = widthValue

  def height:Int = lines.size
  def get(x:Int,y:Int):Option[ScreenChar] = 
    if x<0 || y<0 || x>=width || y>=height 
    then None
    else 
      val seq = lines(y)
      if x>=seq.length 
      then None
      else Some(seq(x))
  def set(x:Int,y:Int,chr:ScreenChar):Either[ScreenBufferError,Unit] = 
    if x<0 || x>=width 
    then Left(ScreenBufferError.AgrumentOutRange("x",x,0,width-1))
    else if y<0 || y>=height
      then Left(ScreenBufferError.AgrumentOutRange("y",y,0,height-1))
      else 
        val line = lines(y)
        if x>=line.length then
          val newLine = Array.copyOf(line,x+1)
          lines = lines.updated(y,newLine)
          newLine(x) = chr
          Right(())
        else
          line(x) = chr
          Right(())

  def copy:Buffer = 
    val buf = Buffer()
    buf.lines = lines.map( line => Array.copyOf(line,line.length) )
    buf.widthValue = widthValue
    buf
  def diff(screenBuf:ScreenBuffer):Seq[CharDifference] = 
    var diffs = List[CharDifference]()
    screenBuf match
      case buf:Buffer => diff(buf)
      case buf => diffDefault(buf)

  private def diffDefault(buf:ScreenBuffer):Seq[CharDifference] = BuffDiff.diff(this,buf)

  /* #region diff for Buffer */

  private def diff(buf:Buffer):Seq[CharDifference] = 
    // var diffs = List[CharDifference]()
    val diffs = lines.zip(buf.lines).zipWithIndex.map { case ((lineLeft, lineRight),y) => diffLine(y, lineLeft, lineRight) }.flatten

    val changes =
    if buf.height < height
    then 
      // oldLines
      lines.drop(buf.height).zipWithIndex
        .map { case(line,y) => (line,y + buf.height) }
        .flatMap { case(line,y) => line.zipWithIndex.map { case (chr,x) => deletedChar(x,y,chr) } }.toList
    else if( buf.height > height)
        {
          // val newLines = 
          buf.lines.drop(lines.size).zipWithIndex
          .map { case(line,y) => (line,y + height) }
          .flatMap { case(line,y) => line.zipWithIndex.map { case (chr,x) => insertedChar(x,y,chr) } }.toList
        }
      else
        { List[CharDifference]() }

    diffs ++ changes

  private def diffLine( y:Int, left:Array[ScreenChar], right:Array[ScreenChar] ):Seq[CharDifference] =
    val diffChars = left.zip(right).zipWithIndex.map { case ((leftChar, rightChar), x) => diffChar(x, y, leftChar, rightChar) }

    val (removed,inserted) = 
    if left.size < right.size
    then (
      List[CharDifference](), 
      right.drop(left.size).zipWithIndex.map((chr,idx) => (chr,idx + left.size)).toList.map((chr,x)=>insertedChar(x,y,chr))
    )
    else if left.size > right.size
      then (
        left.drop(right.size).zipWithIndex.map( (chr,idx) => (chr,idx + right.size) ).toList.map((chr,x)=>deletedChar(x,y,chr)),
        List[CharDifference]()
      )
      else (List[CharDifference](),List[CharDifference]())

    diffChars.flatten ++ removed ++ inserted


  /* #endregion */

  private def defaultChar : ScreenChar = ScreenChar(' ', Color.White, Color.Black)

  def resize(width:Int,height:Int):Either[ScreenBufferError,Unit] = 
    if( width<0 ) Left(ScreenBufferError.AgrumentOutRange("width",width,0,Int.MaxValue))
    if( height<0 ) Left(ScreenBufferError.AgrumentOutRange("height",height,0,Int.MaxValue))
    if height<lines.size 
    then lines = lines.take(height)
    else if height>lines.size
      then lines = lines ++ (0 until (height - lines.size)).map ( _ => {
        ((0 until width).map { _ => defaultChar }).toArray
      })
    lines = lines.map { line =>
      if line.length > width 
      then line.take(width).toArray
      else if line.length < width 
        then (line ++ ( 0 until (width - line.length) ).map { _ => defaultChar }).toArray
        else line
    }
    widthValue = width
    Right(())
