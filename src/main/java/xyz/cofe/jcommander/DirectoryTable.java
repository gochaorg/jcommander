package xyz.cofe.jcommander;

import xyz.cofe.collection.EventList;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectoryTable extends FilesTable {
    @SuppressWarnings("nullness")
    public DirectoryTable(){
        directory = new SimpleProperty<>(Paths.get(".").toAbsolutePath());
        bindProps();
    }

    @SuppressWarnings("nullness")
    public DirectoryTable(Path path){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        directory = new SimpleProperty<>(path);
        bindProps();
    }

    private void bindProps(){
        directory.listen( (prop,old,cur) -> {
        });
    }

    public final SimpleProperty<Path> directory;

    private void readDirectory(Path path){
        getValues().clear();
    }
}
