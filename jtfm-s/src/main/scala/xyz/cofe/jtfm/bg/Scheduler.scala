package xyz.cofe.jtfm.bg

trait Task[A]:
  def poll:Option[A]
  def name:String
  def onFinish(value:Option[A]=>Any):Task[A]

trait Scheduler:
  def submit[A]( code:()=>A ):Task[A]
  def tasks:List[Task[?]]

object Scheduler:
  class TaskImpl[A]( code:()=>A ) extends Task[A]:
    @volatile var onFinishListeners : List[Option[A]=>Any] = List.empty
    @volatile var result:Option[A] = None

    lazy val thread:Thread = new Thread(){
      override def run(): Unit = {
        try
          result = Some(code())        
        finally
          onFinishListeners.foreach(ls => ls(result))
      }
    }
    def poll: Option[A] = result
    def name: String = thread.getName()
    override def onFinish(value: Option[A] => Any): Task[A] = 
      onFinishListeners = onFinishListeners :+ value
      this

  object defaultScheduler extends Scheduler:
    @volatile var runnedTasks: List[Task[?]] = List.empty
    override def submit[A](code: () => A): Task[A] = 
    this.synchronized {
      val task = TaskImpl(code)
      task.thread.setDaemon(true)
      runnedTasks = runnedTasks :+ task
      task.onFinish { _ =>
        this.synchronized {
          runnedTasks = runnedTasks.filterNot(t => t==task)
        }
      }
      task.thread.start()
      task
    }
    override def tasks: List[Task[?]] = runnedTasks

