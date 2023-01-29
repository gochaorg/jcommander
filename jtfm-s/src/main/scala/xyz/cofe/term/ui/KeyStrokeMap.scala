package xyz.cofe.term.ui

import scala.collection.immutable.SortedSet
import xyz.cofe.term.common.InputEvent
import xyz.cofe.lazyp.ReleaseListener
import xyz.cofe.lazyp.Prop
import scala.collection.immutable.TreeMap
import xyz.cofe.term.common.InputKeyEvent
import xyz.cofe.term.common.InputCharEvent

class KeyStrokeMap[A] extends Prop[KeyStrokeMap[A]]:
  private var shortcuts:Map[KeyStroke,Set[A]] = Map.empty
  private var shortcutsByLen:TreeMap[Int,Set[KeyStroke]] = TreeMap.empty

  def bind( keyStroke:KeyStroke, value:A ):Unit =
    shortcuts = shortcuts + (keyStroke -> (shortcuts.get(keyStroke).getOrElse(Set.empty) + value))
    shortcutsByLen = shortcutsByLen + 
      (keyStroke.sequenceSize -> 
        (shortcutsByLen.get(keyStroke.sequenceSize).getOrElse(Set()) ++ Set(keyStroke))
      )
    changeListeners.emit()

  def unbind( value:A ):Unit =
    shortcuts = shortcuts.map { case (ks, actions) => 
      (ks, actions.filterNot(a => a==value))
    }.filter { case (ks,actions) => actions.nonEmpty }
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

  def toMap:Map[KeyStroke,Set[A]] =
    shortcuts

  def sequenceLengths:SortedSet[Int] = SortedSet.from(shortcutsByLen.keySet)
  def sequenceMaxLength:Int = sequenceLengths.lastOption.getOrElse(0)

  def find(inputHistory:List[InputEvent]):Option[(KeyStroke,Set[A])] =
    shortcutsByLen.values.toList.flatten.reverse
      .find( ks => ks.matchLeft(inputHistory) )
      .flatMap { ks =>
        shortcuts.get(ks).map(set => (ks,set))
      }

  override def get: KeyStrokeMap[A] = this
  
  private val changeListeners = Listener()
  override def onChange(listener: => Unit): ReleaseListener = changeListeners(listener)

object KeyStrokeMap:
  def apply[A]():KeyStrokeMap[A] = new KeyStrokeMap[A]()

  def apply[A]( map:Map[KeyStroke,Set[A]] ):KeyStrokeMap[A] =
    val m = new KeyStrokeMap[A]()
    map.foreach( (ks,set) => set.foreach( a => m.bind(ks,a)))
    m

  case class KeyStrokeInputParser[A]( map:KeyStrokeMap[A] ):
    private var history:List[InputEvent] = List.empty
    def input( event:InputEvent )( consumer:A=>Unit ):Unit = {
      event match
        case ke:InputKeyEvent  => input(ke, consumer)
        case ke:InputCharEvent => input(ke, consumer)
        case _ => ()
    }

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
        val (ks,set) = found.get
        set.foreach(consumer)
        history.drop(ks.sequenceSize)
      else
        history
