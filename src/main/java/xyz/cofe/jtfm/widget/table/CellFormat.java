package xyz.cofe.jtfm.widget.table;

import com.googlecode.lanterna.TextColor;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.cofe.jtfm.Rect;
import xyz.cofe.text.Align;

/**
 * Форматирование ячейки.
 *
 * <p>
 * Процесс форматирования заключается в следующих набора шагов:
 *
 * <ol>
 *     <li> Получение строки данных {@link #getItem()} для указанного индекса строки {@link #getRowIdx()}.
 *     <li> Получение "сырого" представления данных {@link #getRawString()} из колонки {@link Column#text(Object)}
 *     <li> Расчет координат вывода ячейки:
 *     <ol>
 *         <li> Получение координат вывода табличных данных {@link #getContentLocation()}
 *         <li> Получение координат вывода данных колонки {@link #getColumnLocation()}
 *         <li> Получение координат вывода ячейки {@link #getY()}
 *     </ol>
 *     <li> Расчет цвета ({@link #getBackground()}, {@link #getForeground()}),
 *          в зависимости от фокуса {@link #isFocus()} и выделения строки {@link #isSelected()}
 *     <li> Усечение пробелов с краев "сырого" представления в зависимости от флага {@link #isTrimRaw()}
 *     <li> Разбиение "сырого" представления, на набор строк по CRLF
 *     <li> Усечение пробелов с краев каждой строки в зависимости от флага {@link #isTrimLine()}
 *     <li> Выравнивание каждой строки в зависимости от {@link #getHalign()}
 * </ol>
 *
 * @param <A> тип данных в таблице
 */
@SuppressWarnings("ConstantConditions")
public class CellFormat<A> {
    //region item - строка данных
    private @MonotonicNonNull A item;

    /**
     * Возвращает строку данных таблицы
     * @return строка данных
     */
    public @NonNull A getItem(){
        if( item==null )throw new IllegalStateException("item is null");
        return item;
    }

    /**
     * Устанавливает строку данных таблицы
     * @param item строка данных
     */
    @EnsuresNonNull("this.item")
    public void setItem( @NonNull A item ){
        if( item==null )throw new IllegalArgumentException( "item==null" );
        this.item = item;
    }
    //endregion
    //region rowIdx - индекс строки в списке данных таблицы
    private int rowIdx;

    /**
     * Возвращает индекс строки в списке данных таблицы
     * @return индекс строки в списке данных таблицы
     */
    public int getRowIdx(){
        return rowIdx;
    }

    /**
     * Устанавливает индекс строки в списке данных таблицы
     * @param rowIdx индекс строки в списке данных таблицы
     */
    public void setRowIdx( int rowIdx ){
        this.rowIdx = rowIdx;
    }
    //endregion
    //region columnLocation - расположение колонки таблицы на экране
    private @MonotonicNonNull Rect columnLocation;

    /**
     * Возвращает расположение колонки таблицы на экране
     * @return расположение колонки таблицы на экране
     */
    public @NonNull Rect getColumnLocation(){
        if( columnLocation==null )throw new IllegalStateException("columnLocation==null");
        return columnLocation;
    }

    /**
     * Устанавливает расположение колонки таблицы на экране
     * @param columnLocation расположение колонки таблицы на экране
     */
    @EnsuresNonNull("this.columnLocation")
    public void setColumnLocation( @NonNull Rect columnLocation ){
        if( columnLocation==null )throw new IllegalArgumentException( "columnLocation==null" );
        this.columnLocation = columnLocation;
    }
    //endregion
    //region contentLocation - расположение области данных таблицы на экране
    private @MonotonicNonNull Rect contentLocation;

    /**
     * Возвращает расположение области данных таблицы на экране
     * @return расположение данных на экране
     */
    public @NonNull Rect getContentLocation(){
        if( contentLocation==null )throw new IllegalStateException("contentLocation==null");
        return contentLocation;
    }

    /**
     * Устанавливает расположение области данных таблицы на экране
     * @param contentLocation расположение данных на экране
     */
    @EnsuresNonNull("this.contentLocation")
    public void setContentLocation( @NonNull Rect contentLocation ){
        if( contentLocation==null )throw new IllegalArgumentException( "contentLocation==null" );
        this.contentLocation = contentLocation;
    }
    //endregion
    //region y - расположение ячейки по вертикали
    private int y;

    /**
     * Возвращает расположение ячейки по вертикали
     * @return расположение ячейки по вертикали
     */
    public int getY(){
        return y;
    }

    /**
     * Устанавливает расположение ячейки по вертикали
     * @param y расположение ячейки по вертикали
     */
    public void setY( int y ){
        this.y = y;
    }
    //endregion
    //region foreground - цвет текста ячейки
    private @MonotonicNonNull TextColor foreground;

    /**
     * Возвращает цвет текста ячейки
     * @return цвет текста ячейки
     */
    public @NonNull TextColor getForeground(){
        if( foreground==null )throw new IllegalStateException("foreground==null");
        return foreground;
    }

    /**
     * Устанавливает цвет текста ячейки
     * @param foreground цвет текста ячейки
     */
    @EnsuresNonNull("this.foreground")
    public void setForeground( @NonNull TextColor foreground ){
        if( foreground==null )throw new IllegalArgumentException( "foreground==null" );
        this.foreground = foreground;
    }
    //endregion
    //region background - цвет фона ячейки
    private @MonotonicNonNull TextColor background;

