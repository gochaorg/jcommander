package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import xyz.cofe.jtfm.OwnProperty;

public class MenuItem extends Widget<MenuItem> implements Menu<MenuItem>, MenuText<MenuItem> {
    private @MonotonicNonNull OwnProperty<String,MenuItem> menuText_prop;

    @Override
    public OwnProperty<String, MenuItem> menuText(){
        if( menuText_prop==null )menuText_prop = new OwnProperty<>("?", this);
        return menuText_prop;
    }
}
