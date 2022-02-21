package xyz.cofe.jtfm.widget.table;

import xyz.cofe.jtfm.gr.Rect;

/**
 * Расположение данных колонки на экране
 *
 * @param <A> Тип колонки
 */
public class ColumnLocation<A> {
    /**
     * Колонка
     */
    public final Column<A, ?> column;

    /**
     * Расположение колонки на экране
     */
    public final Rect location;

    /**
     * Конструктор
     * @param column Колонка
     * @param location Расположение колонки на экране
     */
    public ColumnLocation( Column<A, ?> column, Rect location ){
        if( column == null ) throw new IllegalArgumentException("column==null");
        if( location == null ) throw new IllegalArgumentException("location==null");
        this.column = column;
        this.location = location;
    }
}
