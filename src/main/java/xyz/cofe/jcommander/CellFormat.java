package xyz.cofe.jcommander;

import com.googlecode.lanterna.TextColor;
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
public class CellFormat<A> {
    //region item - строка данных
    private A item;

    /**
     * Возвращает строку данных таблицы
     * @return строка данных
     */
    public A getItem(){
        return item;
    }

    /**
     * Устанавливает строку данных таблицы
     * @param item строка данных
     */
    public void setItem( A item ){
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
    private Rect columnLocation;

    /**
     * Возвращает расположение колонки таблицы на экране
     * @return расположение колонки таблицы на экране
     */
    public Rect getColumnLocation(){
        return columnLocation;
    }

    /**
     * Устанавливает расположение колонки таблицы на экране
     * @param columnLocation расположение колонки таблицы на экране
     */
    public void setColumnLocation( Rect columnLocation ){
        if( columnLocation==null )throw new IllegalArgumentException( "columnLocation==null" );
        this.columnLocation = columnLocation;
    }
    //endregion
    //region contentLocation - расположение области данных таблицы на экране
    private Rect contentLocation;

    /**
     * Возвращает расположение области данных таблицы на экране
     * @return расположение данных на экране
     */
    public Rect getContentLocation(){
        return contentLocation;
    }

    /**
     * Устанавливает расположение области данных таблицы на экране
     * @param contentLocation расположение данных на экране
     */
    public void setContentLocation( Rect contentLocation ){
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
    private TextColor foreground;

    /**
     * Возвращает цвет текста ячейки
     * @return цвет текста ячейки
     */
    public TextColor getForeground(){
        return foreground;
    }

    /**
     * Устанавливает цвет текста ячейки
     * @param foreground цвет текста ячейки
     */
    public void setForeground( TextColor foreground ){
        if( foreground==null )throw new IllegalArgumentException( "foreground==null" );
        this.foreground = foreground;
    }
    //endregion
    //region background - цвет фона ячейки
    private TextColor background;

    /**
     * Возвращает цвет фона ячейки
     * @return цвет фона ячейки
     */
    public TextColor getBackground(){
        return background;
    }

    /**
     * Устанавливает цвет фона ячейки
     * @param background цвет фона ячейки
     */
    public void setBackground( TextColor background ){
        if( background==null )throw new IllegalArgumentException( "background==null" );
        this.background = background;
    }
    //endregion
    //region rawString - сырое текстовое представление данных
    private String rawString;

    /**
     * Возвращает сырое текстовое представление данных
     * @return текстовое представление данных
     */
    public String getRawString(){
        return rawString;
    }

    /**
     * Устанавливает сырое текстовое представление данных
     * @param rawString текстовое представление данных
     */
    public void setRawString( String rawString ){
        this.rawString = rawString;
    }
    //endregion
    //region column - колонка таблицы
    private Column<A, ?> column;

    /**
     * Возвращает колонку таблицы
     * @return колонка
     */
    public Column<A, ?> getColumn(){
        return column;
    }

    /**
     * Устанавливает колонку таблицы
     * @param column колонка
     */
    public void setColumn( Column<A, ?> column ){
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
    private Align halign;

    /**
     * Возвращает выравнивание по горизонтали
     * @return выравнивание по горизонтали
     */
    public Align getHalign(){
        return halign;
    }

    /**
     * Устанавливает выравнивание по горизонтали
     * @param halign выравнивание по горизонтали
     */
    public void setHalign( Align halign ){
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
    private Table<A> table;

    /**
     * Возвращает ссылку таблицу
     * @return таблица
     */
    public Table<A> getTable(){
        return table;
    }

    /**
     * Устанавливает ссылку таблицу
     * @param table таблица
     */
    public void setTable( Table<A> table ){
        if( table==null )throw new IllegalArgumentException( "table==null" );
        this.table = table;
    }
    //endregion
}
