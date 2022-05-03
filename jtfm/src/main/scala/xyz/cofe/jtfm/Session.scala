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
          text( "Sample" )
          click { println("a") }
        }
      }
    }

    wc.root.nested.append( mb )

  case class SomeFile( val name:String, size:Long )

