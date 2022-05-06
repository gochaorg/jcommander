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
    import wid.cmpt.Menu._

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

    val sizeCol = Column[SomeFile,Long]("size", _.size, _.toString )
    sizeCol.width.prefect = Some(6)
    tbl.columns = List(
      Column("name", _.name, _.toString ),
      sizeCol,
    )
    tbl.rect.bindTo( wc.root ) { r => Rect(2,2).size(r.width-4, r.height-4) }

    // val mc1v = new MenuAction( 
    //   action= (_)=>{
    //     println("view!action")
    //   },
    //   shortcut = Some(Shortcut.parse("C+u").get)
    // )

    // KeyboardInterceptor.bind(Shortcut.parse("C+y").get).eat {
    //   println("!! accept shortcut! ")
    // }

    val mb = menubar {
      menu( "File" ) {
        action {
          text( "Exit" )
          click{ wc.stop() }
        }
      }
      menu( "View" ) {        
        action {
          text( "Dialog 1" )
          click {  
            import Dialog._
            Dialog.show {
              title( "Color select" )              
              onClose {
                println("closed dlg")
              }
              size( 30, 10 )
              toCenter()
              color {
                foreground( TextColor.ANSI.WHITE_BRIGHT )
                background( TextColor.ANSI.GREEN )
              }

              val pnl = Panel()

              val c0 = Label()
              c0.text.value = "A"
              c0.foreground.value = TextColor.ANSI.WHITE_BRIGHT
              c0.background.value = TextColor.ANSI.BLACK
              c0.rect.value = Rect(0,0).size(1,1)

              val c1 = Label()
              c1.text.value = "B"
              c1.foreground.value = TextColor.ANSI.WHITE_BRIGHT
              c1.background.value = TextColor.ANSI.BLUE
              c1.rect.value = Rect(1,0).size(1,1)

              val c2 = Label()
              c2.text.value = "C"
              c2.foreground.value = TextColor.ANSI.WHITE_BRIGHT
              c2.background.value = TextColor.ANSI.GREEN
              c2.rect.value = Rect(2,0).size(1,1)

              pnl.nested.append(c0)
              pnl.nested.append(c1)
              pnl.nested.append(c2)

              content(pnl)
            }
          }
        }
      }
    }

    wc.root.nested.append( mb )

  case class SomeFile( val name:String, size:Long )

