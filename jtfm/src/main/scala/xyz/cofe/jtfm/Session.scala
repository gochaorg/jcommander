package xyz.cofe.jtfm

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.Terminal
import xyz.cofe.jtfm.gr.Rect
import xyz.cofe.jtfm.wid.{VirtualWidgetRoot, Widget, WidgetCycle}
import xyz.cofe.jtfm.wid.cmpt.*
import xyz.cofe.jtfm.wid.wc.{InputDummy, InputProcess, Jobs}

import java.util.concurrent.atomic.AtomicReference
import xyz.cofe.jtfm.wid.Shortcut
import xyz.cofe.jtfm.wid.wc.KeyboardInterceptor

/**
 * Сессия пользователя - текущий экземпляр
 */
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
      
  private def buildUi(wc: WidgetCycle):Unit =
    wc.root.background.value = TextColor.ANSI.BLACK
    wc.root.opaque.value = true

    val files = List(
      SomeFile("abc",123), SomeFile("bcd",234), SomeFile("cde",345), SomeFile("def",345), SomeFile("efg",345),
      SomeFile("qwe",123), SomeFile("wer",234), SomeFile("qwerty",345), SomeFile("ert",345), SomeFile("rt y",345),
      SomeFile("azx",123), SomeFile("qaed",234), SomeFile("eedd",345), SomeFile("zaq",345), SomeFile("xswedc",345),
      SomeFile("eedc",123), SomeFile("Tyuu",234), SomeFile("xDcf",345), SomeFile("rfVV",345), SomeFile("fggRR",345),
      SomeFile("r z c",123), SomeFile("a s d",234), SomeFile("yyUUi",345), SomeFile("s76d",345), SomeFile("muuU",345),
    )

    val tbl = new Table[SomeFile]()
    wc.root.nested.append(tbl)
    tbl.rect.value = Rect( 1,1 ).size( 40, 20 )
    tbl.data = files
    tbl.background.value = TextColor.ANSI.BLACK_BRIGHT
    tbl.columns = List(
      Column("name", _.name, _.toString ),
      Column("size", _.size, _.toString ),
    )
    tbl.rect.bindTo( wc.root ) { r => Rect(2,2).size(r.width-4, r.height-4) }

    val mbar = new MenuBar
    wc.root.nested.append(mbar)
    
    val mc1 = new MenuContainer
    mc1.text.value = "File"
    mbar.nested.append(mc1)

    val mc1v = new MenuAction( 
      action= (_)=>{
        println("view!action")
      },
      shortcut = Some(Shortcut.parse("C+u").get)
    )

    KeyboardInterceptor.bind(Shortcut.parse("C+y").get).eat {
      println("!! accept shortcut! ")
    }

    mc1v.text.value = "View"
    mc1.nested.append(mc1v)

    val mc1e = new MenuContainer
    mc1e.text.value = "Exit"
    mc1.nested.append(mc1e)

    val mc2 = new MenuContainer
    mc2.text.value = "Edit"
    mbar.nested.append(mc2)

    val mc3 = new MenuContainer
    mc3.text.value = "Help"
    mbar.nested.append(mc3)

    // val brd1 = new Border()
    // wc.root.nested.append(brd1)
    // brd1.rect.value = Rect( 20,3 ).size(10,4)

  case class SomeFile( val name:String, size:Long )

  
  private def buildUi_1( wc:WidgetCycle ):Unit =
    val lbl1 = Label()
    wc.root.nested.append( lbl1 )
    lbl1.rect.value = Rect(1,1).size(15,3)
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
    
    val brd1 = new Border()
    wc.root.nested.append(brd1)
    brd1.rect.value = Rect( 20,3 ).size(10,4)
  
    val brd2 = new Border()
    wc.root.nested.append(brd2)
    brd2.rect.value = Rect( 20,9 ).size(10,4)
    
    val brd3 = new Border()
    wc.root.nested.append(brd3)
    brd3.rect.value = Rect( 20,15 ).size(10,4)
    
    val scr1 = new ScrollBar()
    wc.root.nested.append(scr1)
    scr1.rect.value = Rect(33,0).size(1,10)
    
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