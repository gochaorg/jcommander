package xyz.cofe.jtfm

import xyz.cofe.files.AppHome

object Main:
  implicit object appHome extends AppHome("jtfm")

  def main(args:Array[String]):Unit =
    //println(s"home ${appHome.directory}")
    ()
