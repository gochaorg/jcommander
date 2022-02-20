package xyz.cofe.jtfm.widget;

import xyz.cofe.jtfm.OwnProperty;
import xyz.cofe.jtfm.gr.Rect;

/**
 * Свойство расположения виджета
 * @param <OWN> владелец свойства
 */
public class RectProp<OWN> extends OwnProperty<Rect,OWN> {
    /**
     * Конструктор
     *
     * @param initial начальное значение
     * @param owner   владелец свойства
     */
    public RectProp( Rect initial, OWN owner ){
        super(initial, owner);
    }
}
