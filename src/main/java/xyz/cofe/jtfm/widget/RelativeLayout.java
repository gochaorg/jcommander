package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.graphics.TextGraphics;

public interface RelativeLayout {
    /**
     * Рендер ({@link Widget#render(TextGraphics)}) относительно расположения
     * @return true - рендер относительно; false - абсолютно
     */
    default boolean relativeLayout() { return true; }
}
