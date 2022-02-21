package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import xyz.cofe.collection.EventList;
import xyz.cofe.jtfm.OwnProperty;
import xyz.cofe.jtfm.gr.Point;
import xyz.cofe.jtfm.gr.Rect;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WidgetTreeTest {
    public static class SomeWidget
        implements
            ParentProperty<SomeWidget,SomeWidget>,
            NestedNodes<SomeWidget>,
            NodePath<SomeWidget>,
            RectProperty<SomeWidget>,
            AbsoluteToLocal<SomeWidget>
    {
        public SomeWidget(){
            nested = new ArrayList<>();
        }
        public SomeWidget(@NonNull String text){
            this.text = text;
            nested = new ArrayList<>();
        }

        //region parent
        public SomeWidget parent(@NonNull SomeWidget widget){
            parent().get().ifPresent(someWidget -> someWidget.nested.remove(this));
            parent().set(Optional.of(widget));
            widget.nested.add(this);
            return this;
        }

        private @Nullable OwnProperty<Optional<SomeWidget>, SomeWidget> parent_prop;

        @Override
        public @NonNull OwnProperty<Optional<SomeWidget>, SomeWidget> parent(){
            if( parent_prop!=null )return parent_prop;
            parent_prop = new OwnProperty<>(Optional.empty(), this);
            return parent_prop;
        }
        //endregion
        //region nested
        public final @NotOnlyInitialized List<SomeWidget> nested;

        @Override
        public @NonNull Iterable<SomeWidget> nestedNodes(){
            return nested;
        }
        //endregion

        private @MonotonicNonNull OwnProperty<Rect,SomeWidget> rect_prop;
        public OwnProperty<Rect,SomeWidget> rect(){
            if( rect_prop!=null )return rect_prop;
            rect_prop = new OwnProperty<>(Rect.of(0,0,1,1),this);
            return rect_prop;
        }

        public @NonNull String text = "?";
    }

    @Test
    public void test01(){
        SomeWidget w_root = new SomeWidget("root");
        SomeWidget w1 = new SomeWidget("w1").parent(w_root);
        SomeWidget w2 = new SomeWidget("w2").parent(w_root);
        SomeWidget w3 = new SomeWidget("w3").parent(w2);

        w1.rect().set( Rect.of(3,3,20,20));
        w2.rect().set( Rect.of(6,6,20,20));
        w3.rect().set( Rect.of(2,2,20,20));

        List.of(w_root, w1, w2, w3).forEach( w -> {
            var rect_str = w.rect().get().leftTop()+"-"+w.rect().get().rightBottom();
            System.out.println(
                ""+w.text
                +" rect "+rect_str
                +" path "
                +w.nodePath().stream().map(x -> x.text).collect(Collectors.toList())
            );

            var p_a = new Point(9,9);
            var p_l = w.toLocal(p_a);
            var p_aa = w.toAbsolute(p_l);
            System.out.println("  abs: "+p_a+" -> loc: "+p_l+" rev: "+p_aa+" eq: "+p_aa.equals(p_a));
        });
    }
}
