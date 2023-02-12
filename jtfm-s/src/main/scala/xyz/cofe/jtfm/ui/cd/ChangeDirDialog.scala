package xyz.cofe.jtfm.ui
package cd

import java.nio.file.Path
import xyz.cofe.term.ui.Listener
import xyz.cofe.term.ui._
import xyz.cofe.term.ui.Dialog
import xyz.cofe.term.common.Size
import xyz.cofe.term.ui.TextField
import xyz.cofe.term.ui.Button
import xyz.cofe.term.ui.prop.bind
import xyz.cofe.term.geom.Rect
import xyz.cofe.term.common.KeyName
import xyz.cofe.files.isDirectory
import xyz.cofe.jtfm.ui.warn.WarnDialog

object ChangeDirDialog:
  def open(currentDir:Option[Path]):Promise[Path] =
    val prom = Promise[Path]()

    Dialog
      .title("chage dir")
      .relocateWhenOpen { rootw => rootw.center(Size(rootw.size.width-6,10)) }
      .content { (panel,hdl) =>
        val input = TextField()
        panel.children.append(input)
        input.bind(panel) { b => Rect(1,1,b.width-2,1) }

        val butOk = Button("Ok")
        panel.children.append(butOk)
        butOk.bind(panel) { b => Rect(b.width-3, b.height-1, 2, 1) }

        val butCancel = Button("Cancel")
        panel.children.append(butCancel)
        butCancel.bind(panel){b => Rect(1,b.height-1,6,1)}
        butCancel.action { hdl.close() }

        currentDir match
          case None => input.text.set("enter path to directory")
          case Some(value) =>
            input.text.set {
              if value.isAbsolute()
              then value.toString()
              else value.toAbsolutePath().normalize().toString()
            }

        input.selectAll()
        input.moveCursorEnd(true)
        hdl.onOpen { input.focus.request }

        def tryEnter:Unit =
          val path = Path.of(input.text.get)
          path.isDirectory match
            case Left(err) => 
              WarnDialog.message(err.getMessage()).title(err.getClass().getSimpleName()).show
            case Right(value) =>
              if value then
                prom.ok.emit(path)
                hdl.close()
              else
                WarnDialog.message(s"directory not exists or not directory: $path").title("can't change dir").show

        input.keyStrokeMap.bind(KeyStroke.KeyEvent(KeyName.Enter,false,false,false), TextField.Action.Custom(tf => {
          tryEnter
        }))
        butOk.action {
          tryEnter
        }
      }
      .onClose { prom.closed.emit() }
      .open()
    prom
