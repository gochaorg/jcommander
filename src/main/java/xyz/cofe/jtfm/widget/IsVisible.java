package xyz.cofe.jtfm.widget;

/**
 * Интерфейс проверки видимости
 */
public interface IsVisible {
    default boolean isVisible(){ return true; }
}
