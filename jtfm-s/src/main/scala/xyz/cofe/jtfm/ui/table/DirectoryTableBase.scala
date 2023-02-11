package xyz.cofe.jtfm.ui.table

import xyz.cofe.lazyp.ReadWriteProp
import java.nio.file.Path
import xyz.cofe.term.ui.Table

trait DirectoryTableBase extends Table[Path]:
  def directory: ReadWriteProp[Option[Path]]
