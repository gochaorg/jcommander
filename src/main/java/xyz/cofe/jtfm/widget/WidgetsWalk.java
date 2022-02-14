package xyz.cofe.jtfm.widget;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.iter.Eterable;
import xyz.cofe.iter.TreeIterBuilder;

import java.util.List;
import java.util.function.Function;

/**
 * Обход дерева виджетов
 */
public class WidgetsWalk {

    private static final Function<IWidget<?>,Iterable<? extends IWidget<?>>> visibleTree
        = root ->
        root == null || !root.visible().get() ? List.<Widget<?>>of() :
            Eterable.of(root.getNestedWidgets()).filter(
                w -> w!=null && w.visible().get()
            )
        ;

    public static TreeIterBuilder<IWidget<?>> visibleTree( @NonNull Iterable<IWidget<?>> roots ){
        return Eterable.<IWidget<?>>tree(new VirtualRoot(roots), visibleTree);
    }
}
