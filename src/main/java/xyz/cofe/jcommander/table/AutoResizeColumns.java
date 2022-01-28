package xyz.cofe.jcommander.table;

import xyz.cofe.jcommander.Rect;

import java.util.function.Consumer;

/**
 * Способ автоматического изменения размера колонок
 */
public enum AutoResizeColumns {
    /**
     * Нет автоматического изменения колонок
     */
    None(AutoResizeColumns::none),

    /**
     * Изменяет размер колонок так, что бы заполнить все доступное пространство, чтоб влезли все колонки.
     *
     * <ul>
     *   <li> Изменяет колонки последовательно, от первой к последней.
     *   <li> Пропускает колонки {@link ColumnWidth#isFixed()}
     *   <li> Изменяет ширину колонки {@link ColumnWidth#setSize(int)} в пределах допустимого, от {@link ColumnWidth#getMinSize()} до {@link ColumnWidth#getMaxSize()}
     * </ul>
     */
    Sequence(AutoResizeColumns::sequence)
    ;

    /**
     * Функция изменения ширины колонки
     */
    public final Consumer<Table<?>> resizer;

    AutoResizeColumns(Consumer<Table<?>> resizer){
        if( resizer==null )throw new IllegalArgumentException( "resizer==null" );
        this.resizer = resizer;
    }

    private static void none(Table<?> table){
        if( table==null )throw new IllegalArgumentException( "table==null" );
    }

    private static void sequence(Table<?> table){
        if( table==null )throw new IllegalArgumentException( "table==null" );

        var contentLoc = table.contentLocation();
        if( contentLoc.isEmpty() )return;

        var cntLoc = contentLoc.get();
        int columnDelimWidth = 1;
        int totalWidth = 0;
        int totalMinWidth = 0;
        int totalMaxWidth = 0;

        int colIdx = -1;
        for( var col : table.getColumns() ){
            colIdx++;
            if( colIdx>0 ){
                totalWidth += columnDelimWidth;
                totalMaxWidth += columnDelimWidth;
                totalMinWidth += columnDelimWidth;
            }

            totalWidth += col.getColumnWidth().getSize();
            totalMinWidth += col.getColumnWidth().getMinSize();
            totalMaxWidth += col.getColumnWidth().getMaxSize();
        }

        if( cntLoc.width()>totalWidth ){
            // extends
            sequence_extends(table,cntLoc,totalWidth);
        }else if( cntLoc.width()<totalWidth ){
            // reduce
            sequence_reduce(table,cntLoc,totalWidth);
        }
    }

    private static void sequence_extends( Table<?> table, Rect cntLoc, int currentWidth) {
        int extds_size = Math.abs(currentWidth-cntLoc.width());
        for( var col : table.getColumns() ){
            if( extds_size<=0 )return;
            if( col.getColumnWidth().isFixed() )continue;

            int free = col.getColumnWidth().getMaxSize() - col.getColumnWidth().getSize();
            if( free<1 )continue;

            int ext = Math.min(extds_size, free);
            col.getColumnWidth().setSize( col.getColumnWidth().getSize()+ext );
            extds_size -= ext;
        }
    }

    private static void sequence_reduce(Table<?> table, Rect cntLoc, int currentWidth) {
        int reduce_size = Math.abs(currentWidth-cntLoc.width());
        for( var col : table.getColumns() ){
            if( reduce_size<=0 )break;
            if( col.getColumnWidth().isFixed() )continue;

            int free = col.getColumnWidth().getMinSize() - col.getColumnWidth().getSize();
            if( free<1 )continue;

            int rdc = Math.min(reduce_size, free);
            col.getColumnWidth().setSize( col.getColumnWidth().getSize()-rdc );
            reduce_size -= rdc;
        }
    }
}
