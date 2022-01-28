package xyz.cofe.jcommander.widget;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import xyz.cofe.jcommander.Observer2;
import xyz.cofe.jcommander.Rect;

/**
 * Абстрактный компонент (виджет)
 */
public abstract class Widget<SELF extends Widget<?>> {
    //region rect : Rect - Расположение компонента
    /**
     * Событие смены расположения компонента
     */
    public final Observer2<SELF, Rect> rectChanged = new Observer2<>();

    private Rect rect = Rect.of(0,0,1,1);

    /**
     * Указывает расположение элемента
     * @return расположение элемента
     */
    public Rect getRect(){ return rect; }

    /**
     * Указывает расположение элемента
     * @param r расположение
     */
    public void setRect( Rect r ){
        if( r==null )throw new IllegalArgumentException( "r==null" );
        this.rect = r;
        //noinspection unchecked
        rectChanged.fire((SELF) this, r);
    }
    //endregion

    //region render( g ) - Рендер компонента
    /**
     * Рендер компонента
     * @param g рендер
     */
    public abstract void render( TextGraphics g );
    //endregion

    //region input( KeyStroke ks ) - Обработка событий ввода
    /**
     * Обработка событий ввода
     * @param ks событие ввода
     */
    public abstract void input( KeyStroke ks );
    //endregion
}
