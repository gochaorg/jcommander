package xyz.cofe.jtfm.widget;

import java.util.Optional;

public interface WidgetParent {
    Optional<Widget<?>> getParent();
}
