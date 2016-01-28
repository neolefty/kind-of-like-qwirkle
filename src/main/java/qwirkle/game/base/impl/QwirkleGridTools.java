package qwirkle.game.base.impl;

import qwirkle.game.base.HasQwirkleLocation;

import java.util.Collection;

/** Helper methods */
public class QwirkleGridTools {
    public static <T extends HasQwirkleLocation> int getYMin(Collection<T> locs) {
        if (locs.isEmpty())
            return 0;
        else {
            int result = Integer.MAX_VALUE;
            for (HasQwirkleLocation loc : locs)
                if (loc.getQwirkleLocation().getY() < result)
                    result = loc.getQwirkleLocation().getY();
            return result;
        }
    }

    public static <T extends HasQwirkleLocation> int getYMax(Collection<T> locs) {
        if (locs.isEmpty())
            return 0;
        else {
            int result = Integer.MIN_VALUE;
            for (HasQwirkleLocation loc : locs)
                if (loc.getQwirkleLocation().getY() > result)
                    result = loc.getQwirkleLocation().getY();
            return result;
        }
    }

    public static <T extends HasQwirkleLocation> int getXMin(Collection<T> locs) {
        if (locs.isEmpty()) return 0;
        else {
            int result = Integer.MAX_VALUE;
            for (HasQwirkleLocation loc : locs)
                if (loc.getQwirkleLocation().getX() < result)
                    result = loc.getQwirkleLocation().getX();
            return result;
        }
    }

    public static <T extends HasQwirkleLocation> int getXMax(Collection<T> locs) {
        if (locs.isEmpty())
            return 0;
        else {
            int result = Integer.MIN_VALUE;
            for (HasQwirkleLocation loc : locs)
                if (loc.getQwirkleLocation().getX() > result)
                    result = loc.getQwirkleLocation().getX();
            return result;
        }
    }
}
