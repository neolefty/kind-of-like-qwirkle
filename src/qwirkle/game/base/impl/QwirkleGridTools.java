package qwirkle.game.base.impl;

import qwirkle.game.base.QwirklePlacement;

import java.util.Collection;

/** Helper methods */
public class QwirkleGridTools {
    public static int getYMin
            (Collection<QwirklePlacement> placements)
    {
        if (placements.isEmpty())
            return 0;
        else {
            int result = Integer.MAX_VALUE;
            for (QwirklePlacement p : placements)
                if (p.getY() < result)
                    result = p.getY();
            return result;
        }
    }

    public static int getYMax
            (Collection<QwirklePlacement> placements)
    {
        if (placements.isEmpty())
            return 0;
        else {
            int result = Integer.MIN_VALUE;
            for (QwirklePlacement p : placements)
                if (p.getY() > result)
                    result = p.getY();
            return result;
        }
    }

    public static int getXMin
            (Collection<QwirklePlacement> placements)
    {
        if (placements.isEmpty()) return 0;
        else {
            int result = Integer.MAX_VALUE;
            for (QwirklePlacement p : placements)
                if (p.getX() < result)
                    result = p.getX();
            return result;
        }
    }

    public static int getXMax
            (Collection<QwirklePlacement> placements)
    {
        if (placements.isEmpty())
            return 0;
        else {
            int result = Integer.MIN_VALUE;
            for (QwirklePlacement p : placements)
                if (p.getX() > result)
                    result = p.getX();
            return result;
        }
    }
}
