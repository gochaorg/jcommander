package xyz.cofe.term.ui

import scala.collection.immutable.SortedSet
import xyz.cofe.term.common.InputEvent
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.lazyp.Prop
import scala.collection.immutable.TreeMap
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.InputCharEvent

class KeyStrokeMap[A] extends Prop[KeyStrokeMap[A]]:
  private var shortcuts:Map[KeyStroke,A] = Map.empty
  private var shortcutsByLen:TreeMap[Int,Set[KeyStroke]] = TreeMap.empty

  def bind( keyStroke:KeyStroke, value:A ):Unit =
    shortcuts = shortcuts + (keyStroke -> value)
    shortcutsByLen = shortcutsByLen + 
      (keyStroke.sequenceSize -> 
        (shortcutsByLen.get(keyStroke.sequenceSize).getOrElse(Set()) ++ Set(keyStroke))
      )
    changeListeners.emit()

  def unbind( value:A ):Unit =
    shortcuts = shortcuts.filterNot { case (ks, action) => 
      value == action
    }

    val ksAll = shortcuts.keySet
    shortcutsByLen = shortcutsByLen.map { case (len, kss) => 
      (len,kss.filter { ks => ksAll.contains(ks)} )
    }.filter { case (len,kss) => kss.nonEmpty }
    changeListeners.emit()

  def unbind( keyStroke:KeyStroke ):Unit =
    shortcuts = shortcuts.filterNot(a => a==keyStroke)
    val ksAll = shortcuts.keySet
    shortcutsByLen = shortcutsByLen.map { case (len, kss) => 
      (len,kss.filter { ks => ksAll.contains(ks)} )
    }.filter { case (len,kss) => kss.nonEmpty }
    changeListeners.emit()

  def toMap:Map[KeyStroke,A] =
    shortcuts

  def sequenceLengths:SortedSet[Int] = SortedSet.from(shortcutsByLen.keySet)
  def sequenceMaxLength:Int = sequenceLengths.lastOption.getOrElse(0)

  def find(inputHistory:List[InputEvent]):Option[(KeyStroke,A)] =
    shortcutsByLen.values.toList.flatten.reverse
      .find( ks => ks.matchLeft(inputHistory) )
      .flatMap { ks =>
        shortcuts.get(ks).map(action => (ks,action))
      }

  override def get: KeyStrokeMap[A] = this
  
  private val changeListeners = Listener.unit
  override def onChange(listener: => Unit): ReleaseListener = changeListeners.listen( _ => listener)

object KeyStrokeMap:
  def apply[A]():KeyStrokeMap[A] = new KeyStrokeMap[A]()

  def apply[A]( map:Map[KeyStroke,A] ):KeyStrokeMap[A] =
    val m = new KeyStrokeMap[A]()
    map.foreach( (ks,action) => m.bind(ks,action))
    m

  case class InputParser[A]( map:KeyStrokeMap[A] ):
    private var history:List[InputEvent] = List.empty

    def input( event:InputEvent )( consumer:A=>Unit ):Unit = {
      event match
        case ke:InputKeyEvent  => input(ke, consumer)
        case ke:InputCharEvent => input(ke, consumer)
        case _ => ()
    }

    def input( event:InputEvent ):List[A] =
      var lst = List.empty[A]
      input(event){ e => lst = lst :+ e }
      lst

    private def input(ke:InputKeyEvent, consumer:A=>Unit):Unit =
      history = 
        findMatched(
          (ke :: history).take( map.sequenceMaxLength ),
          consumer
        )

    private def input(ke:InputCharEvent, consumer:A=>Unit):Unit =
      history = 
        findMatched(
          (ke :: history).take( map.sequenceMaxLength ),
          consumer
        )

    private def findMatched(history:List[InputEvent],consumer:A=>Unit):List[InputEvent] =
      val found = map.find(history)
      if found.isDefined 
      then
        val (ks,action) = found.get
        consumer(action)
        history.drop(ks.sequenceSize)
      else
        history
