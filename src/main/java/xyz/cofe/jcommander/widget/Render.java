package xyz.cofe.jcommander.widget;

import com.googlecode.lanterna.graphics.TextGraphics;

/**
 * рендер/отрисовка виджета
 */
public interface Render {
    /**
     * Рендер компонента
     * @param g рендер
     */
    void render( TextGraphics g );
}
