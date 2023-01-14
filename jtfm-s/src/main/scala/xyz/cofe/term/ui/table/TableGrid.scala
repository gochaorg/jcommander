package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.Widget
import xyz.cofe.term.geom._
import xyz.cofe.term.common.Position
import xyz.cofe.term.ui.SizeProp


trait TableGrid[A]:
  this: SizeProp & ColumnsProp[A] & HeaderProp & BorderProp =>
    import TableGrid._

    def columnsLocations:List[ColumnLocation[A]] =
      val xMin = border.get.left.size
      val xMax = size.get.width() - border.get.left.size - border.get.right.size

      columns.foldLeft( (List.empty[ColumnLocation[A]],xMin) ){ 
        case ((list,x),col) =>           
          val wBefore = col.leftDelimiter.get.size
          val wAfter = col.rightDelimiter.get.size
          val w = col.width.get
          val lst = list :+ ColumnLocation( col,x + wBefore,x + wBefore + w )
          (lst,x + w + wBefore + wAfter)
      }._1.flatMap( colLoc => 
        if colLoc.isBeetwin(xMin,xMax)
        then List(colLoc)
        else
          colLoc.intersect(xMin,xMax).map { case (x0,x1) => 
            List(
              colLoc.copy(
                x0 = x0,
                x1 = x1
              )
            )
          }.getOrElse(List.empty)
      )

    def headersYPos:Option[(Int,Int)] =
      if header.visible.get
      then Some(border.get.top.size, border.get.top.size+1)
      else None

    def headersBlocks:List[RenderBlock[A]] =
      headersYPos match
        case None => List.empty[RenderBlock[A]]
        case Some((yMin, yMax)) =>
          columnsLocations.map { colLoc =>
            RenderBlock.HeaderBlock( (Position(colLoc.x0, yMin), Position(colLoc.x1, yMax)).rect, colLoc.column )
          }

    def dataYPos:(Int,Int) =
      val y0 = headersYPos.map { case (headerYMin,headerYMax) =>
        headerYMax + header.delimiter.get.size
      }.getOrElse {
        border.get.top.size
      }
      val y1 = size.get.height()-border.get.bottom.size
      ( y0, y1 )

    def dataBlocks:List[RenderBlock[A]] =
      val (yMin,yMax) = dataYPos
      columnsLocations.map { colLoc =>
        RenderBlock.DataBlock(
          (Position(colLoc.x0, yMin), Position(colLoc.x1, yMax)).rect,
          colLoc.column
        )
      }

    def headerRenderDelims:List[RenderDelim] = 
      headersBlocks.bounds.map { rect => 
        val y = rect.bottom + 1
        val x0 = rect.left - 1
        val x1 = rect.right
        header.delimiter.get match
          case Delimeter.None => List.empty[RenderDelim]
          case Delimeter.Space(width) => 
            if width<=0
            then List.empty[RenderDelim]
            else List(RenderDelim.Whitespace(((Position(x0,y), Position(x1,y+width)).rect)))
          case Delimeter.SingleLine =>
            List(RenderDelim.RenderLine(Line( Position(x0,y), Position(x1,y), Symbols.Style.Single )))
          case Delimeter.DoubleLine => 
            List(RenderDelim.RenderLine(Line( Position(x0,y), Position(x1,y), Symbols.Style.Single )))
      }.getOrElse(List.empty)
      
    def innerRenderDelims:List[RenderDelim] =
      val bounds = (headersBlocks ++ dataBlocks).bounds
      bounds.map { bounds =>
        val colCount = columnsLocations.size
        columnsLocations.zipWithIndex.flatMap { case(colLoc,colIdx) =>
          val y0 = bounds.top - 1
          val y1 = bounds.bottom + 1

          val left = colLoc.column.leftDelimiter.get match
            case Delimeter.None => 
              List.empty[RenderDelim]
            case Delimeter.Space(width) =>
              if width<=0 
              then
                val x1 = bounds.left
                val x0 = x1 - width
                List( RenderDelim.Whitespace((Position(x0,y0),Position(x1,y1)).rect) )
              else
                List.empty[RenderDelim]
            case Delimeter.SingleLine =>
              val x = bounds.left - 1
              List(RenderDelim.RenderLine(Line(Position(x,y0),Position(x,y1),Symbols.Style.Single)))
            case Delimeter.DoubleLine =>
              val x = bounds.left - 1
              List(RenderDelim.RenderLine(Line(Position(x,y0),Position(x,y1),Symbols.Style.Double)))          

          val right = colLoc.column.rightDelimiter.get match
            case Delimeter.None => 
              List.empty[RenderDelim]
            case Delimeter.Space(width) =>
              if width<=0 
              then
                val x2 = bounds.right
                val x3 = x2 + width
                List( RenderDelim.Whitespace((Position(x2,y0),Position(x3,y1)).rect) )
              else
                List.empty[RenderDelim]
            case Delimeter.SingleLine =>
              val x = bounds.left + 1
              List(RenderDelim.RenderLine(Line(Position(x,y0),Position(x,y1),Symbols.Style.Single)))
            case Delimeter.DoubleLine =>          
              val x = bounds.left + 1
              List(RenderDelim.RenderLine(Line(Position(x,y0),Position(x,y1),Symbols.Style.Double)))

          if colCount==1 then
            List.empty[RenderDelim]
          else if colIdx==0 then
            right
          else if colIdx==(colCount-1) then
            left
          else
            left ++ right
        }
      }.getOrElse(List.empty[RenderDelim])

    def outterRenderDelims:List[RenderDelim] =
      val left = border.get.left match
        case Delimeter.None => List.empty[RenderDelim]
        case Delimeter.Space(width) => 
          val x0 = 0
          val x1 = width
          val y0 = 0
          val y1 = size.get.height
          List(RenderDelim.Whitespace((Position(x0,y0),Position(x1,y1)).rect))
        case Delimeter.SingleLine =>
          val (x0,y0,x1,y1) = ( 0,0, 0,size.get.height )
          List(RenderDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Single)))
        case Delimeter.DoubleLine =>
          val (x0,y0,x1,y1) = ( 0,0, 0,size.get.height )
          List(RenderDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Double)))      

      val right = border.get.right match
        case Delimeter.None => List.empty[RenderDelim]
        case Delimeter.Space(width) => 
          val x1 = size.get.width()
          val x0 = x1 - width
          val y0 = 0
          val y1 = size.get.height
          List(RenderDelim.Whitespace((Position(x0,y0),Position(x1,y1)).rect))
        case Delimeter.SingleLine =>
          val x = size.get.width()-1
          val (x0,y0,x1,y1) = ( x,0, x,size.get.height )
          List(RenderDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Single)))
        case Delimeter.DoubleLine =>
          val x = size.get.width()-1
          val (x0,y0,x1,y1) = ( x,0, x,size.get.height )
          List(RenderDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Double)))      

      val top = border.get.top match
        case Delimeter.None => List.empty[RenderDelim]
        case Delimeter.Space(width) => 
          val x0 = 0
          val x1 = size.get.width
          val y0 = 0
          val y1 = width
          List(RenderDelim.Whitespace((Position(x0,y0),Position(x1,y1)).rect))
        case Delimeter.SingleLine =>
          val (x0,y0,x1,y1) = ( 0,0, size.get.width(),0 )
          List(RenderDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Single)))
        case Delimeter.DoubleLine =>
          val (x0,y0,x1,y1) = ( 0,0, size.get.width(),0 )
          List(RenderDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Double)))      

      val bottom = border.get.bottom match
        case Delimeter.None => List.empty[RenderDelim]
        case Delimeter.Space(width) => 
          val x0 = 0
          val x1 = size.get.width
          val y1 = size.get.height()
          val y0 = y1 - width
          List(RenderDelim.Whitespace((Position(x0,y0),Position(x1,y1)).rect))
        case Delimeter.SingleLine =>
          val y = size.get.height()-1
          val (x0,y0,x1,y1) = ( 0,y, size.get.width(),y )
          List(RenderDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Single)))
        case Delimeter.DoubleLine =>
          val y = size.get.height()-1
          val (x0,y0,x1,y1) = ( 0,y, size.get.width(),y )
          List(RenderDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Double)))

      left ++ top ++ right ++ bottom

    def renderDelims = headerRenderDelims ++ innerRenderDelims ++ outterRenderDelims

