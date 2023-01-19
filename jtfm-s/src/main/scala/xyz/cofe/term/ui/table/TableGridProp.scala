package xyz.cofe.term.ui.table

import xyz.cofe.term.ui.Widget
import xyz.cofe.term.geom._
import xyz.cofe.term.common.Position
import xyz.cofe.lazyp.Prop
import xyz.cofe.term.ui.prop.SizeProp

trait TableGridProp[A] extends SizeProp with ColumnsProp[A] with HeaderProp with BorderProp:
  import TableGridProp._

  val columnsLocations:Prop[List[ColumnLocation[A]]] = Prop.eval(border,size,columns) { case(border,size,columns) =>
    val xMin = (border.left.size)
    val xMax = (size.width() - border.left.size - border.right.size)

    val cols = columns.foldLeft( (List.empty[ColumnLocation[A]],xMin) ){ 
      case ((list,x),col) =>
        val wBefore = col.leftDelimiter.get.size
        val wAfter = col.rightDelimiter.get.size
        val w = col.width.get
        val lst = list :+ ColumnLocation( col,x + wBefore,x + wBefore + w )
        (lst,x + w + wBefore + wAfter)
    }._1
    
    val cuttedCols = cols.flatMap{ case(colLoc) => 
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
      }

    if cuttedCols.nonEmpty
    then cuttedCols.updated(cuttedCols.size-1, {
      val lastCol = cuttedCols.last
      if lastCol.x1 < xMax 
      then lastCol.copy(x1 = xMax)
      else lastCol
    })
    else cuttedCols
  }

  val headersYPos:Prop[Option[(Int,Int)]] = Prop.eval(header.visible, border) { case (hVisible, border) =>
    if hVisible
    then Some(border.top.size, border.top.size+1)
    else None
  }

  val headersBlocks:Prop[List[ContentBlock.HeaderBlock[A]]] = Prop.eval( headersYPos,columnsLocations ){ case (hYPos,columnsLocations) =>
    hYPos match
      case None => List.empty[ContentBlock.HeaderBlock[A]]
      case Some((yMin, yMax)) =>
        columnsLocations.map { colLoc =>
          ContentBlock.HeaderBlock( (Position(colLoc.x0, yMin), Position(colLoc.x1, yMax)).rect, colLoc.column )
        }
  }

  val dataYPos:Prop[(Int,Int)] = Prop.eval(headersYPos,border,size) { case(headersYPos,border,size) =>
    val y0 = headersYPos.map { case (headerYMin,headerYMax) =>
      headerYMax + header.delimiter.get.size
    }.getOrElse {
      border.top.size
    }
    val y1 = size.height()-border.bottom.size
    ( y0, y1 )
  }

  val dataBlocks:Prop[List[ContentBlock.DataBlock[A]]] = Prop.eval(dataYPos,columnsLocations){ case (dataYPos,columnsLocations) =>
    val (yMin,yMax) = dataYPos
    columnsLocations.map { colLoc =>
      ContentBlock.DataBlock(
        (Position(colLoc.x0, yMin), Position(colLoc.x1, yMax)).rect,
        colLoc.column
      )
    }
  }

  val headerRenderDelims:Prop[List[ContentDelim]] = Prop.eval(headersBlocks,header.delimiter,size){ case (headersBlocks,delim,size) =>
    headersBlocks.bounds.map { rect => 
      val y = rect.bottom
      val x0 = 0
      val x1 = size.width()-1
      delim match
        case Delimeter.None => List.empty[ContentDelim]
        case Delimeter.Space(width) => 
          if width<=0
          then List.empty[ContentDelim]
          else List(ContentDelim.Whitespace((Position(x0,y), Position(x1+1,y+width)).rect, WhitespaceOrient.Horizontal))
        case Delimeter.SingleLine =>
          List(ContentDelim.RenderLine(Line( Position(x0,y), Position(x1,y), Symbols.Style.Single )))
        case Delimeter.DoubleLine => 
          List(ContentDelim.RenderLine(Line( Position(x0,y), Position(x1,y), Symbols.Style.Double )))
    }.getOrElse(List.empty)
  }
    
  val innerRenderDelims:Prop[List[ContentDelim]] = Prop.eval(headersBlocks,dataBlocks,columnsLocations){ case(headersBlocks,dataBlocks,columnsLocations) =>
    val bounds = (headersBlocks ++ dataBlocks).bounds
    bounds.map { bounds =>
      val colCount = columnsLocations.size
      val y0 = bounds.top - 1
      val y1 = bounds.bottom

      columnsLocations.zipWithIndex.flatMap { case(colLoc,colIdx) =>
        val left = colLoc.column.leftDelimiter.get match
          case Delimeter.None => 
            List.empty[ContentDelim]
          case Delimeter.Space(width) =>
            if width<=0 
            then
              val x1 = colLoc.x0-1
              val x0 = x1 - width
              List( ContentDelim.Whitespace((Position(x0,y0),Position(x1,y1+1)).rect, WhitespaceOrient.Vertical) )
            else
              List.empty[ContentDelim]
          case Delimeter.SingleLine =>
            val x = colLoc.x0-1
            List(ContentDelim.RenderLine(Line(Position(x,y0),Position(x,y1),Symbols.Style.Single)))
          case Delimeter.DoubleLine =>
            val x = colLoc.x0-1
            List(ContentDelim.RenderLine(Line(Position(x,y0),Position(x,y1),Symbols.Style.Double)))          

        val right = colLoc.column.rightDelimiter.get match
          case Delimeter.None => 
            List.empty[ContentDelim]
          case Delimeter.Space(width) =>
            if width<=0 
            then
              val x2 = colLoc.x1
              val x3 = x2 + width
              List( ContentDelim.Whitespace((Position(x2,y0),Position(x3,y1)).rect, WhitespaceOrient.Vertical) )
            else
              List.empty[ContentDelim]
          case Delimeter.SingleLine =>
            val x = colLoc.x1
            List(ContentDelim.RenderLine(Line(Position(x,y0),Position(x,y1),Symbols.Style.Single)))
          case Delimeter.DoubleLine =>          
            val x = colLoc.x1
            List(ContentDelim.RenderLine(Line(Position(x,y0),Position(x,y1),Symbols.Style.Double)))

        if colCount==1 then
          List.empty[ContentDelim]
        else if colIdx==0 then
          right
        else if colIdx==(colCount-1) then
          left
        else
          left ++ right
      }
    }.getOrElse(List.empty[ContentDelim])
  }

  val outterRenderDelims:Prop[List[ContentDelim]] = Prop.eval(border,size){ case(border,size) =>
    val left = border.left match
      case Delimeter.None => List.empty[ContentDelim]
      case Delimeter.Space(width) => 
        val x0 = 0
        val x1 = width
        val y0 = 0
        val y1 = size.height
        List(ContentDelim.Whitespace((Position(x0,y0),Position(x1,y1)).rect, WhitespaceOrient.Vertical))
      case Delimeter.SingleLine =>
        val (x0,y0,x1,y1) = ( 0,0, 0,size.height-1 )
        List(ContentDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Single)))
      case Delimeter.DoubleLine =>
        val (x0,y0,x1,y1) = ( 0,0, 0,size.height-1 )
        List(ContentDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Double)))      

    val right = border.right match
      case Delimeter.None => List.empty[ContentDelim]
      case Delimeter.Space(width) => 
        val x1 = size.width()
        val x0 = x1 - width
        val y0 = 0
        val y1 = size.height
        List(ContentDelim.Whitespace((Position(x0,y0),Position(x1,y1)).rect, WhitespaceOrient.Vertical))
      case Delimeter.SingleLine =>
        val x = size.width()-1
        val (x0,y0,x1,y1) = ( x,0, x,size.height-1 )
        List(ContentDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Single)))
      case Delimeter.DoubleLine =>
        val x = size.width()-1
        val (x0,y0,x1,y1) = ( x,0, x,size.height-1 )
        List(ContentDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Double)))      

    val top = border.top match
      case Delimeter.None => List.empty[ContentDelim]
      case Delimeter.Space(width) => 
        val x0 = 0
        val x1 = size.width
        val y0 = 0
        val y1 = width
        List(ContentDelim.Whitespace((Position(x0,y0),Position(x1,y1)).rect, WhitespaceOrient.Horizontal))
      case Delimeter.SingleLine =>
        val (x0,y0,x1,y1) = ( 0,0, size.width-1,0 )
        List(ContentDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Single)))
      case Delimeter.DoubleLine =>
        val (x0,y0,x1,y1) = ( 0,0, size.width-1,0 )
        List(ContentDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Double)))      

    val bottom = border.bottom match
      case Delimeter.None => List.empty[ContentDelim]
      case Delimeter.Space(width) => 
        val x0 = 0
        val x1 = size.width
        val y1 = size.height()
        val y0 = y1 - width
        List(ContentDelim.Whitespace((Position(x0,y0),Position(x1,y1)).rect, WhitespaceOrient.Horizontal))
      case Delimeter.SingleLine =>
        val y = size.height()-1
        val (x0,y0,x1,y1) = ( 0,y, size.width-1,y )
        List(ContentDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Single)))
      case Delimeter.DoubleLine =>
        val y = size.height()-1
        val (x0,y0,x1,y1) = ( 0,y, size.width-1,y )
        List(ContentDelim.RenderLine(Line(Position(x0,y0),Position(x1,y1),Symbols.Style.Double)))

    left ++ top ++ right ++ bottom
  }

  val renderDelims:Prop[List[ContentDelim]] = Prop.eval(headerRenderDelims,innerRenderDelims,outterRenderDelims){ 
    case(headerRenderDelims,innerRenderDelims,outterRenderDelims) =>
    headerRenderDelims ++ innerRenderDelims ++ outterRenderDelims
  }

