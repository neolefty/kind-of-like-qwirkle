package qwirkle.game;

import qwirkle.game.impl.QwirkleGridTools;

import java.util.*;

/** A line of pieces, all either the same color or the same
 *  shape (but not both).
 *
 *  A line may have a size of 1.
 *
 *  A line has two ends. When it has 6 pieces, it is complete
 *  (but use isComplete() instead of size(). */
public class QwirkleLine implements Collection<QwirklePlacement> {
    private Set<QwirklePlacement> placements = new HashSet<>();
    private QwirkleSettings settings;
    private List<QwirkleLocation> ends = null;
    private boolean sameColor, sameShape;

    /** Construct a new line (with only one piece). */
    public QwirkleLine(QwirklePlacement placement, QwirkleSettings settings) {
        this.settings = settings;
        placements.add(placement);
        sameColor = true;
        sameShape = true;
    }

    public QwirkleLine(QwirkleLine parent, QwirklePlacement additional) {
        if (!parent.isLegal(additional))
            throw new IllegalStateException("Cannot add " + additional + " to " + parent);

        this.settings = parent.settings;

        this.placements.addAll(parent.placements);
        this.placements.add(additional);
        if (parent.size() > 1) {
            sameColor = parent.sameColor;
            sameShape = parent.sameShape;
        }
        else {
            QwirklePlacement first = parent.placements.iterator().next();
            sameColor = (first.getColor() == additional.getColor());
            sameShape = (first.getShape() == additional.getShape());
            if (sameColor && sameShape)
                throw new IllegalStateException("Can't be both the same color and the same shape.");
        }
    }

//    /** Does this line contain <tt>placement</tt>? */
//    public boolean contains(QwirklePlacement placement) {
//        return placements.contains(placement);
//    }

    /** What is the score for this line?
     *  Bonus if it has all shapes or all colors. */
    public int getScore() {
        return size() + (isComplete()
                ? (isSameColor() ? settings.getColors().size()
                                : settings.getShapes().size())
                : 0);
    }

    public boolean contains(QwirkleLocation location) {
        for (QwirklePlacement p : placements)
            if (p.getLocation().equals(location))
                return true;
        return false;
    }

    /** Does this line already contain a certain color? */
    public boolean contains(QwirkleColor color) {
        for (QwirklePlacement p : placements)
            if (p.getPiece().getColor() == color)
                return true;
        return false;
    }

    /** Does this line already contain a certain shape? */
    public boolean contains(QwirkleShape shape) {
        for (QwirklePlacement p : placements)
            if (p.getPiece().getShape() == shape)
                return true;
        return false;
    }

    public boolean isComplete() {
        return (isSameColor() && size() == settings.getColors().size())
                || (isSameShape() && size() == settings.getShapes().size());
    }

    /** Add a placement to this line, if it is legal.
     *  Doesn't modify this line -- instead, creates a new line. */
    public QwirkleLine augment(QwirklePlacement placement) {
        return new QwirkleLine(this, placement);
    }

    /** Is it legal to add a placement to this line? */
    public boolean isLegal(QwirklePlacement placement) {
        return !isComplete()
                && isLegalShapeAndColor(placement)
                && getEnds().contains(placement.getLocation());
    }

    private boolean isLegalShapeAndColor(QwirklePlacement placement) {
        QwirklePlacement first = placements.iterator().next();
        if (size() == 1)
            // can't play exactly the same piece
            return !first.getPiece().equals(placement.getPiece())
                    // but a piece that matches either the shape or color is fine
                    && (first.getShape() == placement.getShape()
                        || first.getColor() == placement.getColor());
        else {
            // same color --> color matches && new shape
            if (isSameColor())
                return placement.getColor() == first.getColor()
                && !contains(placement.getShape());
            // same shape --> shape matches && new color
            else if (isSameShape())
                return placement.getShape() == first.getShape()
                && !contains(placement.getColor());
            // oops
            else
                throw new IllegalStateException("Neither color nor shape is uniform.");
        }
    }

    /** What are the positions beyond this line's ends,
     *  where we can add a piece? */
    public List<QwirkleLocation> getEnds() {
        if (ends == null) {
            QwirklePlacement first = placements.iterator().next();
            // if size is 1, we have 4 neighbors
            if (size() == 1)
                ends = Collections.unmodifiableList(first.getLocation().getNeighbors());
            else {
                List<QwirkleLocation> result = new ArrayList<>();
                if (isVertical()) {
                    result.add(new QwirkleLocation(first.getX(), getYMin() - 1));
                    result.add(new QwirkleLocation(first.getX(), getYMax() + 1));
                }
                else {
                    if (!isHorizontal())
                        throw new IllegalStateException("Neither horizontal nor vertical: " + this);
                    result.add(new QwirkleLocation(getXMin() - 1, first.getY()));
                    result.add(new QwirkleLocation(getXMax() + 1, first.getY()));
                }
                ends = Collections.unmodifiableList(result);
            }
        }
        return ends;
    }

