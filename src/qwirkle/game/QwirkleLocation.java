package qwirkle.game;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An immutable x,y int pair */
public class QwirkleLocation implements Comparable<QwirkleLocation> {
    private int x, y;

    public QwirkleLocation(int x, int y) { this.x = x; this.y = y; }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public String toString() { return "(" + x + "," + y + ")"; }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        else if (o == null || !o.getClass().equals(getClass()))
            return false;
        else {
            QwirkleLocation that = (QwirkleLocation) o;
            return that.x == this.x && that.y == this.y;
        }
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    private SoftReference<List<QwirkleLocation>> neighbSoft;
    private List<QwirkleLocation> neighbors;
    public List<QwirkleLocation> getNeighbors() {
//        List<QwirkleLocation> result = neighbSoft == null ? null : neighbSoft.get();
//        if (result == null) {
        if (neighbors == null) {
            List<QwirkleLocation> tmp = new ArrayList<>();
            tmp.add(getAbove());
            tmp.add(getRight());
            tmp.add(getBelow());
            tmp.add(getLeft());
            neighbors = Collections.unmodifiableList(tmp);
        }
        return neighbors;

//            result = Collections.unmodifiableList(tmp);
//            neighbSoft = new SoftReference<>(result);
//        }
//        return result;
    }

    public boolean isNeighbor(QwirkleLocation loc) {
        return (loc.x == x && Math.abs(loc.y - y) == 1)
                || (loc.y == y && Math.abs(loc.x - x) == 1);
    }

    /** y then x */
    @Override
    public int compareTo(QwirkleLocation other) {
        if (other == null)
            return 1;
        else
            return y == other.y ? x - other.x : y - other.y;
    }

    public QwirkleLocation getLeft() {
        return new QwirkleLocation(x-1, y);
    }
    public QwirkleLocation getRight() {
        return new QwirkleLocation(x+1, y);
    }
    public QwirkleLocation getBelow() {
        return new QwirkleLocation(x, y-1);
    }
    public QwirkleLocation getAbove() {
        return new QwirkleLocation(x, y+1);
    }
}
