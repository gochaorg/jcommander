package xyz.cofe.term.ui.table

import xyz.cofe.lazyp.Prop

enum PreferredWidth:
  case Auto

enum HorizontalAlign:
  case Left
  case Center
  case Right

trait Column[R,V:CellText]:
  def id:String
  def read(row:R):V
  def textOf(row:R):String = summon[CellText[V]].cellTextOf(read(row))
  
  val title = Prop.rw("?")
  val width = Prop.rw(1)

  val preferredWidth = Prop.rw(PreferredWidth)
  val horizontalAlign = Prop.rw(HorizontalAlign.Left)
  val rightDelimiter = Prop.rw(Delimeter.SingleLine)
  val leftDelimiter = Prop.rw(Delimeter.None)

object Column:
  def id(id:String) = ColumnId(id)
  case class ColumnId(id:String):
    def reader[A,Z]( reader:A=>Z ) = ColumnMap(id, reader)

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
    alignOpt:Option[HorizontalAlign]=None
  ):
    def title(string:String) = copy( titleOpt=Some(string) )
    def width(w:Int) = copy( widthOpt=Some(w) )
    def build =
      val col = ColumnImpl(id,reader)
      col.title.set( titleOpt.getOrElse("?"))
      col.width.set( widthOpt.getOrElse(5) )
      col.leftDelimiter.set( leftDelimOpt.getOrElse(Delimeter.None) )
      col.rightDelimiter.set( rightDelimOpt.getOrElse(Delimeter.None) )
      col.horizontalAlign.set( alignOpt.getOrElse(HorizontalAlign.Left) )
      col

  case class ColumnImpl[A,Z:CellText](id:String,reader:A=>Z) extends Column[A,Z]:
    override def read(row: A): Z = reader(row)
