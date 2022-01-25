package xyz.cofe.jcommander;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.MouseAction;
import xyz.cofe.collection.BasicEventList;
import xyz.cofe.collection.EventList;
import xyz.cofe.fn.Consumer1;
import xyz.cofe.fn.Fn1;
import xyz.cofe.fn.Tuple2;
import xyz.cofe.text.Align;
import xyz.cofe.text.Text;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Табличные данные
 * @param <A> тип строк в таблице
 */
public class Table<A> extends Widget<Table<A>> {
    /**
     * Колонки таблицы
     */
    public class Columns implements Iterable<Column<A,?>> {
        /**
         * Возвращает список колонок в таблице
         * @return список колонок в таблице
         */
        public EventList<Column<A,?>> list(){
            return columns_list;
        }

        /**
         * Добавление колонки в таблицу
         * @param valueMapper функция сопоставления объекта и его значения
         * @param builder конструктор колонки
         * @param <C> тип данных в колонке
         * @return Созданная колонка
         */
        @SuppressWarnings("UnusedReturnValue")
        public <C> Column<A,C> append( Fn1<A,C> valueMapper, Consumer1<Column.Builder<A,C>> builder ){
            if( builder==null )throw new IllegalArgumentException( "builder==null" );
            if( valueMapper==null )throw new IllegalArgumentException( "valueMapper==null" );
            Column.Builder<A,C> bld = new Column.Builder<>();
            bld.value(valueMapper);
            builder.accept(bld);
            var col = bld.build();
            if( col!=null ){
                columns_list.add(col);
            }
            return col;
        }

        /**
         * Возвращает итератор по колонкам
         * @return итератор
         */
        @Override
        public Iterator<Column<A, ?>> iterator(){
            return columns_list.iterator();
        }
    }

    /** Колонки таблицы */
    private EventList<Column<A,?>> columns_list = new BasicEventList<>();

    /**
     * колонки таблицы
     */
    public Columns columns = new Columns();

    /**
     * список значений / данные таблицы
     */
    private EventList<A> values;

    /**
     * Возвращает Данные таблицы (строки)
     * @return Данные таблицы (строки)
     */
    public EventList<A> getValues(){ return values; }

    /**
     * Конструктор таблицы
     * @param values начальные значения таблицы
     */
    public Table( EventList<A> values ){
        if( values==null )throw new IllegalArgumentException( "values==null" );
        this.values = values;

        border.background.bind(background);
        border.foreground.bind(foreground);

        values.onChanged( (idx,old,cur) -> {
            if( old!=null && cur==null ){
                //selection.remove(old);
                setSelected(old,false);
            }
        });
    }

    private int scroll_y = 0;
    private int getScrollY(){ return scroll_y; }
    private int scroll_width() { return getRect().width() - 2;}
    private int getScrollHeight() {
        int h = getRect().height() - 2;
        if( header_visible ){
            h-=1;
        }
        return h;
    }

    private boolean border_visible = true;
    private boolean border_visible(){ return border_visible; }

    //region focused : A - элемент, который содержит фокус
    /**
     * Событие смены сфокусированного элемента
     */
    public final Observer2<Table<A>, Change<A,A>> focusedChanged = new Observer2<>();
    private A focused;

    /**
     * Возвращает элемент, который содержит фокус
     * @return элемент или null
     */
    public A getFocused(){ return focused; }

    /**
     * Возвращает элемент, который содержит фокус
     * @return элемент
     */
    public Optional<A> focused(){
        A itm = getFocused();
        return itm!=null ? Optional.of(itm) : Optional.empty();
    }

    /**
     * Указывает элемент, который должен содержать фокус
     * @param focused элемент или null
     */
    public void setFocused( A focused ){
        var old = this.focused;
        this.focused = focused;
        focusedChanged.fire(this, Change.fromTo(old,focused));
    }

    /**
     * Указывает элемент, который должен содержать фокус
     * @param itm элемент или null
     */
    public Table<A> focused( A itm ){
        setFocused(itm);
        return this;
    }

    /**
     * Указывает элемент, который должен содержать фокус
     * @param itm элемент
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Table<A> focused( Optional<A> itm ){
        setFocused(itm.isEmpty() ? null : itm.get());
        return this;
    }
    //endregion

    //region visibleItemsRange() - диапазон видимых строк
    /**
     * Возвращает диапазон видимых строк
     * @return кортеж: (начало включительно - индекс сроки; конец исключительно - индекс строки)
     */
    public Optional<Tuple2<Integer,Integer>> visibleItemsRange(){
        int scl_h = getScrollHeight();
        if( scl_h<1 )return Optional.empty();
        return Optional.of(Tuple2.of(getScrollY(), getScrollY()+scl_h));
    }
    //endregion

