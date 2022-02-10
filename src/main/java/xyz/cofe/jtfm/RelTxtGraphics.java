package xyz.cofe.jtfm;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.StyleSet;
import com.googlecode.lanterna.graphics.TextImage;
import com.googlecode.lanterna.screen.TabBehaviour;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Рендер текста/графики относительно указанных координат
 */
public class RelTxtGraphics implements com.googlecode.lanterna.graphics.TextGraphics {
    public final com.googlecode.lanterna.graphics.TextGraphics target;

    public RelTxtGraphics( com.googlecode.lanterna.graphics.TextGraphics target ){
        if( target==null )throw new IllegalArgumentException( "target==null" );
        this.target = target;
    }

    public RelTxtGraphics( com.googlecode.lanterna.graphics.TextGraphics target, int left, int top ){
        if( target==null )throw new IllegalArgumentException( "target==null" );
        this.target = target;
        this.left = left;
        this.top = top;
    }

    private int left = 0;
    private int top = 0;

    public int left(){ return left; }
    public RelTxtGraphics withLeft( int newLeft ){
        return new RelTxtGraphics(target, newLeft, top);
    }

    public int top(){ return top; }
    public RelTxtGraphics withTop( int newTop ){
        return new RelTxtGraphics(target, left, newTop);
    }

    public RelTxtGraphics withLeftTop( int left, int top ){
        return new RelTxtGraphics(target, left, top);
    }

    public RelTxtGraphics withOffset( int offsetLeft, int offsetTop ){
        return new RelTxtGraphics(target, left+offsetLeft, top+offsetTop);
    }

    public TerminalSize getSize(){
        return target.getSize();
    }

    public RelTxtGraphics
        newTextGraphics( @NonNull TerminalPosition topLeftCorner, @NonNull TerminalSize size ) throws IllegalArgumentException {
        return new RelTxtGraphics(
            target.newTextGraphics(
                new TerminalPosition(topLeftCorner.getColumn() + left, topLeftCorner.getRow() + top),
                size)
        );
    }

    public TabBehaviour getTabBehaviour(){
        return target.getTabBehaviour();
    }

    public RelTxtGraphics setTabBehaviour( TabBehaviour tabBehaviour ){
        target.setTabBehaviour(tabBehaviour);
        return this;
    }

    public RelTxtGraphics fill( char c ){
        target.fill(c);
        return this;
    }

    public RelTxtGraphics setCharacter( int column, int row, char character ){
        target.setCharacter(column + left, row + top, character);
        return this;
    }

    public RelTxtGraphics setCharacter( int column, int row, @NonNull TextCharacter character ){
        target.setCharacter(column + left, row + top, character);
        return this;
    }

    public RelTxtGraphics setCharacter( @NonNull TerminalPosition position, char character ){
        target.setCharacter(
            new TerminalPosition(position.getColumn()+left, position.getRow()+top), character);
        return this;
    }

    public RelTxtGraphics setCharacter( @NonNull TerminalPosition position, @NonNull TextCharacter character ){
        target.setCharacter(new TerminalPosition(position.getColumn()+left, position.getRow()+top), character);
        return this;
    }

    public RelTxtGraphics drawLine( @NonNull TerminalPosition fromPoint, @NonNull TerminalPosition toPoint, char character ){
        target.drawLine(
            new TerminalPosition(fromPoint.getColumn()+left, fromPoint.getRow()+top),
            new TerminalPosition(toPoint.getColumn()+left, toPoint.getRow()+top),
            character);
        return this;
    }

    public RelTxtGraphics drawLine( @NonNull TerminalPosition fromPoint, @NonNull TerminalPosition toPoint, @NonNull TextCharacter character ){
        target.drawLine(
            new TerminalPosition(fromPoint.getColumn()+left, fromPoint.getRow()+top),
            new TerminalPosition(toPoint.getColumn()+left, toPoint.getRow()+top),
            character);
        return this;
    }

    public RelTxtGraphics drawLine( int fromX, int fromY, int toX, int toY, char character ){
        target.drawLine(
            fromX+left,
            fromY+top,
            toX+left,
            toY+top,
            character);
        return this;
    }

    public RelTxtGraphics drawLine( int fromX, int fromY, int toX, int toY, @NonNull TextCharacter character ){
        target.drawLine(
            fromX+left,
            fromY+top,
            toX+left,
            toY+top,
            character);
        return this;
    }

    public RelTxtGraphics drawTriangle( @NonNull TerminalPosition p1, @NonNull TerminalPosition p2, @NonNull TerminalPosition p3, char character ){
        target.drawTriangle(
            new TerminalPosition(p1.getColumn()+left, p1.getRow()+top),
            new TerminalPosition(p2.getColumn()+left, p2.getRow()+top),
            new TerminalPosition(p3.getColumn()+left, p3.getRow()+top),
            character);
        return this;
    }

    public RelTxtGraphics drawTriangle( @NonNull TerminalPosition p1, @NonNull TerminalPosition p2, @NonNull TerminalPosition p3, @NonNull TextCharacter character ){
        target.drawTriangle(
            new TerminalPosition(p1.getColumn()+left, p1.getRow()+top),
            new TerminalPosition(p2.getColumn()+left, p2.getRow()+top),
            new TerminalPosition(p3.getColumn()+left, p3.getRow()+top),
            character);
        return this;
    }

    public RelTxtGraphics fillTriangle( @NonNull TerminalPosition p1, @NonNull TerminalPosition p2, @NonNull TerminalPosition p3, char character ){
        target.fillTriangle(
            new TerminalPosition(p1.getColumn()+left, p1.getRow()+top),
            new TerminalPosition(p2.getColumn()+left, p2.getRow()+top),
            new TerminalPosition(p3.getColumn()+left, p3.getRow()+top),
            character);
        return this;
    }