object TableGrid:
  case class ColumnLocation[A](column:Column[A,_], x0:Int, x1:Int):
    require( x0 <= x1 )
    def isBeetwin( xMin:Int, xMax:Int ):Boolean =
      x0 >= xMin && x1 <= xMax
    def intersect( xMin:Int, xMax:Int ):Option[(Int,Int)] =
      if x0 >= xMin && x0 <= xMax then
        Some( xMin min x0, xMax min x1 )
      else
        None

  enum RenderBlock[A]:
    case HeaderBlock[A]( rect0:Rect, col0:Column[A,_] ) extends RenderBlock[A]
    case DataBlock[A]( rect0:Rect, col0:Column[A,_] )  extends RenderBlock[A]

    def rect:Rect = this match
      case HeaderBlock(rect, col) => rect
      case DataBlock(rect, col) => rect

    def col:Column[A,_] = this match
      case HeaderBlock(rect, col) => col
      case DataBlock(rect, col) => col

  extension [A]( blocks: List[RenderBlock[A]] )
    def bounds =
      blocks.foldLeft( None:Option[Rect] ){ case (rect,block) => 
        rect.map { rect =>
          ( Position(rect.left  min block.rect.left,  rect.top    min block.rect.top)
          , Position(rect.right max block.rect.right, rect.bottom max block.rect.bottom)
          ).rect
        }.orElse( Some(block.rect) )
      }

  enum RenderDelim:
    case RenderLine( line:Line )
    case Whitespace( rect:Rect )