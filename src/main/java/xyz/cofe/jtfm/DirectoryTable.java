package xyz.cofe.jtfm;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Виджет для работы с каталогом
 */
public class DirectoryTable extends FilesTable {
    private void log(Throwable err){}
    private void log(String msg){}

    @SuppressWarnings("nullness")
    public DirectoryTable(){
        var p = Paths.get(".").toAbsolutePath();
        directory = new SimpleProperty<>(p);
        init();
        readDirectory(p);
    }

    @SuppressWarnings("nullness")
    public DirectoryTable(Path path){
        if( path==null )throw new IllegalArgumentException( "path==null" );
        directory = new SimpleProperty<>(path);
        init();
        readDirectory(path);
    }

    private void init(){
        directory.listen( (prop,old,cur) -> {
            readDirectory(cur);
        });

        getKeyStokes().put(KeyShortcut.from("enter"), this::openFocused);
        getKeyStokes().put(KeyShortcut.from("bs"), this::openParentDirectory);
    }

    /**
     * Текущий каталог
     */
    public final SimpleProperty<Path> directory;
    public final SimpleProperty<Consumer<Path>> openRegularFile = new SimpleProperty<>(f -> {});
    public final Observer2<DirectoryTable,Path> onReadDirectory = new Observer2<>();
    public final Observer2<DirectoryTable,Path> onOpenRegularFile = new Observer2<>();

    private void readDirectory(@Nullable Path path){
        getValues().clear();
        getSelection().clear();
        focused((Path)null);
        if( path==null )return;
        if( !Files.isDirectory(path) )return;

        try( DirectoryStream<Path> dstrm = Files.newDirectoryStream(path) ){
            for( Path p : dstrm ){
                getValues().add(p);
            }
        } catch( IOException ex ){
            log(ex);
        }

        getValues().sort(defaultSort);

        getValues().add(0,path.resolve(".."));
        if( !getValues().isEmpty() ){
            setFocused(getValues().get(0));
        }

        onReadDirectory.fire(this,path);
    }

    @SuppressWarnings("nullness")
    public void openFocused() {
        var f = getFocused();
        if( f==null )return;

        if( Files.isRegularFile(f) )openRegularFile(f);
        else if( Files.isDirectory(f) ){
            if( f.getFileName().toString().equals("..") ){
                openParentDirectory();
            }else {
                openSubDirectory(f);
            }
        }
    }
    public void openRegularFile( @NonNull Path path ){
        openRegularFile.get().accept(path);
        onOpenRegularFile.fire(this,path);
    }
    public void openParentDirectory(){
        var curdir = directory.get();

        var prnt = curdir.getParent();
        if( prnt==null && !curdir.isAbsolute() ){
            prnt = curdir.toAbsolutePath().getParent();
            if( prnt==null ){
                log("can't exit to parent directory of "+curdir);
                return;
            }
        }

        if( prnt==null ){
            log("can't exit to parent directory of "+curdir);
            return;
        }

        directory.set(prnt);
    }
    public void openSubDirectory( @NonNull Path path ){
        //noinspection ConstantConditions
        if( path==null )throw new IllegalArgumentException( "path==null" );

        directory.set(path);
    }
}
