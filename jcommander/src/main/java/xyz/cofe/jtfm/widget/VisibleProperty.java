package xyz.cofe.jtfm.widget;

import xyz.cofe.jtfm.OwnProperty;

/**
 * Свойство видимости объекта
 * @param <SELF> видимость
 */
public interface VisibleProperty<SELF> {
    /**
     * Свойство видимости объекта
     * @return свойство
     */
    OwnProperty<Boolean,SELF> visible();
}
