package xyz.cofe.files.util

import java.io.OutputStream

class NullOutput extends OutputStream:
  override def close(): Unit = ()
  override def flush(): Unit = ()
  override def write(b: Int): Unit = ()
  override def write(b: Array[Byte]): Unit = ()
  override def write(b: Array[Byte], off: Int, len: Int): Unit = ()
