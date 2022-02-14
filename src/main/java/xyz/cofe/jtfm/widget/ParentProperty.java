package xyz.cofe.jtfm.widget;

import xyz.cofe.jtfm.OwnProperty;

import java.util.Optional;

/**
 * Свойство - родительский виджет
 */
public interface ParentProperty<SELF> {
    /**
     * Свойство - родительский виджет
     * @return виджет
     */
    OwnProperty<Optional<IWidget<?>>,SELF> parent();
}
