package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.nullness.qual.NonNull;

public class VirtualRoot extends Widget<VirtualRoot> {
    @SuppressWarnings("nullness")
    public VirtualRoot( @NonNull Iterable<IWidget<?>> widgets ){
        for( var w : widgets ){
            nestedWidgets().add(w);
        }
    }
}
