package xyz.cofe.jtfm.widget;

import xyz.cofe.jtfm.SimpleProperty;

public interface VisibleProperty extends IsVisible {
    SimpleProperty<Boolean> visible();

    @Override
    default boolean isVisible(){
        return visible().get();
    }
}