    /**
     * Скорллинг до указанной строки
     * @param visibleRow индекс строки
     */
    public void scrollToRow( int visibleRow ){
        if( visibleRow<0 )throw new IllegalArgumentException( "visibleRow<0" );
        visibleItemsRange().ifPresent( visibleRange -> {
            var from = visibleRange.a();
            var to = visibleRange.b();

            if( visibleRow >= from && visibleRow<to )return;
            if( visibleRow < from ){
                scroll_y = visibleRow;
            }else if( visibleRow >= to ){
                var h = to - from;
                scroll_y = visibleRow - h + 1;
            }
        });
    }

    private final Border border = new Border();

    @Override
    public void render( TextGraphics g ){
        if( g==null )throw new IllegalArgumentException( "g==null" );

        var r = getRect();
        if( r==null )return;
        if( r.width()<=0 || r.height()<=0 )return;

        renderBackground(g,r);

        // Рисование рамки
        if( border_visible() ){
            border.render(g, r);
        }

        renderContent(g);
    }

    //region Цвета
    /** Цвет текста по умолчанию */
    public final SimpleProperty<TextColor> foreground = new SimpleProperty<>(TextColor.ANSI.WHITE_BRIGHT);

    /** Цвет фона по умолчанию */
    public final SimpleProperty<TextColor> background = new SimpleProperty<>(TextColor.ANSI.BLUE);

    /** Цвет текста строки содержащая фокус */
    public final SimpleProperty<TextColor> focusedForeground = new SimpleProperty<>(TextColor.ANSI.BLUE);

    /** Цвет фона строки содержащая фокус */
    public final SimpleProperty<TextColor> focusedBackground = new SimpleProperty<>(TextColor.ANSI.WHITE_BRIGHT);

    /** Цвет текста заголовка колонки */
    public final SimpleProperty<TextColor> headerForeground = new SimpleProperty<>(TextColor.ANSI.YELLOW_BRIGHT);

    /** Цвет текста выделенной/выбранной строки */
    public final SimpleProperty<TextColor> selectedForeground = new SimpleProperty<>(TextColor.ANSI.RED_BRIGHT);

    /** Цвет фона выделенной/выбранной строки */
    public final SimpleProperty<TextColor> selectedBackground = new SimpleProperty<>(TextColor.ANSI.BLUE);

    /** Цвет текста выделенной/выбранной + фокус строки */
    public final SimpleProperty<TextColor> selectedFocusForeground = new SimpleProperty<>(TextColor.ANSI.RED_BRIGHT);

    /** Цвет фона выделенной/выбранной + фокус строки */
    public final SimpleProperty<TextColor> selectedFocusBackground = new SimpleProperty<>(TextColor.ANSI.WHITE_BRIGHT);
    //endregion

    /**
     * Указывает способ автоматического изменения размера колонок
     */
    public final SimpleProperty<AutoResizeColumns> autoResizeColumns = new SimpleProperty<>(AutoResizeColumns.Sequence);

    private boolean header_visible = true;

    /**
     * Расчет области которая содержит основные данные
     * @return расположение области данных на экране
     */
    public Optional<Rect> contentLocation(){
        Rect r = getRect();
        if( r==null )return Optional.empty();
        if( r.width()<1 || r.height()<1 )return Optional.empty();

        int content_x0 = r.left();
        int content_x1 = r.left() + r.width();
        int content_y0 = r.top();
        int content_y1 = r.top() + r.height();
        if( border_visible() ){
            content_x0++;
            content_x1--;
            content_y0++;
            content_y1--;
        }

        if( header_visible ){
            content_y0++;
        }

        int y_diff = content_y1 - content_y0;
        if( y_diff<1 )return Optional.empty();

        int x_diff = content_x1 - content_x0;
        if( x_diff<1 )return Optional.empty();

        return Optional.of(Rect.create(content_x0, content_y0, content_x1, content_y1));
    }

