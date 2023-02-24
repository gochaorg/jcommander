package xyz.cofe.jtfm.bg.copy

import java.nio.file.Path
import xyz.cofe.files._
import java.util.concurrent.atomic.AtomicBoolean

class CopyStream( using 
  log:        FilesLogger, 
  opts:       FilesOption,
  cancel:     CancelSignal,
  listener:   CopyStreamListener,
  bufferSize: Int
):
  def copy( from:Path, to:Path ) =
    from.inputStream.flatMap { inputStream => 
      try
        to.outputStream.map { outputStream => 
          try
            val buff = new Array[Byte](bufferSize)
            val stop = new AtomicBoolean(false)
            var count = 0L
            cancel.listen { stop.set(true) }
            listener.started()
            while ! stop.get do
              val readed = inputStream.read(buff)
              if readed<0 then
                stop.set(true)
              else if readed>0 then
                outputStream.write(buff,0,readed)
                count += readed
                listener.progress(count)
            listener.stopped()
          finally
            outputStream.close()
        }
      finally
        inputStream.close()
    }
