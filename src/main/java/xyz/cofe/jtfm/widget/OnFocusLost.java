package xyz.cofe.jtfm.widget;

import java.util.Optional;

public interface OnFocusLost {
    void focusLost( Optional<IWidget<?>> previousOwner );
}
