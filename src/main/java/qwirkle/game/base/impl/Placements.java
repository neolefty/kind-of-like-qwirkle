package qwirkle.game.base.impl;

import qwirkle.game.base.QwirkleLocation;
import qwirkle.game.base.QwirklePlacement;

import java.util.Collection;
import java.util.TreeSet;

public class Placements extends TreeSet<QwirklePlacement> {
    public Placements() { }
    public Placements(Collection<QwirklePlacement> c) { super(c); }

    public QwirklePlacement getUpperLeft() {
        return isEmpty() ? null : first();
    }

    public QwirklePlacement getLowerRight() {
        return isEmpty() ? null : last();
    }

    public int getXMin() { return first().getX(); }
    public int getXMax() { return last().getX(); }
    public int getYMin() { return first().getY(); }
    public int getYMax() { return last().getY(); }

    public boolean isWithinBounds(QwirkleLocation loc) {
        if (isEmpty())
            return false;
        if (size() == 1)
            return loc.equals(first().getLocation());
        else
            return getXMin() <= loc.getX() && getXMax() >= loc.getX()
                && getYMin() <= loc.getY() && getYMax() >= loc.getY();
    }

    /** Does <tt>loc</tt> border this or intersect it? */
    public boolean isTouching(QwirkleLocation loc) {
        if (isEmpty())
            return false;
        else if (contains(loc))
            return true;
        else if (size() == 1)
            return first().getLocation().isNeighbor(loc);
        else
            return getXMin()-1 <= loc.getX() && getXMax()+1 >= loc.getX()
                    && getYMin()-1 <= loc.getY() && getYMax()+1 >= loc.getY();
    }

    /** Does <tt>other</tt> border this or intersect it? */
    public boolean isTouching(Placements other) {
        //noinspection SimplifiableIfStatement
        if (isEmpty() || other.isEmpty())
            return false;
        else
            return getXMin()-1 <= other.getXMax() && getXMin()+1 >= other.getXMin()
                    && getYMin()-1 <= other.getYMax() && getYMax()+1 >= other.getYMin();
    }
}
