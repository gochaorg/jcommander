package xyz.cofe.jtfm.widget;

public interface IWidget<SELF>
    extends
        Render,
        RectProperty<SELF>,
        IsFocusable,
        VisibleProperty<SELF>,
        Input,
        RelativeLayout,
        NestedWidgets,
        ParentProperty<IWidget<?>,SELF>
{
}