    /**
     * Расчет расположения областей колонок с данными.
     * Отсекает колонки которые не видимы.
     * Усекает ширину последней колонки до видимой области.
     * @return расположение колонок с данным
     */
    protected List<ColumnLocation<A>> columnLocations(){
        ArrayList<ColumnLocation<A>> cols = new ArrayList<>();

        var contentLoc = contentLocation();
        if( contentLoc.isEmpty() )return cols;

        var cl = contentLoc.get();

        var x_out = cl.left();
        for( var col_idx = 0; col_idx < columns_list.size(); col_idx++ ){
            var col = columns_list.get(col_idx);
            int colSize = col.getColumnWidth().getSize();

            int x0 = x_out;
            int x1 = x_out+colSize;
            if( !cl.include(x0,cl.top()) )break;
            if( !cl.include(x1,cl.top()) ){
                x1 = cl.right();
            }

            Rect r = Rect.create(x0, cl.top(), x1, cl.bottom());
            cols.add( new ColumnLocation<>(col, r));

            x_out += colSize + 1;
        }

        return cols;
    }

    private void renderBackground( TextGraphics g, Rect r ){
        int x0 = r.left();
        int y0 = r.top();
        int x1 = r.left() + r.width() - 1;
        int y1 = r.top() + r.height() - 1;

        g.setForegroundColor(foreground.get());
        g.setBackgroundColor(background.get());
        for( int y=y0; y<y1; y++ ){
            g.putString(x0,y," ".repeat(x1-x0));
        }
    }
    private void renderVerticalLines( TextGraphics g ){
        var colLocs = columnLocations();
        for( var ci=0; ci<colLocs.size()-1; ci++ ){
            var cl = colLocs.get(ci);
            if( border_visible() ){
                int header = cl.location.top()-1-(header_visible ? 1 : 0);
                int footer = cl.location.bottom();

                border.charOf(Border.Symbol.Vertical_Line).ifPresent( vert -> {
                    for( int y = header; y < footer; y++ ){
                        g.setForegroundColor(border.foreground.get());
                        g.setBackgroundColor(border.background.get());

                        g.putString(cl.location.right(), y, ""+vert);
                    }
                });

                border.charOf(Border.Symbol.Horizontal2Down_T).ifPresent( hor2dwn -> {
                    border.charOf(Border.Symbol.Horizontal2Up_T).ifPresent( hor2up -> {

                        g.setForegroundColor(border.foreground.get());
                        g.setBackgroundColor(border.background.get());

                        g.putString(cl.location.right(), header, "" + hor2dwn);
                        g.putString(cl.location.right(), footer, "" + hor2up);
                    });
                });
            }
        }
    }

    private String[] cellText( String rawString, int cellWidth, int cellHeight, boolean trimRaw, boolean trimLine, Align halign ){
        if( rawString==null )throw new IllegalArgumentException( "rawString==null" );
        if( cellHeight<0 )throw new IllegalArgumentException( "cellHeight<0" );
        if( cellWidth<0 )throw new IllegalArgumentException( "cellWidth<0" );
        if( halign==null )throw new IllegalArgumentException( "halign==null" );

        if( cellHeight==0 ){
            return new String[] {};
        }
        if( cellWidth==0 ){
            var lines = new String[cellHeight];
            Arrays.fill(lines, "");
            return lines;
        }

        rawString = trimRaw ? rawString.trim() : rawString;
        String[] lines = Text.splitNewLines(rawString);
        if( lines.length<cellHeight ){
            int diff = cellHeight-lines.length;
            lines = Arrays.copyOf(lines,lines.length+diff);
            for( int i= lines.length-diff;i<lines.length;i++ ){
                lines[i] = "";
            }
        }else if( lines.length>cellHeight ){
            lines = Arrays.copyOf(lines,cellHeight);
        }

        for( int i=0;i<lines.length;i++ ){
            var line = lines[i];
            int s0 = line.length();

            if( trimLine ){
                line = line.trim();
            }

            if( line.length()>cellWidth ){
                line = line.substring(0,cellWidth);
            }

            line = Text.align(line, halign, " ", cellWidth);
            lines[i] = line;
        }

        return lines;
    }

    private final List<Consumer1<CellFormat<A>>> cellFormatters = new ArrayList<>();

    /**
     * Добавляет форматирование ячеек таблицы
     * @param fmt функция форматирования ячейки
     * @return удаление функции форматирования
     * @see CellFormat
     */
    public Runnable addFormatter(Consumer1<CellFormat<A>> fmt){
        if( fmt==null )throw new IllegalArgumentException( "fmt==null" );

        cellFormatters.add(fmt);

        var ref = new WeakReference<>(fmt);
        return ()->{
            var r = ref.get();
            if( r==null )return;
            cellFormatters.removeAll(List.of(r));
        };
    }

