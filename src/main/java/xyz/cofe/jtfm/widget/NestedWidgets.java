package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.collection.EventList;
import xyz.cofe.jtfm.widget.impl.NestedWidgetsImpl;

/**
 * Дочерние виджеты
 */
public interface NestedWidgets extends NestedNodes<IWidget<?>> {
    /**
     * Возвращает дочерние виджеты
     * @return дочерние виджеты
     */
    @SuppressWarnings("unchecked")
    @Override
    default @NonNull EventList<IWidget<?>> nestedNodes(){ return NestedWidgetsImpl.nonModify; }
}
