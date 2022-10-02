package xyz.cofe.jtfm.ui

import xyz.cofe.jtfm.store.json.DefaultValue
import xyz.cofe.jtfm.store.json.FromJson
import xyz.cofe.jtfm.store.json.ToJson
import java.awt.GraphicsEnvironment

case class SwingConfig(
  scale: Double = 1,
  prefectScale: List[SwingScale] = List()
):
  def apply():Unit = {
    System.setProperty("sun.java2d.uiScale",scale.toString())
  }

case class SwingScale( heightMin:Int, heightMax:Int, scale:Double)

object SwingScale:
  given fromJson:FromJson[SwingScale] = FromJson.derived[SwingScale]
  given toJson:ToJson[SwingScale] = ToJson.derived[SwingScale]

object SwingConfig:
  given DefaultValue[SwingConfig] with
    override def defaultValue: Option[SwingConfig] = Some(
      SwingConfig(
        scale = 1.0,
        prefectScale = List(
          SwingScale(1500,2200,2.0),
          SwingScale(2200,3000,2.5),
          SwingScale(3000,6000,3.0)
        )
      )
    )
  given fromJson:FromJson[SwingConfig] = FromJson.derived[SwingConfig]
  given toJson:ToJson[SwingConfig] = ToJson.derived[SwingConfig]