object TableGridProp:
  case class ColumnLocation[A](column:Column[A,_], x0:Int, x1:Int):
    require( x0 <= x1 )
    def isBeetwin( xMin:Int, xMax:Int ):Boolean =
      x0 >= xMin && x1 <= xMax
    def intersect( xMin:Int, xMax:Int ):Option[(Int,Int)] =
      if x0 >= xMin && x0 <= xMax then
        Some( xMin min x0, xMax min x1 )
      else
        None

  enum ContentBlock[A]:
    case HeaderBlock[A]( rect0:Rect, col0:Column[A,_] ) extends ContentBlock[A]
    case DataBlock[A]( rect0:Rect, col0:Column[A,_] )  extends ContentBlock[A]

    def rect:Rect = this match
      case HeaderBlock(rect, col) => rect
      case DataBlock(rect, col) => rect

    def col:Column[A,_] = this match
      case HeaderBlock(rect, col) => col
      case DataBlock(rect, col) => col

  extension [A]( blocks: List[ContentBlock[A]] )
    def bounds =
      blocks.foldLeft( None:Option[Rect] ){ case (rect,block) => 
        rect.map { rect =>
          ( Position(rect.left  min block.rect.left,  rect.top    min block.rect.top)
          , Position(rect.right max block.rect.right, rect.bottom max block.rect.bottom)
          ).rect
        }.orElse( Some(block.rect) )
      }

  enum ContentDelim:
    case RenderLine( line:Line )
    case Whitespace( rect:Rect, orient:WhitespaceOrient )
    def delimSize:Int = this match
      case RenderLine(line) => 1
      case Whitespace(rect, orient) => orient match
        case WhitespaceOrient.Horizontal => rect.height
        case WhitespaceOrient.Vertical => rect.width
    def delimRect:Rect = this match
      case RenderLine(line) => 
        val x0 = line.a.x min line.b.x
        val x1 = (line.a.x max line.b.x)+1
        val y0 = line.a.y min line.b.y
        val y1 = (line.a.y max line.b.y)+1
        (Position(x0,y0),Position(x1,y1)).rect
      case Whitespace(rect, orient) =>
        rect    
      
  enum WhitespaceOrient:
    case Horizontal
    case Vertical
