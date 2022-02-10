package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.jtfm.gr.Border;
import xyz.cofe.jtfm.gr.Rect;

import java.util.Optional;

class Widget1Test extends Widget<Widget1Test> implements OnFocusLost, OnFocusGain {
    private boolean hasFocus(){
        return WidgetCycle.tryGet().flatMap( wc -> wc.findFocusOwner().map( fo -> fo== Widget1Test.this ) ).orElse(false);
    }

    private Border singleBorder = new Border(true);
    private Border doubleBorder = new Border(false);

    private String text = "sample";

    public @NonNull String getText(){
        return text;
    }

    public void setText( @NonNull String text ){
        this.text = text;
    }

    @Override
    public void render( @NonNull TextGraphics g ){
        var r = rect().get();
        if( hasFocus() )
            doubleBorder.render(g,Rect.of(0,0,r.width(),r.height()));
        else
            singleBorder.render(g,Rect.of(0,0,r.width(),r.height()));

        g.putString(0,0,text!=null ? text : "sample");
    }

    @Override
    public boolean isFocusable(){
        return true;
    }

    @Override
    public void focusGain( Optional<IWidget<?>> last ){
        repaint();
    }

    @Override
    public void focusLost( Optional<IWidget<?>> cur ){
        repaint();
    }

    @Override
    public boolean input( @NonNull KeyStroke ks ){
        System.out.println("accept " + ks);
        if( ks.getKeyType() == KeyType.ArrowLeft ){
            var r = rect().get();
            rect().set(Rect.of(r.left() - 1, r.top(), r.width(), r.height()));
            return true;
        }
        if( ks.getKeyType() == KeyType.ArrowRight ){
            var r = rect().get();
            rect().set(Rect.of(r.left() + 1, r.top(), r.width(), r.height()));
            return true;
        }
        return false;
    }
}
