package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.collection.BasicEventList;
import xyz.cofe.collection.EventList;
import xyz.cofe.jtfm.OwnProperty;
import xyz.cofe.jtfm.gr.Rect;

import java.util.Optional;

/**
 * Абстрактный компонент (виджет)
 */
public abstract class Widget<SELF extends Widget<?>>
implements IWidget<SELF>
{
    @SuppressWarnings("nullness")
    protected Widget(){
        listenForSetParent(this);
        listenRect2(this);
        listenVisible(this);
    }

    protected void repaint(){
        WidgetCycle.tryGet().ifPresent( wc -> {
            wc.addRenderRequest(this);
        });
    }

    //region parent : Optional<Widget<?>> - родительский виджет
    @SuppressWarnings({"unchecked","cast.unsafe"})
    private final OwnProperty<Optional<IWidget<?>>,SELF> o_parent = new OwnProperty<>(
        Optional.empty(),
        (SELF) this);

    /**
     * Свойство - родительский виджет
     * @return виджет
     */
    public OwnProperty<Optional<IWidget<?>>,SELF> parent(){ return o_parent; }

    @SuppressWarnings("nullness")
    private static void listenForSetParent( @UnderInitialization(Widget.class) @NonNull Widget<?> wid ){
        wid.getNestedWidgets().onChanged( (idx,oldWid,curWid) -> {
            if( oldWid!=null ){
                var p = oldWid.parent().get();
                if( p.isPresent() && p.get()==wid ){
                    oldWid.parent().set(Optional.empty());
                }
            }
            if( curWid!=null ){
                curWid.parent().set(Optional.of(wid));
            }
        });
    }

    private EventList<IWidget<?>> nestedWidgets;

    @Override
    public @NonNull EventList<IWidget<?>> getNestedWidgets(){
        if( nestedWidgets!=null )return nestedWidgets;
        nestedWidgets = new BasicEventList<>();
        return nestedWidgets;
    }
    //endregion

    //region relativeLayout() : boolean - true - рендер относительно; false - абсолютно
    /**
     * Рендер ({@link #render(TextGraphics)}) относительно расположения
     * @return true - рендер относительно; false - абсолютно
     */
    public boolean relativeLayout(){ return true; }
    //endregion

    //region visible : Boolean - видимость
    @SuppressWarnings({"unchecked", "ConstantConditions", "cast.unsafe"})
    private final OwnProperty<Boolean,SELF> visible = new OwnProperty<>(true, (SELF)this);

    @Override public OwnProperty<Boolean, SELF> visible(){ return visible; }

    private static void listenVisible( @UnderInitialization(Widget.class) @NonNull Widget<?> w ){
        w.visible.listen( (s,old,cur) -> {
            s.owner().repaint();
        });
    }
    //endregion

    //region rect : Rect - Расположение компонента
    @SuppressWarnings({"unchecked","cast.unsafe"})
    private final OwnProperty<Rect,SELF> o_rect = new OwnProperty<>(Rect.of(0,0,1,1), (SELF) this);

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
    public boolean input( @NonNull KeyStroke ks ){ return false; }
    //endregion
}
