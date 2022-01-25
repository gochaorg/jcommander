package xyz.cofe.jcommander;

import xyz.cofe.fn.Consumer3;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * Простое свойство
 * @param <A> тип данных свойства
 */
public class SimpleProperty<A> implements MutableProperty<A,SimpleProperty<A>> {
    private A value;

    /**
     * Конструктор
     * @param initial начальное значение, не null
     */
    public SimpleProperty( A initial ){
        if( initial==null )throw new IllegalArgumentException( "initial==null" );
        this.value = initial;
    }

    /**
     * Установка значения
     * @param newValue значение, не null
     */
    @Override
    public void set( A newValue ){
        if( newValue==null )throw new IllegalArgumentException( "newValue==null" );
        var old = value;
        value = newValue;
        fire(old, value);
    }

    @Override
    public A get(){
        return value;
    }

    private Set<Consumer3<SimpleProperty<A>, A, A>> listeners = new HashSet<>();

    /**
     * Получение копии подписчиков
     * @return подписчики
     */
    public Set<Consumer3<SimpleProperty<A>, A, A>> listeners(){
        return new HashSet<>(listeners);
    }

    /**
     * Удаление подписчика
     * @param ls подписчик
     */
    public void dropListener( Consumer3<SimpleProperty<A>, A, A> ls ){
        if( ls==null )throw new IllegalArgumentException( "ls==null" );
        listeners.remove(ls);
    }

    /**
     * Удаление подписчиков
     */
    public void dropListeners(){
        listeners.clear();
    }

    private void fire( A old, A cur ){
        for( var ls : listeners ){
            ls.accept(this, old, cur );
        }
    }

    @Override
    public Runnable listen( Consumer3<SimpleProperty<A>, A, A> listener ){
        if( listener==null )throw new IllegalArgumentException( "listener==null" );
        listeners.add(listener);
        var r_ls = new WeakReference<>(listener);
        return ()->{
            var ls = r_ls.get();
            if( ls!=null ){
                listeners.remove(ls);
                r_ls.clear();
            }
        };
    }

    /**
     * Привязка свойства к другому свойству
     * @param prop целевое свойство
     * @return отписка от уведомлений
     */
    public Runnable bind( Property<A,?> prop ){
        if( prop==null )throw new IllegalArgumentException( "prop==null" );
        set(prop.get());
        return prop.listen( (owner,from,to) -> {
            set(to);
        });
    }
}