    public String toString() {
        return "Line: " + placements + "; endpoints: " + getEnds()
                + (size() > 1 ? (" - same " + (sameColor ? "color" : "shape")) : "");
    }

    /** Size 1 is both vertical and horizontal. */
    private boolean isVertical() {
        if (size() == 1)
            return true;
        else {
            Iterator<QwirklePlacement> i = placements.iterator();
            return i.next().getX() == i.next().getX();
        }
    }

    /** Size 1 is both vertical and horizontal. */
    private boolean isHorizontal() {
        if (size() == 1)
            return true;
        else {
            Iterator<QwirklePlacement> i = placements.iterator();
            return i.next().getY() == i.next().getY();
        }
    }

    private boolean isSameShape() { return sameShape; }
    private boolean isSameColor() { return sameColor; }

    private int getYMin() { return QwirkleGridTools.getYMin(placements); }
    private int getYMax() { return QwirkleGridTools.getYMax(placements); }
    private int getXMin() { return QwirkleGridTools.getXMin(placements); }
    private int getXMax() { return QwirkleGridTools.getXMax(placements); }

    /** Can these two lines be joined together legally by <tt>placement</tt>? */
    public boolean canJoinWith(QwirkleLine other, QwirklePlacement placement) {

        if (!isLegal(placement))
            return false;
        else {
            // 1. add the joining placement
            QwirkleLine scratch = augment(placement);

            // 2. one by one, add all the placements from the other line by growing scratch
            Map<QwirkleLocation, QwirklePlacement> otherPlaces = new HashMap<>();
            for (QwirklePlacement otherP : other)
                otherPlaces.put(otherP.getLocation(), otherP);
            while (!otherPlaces.isEmpty()) {
                // 2a. look for a placement that can be added to this line
                QwirklePlacement candidate;
                boolean foundOne = false;
                for (QwirkleLocation end : scratch.getEnds()) {
                    if (otherPlaces.containsKey(end)) {
                        candidate = otherPlaces.get(end);
                        // 2b. if we find one that fits, use it and continue searching
                        if (scratch.isLegal(candidate)) {
                            scratch = scratch.augment(candidate);
                            otherPlaces.remove(candidate.getLocation());
                            foundOne = true;
                        }
                    }
                }
                // 2c. if none fit, then this isn't legal
                if (!foundOne)
                    return false;
            }
            return true;
        }
    }

    /** Would this and other be connected by placement? */
    public boolean wouldJoinWith(QwirkleLine other, QwirklePlacement placement) {
        return this != other
                && this.getEnds().contains(placement.getLocation())
                && other.getEnds().contains(placement.getLocation());
    }

    public boolean isAlignedWith(QwirkleLine other) {
        QwirklePlacement first = placements.iterator().next();
        // do all the X's match or all the Y's match?
        boolean allX = true, allY = true;
        for (QwirklePlacement p : placements) {
            allX &= p.getX() == first.getX();
            allY &= p.getY() == first.getY();
        }
        for (QwirklePlacement p : other.placements) {
            allX &= p.getX() == first.getX();
            allY &= p.getY() == first.getY();
        }
        return allX || allY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QwirkleLine that = (QwirkleLine) o;
        return placements.equals(that.placements);
    }

    @Override
    public int hashCode() {
        return placements.hashCode();
    }

    // Collection implementation
    @Override
    public Iterator<QwirklePlacement> iterator() {
        return Collections.unmodifiableCollection(placements).iterator();
    }

    @Override public int size() { return placements.size(); }
    @Override public boolean isEmpty() { return placements.isEmpty(); }
    @Override public boolean contains(Object o) { return placements.contains(o); }
    @Override public Object[] toArray() { return placements.toArray(); }
    @Override public <T> T[] toArray(T[] a) { return placements.toArray(a); }
    @Override public boolean containsAll(Collection<?> c) { return placements.containsAll(c); }

    @Override
    public boolean add(QwirklePlacement qwirklePlacement) {
        throw new UnsupportedOperationException("use augment() instead");
    }
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("cannot remove");
    }
    @Override
    public boolean addAll(Collection<? extends QwirklePlacement> c) {
        throw new UnsupportedOperationException("use augment() instead");
    }
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("cannot remove");
    }
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("cannot remove");
    }
    @Override
    public void clear() {
        throw new UnsupportedOperationException("cannot remove");
    }
}