    private void renderContent( TextGraphics g ){
        autoResizeColumns.get().resizer.accept(this);

        if( border_visible() ){
            renderHeaders(g);
        }

        // Рисование перемычек на рамке
        renderVerticalLines(g);

        var colLocs = columnLocations();
        var contLoc = contentLocation();
        if( contLoc.isEmpty() || contLoc.get().width()<1 || contLoc.get().height()<1 )return;
        if( values==null )return;

        // Рисование значений
        int y_out = contLoc.get().top();
        for( var rowIdx = getScrollY(); rowIdx< getScrollY()+getScrollHeight(); rowIdx++ ){
            if( rowIdx>=values.size() )continue;

            var item = values.get(rowIdx);
            var colIdx = -1;
            for( var col : colLocs ){
                colIdx++;

                var cf = new CellFormat<A>();

                var raw_string = col.column.text(item);
                cf.setRawString(raw_string);

                var fc = foreground.get();
                var bc = background.get();
                var hasFocus = false;

                if( focused!=null && focused==item ){
                    fc = focusedForeground.get();
                    bc = focusedBackground.get();
                    hasFocus = true;
                    cf.setFocus(true);
                }else{
                    cf.setFocus(false);
                }

                var f_fc = fc;
                var f_bc = bc;

                if( isSelected(item) ){
                    if( hasFocus ){
                        fc = selectedFocusForeground.get();
                        bc = selectedFocusBackground.get();
                    }else{
                        fc = selectedForeground.get();
                        bc = selectedBackground.get();
                    }
                    cf.setSelected(true);
                }else{
                    cf.setSelected(false);
                }

                cf.setItem(item);
                cf.setColumn(col.column);
                cf.setBackground(bc);
                cf.setForeground(fc);
                cf.setY(y_out);
                cf.setContentLocation(contLoc.get());
                cf.setColumnLocation(col.location);
                cf.setRowIdx(rowIdx);
                cf.setTable(this);
                cf.setTrimRaw(false);
                cf.setTrimLine(false);
                cf.setHalign(Align.Begin);

                for( var fmt : cellFormatters ){
                    fmt.accept(cf);
                }

                g.setForegroundColor(cf.getForeground());
                g.setBackgroundColor(cf.getBackground());

                var render_string =
                    cellText(
                        raw_string!=null ? raw_string : "",
                        col.location.width(),
                        1,
                        cf.isTrimRaw(),cf.isTrimLine(),
                        cf.getHalign());

                g.putString(col.location.left(), y_out, render_string[0]);

                if( colIdx<colLocs.size()-1 ){
                    var f_y = y_out;
                    border.charOf(Border.Symbol.Vertical_Line).ifPresent( vert -> {
                        g.setForegroundColor(f_fc);
                        g.setBackgroundColor(f_bc);
                        g.putString(col.location.right(), f_y, ""+vert);
                    });
                }
            }

            y_out++;
        }
    }
    private void renderHeaders( TextGraphics g ){
        g.setForegroundColor(headerForeground.get());
        g.setBackgroundColor(background.get());

        var colLocs = columnLocations();
        for( var colLoc : colLocs ){
            var name = colLoc.column.getName();
            if( name.length()<1 )continue;

            var name_lines = cellText(name,colLoc.location.width(),1,true,true, Align.Center);
            if( name_lines.length>0 ){
                var render_name = name_lines[0];

                g.putString(colLoc.location.left(), colLoc.location.top()-1, render_name);
            }
        }
    }

    @Override
    public void input( KeyStroke ks ){
        if( ks==null )throw new IllegalArgumentException( "ks==null" );

        boolean processed = false;
        switch( ks.getKeyType() ){
            case ArrowDown: focusNext(); processed=true; break;
            case ArrowUp: focusPrevious(); processed=true; break;
            case PageDown: focusPageNext(); processed=true; break;
            case PageUp: focusPagePrevious(); processed=true; break;
        }
        if( processed )return;

        if( !ks.isCtrlDown() && !ks.isAltDown() && !ks.isShiftDown() ){
            if( ks.getCharacter()!=null ){
                switch( ks.getCharacter() ){
                    case ' ':
                        invertSelectionOfFocused();
                        processed = true;
                        break;
                }
            }
        }
        if( processed )return;

        if( ks instanceof MouseAction ){
            var ma = (MouseAction)ks;
            switch( ma.getActionType() ){
                case CLICK_DOWN:
                    if( ma.getButton()==1 && !ma.isCtrlDown() && !ma.isAltDown() && !ma.isShiftDown() ){
                        rowIndexOf(ma.getPosition()).ifPresent( idx -> {
                            if( values==null || values.isEmpty() )return;
                            if( idx>=values.size() )idx = values.size()-1;
                            setFocused(values.get(idx));
                        });
                    }
                    if( ma.getButton()==1 && ma.isCtrlDown() && !ma.isAltDown() && !ma.isShiftDown() ){
                        itemOf(ma.getPosition()).ifPresent( itm -> {
                            setSelected(itm, !isSelected(itm));
                        });
                    }
                    break;
            }
        }
    }

