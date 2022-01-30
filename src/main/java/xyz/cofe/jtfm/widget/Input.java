package xyz.cofe.jtfm.widget;

import com.googlecode.lanterna.input.KeyStroke;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Обработка событий ввода
 */
public interface Input {
    /**
     * Обработка событий ввода
     * @param ks событие ввода (клавиатура, мышь)
     * @return true - обработано, false - не обработано
     */
    default boolean input( @NonNull KeyStroke ks ){ return false; }
}
