package xyz.cofe.jtfm.wid.wc

import java.io.InputStream
import xyz.cofe.jtfm.CycleBuff

class InputStreamHist( source:InputStream, histSize:Int )
extends InputStream {  
  require(histSize>0)

  val history:CycleBuff[Int] = CycleBuff[Int](histSize)

  override def available():Int = source.available()
  override def read():Int = {
    val b = source.read()
    history.push(b)
    b
  }
  override def close():Unit = source.close()
}
