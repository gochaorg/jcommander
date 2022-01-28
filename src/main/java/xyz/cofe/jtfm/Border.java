package xyz.cofe.jtfm;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

import java.util.Optional;

/**
 * Отрисовка рамок
 */
public class Border {
    private static final String border_s_chars =
        "\u2500" + // 0  ─
        "\u2502" + // 1  │
        "\u250C" + // 2  ┌ left top
        "\u2510" + // 3  ┐ right top
        "\u2514" + // 4  └ left bottom
        "\u2518" + // 5  ┘ right bottom
        "\u251C" + // 6  ├
        "\u2524" + // 7  ┤
        "\u252C" + // 8  ┬
        "\u2534" + // 9  ┴
        "\u253C" + // 10 ┼
        "";

    private static final String border_w_chars =
        "\u2550" + // 0  ═
        "\u2551" + // 1  ║
        "\u2554" + // 2  ╔ left top
        "\u2557" + // 3  ╗ right top
        "\u255a" + // 4  ╚ left bottom
        "\u255d" + // 5  ╝ right bottom
        "\u2560" + // 6  ╠
        "\u2563" + // 7  ╣
        "\u2566" + // 8  ╦
        "\u2569" + // 9  ╩
        "\u256C" + // 10 ╬
        "";

    private static final String connectors =
        "\u2552" + //  0  ╒
        "\u2553" + //  1  ╓
        "\u2555" + //  2  ╕
        "\u2556" + //  3  ╖
        "\u2558" + //  4  ╘
        "\u2559" + //  5  ╙
        "\u255b" + //  6  ╛
        "\u255c" + //  7  ╜
        "\u255e" + //  8  ╞
        "\u255f" + //  9  ╟
        "\u2561" + // 10  ╡
        "\u2562" + // 11  ╢
        "\u2564" + // 12  ╤
        "\u2565" + // 13  ╥
        "\u2567" + // 14  ╧
        "\u2568" + // 15  ╨
        "\u256a" + // 16  ╪
        "\u256b" + // 17  ╫
        ""
        ;

    private String border_chars =
        "\u2500" + // 0  ─
            "\u2502" + // 1  │
            "\u250C" + // 2  ┌ left top
            "\u2510" + // 3  ┐ right top
            "\u2514" + // 4  └ left bottom
            "\u2518" + // 5  ┘ right bottom
            "\u251C" + // 6  ├
            "\u2524" + // 7  ┤
            "\u252C" + // 8  ┬
            "\u2534" + // 9  ┴
            "\u253C" + // 10 ┼
            "";

    public Border(){
    }

    public Border(boolean single_line_border){
        border_chars = single_line_border ? border_s_chars : border_w_chars;
    }

    /**
     * Символ рамки
     */
    public enum Symbol {
        /** Горизонтальная линия */
        Horizontal_Line,

        /** Вертикальная линия */
        Vertical_Line,

        /** Левый-верхний угол */
        LeftTop_Corner,

        /** Правый-верхний угол */
        RightTop_Corner,

        /** Левый-нижний угол */
        LeftBottom_Corner,

        /** Правый-нижний угол */
        RightBottom_Corner,

        /** Т образный символ - Вертикальная линия, с хвостом вправо */
        Vertical2Right_T,

        /** Т образный символ - Вертикальная линия, с хвостом влево */
        Vertical2Left_T,

        /** Т образный символ - Горизонтальная линия, с хвостом вниз */
        Horizontal2Down_T,

        /** Т образный символ - Горизонтальная линия, с хвостом вверх */
        Horizontal2Up_T,

        /** Пересечение вертикальной и горизонтальной линии */
        Cross
    }

    /**
     * Получение символа по его назначению
     * @param s назначение символа
     * @return символ
     */
    public Optional<Character> charOf( Symbol s ){
        if( s==null )throw new IllegalArgumentException( "s==null" );
        switch( s ){
            case Horizontal_Line: return Optional.of(border_chars.charAt(0));
            case Vertical_Line: return Optional.of(border_chars.charAt(1));
            case LeftTop_Corner: return Optional.of(border_chars.charAt(2));
            case RightTop_Corner: return Optional.of(border_chars.charAt(3));
            case LeftBottom_Corner: return Optional.of(border_chars.charAt(4));
            case RightBottom_Corner: return Optional.of(border_chars.charAt(5));
            case Vertical2Right_T: return Optional.of(border_chars.charAt(6));
            case Vertical2Left_T: return Optional.of(border_chars.charAt(7));
            case Horizontal2Down_T: return Optional.of(border_chars.charAt(8));
            case Horizontal2Up_T: return Optional.of(border_chars.charAt(9));
            case Cross: return Optional.of(border_chars.charAt(10));
        }
        return Optional.empty();
    }

    /**
     * Цвет переднего плана, цвет текста
     */
    public final SimpleProperty<TextColor> foreground = new SimpleProperty<>(TextColor.ANSI.WHITE_BRIGHT);

    /**
     * Цвет фона
     */
    public final SimpleProperty<TextColor> background = new SimpleProperty<>(TextColor.ANSI.BLUE);

    /**
     * Отрисовка рамки
     * @param g интерфейс отрисовки
     * @param r рамка
     */
    public void render( TextGraphics g, Rect r ){
        if( g==null )throw new IllegalArgumentException( "g==null" );
        if( r==null )throw new IllegalArgumentException( "r==null" );

        int x0 = r.left();
        int y0 = r.top();
        int x1 = r.left() + r.width() - 1;
        int y1 = r.top() + r.height() - 1;

        var c = TextCharacter.fromCharacter(border_chars.charAt(0))[0];
        c = c.withForegroundColor(foreground.get());
        c = c.withBackgroundColor(background.get());
        g.setForegroundColor(foreground.get());
        g.setBackgroundColor(background.get());

        g.drawLine(x0, y0, x1, y0, c);
        g.drawLine(x0, y1, x1, y1, c);

        c = TextCharacter.fromCharacter(border_chars.charAt(1))[0];
        c = c.withForegroundColor(foreground.get());
        c = c.withBackgroundColor(background.get());
        g.setForegroundColor(foreground.get());
        g.setBackgroundColor(background.get());

        g.drawLine(x0, y0, x0, y1, c);
        g.drawLine(x1, y0, x1, y1, c);

        g.putString(x0, y0, border_chars.substring(2, 3));
        g.putString(x1, y0, border_chars.substring(3, 4));

        g.putString(x0, y1, border_chars.substring(4, 5));
        g.putString(x1, y1, border_chars.substring(5, 6));
    }
}
