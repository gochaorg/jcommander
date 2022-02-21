package xyz.cofe.jtfm.widget.table;

import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.cofe.collection.BasicEventList;
import xyz.cofe.collection.CollectionEvent;
import xyz.cofe.collection.EventList;
import xyz.cofe.fn.Consumer1;
import xyz.cofe.fn.Fn1;
import xyz.cofe.fn.Pair;
import xyz.cofe.fn.TripleConsumer;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Колонка таблицы
 * @param <A> тип значения в таблице
 */
public class TableColumns<A> implements EventList<Column<A,?>> {
    private EventList<Column<A,?>> columns_list;

    public TableColumns(){
         columns_list = new BasicEventList<>();
    }

    //region scn
    @Override
    public Long scn(){
        return columns_list.scn();
    }

    @Override
    public Pair<Long, Long> nextscn(){
        return columns_list.nextscn();
    }

    @Override
    public AutoCloseable onScn( TripleConsumer<Long, Long, CollectionEvent<EventList<Column<A, ?>>, Column<A, ?>>> listener ){
        return columns_list.onScn(listener);
    }
    //endregion
    //region EventList
    @Override
    public List<Column<A, ?>> target(){
        return columns_list.target();
    }

    @Override
    public AutoCloseable onInserted( TripleConsumer<Integer, Column<A, ?>, Column<A, ?>> ls ){
        return columns_list.onInserted(ls);
    }

    @Override
    public AutoCloseable onInserted( boolean weak, TripleConsumer<Integer, Column<A, ?>, Column<A, ?>> ls ){
        return columns_list.onInserted(weak, ls);
    }

    @Override
    public AutoCloseable onUpdated( TripleConsumer<Integer, Column<A, ?>, Column<A, ?>> ls ){
        return columns_list.onUpdated(ls);
    }

    @Override
    public AutoCloseable onUpdated( boolean weak, TripleConsumer<Integer, Column<A, ?>, Column<A, ?>> ls ){
        return columns_list.onUpdated(weak, ls);
    }

    @Override
    public AutoCloseable onDeleted( TripleConsumer<Integer, Column<A, ?>, Column<A, ?>> ls ){
        return columns_list.onDeleted(ls);
    }

    @Override
    public AutoCloseable onDeleted( boolean weak, TripleConsumer<Integer, Column<A, ?>, Column<A, ?>> ls ){
        return columns_list.onDeleted(weak, ls);
    }

    @Override
    public AutoCloseable onChanged( TripleConsumer<Integer, Column<A, ?>, Column<A, ?>> ls ){
        return columns_list.onChanged(ls);
    }

    @Override
    public AutoCloseable onChanged( boolean weak, TripleConsumer<Integer, Column<A, ?>, Column<A, ?>> ls ){
        return columns_list.onChanged(weak, ls);
    }

    @Override
    public void fireCollectionEvent( CollectionEvent<EventList<Column<A, ?>>, Column<A, ?>> event ){
        columns_list.fireCollectionEvent(event);
    }

    @Override
    public void fireInserted( int index, Column<A, ?> aColumn ){
        columns_list.fireInserted(index, aColumn);
    }

    @Override
    public void fireUpdated( int index, Column<A, ?> old, Column<A, ?> current ){
        columns_list.fireUpdated(index, old, current);
    }

    @Override
    public void fireDeleted( int index, Column<A, ?> aColumn ){
        columns_list.fireDeleted(index, aColumn);
    }

    @Override
    public int size(){
        return columns_list.size();
    }

    @Override
    public boolean isEmpty(){
        return columns_list.isEmpty();
    }

    @Override
    public boolean contains( Object o ){
        return columns_list.contains(o);
    }

    @Override
    public Object[] toArray(){
        return columns_list.toArray();
    }

    @SuppressWarnings("nullness")
    @Override
    public <T> T[] toArray( T[] a ){
        return columns_list.toArray(a);
    }

    @Override
    public boolean containsAll( Collection<?> c ){
        return columns_list.containsAll(c);
    }

    @Override
    public Column<A, ?> get( int index ){
        return columns_list.get(index);
    }

    @Override
    public int indexOf( Object o ){
        return columns_list.indexOf(o);
    }

    @Override
    public int lastIndexOf( Object o ){
        return columns_list.lastIndexOf(o);
    }

    @Override
    public Iterator<Column<A, ?>> iterator(){
        return columns_list.iterator();
    }

    @Override
    public ListIterator<Column<A, ?>> listIterator(){
        return columns_list.listIterator();
    }

    @Override
    public ListIterator<Column<A, ?>> listIterator( int index ){
        return columns_list.listIterator(index);
    }

    @Override
    public List<Column<A, ?>> subList( int fromIndex, int toIndex ){
        return columns_list.subList(fromIndex, toIndex);
    }

    @Override
    public boolean add( Column<A, ?> aColumn ){
        return columns_list.add(aColumn);
    }

    @Override
    public boolean remove( Object o ){
        return columns_list.remove(o);
    }

    @Override
    public boolean addAll( Collection<? extends Column<A, ?>> c ){
        return columns_list.addAll(c);
    }

    @Override
    public boolean addAll( int index, Collection<? extends Column<A, ?>> c ){
        return columns_list.addAll(index, c);
    }

    @Override
    public boolean removeAll( Collection<?> coll ){
        return columns_list.removeAll(coll);
    }

    @Override
    public boolean retainAll( Collection<?> coll ){
        return columns_list.retainAll(coll);
    }

    @Override
    public void clear(){
        columns_list.clear();
    }

    @Override
    public Column<A, ?> set( int index, Column<A, ?> element ){
        return columns_list.set(index, element);
    }

    @Override
    public void add( int index, Column<A, ?> element ){
        columns_list.add(index, element);
    }

    @Override
    public Column<A, ?> remove( int index ){
        return columns_list.remove(index);
    }

    @Override
    public void replaceAll( UnaryOperator<Column<A, ?>> operator ){
        columns_list.replaceAll(operator);
    }

    @Override
    public void sort( Comparator<? super Column<A, ?>> c ){
        columns_list.sort(c);
    }

    @Override
    public boolean removeIf( Predicate<? super Column<A, ?>> filter ){
        return columns_list.removeIf(filter);
    }
    //endregion

    /**
     * Добавление колонки в таблицу
     * @param valueMapper функция сопоставления объекта и его значения
     * @param builder конструктор колонки
     * @param <C> тип данных в колонке
     * @return Созданная колонка
     */
    @SuppressWarnings("UnusedReturnValue")
    public <C> Column<A,C> append( Fn1<A,@Nullable  C> valueMapper, Consumer1<Column.Builder<A,C>> builder ){
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
     * Создание колонки
     * @param valueMapper функция сопоставления объекта и его значения
     * @param builder конструктор колонки
     * @param <C> тип данных в колонке
     * @return Созданная колонка
     */
    @SuppressWarnings("UnusedReturnValue")
    public <C> Column<A,C> build( Fn1<A,@Nullable C> valueMapper, Consumer1<Column.Builder<A,C>> builder ){
        if( builder==null )throw new IllegalArgumentException( "builder==null" );
        if( valueMapper==null )throw new IllegalArgumentException( "valueMapper==null" );
        Column.Builder<A,C> bld = new Column.Builder<>();
        bld.value(valueMapper);
        builder.accept(bld);
        var col = bld.build();
        return col;
    }
}
