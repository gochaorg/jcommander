package xyz.cofe.jtfm;

import xyz.cofe.fn.Consumer3;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

public class OwnProperty<VALUE,OWN> implements Property<VALUE,OwnProperty<VALUE,OWN>>, MutableProperty<VALUE, OwnProperty<VALUE,OWN>> {
    public OwnProperty( VALUE initial, OWN owner ){
        if( owner==null )throw new IllegalArgumentException( "owner==null" );
        if( initial==null )throw new IllegalArgumentException( "initial==null" );
        this.owner = owner;
        this.value = initial;
    }

    private OWN owner;
    private VALUE value;

    public OWN owner(){ return owner; }

    @Override
    public VALUE get(){
        return value;
    }

    @Override
    public void set( VALUE newValue ){
        if( newValue==null )throw new IllegalArgumentException( "newValue==null" );
        var old = this.value;
        this.value = newValue;
        fire(old,newValue);
    }

    @Override
    public Runnable listen( Consumer3<OwnProperty<VALUE, OWN>, VALUE, VALUE> listener ){
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

    private Set<Consumer3<OwnProperty<VALUE,OWN>, VALUE, VALUE>> listeners = new HashSet<>();

    /**
     * Получение копии подписчиков
     * @return подписчики
     */
    public Set<Consumer3<OwnProperty<VALUE,OWN>, VALUE, VALUE>> listeners(){
        return new HashSet<>(listeners);
    }

    /**
     * Удаление подписчика
     * @param ls подписчик
     */
    public void dropListener( Consumer3<OwnProperty<VALUE,OWN>, VALUE, VALUE> ls ){
        if( ls==null )throw new IllegalArgumentException( "ls==null" );
        listeners.remove(ls);
    }

    /**
     * Удаление подписчиков
     */
    public void dropListeners(){
        listeners.clear();
    }

    private void fire( VALUE old, VALUE cur ){
        for( var ls : listeners ){
            ls.accept(this, old, cur );
        }
    }
}
