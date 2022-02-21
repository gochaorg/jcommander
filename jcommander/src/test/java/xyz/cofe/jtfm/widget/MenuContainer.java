package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import xyz.cofe.jtfm.OwnProperty;

public class MenuContainer extends Widget<MenuContainer> implements Menu<MenuContainer>, MenuText<MenuContainer> {
    private @MonotonicNonNull OwnProperty<String,MenuContainer> menuText_prop;

    @Override
    public OwnProperty<String, MenuContainer> menuText(){
        if( menuText_prop==null )menuText_prop = new OwnProperty<>("?", this);
        return menuText_prop;
    }
}
