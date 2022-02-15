package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.collection.EventList;
import xyz.cofe.jtfm.widget.impl.NestedWidgetsImpl;

/**
 * Дочерние виджеты
 */
public interface NestedWidgets {
    /**
     * Возвращает дочерние виджеты
     * @return дочерние виджеты
     */
    @SuppressWarnings("unchecked")
    default @NonNull EventList<IWidget<?>> nestedWidgets(){ return NestedWidgetsImpl.nonModify; }
}