    public RelTxtGraphics fillTriangle( @NonNull TerminalPosition p1, @NonNull TerminalPosition p2, @NonNull TerminalPosition p3, @NonNull TextCharacter character ){
        target.fillTriangle(
            new TerminalPosition(p1.getColumn()+left, p1.getRow()+top),
            new TerminalPosition(p2.getColumn()+left, p2.getRow()+top),
            new TerminalPosition(p3.getColumn()+left, p3.getRow()+top),
            character);
        return this;
    }

    public RelTxtGraphics drawRectangle( @NonNull TerminalPosition topLeft, @NonNull TerminalSize size, char character ){
        target.drawRectangle(
            new TerminalPosition(topLeft.getColumn()+left, topLeft.getRow()+top),
            size,
            character);
        return this;
    }

    public RelTxtGraphics drawRectangle( @NonNull TerminalPosition topLeft, @NonNull TerminalSize size, @NonNull TextCharacter character ){
        target.drawRectangle(
            new TerminalPosition(topLeft.getColumn()+left, topLeft.getRow()+top),
            size, character);
        return this;
    }

    public RelTxtGraphics fillRectangle( @NonNull TerminalPosition topLeft, @NonNull TerminalSize size, char character ){
        target.fillRectangle(
            new TerminalPosition(topLeft.getColumn()+left, topLeft.getRow()+top),
            size, character);
        return this;
    }

    public RelTxtGraphics fillRectangle( @NonNull TerminalPosition topLeft, @NonNull TerminalSize size, @NonNull TextCharacter character ){
        target.fillRectangle(
            new TerminalPosition(topLeft.getColumn()+left, topLeft.getRow()+top),
            size, character);
        return this;
    }

    public RelTxtGraphics drawImage( @NonNull TerminalPosition topLeft, @NonNull TextImage image ){
        target.drawImage(
            new TerminalPosition(topLeft.getColumn()+left, topLeft.getRow()+top),
            image);
        return this;
    }

    public RelTxtGraphics drawImage( @NonNull TerminalPosition topLeft, @NonNull TextImage image, @NonNull TerminalPosition sourceImageTopLeft, @NonNull TerminalSize sourceImageSize ){
        target.drawImage(
            new TerminalPosition(topLeft.getColumn()+left, topLeft.getRow()+top),
            image,
            sourceImageTopLeft,
            sourceImageSize);
        return this;
    }

    public RelTxtGraphics putString( int column, int row, String string ){
        target.putString(column+left, row+top, string);
        return this;
    }

    public RelTxtGraphics putString( @NonNull TerminalPosition position, String string ){
        target.putString(
            new TerminalPosition(position.getColumn()+left, position.getRow()+top)
            , string);
        return this;
    }

    public RelTxtGraphics putString( int column, int row, String string, SGR extraModifier, SGR... optionalExtraModifiers ){
        target.putString(column+left, row+top, string, extraModifier, optionalExtraModifiers);
        return this;
    }

    public RelTxtGraphics putString( @NonNull TerminalPosition position, String string, @NonNull SGR extraModifier, SGR... optionalExtraModifiers ){
        target.putString(
            new TerminalPosition(position.getColumn()+left, position.getRow()+top),
            string, extraModifier, optionalExtraModifiers);
        return this;
    }

    public RelTxtGraphics putString( int column, int row, String string, @NonNull Collection<SGR> extraModifiers ){
        target.putString(column+left, row+top, string, extraModifiers);
        return this;
    }

    public RelTxtGraphics putCSIStyledString( int column, int row, String string ){
        target.putCSIStyledString(column+left, row+top, string);
        return this;
    }

    public RelTxtGraphics putCSIStyledString( @NonNull TerminalPosition position, String string ){
        target.putCSIStyledString(
            new TerminalPosition(position.getColumn()+left, position.getRow()+top),
            string);
        return this;
    }

    public TextCharacter getCharacter( @NonNull TerminalPosition position ){
        return target.getCharacter(
            new TerminalPosition(position.getColumn()+left, position.getRow()+top)
        );
    }

    public TextCharacter getCharacter( int column, int row ){
        return target.getCharacter(column+left, row+top);
    }

    public TextColor getBackgroundColor(){
        return target.getBackgroundColor();
    }

    public RelTxtGraphics setBackgroundColor( @NonNull TextColor backgroundColor ){
        target.setBackgroundColor(backgroundColor);
        return this;
    }

    public TextColor getForegroundColor(){
        return target.getForegroundColor();
    }

    public RelTxtGraphics setForegroundColor( @NonNull TextColor foregroundColor ){
        target.setForegroundColor(foregroundColor);
        return this;
    }

    public RelTxtGraphics enableModifiers( SGR... modifiers ){
        target.enableModifiers(modifiers);
        return this;
    }

    public RelTxtGraphics disableModifiers( SGR... modifiers ){
        target.disableModifiers(modifiers);
        return this;
    }

    public RelTxtGraphics setModifiers( @NonNull EnumSet<SGR> modifiers ){
        target.setModifiers(modifiers);
        return this;
    }

    public RelTxtGraphics clearModifiers(){
        target.clearModifiers();
        return this;
    }

    public EnumSet<SGR> getActiveModifiers(){
        return target.getActiveModifiers();
    }

    public RelTxtGraphics setStyleFrom( @NonNull StyleSet<?> source ){
        target.setStyleFrom(source);
        return this;
    }
}
