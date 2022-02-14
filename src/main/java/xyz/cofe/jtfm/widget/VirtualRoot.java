package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.graphics.TextGraphics;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.collection.BasicEventList;
import xyz.cofe.collection.EventList;
import xyz.cofe.jtfm.OwnProperty;
import xyz.cofe.jtfm.gr.Rect;

import java.util.Optional;

public class VirtualRoot extends Widget<VirtualRoot> {
    @SuppressWarnings("nullness")
    public VirtualRoot( @NonNull Iterable<IWidget<?>> widgets ){
        for( var w : widgets ){
            getNestedWidgets().add(w);
        }
    }
}
