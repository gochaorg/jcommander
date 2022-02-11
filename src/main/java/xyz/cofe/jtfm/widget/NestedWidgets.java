package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

/**
 * Дочерние виджеты
 */
public interface NestedWidgets {
    /**
     * Возвращает дочерние виджеты
     * @return дочерние виджеты
     */
    default @NonNull Iterable<? extends IWidget<?>> getNestedWidgets(){ return List.of(); }
}
