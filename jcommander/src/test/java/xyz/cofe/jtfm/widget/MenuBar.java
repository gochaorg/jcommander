package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.graphics.TextGraphics;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MenuBar extends Widget<MenuBar> implements Menu<MenuBar> {
    public MenuBar(){
        parent().listen( (prop,oldParent,newParent) -> {
            unbindParent();
            newParent.ifPresent(this::bindParentRect);
        });
    }

    private void unbindParent(){
    }

    private void bindParentRect(IWidget<?> parent){
        rect().set(0,0,parent.rect().width(),1);
    }

    @Override
    public void render( @NonNull TextGraphics g ){
        super.render(g);
    }
}
