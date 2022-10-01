package xyz.cofe.jtfm.ui

import xyz.cofe.jtfm.store.json._
import javax.swing.JFrame
import javax.swing.WindowConstants
import java.awt.Window
import java.awt.Frame

enum WindowState:
  case Normal, Minimized, MaximizedVert, MaximizedHoriz, MaximizedBoth
  def toExtendedState:Int =
    this match
      case Normal => Frame.NORMAL
      case Minimized => Frame.ICONIFIED
      case MaximizedVert => Frame.MAXIMIZED_VERT
      case MaximizedHoriz => Frame.MAXIMIZED_HORIZ
      case MaximizedBoth => Frame.MAXIMIZED_BOTH

object WindowState:
  def fromExtendedState(state:Int):WindowState =
    val min  = (Frame.ICONIFIED & state) == Frame.ICONIFIED
    val norm = (Frame.NORMAL & state) == Frame.NORMAL
    val max_h = (Frame.MAXIMIZED_HORIZ & state) == Frame.MAXIMIZED_HORIZ
    val max_v = (Frame.MAXIMIZED_VERT & state) == Frame.MAXIMIZED_VERT
    if norm then WindowState.Normal else
      if min then WindowState.Minimized else
        if max_h && !max_v then WindowState.MaximizedHoriz else
          if !max_h && max_v then WindowState.MaximizedVert else
            if max_h && max_v then WindowState.MaximizedBoth else
              WindowState.Normal
  given ToJson[WindowState] with
    def toJson(s:WindowState):Either[String,JS] = s match
      case Normal => Right(JS.Str("norm"))
      case Minimized => Right(JS.Str("min"))
      case MaximizedBoth => Right(JS.Str("max"))
      case MaximizedVert => Right(JS.Str("max_v"))
      case MaximizedHoriz => Right(JS.Str("max_h"))
  given FromJson[WindowState] with
    def fromJson(js:JS):Either[String,WindowState] = 
      js match
        case JS.Str("norm") => Right(WindowState.Normal)
        case JS.Str("min") => Right(WindowState.Minimized)
        case JS.Str("max") => Right(WindowState.MaximizedBoth)
        case JS.Str("max_v") => Right(WindowState.MaximizedVert)
        case JS.Str("max_h") => Right(WindowState.MaximizedHoriz)
        case _ => Left(s"can't read WindowState from $js")

case class WindowLocation(state:WindowState, x:Int, y:Int, width:Int, height:Int)
object WindowLocation:
  def apply(frame:JFrame):WindowLocation =
    val state = frame.getExtendedState() 
    val loc = frame.getLocation()
    val size = frame.getSize()
    WindowLocation(
      WindowState.fromExtendedState(state),
      loc.x, loc.y,
      size.width, size.height
    )

extension (frame: JFrame)
  def apply(loc: WindowLocation):Unit =
    frame.setSize(loc.width, loc.height)
    frame.setLocation(loc.x, loc.y)
    frame.setExtendedState(loc.state.toExtendedState)