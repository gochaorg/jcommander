package xyz.cofe.term.ui.log

trait CommonLog:
  def apply(string:String):Unit

object CommonLog:
  @volatile
  private var defaultOutput : Option[Appendable] = None
  def setOutput(out:Appendable):Unit =
    defaultOutput = Some(out)

  given defaultInstance:CommonLog with
    override def apply(string: String): Unit = 
      defaultOutput.foreach( out => out.append(string).append("\n") )

  def simple(out:Appendable):CommonLog =
    new CommonLog {
      override def apply(string: String): Unit = 
        out.append(string).append("\n")
    }
