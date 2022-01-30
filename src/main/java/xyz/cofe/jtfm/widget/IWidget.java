package xyz.cofe.jtfm.widget;

public interface IWidget<SELF>
    extends Render, RectProperty<SELF>, IsFocusable, IsVisible, Input
{
}
