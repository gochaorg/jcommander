package xyz.cofe.jtfm.fx

import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXML
import javafx.scene.control.cell.{PropertyValueFactory, TextFieldTableCell}
import javafx.scene.control.{TableColumn, TableView}

import java.nio.file.{Path, Paths}

class DirController {
  @FXML var filesTable : TableView[Path] = _
  
  @FXML def initialize(): Unit = {
    println("DirController init")
    filesTable.getColumns.clear()
    
    val nameColumn = new TableColumn[Path,String]("name")
    nameColumn.setCellValueFactory(cellData => new SimpleStringProperty(cellData.getValue.toString))
    filesTable.getColumns.add(nameColumn)
  
    for( i <- 0 to 100) println(i)
//    (0 to 200).foreach ( i =>{
//      filesTable.getItems.add(Paths.get("/a"+i))
//    })
  }
  
  def cd( path:Path ):Unit = {
  }
}
