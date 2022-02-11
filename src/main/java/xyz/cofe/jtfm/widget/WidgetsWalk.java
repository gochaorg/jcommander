package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.graphics.TextGraphics;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.collection.ImTreeWalk;
import xyz.cofe.iter.Eterable;
import xyz.cofe.iter.TreeIterBuilder;
import xyz.cofe.jtfm.OwnProperty;
import xyz.cofe.jtfm.gr.Rect;

import java.util.List;
import java.util.function.Function;

/**
 * Обход дерева виджетов
 */
public class WidgetsWalk {
    public static class VirtualRoot implements IWidget<VirtualRoot> {
        public final Iterable<? extends IWidget<?>> widgets;

        @SuppressWarnings("nullness")
        public VirtualRoot( @NonNull Iterable<? extends IWidget<?>> widgets ){
            this.widgets = widgets;
            //visible_prop = new OwnProperty<>(true, this);

            this.rect_prop = new OwnProperty<>(Rect.of(0,0,100,100), this);
            this.visible_prop = new OwnProperty<>(true, this);
        }

        @Override
        public @NonNull Iterable<? extends IWidget<?>> getNestedWidgets(){
            return widgets;
        }

        private final OwnProperty<Rect,VirtualRoot> rect_prop; // = new OwnProperty<>(Rect.of(0,0,100,100), this);

        @Override
        public OwnProperty<Rect, VirtualRoot> rect(){
            return rect_prop;
        }

        @Override
        public void render( @NonNull TextGraphics g ){
        }

        private OwnProperty<Boolean,VirtualRoot> visible_prop; // = new OwnProperty<>(true, this);

        @Override
        public OwnProperty<Boolean, VirtualRoot> visible(){
            return visible_prop;
        }
    }

    private static final Function<IWidget<?>,Iterable<? extends IWidget<?>>> visibleTree
        = root ->
        root == null || !root.visible().get() ? List.<Widget<?>>of() :
            Eterable.of(root.getNestedWidgets()).filter(
                w -> w!=null && w.visible().get()
            )
        ;

    public static TreeIterBuilder<IWidget<?>> visibleTree( @NonNull Iterable<? extends IWidget<?>> roots ){
        return Eterable.<IWidget<?>>tree(new VirtualRoot(roots), visibleTree);
    }
}
