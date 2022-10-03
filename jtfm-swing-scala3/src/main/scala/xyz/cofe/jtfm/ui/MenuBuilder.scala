package xyz.cofe.jtfm.ui

import javax.swing.JMenuBar
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.Box

trait MenuBuilder:
  def menu(name:String)(mb:SubMenuBuilder=>Unit):MenuBuilder

trait RootMenuBuilder extends MenuBuilder {
  def bar:JMenuBar
  override def menu(name:String)(mb:SubMenuBuilder=>Unit):RootMenuBuilder
  def horizGlue():RootMenuBuilder
}

trait SubMenuBuilder extends MenuBuilder:
  def action(name:String)(code: =>Unit):SubMenuBuilder
  override def menu(name: String)(mb: SubMenuBuilder => Unit): SubMenuBuilder = ???

case class RootMenuBuilderImpl(bar:JMenuBar) extends RootMenuBuilder:
  def menu(name: String)(mb: SubMenuBuilder=>Unit): RootMenuBuilder = 
    val m = new JMenu(name)
    bar.add(m)
    mb(SubMenuBuilderImpl(m))
    this
  def horizGlue():RootMenuBuilder =
    bar.add(Box.createHorizontalGlue())
    this

case class SubMenuBuilderImpl(menu:JMenu) extends SubMenuBuilder:
  override def menu(name: String)(mb: SubMenuBuilder=>Unit): SubMenuBuilder = 
    val m = new JMenu(name)
    menu.add(m)
    mb(SubMenuBuilderImpl(m))
    this

  def action(name:String)(code: =>Unit):SubMenuBuilder = {
    val m = new JMenuItem(name)
    menu.add(m)
    m.addActionListener { e =>
      code
    }
    this
  }

object MenuBuilder:
  def apply(bar:JMenuBar):RootMenuBuilder = RootMenuBuilderImpl(bar)