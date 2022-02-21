package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.TerminalPosition;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.jtfm.OwnProperty;
import xyz.cofe.jtfm.gr.Point;
import xyz.cofe.jtfm.gr.Rect;

/**
 * Свойство расположения виджета
 * @param <OWN> владелец свойства
 */
public class RectProp<OWN> extends OwnProperty<Rect,OWN> implements Rect {
    /**
     * Конструктор
     *
     * @param initial начальное значение
     * @param owner   владелец свойства
     */
    public RectProp( Rect initial, OWN owner ){
        super(initial, owner);
    }

    //region delegate Rect to get()
    @Override
    public int left(){
        return get().left();
    }

    @Override
    public int top(){
        return get().top();
    }

    @Override
    public int width(){
        return get().width();
    }

    @Override
    public int height(){
        return get().height();
    }

    @Override
    public int bottom(){
        return get().bottom();
    }

    @Override
    public int right(){
        return get().right();
    }

    @Override
    public Point leftTop(){
        return get().leftTop();
    }

    @Override
    public Point rightTop(){
        return get().rightTop();
    }

    @Override
    public Point leftBottom(){
        return get().leftBottom();
    }

    @Override
    public Point rightBottom(){
        return get().rightBottom();
    }

    @Override
    public boolean include( int x, boolean inc_left, boolean inc_right, int y, boolean inc_top, boolean inc_bottom ){
        return get().include(x, inc_left, inc_right, y, inc_top, inc_bottom);
    }

    @Override
    public boolean include( int x, boolean inc_right, int y, boolean inc_bottom ){
        return get().include(x, inc_right, y, inc_bottom);
    }

    @Override
    public boolean include( int x, int y, boolean inc_right_bottom ){
        return get().include(x, y, inc_right_bottom);
    }

    @Override
    public boolean include( int x, int y ){
        return get().include(x, y);
    }

    @Override
    public boolean include( @NonNull TerminalPosition p ){
        return get().include(p);
    }

    @Override
    public boolean include( @NonNull Point p ){
        return get().include(p);
    }
    //endregion

    /**
     * Указывает расположение
     * @param x координата
     * @param y координата
     * @param w ширина
     * @param h высота
     */
    public void set( int x, int y, int w, int h ){
        set(Rect.of(x,y,w,h));
    }
}
