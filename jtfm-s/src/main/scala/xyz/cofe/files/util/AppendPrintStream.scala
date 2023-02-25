package xyz.cofe.files.util

import java.io.PrintStream
import java.util.Arrays
import java.{util => ju}

class AppendPrintStream( out:Appendable ) extends PrintStream( new NullOutput() ):
  override def append(csq: CharSequence): PrintStream = 
    out.append(csq)
    this
  override def append(c: Char): PrintStream = 
    out.append(c)
    this
  override def append(csq: CharSequence, start: Int, end: Int): PrintStream = 
    out.append(csq,start,end)
    this
  override def checkError(): Boolean = false
  override def close(): Unit = ()
  override def flush(): Unit = ()
  override def format(format: String, args: Object*): PrintStream = 
    out.append(String.format(format,args))
    this
  override def format(l: ju.Locale, format: String, args: Object*): PrintStream = 
    out.append(String.format(l,format,args))
    this
  //override protected[io] def clearError(): Unit = ???
  override def print(obj: Object): Unit = out.append(if obj eq null then "null" else obj.toString())
  override def print(obj: String): Unit = out.append(if obj eq null then "null" else obj.toString())
  override def print(obj: Boolean): Unit = out.append(if obj eq null then "null" else obj.toString())
  override def print(obj: Char): Unit = out.append(if obj eq null then "null" else obj.toString())
  override def print(obj: Array[Char]): Unit = out.append(if obj eq null then "null" else obj.toString())
  override def print(obj: Double): Unit = out.append(if obj eq null then "null" else obj.toString())
  override def print(obj: Float): Unit = out.append(if obj eq null then "null" else obj.toString())
  override def print(obj: Int): Unit = out.append(if obj eq null then "null" else obj.toString())
  override def print(obj: Long): Unit = out.append(if obj eq null then "null" else obj.toString())

  override def printf(format: String, args: Object*): PrintStream = 
    out.append(String.format(format,args))
    this
    
  override def printf(l: ju.Locale, format: String, args: Object*): PrintStream = 
    out.append(String.format(l,format,args))
    this
  override def println(): Unit = 
    out.append("\n")
  override def println(x: Object): Unit = 
    print(x)
    println()
  override def println(x: String): Unit =
    print(x)
    println()
  override def println(x: Boolean): Unit =
    print(x)
    println()
  override def println(x: Char): Unit = 
    print(x)
    println()
  override def println(x: Array[Char]): Unit = 
    print( if x eq null then "null" else Arrays.toString(x) )
    println()
  override def println(x: Double): Unit =
    print(x)
    println()
  override def println(x: Float): Unit = 
    print(x)
    println()
  override def println(x: Int): Unit = 
    print(x)
    println()
  override def println(x: Long): Unit = 
    print(x)
    println()
  //override protected[io] def setError(): Unit = ???
  override def write(buf: Array[Byte]): Unit = 
    ()
  override def write(b: Int): Unit =
    ()
  override def write(buf: Array[Byte], off: Int, len: Int): Unit = 
    ()
  override def writeBytes(buf: Array[Byte]): Unit =
    ()