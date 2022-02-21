package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.collection.EventList;

/**
 * Описывает дочерние элементы
 */
public interface NestedNodes<N> {
    /**
     * Возвращает дочерние элементы
     * @return дочерние элементы
     */
    @NonNull Iterable<N> nestedNodes();
}
