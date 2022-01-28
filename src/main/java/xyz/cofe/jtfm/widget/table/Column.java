package xyz.cofe.jtfm.widget.table;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.cofe.fn.Fn1;

import java.util.function.Supplier;

/**
 * Колонка таблицы
 * @param <R> Тип строки таблицы
 * @param <V> Тип колонки таблицы
 */
public class Column<R,V> {
    protected Column(){}

    /**
     * Конструктор копирования
     * @param sample образец для копирования
     */
    protected Column(@NonNull Column<R,V> sample){
        //noinspection ConstantConditions
        if( sample==null )throw new IllegalArgumentException( "sample==null" );
        this.name = sample.name;
        this.value = sample.value;
        this.columnWidth = sample.columnWidth != null ? sample.columnWidth.clone() : new ColumnWidth(0,20);
    }

    /**
     * Клонирование
     * @return клон
     */
    public Column<R,V> clone(){
        return new Column<>(this);
    }

    //region name : String - имя колонки
    private @Nullable Supplier<String> name;

    /**
     * Указывает название колонки
     * @param newName название
     */
    //@EnsuresNonNull("name")
    protected void setName(Supplier<String> newName){
        this.name = newName;
    }

    /**
     * Возвращает название колонки
     * @return название колонки, не возвращает null
     */
    public @NonNull String getName(){
        var n = name;
        if( n!=null ){
            var r = n.get();
            return r!=null ? r : "?";
        }
        return "?";
    }
    //endregion
    //region value : Fn1<R,V> - вычисление значение колонки для указанной строки таблицы
    private @Nullable Fn1<R,V> value;

    protected void setValue( Fn1<R,V> value ) {
        this.value = value;
    }

    /**
     * Возвращает значение колонки для указанной строки таблицы
     * @param row строка таблицы
     * @return данные
     */
    public @Nullable V value(R row){
        if( row==null )throw new IllegalArgumentException( "row==null" );
        var v_fn = value;
        if( v_fn==null )return null;

        return v_fn.apply(row);
    }
    //endregion
    //region text() -  Получение текстового представления данных
    /**
     * Получение текстового представления колонки для строки таблицы
     * @param row строка таблицы
     * @return текстовое представление
     */
    public @NonNull String text(@NonNull R row){
        //noinspection ConstantConditions
        if( row==null )throw new IllegalArgumentException( "row==null" );
        var value = value(row);
        if( value==null ){
            return "?";
        }
        return value.toString();
    }
    //endregion
    //region columnWidth - Ширина колонки
    private ColumnWidth columnWidth = new ColumnWidth(0,20);

    /**
     * Возвращает ширину колонки
     * @return ширина колонки
     */
    public @NonNull ColumnWidth getColumnWidth(){
        return columnWidth;
    }

    /**
     * Указывает ширину колонки
     * @param cw ширина колонки
     */
    protected void setColumnWidth(ColumnWidth cw){
        if( cw==null )throw new IllegalArgumentException( "cw==null" );
        columnWidth = cw;
    }
    //endregion
    //region Builder
    /**
     * Конструктор колонки
     * @param <T> тип значения таблицы
     * @param <C> тип значения колонки
     */
    public static class Builder<T,C> {
        private Supplier<String> name = () -> "?";

        /**
         * Возвращает имя колонки
         * @return имя колонки
         */
        public String name(){
            return name!=null ? name.get() : "?";
        }

        /**
         * Указывает имя колонки
         * @param name имя колонки
         * @return SELF ссылка
         */
        @SuppressWarnings("UnusedReturnValue")
        @EnsuresNonNull("this.name")
        public Builder<T,C> name( @NonNull String name ){
            this.name = ()->name;
            return this;
        }

        private @MonotonicNonNull Fn1<T,C> value;

        /**
         * Возвращает функцию получения значения колонки
         * @return функция или null
         */
        public @Nullable Fn1<T,C> value(){ return value; }

        /**
         * Указывает функцию получения значения колонки
         * @param value функция или null
         * @return SELF ссылка
         */
        @SuppressWarnings("UnusedReturnValue")
        @EnsuresNonNull("this.value")
        public Builder<T,C> value( Fn1<T,C> value ){
            this.value = value;
            return this;
        }

        private int width=10;
        private int minWidth = 3;
        private int maxWidth = 20;
        private boolean fixed = false;

        /**
         * Указывает текущую ширину колонки
         * @param w ширина колонки
         * @return SELF ссылка
         */
        public Builder<T,C> width(int w){
            if( w<0 )throw new IllegalArgumentException( "w<0" );
            width = w;
            return this;
        }

        /**
         * Указывает минимальную/максимальную ширину колонки
         * @param minWidth минимальная ширина
         * @param maxWidth максимальная ширина
         * @return SELF ссылка
         */
        public Builder<T,C> minMaxWidth(int minWidth, int maxWidth){
            if( minWidth<0 )throw new IllegalArgumentException( "minWidth<0" );
            if( maxWidth<0 )throw new IllegalArgumentException( "maxWidth<0" );
            if( minWidth>maxWidth )throw new IllegalArgumentException( "minWidth>maxWidth" );
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            return this;
        }

        /**
         * Указывает фиксирована ли ширина колонки
         * @param fix true - фиксирована, false - переменная ширина
         * @return SELF ссылка
         */
        public Builder<T,C> fixed(boolean fix) {
            fixed = fix;
            return this;
        }

        /**
         * Создание колонки
         * @return колонка
         */
        public Column<T,C> build(){
            var col = new Column<T,C>();
            col.setName(name);
            //noinspection ReplaceNullCheck
            if( value!=null ){
                col.setValue(value);
            }else {
                col.setValue((itm)->null);
            }
            col.setColumnWidth(new ColumnWidth(minWidth, maxWidth, width, fixed));
            return col;
        }
    }
    //endregion
}
