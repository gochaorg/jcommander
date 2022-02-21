package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.graphics.TextGraphics;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * рендер/отрисовка виджета
 */
public interface Render {
    /**
     * Рендер компонента
     * @param g рендер
     */
    void render( @NonNull TextGraphics g );
}
