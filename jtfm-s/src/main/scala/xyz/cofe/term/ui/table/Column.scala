package xyz.cofe.term.ui.table

import xyz.cofe.lazyp.Prop
import conf._

enum PreferredWidth:
  case Auto
  case Const(size:Int)
trait Column[R,V:CellText]:
  def id:String
  def read(row:R):V
  def textOf(row:R):String = summon[CellText[V]].cellTextOf(read(row))
  
  val title = Prop.rw("?")
  val width = Prop.rw(1)

  val preferredWidth = Prop.rw(PreferredWidth.Auto)
  val horizontalAlign = Prop.rw(HorizontalAlign.Left)
  val rightDelimiter = Prop.rw(Delimeter.SingleLine)
  val leftDelimiter = Prop.rw(Delimeter.None)

object Column:
  def id(id:String) = ColumnId(id)
  case class ColumnId(id:String):
    def reader[A,Z]( reader:A=>Z ) = ColumnMap(id, reader)
    def extract[A,Z:CellText]( reader:A=>Z ):ColumnBuilder[A,Z] =
      new ColumnBuilder(id, reader)

  case class ColumnMap[A,Z]( id:String, reader:A=>Z ):
    def text( textReader:Z=>String ) =
      given txt:CellText[Z] with
        override def cellTextOf(value: Z): String = textReader(value)

      ColumnBuilder(id, reader)

  case class ColumnBuilder[A,Z:CellText]( 
    id:String, 
    reader:A=>Z, 
    titleOpt:Option[String]=None,
    widthOpt:Option[Int]=None,
    leftDelimOpt:Option[Delimeter]=None,
    rightDelimOpt:Option[Delimeter]=Some(Delimeter.SingleLine),
    alignOpt:Option[HorizontalAlign]=None,
    preferredWidthOpt:Option[PreferredWidth]=None,    
  ):
    def title(string:String) = copy( titleOpt=Some(string) )
    def width(w:Int) = copy(preferredWidthOpt=Some(PreferredWidth.Const(w)))
    def widthAuto = copy(preferredWidthOpt=Some(PreferredWidth.Auto))
    
    def halign(align:HorizontalAlign) = copy(alignOpt = Some(align))
    def leftAlign   = copy(alignOpt = Some(HorizontalAlign.Left))
    def centerAlign = copy(alignOpt = Some(HorizontalAlign.Center))
    def rightAlign  = copy(alignOpt = Some(HorizontalAlign.Right))
    
    def leftDelim( delim:Delimeter ) = copy(leftDelimOpt = Some(delim))
    def leftDelimSpace( w:Int ) = leftDelim(Delimeter.Space(w))
    def leftDelimSingle = leftDelim(Delimeter.SingleLine)
    def leftDelimDouble = leftDelim(Delimeter.DoubleLine)
    def leftDelimNone = leftDelim(Delimeter.None)

    def rightDelim( delim:Delimeter ) = copy(rightDelimOpt = Some(delim))
    def rightDelimSpace( w:Int ) = rightDelim(Delimeter.Space(w))
    def rightDelimSingle = rightDelim(Delimeter.SingleLine)
    def rightDelimDouble = rightDelim(Delimeter.DoubleLine)
    def rightDelimNone = rightDelim(Delimeter.None)

    def build =
      val col = ColumnImpl(id,reader)
      col.title.set( titleOpt.getOrElse("?"))
      col.leftDelimiter.set( leftDelimOpt.getOrElse(Delimeter.None) )
      col.rightDelimiter.set( rightDelimOpt.getOrElse(Delimeter.None) )
      col.horizontalAlign.set( alignOpt.getOrElse(HorizontalAlign.Left) )
      col.preferredWidth.set( 
        preferredWidthOpt
          .orElse(widthOpt.map(w => PreferredWidth.Const(w)))
          .getOrElse(PreferredWidth.Auto) 
      )
      col

  case class ColumnImpl[A,Z:CellText](id:String,reader:A=>Z) extends Column[A,Z]:
    override def read(row: A): Z = reader(row)
