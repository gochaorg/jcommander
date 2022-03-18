package xyz.cofe.jtfm.wid.cmpt

import com.googlecode.lanterna.graphics.TextGraphics
import xyz.cofe.jtfm.gr.Symbols
import xyz.cofe.jtfm.wid.{BackgroundProperty, ForegroundProperty, Widget}

class ScrollBar
  extends Widget[ScrollBar]
  with ForegroundProperty[ScrollBar]
  with BackgroundProperty[ScrollBar]
{
  private val scrollBlockChar = '\u2588'

  private val offset:Double = 0.5
  private val size:Double = 0.25
  private val orientation:ScrollOrientation = ScrollOrientation.Vertical
  
  override def render(gr: TextGraphics): Unit = {
    gr.setBackgroundColor(background.value)
    gr.setForegroundColor(foreground.value)
    if( rect.width>0 && rect.height>0 ){
      val sz = rect.width * rect.height
      if( sz==1 ){
        gr.setCharacter(0,0,scrollBlockChar)
      }else{
        val (sz,toCoord,chr) = orientation match {
          case ScrollOrientation.Vertical =>
            (rect.height,(c:Int)=>((0 until rect.width),List(c)), Symbols.DoubleThin.vert)
          case ScrollOrientation.Horizontal =>
            (rect.width,(c:Int)=>(List(c),(0 until rect.height)), Symbols.DoubleThin.horz)
        }
        val c_1 = (sz*offset)
        val c_2 = c_1.toInt
        val c_3 = c_2.max(0)
        val c_4 = c_3.min(sz-1)
        val coord_from = ((sz*offset).toInt).max(0).min(sz-1)
        val coord_to   = (coord_from+(sz*size).toInt).max(coord_from).min(sz-1)
        (0 until sz).foreach { co =>
          val (xSeq,ySeq) = toCoord(co)
          for( x <- xSeq; y <- ySeq ) gr.setCharacter(x,y,chr)
        }
        (coord_from to coord_to).foreach {co =>
          val (xSeq,ySeq) = toCoord(co)
          for( x <- xSeq; y <- ySeq ) gr.setCharacter(x,y,scrollBlockChar)
        }
      }
    }
  }
}
