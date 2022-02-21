package xyz.cofe.jtfm.gr;

import com.googlecode.lanterna.TerminalPosition;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Точка
 */
public final class Point {
    /** Координата */
    public final int x;

    /** Координата */
    public final int y;

    /**
     * Конструктор
     * @param x координата
     * @param y координата
     */
    public Point( int x, int y ){
        this.x = x;
        this.y = y;
    }

    /**
     * Конструктор x=0, y=0
     */
    public Point(){
        this.x = 0;
        this.y = 0;
    }

    public Point( @NonNull TerminalPosition p ){
        this.x = p.getColumn();
        this.y = p.getRow();
    }

    public TerminalPosition toTerminalPosition(){
        return new TerminalPosition(x,y);
    }

    /**
     * Смещение точки
     * @param p размер сдвига
     * @return смещенная точка (клон)
     */
    public Point translate( @NonNull Point p ){
        return new Point(x+p.x, y+p.y );
    }

    /**
     * Смещение точки
     * @param x_off смещение
     * @param y_off смещение
     * @return смещенная точка (клон)
     */
    public Point translate( int x_off, int y_off ){
        return new Point(x+x_off, y+y_off );
    }

    /**
     * Инверсия координат
     * @param x_inv инвертировать x
     * @param y_inv инвертировать y
     * @return Инвертированная точка
     */
    public Point invert(boolean x_inv, boolean y_inv){
        return new Point(x_inv ? -x : x, y_inv ? -y : y);
    }

    /**
     * Инверсия координат
     * @return Инвертированная точка
     */
    public Point invert() { return invert(true, true); }

    @Override
    public boolean equals( @Nullable Object o ){
        if( this == o ) return true;
        if( o == null || getClass() != o.getClass() ) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode(){
        return Objects.hash(x, y);
    }

    @Override
    public String toString(){
        return "("+x+";"+y+")";
    }

    public static Optional<Point> parse( @NonNull String txt ){
        //noinspection ConstantConditions
        if( txt==null )throw new IllegalArgumentException( "txt==null" );
        if( !txt.startsWith("(") )return Optional.empty();
        if( !txt.endsWith(")") )return Optional.empty();
        if( txt.length()<3 )return Optional.empty();
        var txt1 = txt.substring(1,txt.length()-1);

        var xy = txt1.split(";");
        if( xy.length<2 )return Optional.empty();

        try{
            int x = Integer.parseInt(xy[0]);
            int y = Integer.parseInt(xy[1]);
            return Optional.of(new Point(x,y));
        } catch( NumberFormatException e ){
            return Optional.empty();
        }
    }
}
