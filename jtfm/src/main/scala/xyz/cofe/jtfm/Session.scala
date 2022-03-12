package xyz.cofe.jtfm

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.Terminal
import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.wid.{VirtualWidgetRoot, Widget, WidgetCycle}
import xyz.cofe.jtfm.wid.cmpt.*
import xyz.cofe.jtfm.wid.wc.{InputDummy, InputProcess, Jobs}

import java.util.concurrent.atomic.AtomicReference

class Session ( terminal: Terminal ):
  private val wc_atom : AtomicReference[WidgetCycle] = new AtomicReference[WidgetCycle](null)
  
  def run():Unit =
    println(s"session is started")
    WidgetCycle(terminal) match {
      case Left(err) =>
        println(s"fail $err")
      case Right(wc) =>
        wc_atom.set(wc)
        buildUi(wc)
        wc.run()
        println(s"session is stopped")
    }

  def terminate():Unit =
    val wc = wc_atom.get()
    if wc!=null then
      wc.stop().await()
      
  private def buildUi( wc:WidgetCycle ):Unit =
    val lbl1 = Label()
    wc.root.nested.append( lbl1 )
    lbl1.rect.value = Rect(1,1).size(20,3)
    lbl1.foreground.value = TextColor.ANSI.RED_BRIGHT
    lbl1.background.value = TextColor.ANSI.BLUE
    lbl1.text.value = "label_a"
  
    val lbl2 = Label()
    wc.root.nested.append( lbl2 )
    lbl2.rect.value = Rect(1,5).size(18,3)
    lbl2.text.value = "label_b"
    lbl2.foreground.value = TextColor.ANSI.GREEN_BRIGHT
    lbl2.background.value = TextColor.ANSI.BLACK_BRIGHT
    
    wc.root.rect.listen( (prop,old,cur)=>{
      lbl2.text.value = s"${cur.width} x ${cur.height}"
    })
    
    lbl2.rect.bind( wc.root ) { (_, src) => Rect( src.rightBottom.translate(-15,-4), src.rightBottom.translate(-1,-1) ) }
  
    var c1 = 0
    wc.jobs match {
      case Some(j) =>
        j.add( ()=>{
          wc.workState match {
            case Some(ws) =>
              ws.inputProcess match {
                case d: InputDummy =>
                  val d2: InputDummy = d
                  d2.handler( KeyType.F5, _ => {
                    c1 += 1
                    lbl1.text.value = s"label ${c1}"
                  })
                  d2.handler( KeyType.F6, _ => {
                    lbl2.visible.value = !lbl2.visible.value
                  })
                case _ =>
              }
            case _ =>
          }
        })
      case _: scala.None.type => ()
    }