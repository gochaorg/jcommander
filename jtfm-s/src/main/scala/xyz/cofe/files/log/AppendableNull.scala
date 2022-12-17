package xyz.cofe.files.log

class AppendableNull extends java.lang.Appendable with AutoCloseable{
  override def append(csq: CharSequence): Appendable = this

  override def append(csq: CharSequence, start: Int, end: Int): Appendable = this

  override def append(c: Char): Appendable = this

  override def close(): Unit = ()
}  
