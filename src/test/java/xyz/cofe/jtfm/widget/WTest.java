package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.jtfm.Border;
import xyz.cofe.jtfm.Rect;

class WTest extends Widget<WTest> implements OnFocusLost, OnFocusGain {
    private boolean hasFocus(){
        return WidgetCycle.tryGet().flatMap( wc -> wc.findFocusOwner().map( fo -> fo==WTest.this ) ).orElse(false);
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
            doubleBorder.render(g,r);
        else
            singleBorder.render(g,r);

        g.putString(r.left(), r.top(), text!=null ? text : "sample");
    }

    @Override
    public boolean isFocusable(){
        return true;
    }

    @Override
    public void focusGain(){
        repaint();
    }

    @Override
    public void focusLost(){
        repaint();
    }

    @Override
    public void input( @NonNull KeyStroke ks ){
        System.out.println("accept " + ks);
        if( ks.getKeyType() == KeyType.ArrowLeft ){
            var r = rect().get();
            rect().set(Rect.of(r.left() - 1, r.top(), r.width(), r.height()));
        }
        if( ks.getKeyType() == KeyType.ArrowRight ){
            var r = rect().get();
            rect().set(Rect.of(r.left() + 1, r.top(), r.width(), r.height()));
        }
    }
}