    /**
     * Возвращает цвет фона ячейки
     * @return цвет фона ячейки
     */
    public @NonNull TextColor getBackground(){
        if( background==null )throw new IllegalStateException("background==null");
        return background;
    }

    /**
     * Устанавливает цвет фона ячейки
     * @param background цвет фона ячейки
     */
    @EnsuresNonNull("this.background")
    public void setBackground( @NonNull TextColor background ){
        if( background==null )throw new IllegalArgumentException( "background==null" );
        this.background = background;
    }
    //endregion
    //region rawString - сырое текстовое представление данных
    private @MonotonicNonNull String rawString;

    /**
     * Возвращает сырое текстовое представление данных
     * @return текстовое представление данных
     */
    public @NonNull String getRawString(){
        if( rawString==null )throw new IllegalStateException("rawString==null");
        return rawString;
    }

    /**
     * Устанавливает сырое текстовое представление данных
     * @param rawString текстовое представление данных
     */
    @EnsuresNonNull("this.rawString")
    public void setRawString( @NonNull String rawString ){
        this.rawString = rawString;
    }
    //endregion
    //region column - колонка таблицы
    private @MonotonicNonNull Column<A, ?> column;

    /**
     * Возвращает колонку таблицы
     * @return колонка
     */
    public @NonNull Column<A, ?> getColumn(){
        if( column==null )throw new IllegalStateException("column==null");
        return column;
    }

    /**
     * Устанавливает колонку таблицы
     * @param column колонка
     */
    @EnsuresNonNull("this.column")
    public void setColumn( @NonNull Column<A, ?> column ){
        if( column==null )throw new IllegalArgumentException( "column==null" );
        this.column = column;
    }
    //endregion
    //region trimRaw - удалять (true) или нет (false) пробелы в сырых данных
    private boolean trimRaw;

    /**
     * Возвращает удалять (true) или нет (false) пробелы в сырых данных
     * @return удалять (true) или нет (false)
     * @see CellFormat
     */
    public boolean isTrimRaw(){
        return trimRaw;
    }

    /**
     * Устанавливает удалять (true) или нет (false) пробелы в сырых данных
     * @param trimRaw  удалять (true) или нет (false)
     * @see CellFormat
     */
    public void setTrimRaw( boolean trimRaw ){
        this.trimRaw = trimRaw;
    }
    //endregion
    //region trimLine - удалять (true) или нет (false) пробелы в строке
    private boolean trimLine;

    /**
     * Возвращает удалять или нет пробелы в строке
     * @return удалять (true) или нет (false) пробелы в строке
     * @see CellFormat
     */
    public boolean isTrimLine(){
        return trimLine;
    }

    /**
     * Устанавливает удалять или нет пробелы в строке
     * @param trimLine удалять (true) или нет (false) пробелы в строке
     * @see CellFormat
     */
    public void setTrimLine( boolean trimLine ){
        this.trimLine = trimLine;
    }
    //endregion
    //region halign - выравнивание по горизонтали
    private @MonotonicNonNull Align halign;

    /**
     * Возвращает выравнивание по горизонтали
     * @return выравнивание по горизонтали
     */
    public @NonNull Align getHalign(){
        if( halign==null )throw new IllegalStateException( "halign==null" );
        return halign;
    }

    /**
     * Устанавливает выравнивание по горизонтали
     * @param halign выравнивание по горизонтали
     */
    @EnsuresNonNull("this.halign")
    public void setHalign( @NonNull Align halign ){
        if( halign==null )throw new IllegalArgumentException( "halign==null" );
        this.halign = halign;
    }
    //endregion
    //region selected - флаг выделена ли строка или нет
    private boolean selected;

    /**
     * Возвращает флаг выделена ли строка или нет
     * @return true - выделена
     */
    public boolean isSelected(){
        return selected;
    }

    /**
     * Устанавливает флаг выделена ли строка или нет
     * @param selected true - выделена
     */
    public void setSelected( boolean selected ){
        this.selected = selected;
    }
    //endregion
    //region focus - содержит ли фокус ячейка таблицы
    private boolean focus;

    /**
     * Возвращает флаг - содержит ли фокус ячейка таблицы
     * @return true - содержит
     */
    public boolean isFocus(){
        return focus;
    }

    /**
     * Устанавливает флаг - содержит ли фокус ячейка таблицы
     * @param focus true - содержит
     */
    public void setFocus( boolean focus ){
        this.focus = focus;
    }
    //endregion
    //region table - таблица
    private @MonotonicNonNull Table<A> table;

    /**
     * Возвращает ссылку таблицу
     * @return таблица
     */
    public @NonNull Table<A> getTable(){
        if( table==null )throw new IllegalStateException("table==null");
        return table;
    }

    /**
     * Устанавливает ссылку таблицу
     * @param table таблица
     */
    @EnsuresNonNull("this.table")
    public void setTable( @NonNull Table<A> table ){
        if( table==null )throw new IllegalArgumentException( "table==null" );
        this.table = table;
    }
    //endregion
}
