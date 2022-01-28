package xyz.cofe.jcommander;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.collection.EventList;
import xyz.cofe.jcommander.table.Column;
import xyz.cofe.jcommander.table.Table;
import xyz.cofe.jcommander.table.TableColumns;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

/**
 * Таблица файлов
 */
public class FilesTable extends Table<Path> {
    private static void log( Throwable err ){
    }

    //region ctor
    public FilesTable( EventList<Path> values ){
        super(values);
    }

    public FilesTable(){
    }
    //endregion

    //region columns

    /**
     * Колонки связанные с файлами, для таблицы
     */
    public static class Columns extends TableColumns<Path> {
        /**
         * Конструктор по умолчанию
         */
        public Columns(){
            this(false);
        }

        /**
         * Конструктор
         * @param includeDefaultColumns true - добавить колонки
         */
        @SuppressWarnings("nullness")
        public Columns(boolean includeDefaultColumns){
            if( includeDefaultColumns ){
                add(baseName);
                add(extension);
                add(unixPermissions);
                add(humanSize);
            }
        }

        private final static Set<String> specialNames = Set.of(".", "..");

        //region baseName : Column
        @SuppressWarnings("nullness")
        public final Column<Path,String> baseName = build(
            p -> specialNames.contains(getName(p))
                ? "/"+getName(p)
                : getBasename(p).length()<1
                    ? getName(p)
                    : Files.isDirectory(p)
                        ? "/" + getBasename(p)
                        : Files.isSymbolicLink(p)
                            ? "~" + getBasename(p)
                            : " " + getBasename(p),
            c -> c.width(20).minMaxWidth(5,100).name("name").fixed(false)
        );
        //endregion
        //region extension : Column
        @SuppressWarnings("nullness")
        public final Column<Path,String> extension = build(
            p -> specialNames.contains(getName(p))
                ? ""
                : getBasename(p).length()<1
                    ? ""
                    : getExtension(p),
            c -> c.name("ext").width(5).minMaxWidth(3,20).fixed(false)
        );
        //endregion
        //region humanSize : Column
        private static Optional<Long> safeSize(Path p){
            try{
                if( Files.exists(p) ){
                    return Optional.of(Files.size(p));
                }else{
                    return Optional.empty();
                }
            } catch( IOException e ){
                log(e);
                return Optional.empty();
            }
        }

        @SuppressWarnings("nullness")
        public final Column<Path,String> humanSize = build(
            p -> Files.exists(p)
                ? safeSize(p).map(FilesTable::humanSize).orElse("?")
                : "-1",
            c -> c.name("size").width(6).minMaxWidth(6,12).fixed(false)
        );
        //endregion
        //region unixPermissions : Column
        private String perm( Set<PosixFilePermission> perms ) {
            //noinspection StringBufferReplaceableByString
            StringBuilder sb = new StringBuilder();
            sb.append( perms.contains(PosixFilePermission.OWNER_READ) ? "r" : "-" );
            sb.append( perms.contains(PosixFilePermission.OWNER_WRITE) ? "w" : "-" );
            sb.append( perms.contains(PosixFilePermission.OWNER_EXECUTE) ? "x" : "-" );
            sb.append( perms.contains(PosixFilePermission.GROUP_READ) ? "r" : "-" );
            sb.append( perms.contains(PosixFilePermission.GROUP_WRITE) ? "w" : "-" );
            sb.append( perms.contains(PosixFilePermission.GROUP_EXECUTE) ? "x" : "-" );
            sb.append( perms.contains(PosixFilePermission.OTHERS_READ) ? "r" : "-" );
            sb.append( perms.contains(PosixFilePermission.OTHERS_WRITE) ? "w" : "-" );
            sb.append( perms.contains(PosixFilePermission.OTHERS_EXECUTE) ? "x" : "-" );
            return sb.toString();
        }
        private Optional<Set<PosixFilePermission>> safeUnixPermissions( Path p ){
            try{
                return Optional.of(Files.getPosixFilePermissions(p));
            } catch( IOException e ){
                log(e);
                return Optional.empty();
            }
        }

        @SuppressWarnings("nullness")
        public final Column<Path,String> unixPermissions = build(
            p -> Files.exists(p)
                ? safeUnixPermissions(p).map(this::perm).orElse("?")
                : "?",
            c -> c.width(9).fixed(true).name("perm")
        );
        //endregion
    }

    private @MonotonicNonNull Columns columns;

    @SuppressWarnings("nullness")
    @Override
    @EnsuresNonNull("this.columns")
    public @NonNull Columns getColumns(){
        if( columns!=null )return columns;
        columns = new Columns(true);
        return columns;
    }
    //endregion

    /**
     * Базовое имя файла без расширения
     * @return базовое имя файла
     */
    private static String getBasename( @NonNull Path path ){
        @SuppressWarnings("nullness")
        @NonNull String name = path.getFileName().toString();
        int dot = name.lastIndexOf(".");
        if( dot<0 )return name;
        if( dot==0 )return "";
        return name.substring(0, dot);
    }

    /**
     * Раширение имени файла.
     * @return расширение имени файла без точки
     */
    private static String getExtension( @NonNull Path path ){
        @SuppressWarnings("nullness")
        @NonNull String name = path.getFileName().toString();
        int dot = name.lastIndexOf(".");
        if( dot<0 )return "";
        if( dot>=(name.length()-1) )return "";
        return name.substring(dot+1);
    }

    /**
     * Имя файла
     * @return Имя файла
     */
    @SuppressWarnings("nullness")
    private static String getName( @NonNull Path path ){
        return path.getFileName().toString();
    }

    /**
     * Человеко читабельный размер файла
     * @param bytes размер в байтах
     * @return читабельный размер
     */
    private static String humanSize( long bytes ){
        String sz = "";
        if( bytes>=(1024*1024*1024) ){
            sz = Long.toString(bytes >> 30)+"g";
        }else if( bytes>=(1024*1024) ){
            sz = Long.toString(bytes >> 20)+"m";
        }else if( bytes>=(1024) ){
            sz = Long.toString(bytes >> 10)+"k";
        }else {
            sz = Long.toString(bytes);
        }

        if( sz.length()<5 ){
            sz = " ".repeat(5-sz.length())+sz;
        }

        return sz;
    }

    /**
     * Сортировка по умолчанию:
     * <ol>
     *     <li> Не null идут первыми
     *     <li> Каталоги идут первыми
     *     <li> Файлы/каталог сравниваются по именам
     * </ol>
     */
    public final Comparator<Path> defaultSort = (a,b) -> {
        if( a==null && b==null )return 0;
        if( a!=null && b==null )return -1;
        if( a==null )return 1;

        boolean a_dir = Files.isDirectory(a);
        boolean b_dir = Files.isDirectory(b);
        if( a_dir && !b_dir )return -1;
        if( !a_dir && b_dir )return 1;

        String a_name = getName(a);
        String b_name = getName(b);
        return a_name.compareTo(b_name);
    };
}
