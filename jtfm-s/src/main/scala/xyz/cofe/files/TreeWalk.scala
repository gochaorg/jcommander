package xyz.cofe.files

import java.nio.file.Path

class TreeWalk( var workSet:List[Path] )(implicit
                                         options: FilesOption,
                                         log: FilesLogger) extends Iterator[Path] {
  override def hasNext: Boolean = workSet.nonEmpty
  override def next(): Path = {
    val res = workSet.head
    res.isDirectory.foreach{ isDir =>
      if( isDir ){
        res.readDir.foreach { subFiles =>
          workSet = subFiles ++ workSet
        }
      }
    }
    workSet = workSet.filterNot( p=> p==res )
    res
  }
}
