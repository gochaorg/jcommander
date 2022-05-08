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
import java.nio.file.Files
import java.nio.file.DirectoryStream
import java.nio.file.Paths
import java.nio.file.Path
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Сессия пользователя - текущий экземпляр
 */
class Session ( terminal: Terminal ):
  private val log:Logger = LoggerFactory.getLogger(classOf[Session])

  private val wc_atom : AtomicReference[WidgetCycle] = new AtomicReference[WidgetCycle](null)
  
  def run():Unit =
    log.info("session is started")
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

    val tbl = DirectoryTable()
    wc.root.nested.append(tbl)    
    tbl.rect.value = Rect( 1,1 ).size( 40, 20 )
    tbl.rect.bindTo( wc.root ) { r => Rect(2,2).size(r.width-4, r.height-4) }
    tbl.columns = List( 
      FilesTable.columns.fileName, 
      FilesTable.columns.latModifiedTime,
      FilesTable.columns.size 
      )

    tbl.currentDir.value = Some(Paths.get("."))
    tbl.background.value = TextColor.ANSI.BLACK_BRIGHT

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

              val cpnl = ColorPanel()
              content(cpnl)

              val txt = new TextField()
              txt.text.value = "Sample"
              txt.rect.value = Rect(1,5).size(15,1)
              cpnl.nested.append(txt)

              onClose {
                println(s"closed dlg color=${cpnl.color}, txt=${txt.text.value}")
              }
              size( 30, 10 )
              toCenter()
              color {
                foreground( TextColor.ANSI.WHITE_BRIGHT )
                background( TextColor.ANSI.GREEN )
              }

            }
          }
        }
      }
    }

    wc.root.nested.append( mb )

  case class SomeFile( val name:String, size:Long )

