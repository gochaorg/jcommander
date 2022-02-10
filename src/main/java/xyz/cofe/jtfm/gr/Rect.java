package xyz.cofe.jtfm.gr;

import com.googlecode.lanterna.TerminalPosition;

/**
 * Прямоугольник.
 */
public interface Rect {
    /**
     * Левый край рамки
     * @return левый край
     */
    public int left();

    /**
     * Верхний край рамки
     * @return Верхний край
     */
    public int top();

    /**
     * Ширина рамки
     * @return Ширина рамки
     */
    public int width();

    /**
     * высота рамки
     * @return высота рамки
     */
    public int height();

    /**
     * Нижний край рамки
     * @return Нижний край
     */
    public default int bottom(){ return top()+height(); }

    /**
     * Правый край рамки
     * @return Правый край
     */
    public default int right(){ return left()+width(); }

    /**
     * Проверка нахождения точки (x, y) в рамке.
     * @param x координата точки
     * @param inc_left true - проверка left &lt;= x <br>
     *                 false - проверка left &lt; x <br>
     * @param inc_right true - проверка x &lt;= right <br>
     *                  false - проверка x &lt; right <br>
     * @param y координата точки
     * @param inc_top true  - проверка top &lt;= y <br>
     *                false - проверка top &lt; y <br>
     * @param inc_bottom true - проверка y &lt;= bottom <br>
     *                   false - проверка y &lt; bottom
     * @return true - точка внутри рамки
     */
    public default boolean include( int x, boolean inc_left, boolean inc_right, int y, boolean inc_top, boolean inc_bottom ){
        if( !( (inc_left && left() <= x) || (!inc_left && left() < x) ) ) return false;
        if( !( (inc_right && x <= right()) || (!inc_right && x < right()) ) ) return false;

        if( !( (inc_top && top() <= y) || (!inc_top && top() < y) ) ) return false;
        if( !( (inc_bottom && y <= bottom()) || (!inc_bottom && y < bottom()) ) ) return false;

        return true;
    }

    /**
     * Проверка нахождения точки (x, y) в рамке.
     * <br>
     * Интервал слева и сверху включительный ( &lt;= ),
     * справа и снизу зависит от inc_right, inc_bottom
     * @param x координата точки
     * @param inc_right true - проверка x &lt;= right <br>
     *                  false - проверка x &lt; right <br>
     * @param y координата точки
     * @param inc_bottom true - проверка y &lt;= bottom <br>
     *                   false - проверка y &lt; bottom
     * @return true - точка внутри рамки
     */
    public default boolean include( int x, boolean inc_right, int y, boolean inc_bottom ){
        return include(x,true,inc_right, y, true, inc_bottom );
    }

    /**
     * Проверка нахождения точки (x, y) в рамке.
     * <br>
     * Интервал слева и сверху включительный ( &lt;= ),
     * справа и снизу зависит от inc_right, inc_bottom
     * @param x координата точки
     * @param inc_right_bottom true - проверка x &lt;= right <br>
     *                                 проверка y &lt;= bottom <br>
     *                         false - проверка x &lt; right <br>
     *                                 проверка y &lt; bottom
     * @param y координата точки
     * @return true - точка внутри рамки
     */
    public default boolean include( int x, int y, boolean inc_right_bottom ){
        return include(x,true,inc_right_bottom, y, true, inc_right_bottom );
    }

    /**
     * Проверка нахождения точки (x, y) в рамке, проверка осуществляется в полу интервале
     * @param x координата точки
     * @param y координата точки
     * @return true - точка внутри рамки
     */
    public default boolean include( int x, int y ){
        return include(x, y, false);
    }

    /**
     * Проверка нахождения точки (x, y) в рамке, проверка осуществляется в полу интервале
     * @param p true - точка внутри рамки
     * @return  true - точка внутри рамки
     */
    public default boolean include( TerminalPosition p ){
        if( p==null )throw new IllegalArgumentException( "p==null" );
        return include( p.getColumn(), p.getRow() );
    }

    /**
     * Конструктор по левому верхнему углу и ширине и высоте
     * @param x координаты левого-верхнего угла
     * @param y координаты левого-верхнего угла
     * @param w ширина
     * @param h высота
     * @return Рамка
     */
    public static Rect of( int x, int y, int w, int h ) {
        if( w<0 )throw new IllegalArgumentException( "w<0" );
        if( h<0 )throw new IllegalArgumentException( "h<0" );
        return new Rect() {
            @Override
            public int left(){
                return x;
            }

            @Override
            public int top(){
                return y;
            }

            @Override
            public int width(){
                return w;
            }

            @Override
            public int height(){
                return h;
            }
        };
    }

    /**
     * Конструктор по двум точкам
     * @param x0 координаты левого-верхнего угла
     * @param y0 координаты левого-верхнего угла
     * @param x1 координаты правого-нижнего угла
     * @param y1 координаты правого-нижнего угла
     * @return Рамка
     */
    public static Rect create( int x0, int y0, int x1, int y1 ) {
        int xmin = Math.min(x0,x1);
        int ymin = Math.min(y0,y1);
        int xmax = Math.max(x0,x1);
        int ymax = Math.max(y0,y1);
        int h = ymax - ymin;
        int w = xmax - xmin;
        return new Rect() {
            @Override
            public int left(){
                return xmin;
            }

            @Override
            public int top(){
                return ymin;
            }

            @Override
            public int width(){
                return w;
            }

            @Override
            public int height(){
                return h;
            }
        };
    }
}
