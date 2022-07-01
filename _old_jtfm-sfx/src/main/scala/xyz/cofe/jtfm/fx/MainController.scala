package xyz.cofe.jtfm.fx

import javafx.fxml.FXML
import javafx.scene.Node

class MainController
{
  @FXML private var leftDir:Node = _
  @FXML private var leftDirController:DirController = _
  @FXML private var rightDir:Node = _
  @FXML private var rightDirController:DirController = _
  
  @FXML def initialize(): Unit = {
    println("MainController.init")
    if( leftDirController!=null ){
      println("leftDirController.initialize()")
      leftDirController.initialize()
    }else{
      println("leftDirController is null")
    }
    if( rightDirController!=null ){
      println("rightDirController.initialize()")
      rightDirController.initialize()
    }else{
      println("rightDirController is null")
    }
  }
}