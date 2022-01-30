package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.jtfm.Observer2;
import xyz.cofe.jtfm.OwnProperty;
import xyz.cofe.jtfm.Rect;
import xyz.cofe.jtfm.SimpleProperty;

/**
 * Абстрактный компонент (виджет)
 */
public abstract class Widget<SELF extends Widget<?>>
implements IWidget<SELF>, VisibleProperty
{
    protected void repaint(){
        WidgetCycle.tryGet().ifPresent( wc -> {
            wc.addRenderRequest(this);
        });
    }

    private final SimpleProperty<Boolean> visible = new SimpleProperty<>(true);
    {
        listenVisible(this);
    }

    private static void listenVisible( @UnderInitialization(Widget.class) @NonNull Widget<?> w ){
        w.visible.listen( (p,old,cur) -> {
            //w.repaint();
        });
    }

    @Override
    public SimpleProperty<Boolean> visible(){
        return visible;
    }

    //region rect : Rect - Расположение компонента
    @SuppressWarnings("unchecked")
    private final OwnProperty<Rect,SELF> o_rect = new OwnProperty<>((SELF) this, Rect.of(0,0,1,1));

    /**
     * Свойство - расположение объекта
     * @return расположение объекта
     */
    public OwnProperty<Rect,SELF> rect(){ return o_rect; }

    private static void listenRect2( @UnderInitialization(Widget.class) @NonNull Widget<?> w ){
        w.o_rect.listen( (s,old,cur) -> {
            s.owner().repaint();
        });
    }

    {
        listenRect2(this);
    }
    //endregion

    //region render( g ) - Рендер компонента
    /**
     * Рендер компонента
     * @param g рендер
     */
    public void render( @NonNull TextGraphics g ){};
    //endregion

    //region input( KeyStroke ks ) - Обработка событий ввода
    /**
     * Обработка событий ввода
     * @param ks событие ввода
     */
    public void input( @NonNull KeyStroke ks ){};
    //endregion
}
