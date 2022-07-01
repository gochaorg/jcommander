package xyz.cofe.jtfm.fx

import javafx.application.Application
import javafx.application.Application.launch
import javafx.stage.Stage
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.layout.{AnchorPane, BorderPane, Priority, VBox}
import javafx.stage.Stage
import javafx.scene.control.MenuBar

class AppFx extends Application {
  def start(stage:Stage):Unit = {
    val url = getClass.getResource("MainController.fxml")
    val loader = new FXMLLoader()
    loader.setLocation(url)
  
    val main:Parent = loader.load
    val mainController : MainController = loader.getController

    val border = new BorderPane()
    border.setCenter(main)
    border.setTop(buildMenu)
  
    stage.setScene(new Scene(border))
    stage.setTitle("jtfm-fx")
  
    stage.show()
  }
  
  private def buildMenu = {
    val mbar = new MenuBar()
    val leftMenu = new Menu("Left")
    val leftA = new MenuItem("A")
    leftA.setOnAction { _ =>
        System.out.println("1")
    }
    
    leftMenu.getItems.add(leftA)
    val rightMenu = new Menu("Right")
    mbar.getMenus.add(leftMenu)
    mbar.getMenus.add(rightMenu)
    mbar
  }
}

object AppFx extends AppFx {
  def main(args:Array[String]):Unit = {
    launch(classOf[AppFx],args:_*)
  }
}