    private Optional<Integer> rowIndexOf( TerminalPosition pos ){
        var contentLoc = contentLocation();
        if( contentLoc.isEmpty() )return Optional.empty();

        var contentRect = contentLoc.get();
        if( !contentRect.include(pos) )return Optional.empty();

        int row = pos.getRow() - contentRect.top();
        int scrl_y = getScrollY();

        return Optional.of(row + scrl_y);
    }
    private Optional<A> itemOf( TerminalPosition pos ){
        var row = rowIndexOf(pos);
        if( row.isEmpty() )return Optional.empty();

        if( values==null || values.isEmpty() )return Optional.empty();

        var ri = row.get();
        if( ri>=values.size() )return Optional.empty();

        return Optional.of(values.get(ri));
    }

    private boolean check_focus_null(){
        if( getFocused()==null ){
            if( values==null || values.isEmpty() )return false;
            setFocused(values.get(0));
            scroll_y = 0;
            return true;
        }
        return false;
    }

    /**
     * Смена фокус на следующую строку
     */
    public void focusNext(){
        if( check_focus_null() )return;

        if( values==null )return;
        int idx = values.indexOf(getFocused());
        if( idx<0 ){
            if( values.isEmpty() )return;
            idx = scroll_y;
            if( idx>=values.size() ){
                idx = values.size()-1;
            }
            setFocused(values.get(idx));
            scroll_y = idx;
            return;
        }

        if( idx<(values.size()-1) ){
            setFocused(values.get(idx+1));
            int focused_idx = idx+1;
            scrollToRow(focused_idx);
        }
    }

    /**
     * Смена фокус на предыдущую строку
     */
    public void focusPrevious(){
        if( check_focus_null() )return;
        if( values==null )return;
        int idx = values.indexOf(getFocused());

        if( idx<0 ){
            if( values.isEmpty() )return;
            idx = scroll_y;
            if( idx>=values.size() ){
                idx = values.size()-1;
            }
            setFocused(values.get(idx));
            scroll_y = idx;
            return;
        }

        if( idx>0 ){
            setFocused(values.get(idx-1));
            int focused_idx = idx-1;
            scrollToRow(focused_idx);
        }
    }

    /**
     * Смена фокуса на одну "страницу" вниз
     */
    public void focusPageNext(){
        if( check_focus_null() )return;
        if( values==null || values.isEmpty() )return;

        int idx = values.indexOf(getFocused());

        var scrl = contentLocation();
        if( scrl.isEmpty() || scrl.get().height()<1 )return;

        idx += scrl.get().height();
        if( idx>values.size()-1 )idx = values.size()-1;

        setFocused(values.get(idx));
        scrollToRow(idx);
    }

    /**
     * Смена фокуса на одну "страницу" вверх
     */
    public void focusPagePrevious(){
        if( check_focus_null() )return;
        if( values==null || values.isEmpty() )return;

        int idx = values.indexOf(getFocused());

        var scrl = contentLocation();
        if( scrl.isEmpty() || scrl.get().height()<1 )return;

        idx -= scrl.get().height();
        if( idx<1 )idx = 0;

        setFocused(values.get(idx));
        scrollToRow(idx);
    }

    private final Set<A> selection = new HashSet<>();

    /**
     * Возвращает копию выделенных строк
     * @return копия выделенных строк
     */
    public Set<A> getSelection(){
        return new HashSet<>(selection);
    }

    /**
     * Проверка, что указанная строка выделена
     * @param item строка таблицы
     * @return true - выделена
     */
    public boolean isSelected(A item){
        return selection.contains(item);
    }

    public final Observer2<Table<A>, A> unselectedItem = new Observer2<>();
    public final Observer2<Table<A>, A> selectedItem = new Observer2<>();

    /**
     * Выделение указанной строки
     * @param item строка таблицы
     * @param selected true - выделить или снять выделение
     */
    public void setSelected(A item, boolean selected){
        if( item==null )throw new IllegalArgumentException( "item==null" );
        if( selected ){
            if( selection.add(item) ){
                selectedItem.fire(this,item);
            }
        }else{
            if( selection.remove(item) ){
                unselectedItem.fire(this,item);
            }
        }
    }

    /**
     * Инверсия выделения сфокусированной строки
     */
    public void invertSelectionOfFocused(){
        var f = getFocused();
        if( f==null )return;

        setSelected(f,!isSelected(f));
    }
}
