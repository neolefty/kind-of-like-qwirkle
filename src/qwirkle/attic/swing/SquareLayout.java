package qwirkle.attic.swing;

import javax.swing.*;
import java.awt.*;

/** Make a single component square, maximum size. */
public class SquareLayout implements LayoutManager2 {
    public static class SquarePanel extends JPanel {
        public SquarePanel(Component comp) {
            setLayout(new SquareLayout());
            add(comp);
        }
    }

    private Component component;

    @Override
    public void addLayoutComponent(String name, Component comp) {
        if (component != null)
            throw new IllegalStateException("Already has a component.");
        this.component = comp;
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        if (comp != component)
            throw new IllegalStateException("Trying to remove a different component.");
        this.component = null;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Container gp = parent.getParent();
        return minSquare((gp == null ? parent : gp).getSize());
//        return minSquare(parent.getSize());
//        return maxSquare(component == null ? null : component.getPreferredSize());
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(100, 100);
//        return minSquare(parent.getSize());
//        return maxSquare(component == null ? null : component.getMinimumSize());
    }

    @Override
    public void layoutContainer(Container parent) {
        int ph = parent.getHeight(), pw = parent.getWidth();
        int side = Math.min(ph, pw);
        if (component != null)
            component.setBounds((pw - side) / 2, (ph - side) / 2, side, side);
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        addLayoutComponent("", comp);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        Container gp = target.getParent();
        return minSquare((gp == null ? target : gp).getSize());
    }

    @Override public float getLayoutAlignmentX(Container target) { return 0.5f; }
    @Override public float getLayoutAlignmentY(Container target) { return 0.5f; }
//    @Override public float getLayoutAlignmentX(Container target) { return 0; }
//    @Override public float getLayoutAlignmentY(Container target) { return 0; }
    // no effect -- nothing is cached
    @Override public void invalidateLayout(Container target) { }

    private Dimension maxSquare(Dimension dim) { return square(dim, true); }
    private Dimension minSquare(Dimension dim) { return square(dim, false); }
    private Dimension square(Dimension dim, boolean max) {
        if (dim != null) {
            int side = max ? Math.max(dim.width, dim.height) : Math.min(dim.width, dim.height);
            return new Dimension(side, side);
        }
        else
            return null;
    }
}
