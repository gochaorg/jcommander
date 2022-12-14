package xyz.cofe.lazyp

import scala.runtime.Tuples.apply

trait PropLogger
object PropLogger

trait Prop[V]:
  def get:V
  def onChange( listener: => Unit ):ReleaseListener

object Prop:
  def rw[A](initial:A):ReadWriteProp[A] = ReadWriteProp(initial)
  def eval[A,Z](propA:Prop[A])(fn:A=>Z):Prop[Z] = (propA).compute(fn)
  def eval[A,B,Z](pa:Prop[A],pb:Prop[B])
    (fn:(A,B)=>Z):Prop[Z] = (pa,pb).compute(fn)
  def eval[A,B,C,Z](pa:Prop[A],pb:Prop[B],pc:Prop[C])
    (fn:(A,B,C)=>Z):Prop[Z] = (pa,pb,pc).compute(fn)
  def eval[A,B,C,D,Z](pa:Prop[A],pb:Prop[B],pc:Prop[C],pd:Prop[D])
    (fn:(A,B,C,D)=>Z):Prop[Z] = (pa,pb,pc,pd).compute(fn)
  def eval[A,B,C,D,E,Z](pa:Prop[A],pb:Prop[B],pc:Prop[C],pd:Prop[D],pe:Prop[E])
    (fn:(A,B,C,D,E)=>Z):Prop[Z] = (pa,pb,pc,pd,pe).compute(fn)
  def eval[A,B,C,D,E,F,Z](pa:Prop[A],pb:Prop[B],pc:Prop[C],pd:Prop[D],pe:Prop[E],pf:Prop[F])
    (fn:(A,B,C,D,E,F)=>Z):Prop[Z] = (pa,pb,pc,pd,pe,pf).compute(fn)

trait ListenerSuppor:
  protected def fire():Unit =
    listeners.foreach { ls => ls() }

  private var listeners: List[()=>Unit] = List.empty
  def onChange(listener: => Unit): ReleaseListener = 
    val ls : ()=>Unit = () => {
      listener
    }
    listeners = ls :: listeners
    new ReleaseListener {
      override def release(): Unit = 
        listeners = listeners.filterNot( l => l == ls )
    }

trait ReleaseListener:
  def release():Unit

class ReadWriteProp[V]( initial:V ) extends Prop[V] with ListenerSuppor:
  private var value:V = initial
  def get: V = value

  def set(newValue:V):V = 
    val old = value
    value = newValue
    fire()
    old

class ComputeableProp[A,R]( compute:A=>R, values:()=>A ) extends Prop[R] with ListenerSuppor:
  private var computed:Option[R] = None

  override def get: R = 
    computed match
      case Some(v) => v
      case None => 
        val v = eval
        computed = Some(v)
        fire()
        v

  def reset():Unit = 
    val prev = computed
    computed = None
    if prev.isDefined then
      fire()

  def eval:R = compute(values())

extension [V]( prop:Prop[V] )
  def :* [A]( other:Prop[A] ):(Prop[V],Prop[A]) = (prop, other)
  
extension [A](prop:Prop[A])
  def compute[Z]( f:A=>Z ):Prop[Z] = 
    val cp = ComputeableProp( f, ()=>prop.get )
    prop.onChange { cp.reset() }
    cp

extension [A,B](props:(Prop[A],Prop[B])) 
  def compute[Z]( f:(A,B)=>Z ):Prop[Z] = 
    val cp = ComputeableProp( f.tupled, ()=>(props._1.get, props._2.get) )
    props._1.onChange { cp.reset() }
    props._2.onChange { cp.reset() }
    cp

extension [A,B,C](props:(Prop[A],Prop[B],Prop[C]))
  def compute[Z]( f:(A,B,C)=>Z ):Prop[Z] =
    val cp = ComputeableProp( f.tupled, ()=>(
      props._1.get, 
      props._2.get,
      props._3.get,
    ))
    props._1.onChange { cp.reset() }
    props._2.onChange { cp.reset() }
    props._3.onChange { cp.reset() }
    cp

extension [A,B,C,D](props:(Prop[A],Prop[B],Prop[C],Prop[D]))
  def compute[Z]( f:(A,B,C,D)=>Z ):Prop[Z] = 
    val cp = ComputeableProp( f.tupled, ()=>(
      props._1.get, 
      props._2.get,
      props._3.get,
      props._4.get,
    ))
    props._1.onChange { cp.reset() }
    props._2.onChange { cp.reset() }
    props._3.onChange { cp.reset() }
    props._4.onChange { cp.reset() }
    cp

extension [A,B,C,D,E](props:(Prop[A],Prop[B],Prop[C],Prop[D],Prop[E]))
  def compute[Z]( f:(A,B,C,D,E)=>Z ):Prop[Z] =
    val cp = ComputeableProp( f.tupled, ()=>(
      props._1.get, 
      props._2.get,
      props._3.get,
      props._4.get,
      props._5.get,
    ))
    props._1.onChange { cp.reset() }
    props._2.onChange { cp.reset() }
    props._3.onChange { cp.reset() }
    props._4.onChange { cp.reset() }
    props._5.onChange { cp.reset() }
    cp

extension [A,B,C,D,E,F](props:(Prop[A],Prop[B],Prop[C],Prop[D],Prop[E],Prop[F]))
  def compute[Z]( f:(A,B,C,D,E,F)=>Z ):Prop[Z] =
    val cp = ComputeableProp( f.tupled, ()=>(
      props._1.get, 
      props._2.get,
      props._3.get,
      props._4.get,
      props._5.get,
      props._6.get,
    ))
    props._1.onChange { cp.reset() }
    props._2.onChange { cp.reset() }
    props._3.onChange { cp.reset() }
    props._4.onChange { cp.reset() }
    props._5.onChange { cp.reset() }
    props._6.onChange { cp.reset() }
    cp
