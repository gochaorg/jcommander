package xyz.cofe.jcommander;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

import java.util.Optional;

/**
 * Отрисовка рамок
 */
public class Border {
    private static final String border_chars =
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
