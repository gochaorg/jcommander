package xyz.cofe.jcommander.table;

/**
 * Ширина колонки
 */
public class ColumnWidth {
    //region Конструктор
    /**
     * Фиксированная ширина колонки
     * @param fixedSize ширина колонки
     */
    public ColumnWidth(int fixedSize){
        if( fixedSize<0 )throw new IllegalArgumentException( "fixedSize<0" );
        fixed = true;
        minSize = fixedSize;
        maxSize = fixedSize;
        size = fixedSize;
    }

    /**
     * Изменяемая ширина колонки, начальное значение max
     * @param minSize Минимальная ширина
     * @param maxSize Максимальная ширина
     */
    public ColumnWidth(int minSize, int maxSize){
        if( minSize<0 )throw new IllegalArgumentException( "minSize<0" );
        if( maxSize<0 )throw new IllegalArgumentException( "maxSize<0" );
        if( minSize>maxSize )throw new IllegalArgumentException( "minSize>maxSize" );
        fixed = false;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.size = maxSize;
    }

    /**
     * Изменяемая ширина колонки
     * @param minSize Минимальная ширина
     * @param maxSize Максимальная ширина
     * @param curSize Текущая ширина
     */
    public ColumnWidth(int minSize, int maxSize, int curSize){
        if( minSize<0 )throw new IllegalArgumentException( "minSize<0" );
        if( maxSize<0 )throw new IllegalArgumentException( "maxSize<0" );
        if( minSize>maxSize )throw new IllegalArgumentException( "minSize>maxSize" );
        if( curSize<minSize )throw new IllegalArgumentException( "curSize<minSize" );
        if( curSize>maxSize )throw new IllegalArgumentException( "curSize>maxSize" );
        fixed = false;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.size = curSize;
    }

    /**
     * Конструктор
     * @param minSize Минимальная ширина
     * @param maxSize Максимальная ширина
     * @param curSize Текущая ширина
     * @param fixed true - изменяемая ширина, false - не изменяемая
     */
    public ColumnWidth(int minSize, int maxSize, int curSize, boolean fixed){
        if( minSize<0 )throw new IllegalArgumentException( "minSize<0" );
        if( maxSize<0 )throw new IllegalArgumentException( "maxSize<0" );
        if( minSize>maxSize )throw new IllegalArgumentException( "minSize>maxSize" );
        if( curSize<minSize )throw new IllegalArgumentException( "curSize<minSize" );
        if( curSize>maxSize )throw new IllegalArgumentException( "curSize>maxSize" );
        this.fixed = fixed;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.size = curSize;
    }

    /**
     * Конструктор копирования
     * @param sample образец для копирования
     */
    public ColumnWidth( ColumnWidth sample ){
        if( sample==null )throw new IllegalArgumentException( "sample==null" );
        this.fixed = sample.fixed;
        this.minSize = sample.minSize;
        this.maxSize = sample.maxSize;
        this.size = sample.size;
    }

    /**
     * Создание клона
     * @return клон
     */
    public ColumnWidth clone(){
        return new ColumnWidth(this);
    }
    //endregion

    //region fixed : boolean - размер колонки фиксированный
    private boolean fixed = false;

    /**
     * Возвращает фиксированный размер или нет
     * @return true - размер колонки фиксированный
     */
    public boolean isFixed(){
        return fixed;
    }

    /**
     * Указывает фиксированный размер или нет
     * @param fixed true - размер колонки фиксированный
     */
    private void setFixed(boolean fixed){
        this.fixed = fixed;
    }
    //endregion
    //region minSize : int - Минимальный размер
    /**
     * Минимальный размер
     */
    private int minSize;

    /**
     * Возвращает минимальный размер
     * @return минимальный размер
     */
    public int getMinSize(){
        return minSize;
    }

    /**
     * Указывает минимальный размер
     * @param minSize минимальный размер
     */
    private void setMinSize( int minSize ){
        this.minSize = minSize;
    }
    //endregion
    //region maxSize : int - максимальный размер
    private int maxSize;

    /**
     * Возвращает максимальный размер
     * @return максимальный размер
     */
    public int getMaxSize(){
        return maxSize;
    }

    /**
     * Указывает максимальный размер
     * @param maxSize максимальный размер
     */
    private void setMaxSize( int maxSize ){
        this.maxSize = maxSize;
    }
    //endregion
    //region size : int - Текущий размер
    /**
     * Текущий размер
     */
    private int size;

    /**
     * Возвращает текущий размер колонки
     * @return размер колонки
     */
    public int getSize(){
        return size;
    }

    /**
     * Указывает текущий размер колонки
     * @param size размер колонки
     */
    public void setSize( int size ){
        if( size<0 )throw new IllegalArgumentException( "size<0" );
        if( size<minSize )throw new IllegalArgumentException( "size<minSize" );
        if( size>maxSize )throw new IllegalArgumentException( "size>maxSize" );
        if( fixed )throw new IllegalStateException("fixed");
        this.size = size;
    }
    //endregion
}
