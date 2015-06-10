package qwirkle.ui.swing;

import java.awt.*;

/** A base for our specialized layouts, with some shared functionality and sane boilerplate & defaults.
 *  Note that all of our layouts rely on the parent to keep track of components,
 *  so the methods that would do that are all blank -- <tt>addLayoutComponent()</tt> etc.*/
public abstract class LayoutBase implements LayoutManager2 {
    // center within parent
    @Override public float getLayoutAlignmentX(Container target) { return 0.5f; }
    @Override public float getLayoutAlignmentY(Container target) { return 0.5f; }

    /** Take parent's size, insets, and this' maxSize into account to
     *  find a max size to fit this' contents into. */
    protected Dimension getFitInside(Container parent) {
        Insets pInsets = parent.getInsets();
        Dimension pSize = parent.getSize();
        // subtract insets
        return new Dimension
                (pSize.width - pInsets.left - pInsets.right,
                        pSize.height - pInsets.top - pInsets.bottom);
    }

    /** We're fine with whatever we're given, down to a minimum. */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        // what was I thinking using grandparent?
        //        Container gp = parent.getParent();
        //        Dimension result = fit((gp == null ? parent : gp).getSize());

        // we're fine with whatever we're given, down to a minimum
        Dimension pSize = parent.getSize();

        // leave enough space to draw recognizable icons
        Dimension min = minimumLayoutSize(parent);

        // if a minimum was set, then use the larger of the two minimums
        Dimension setmin = parent.getMinimumSize();
        if (setmin != null)
            min = new Dimension(Math.max(min.width, setmin.width), Math.max(min.height, setmin.height));

        // if the container is big enough, go with it; otherwise, return the minimum
        if (pSize == null || min.getHeight() > pSize.getHeight() || min.getWidth() > pSize.getWidth())
            return min;
        else
            return pSize;
//        System.out.println("Preferred size: " + result);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return preferredLayoutSize(target);
    }

    @Override public void addLayoutComponent(Component comp, Object constraints) { }
    @Override public void invalidateLayout(Container target) { }
    @Override public void addLayoutComponent(String name, Component comp) { }
    @Override public void removeLayoutComponent(Component comp) { }
}
