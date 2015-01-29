package qwirkle.control;

import qwirkle.game.QwirkleColor;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirkleShape;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Miscellaneous utility methods. */
public class QwirkleKit {
    /** Used for deciding who goes first, based on who has the largest
     *  playable set of pieces. How many of the same piece or color are there in this set of pieces? */
    public static int countMatches(Collection<QwirklePiece> pieces) {
        // only count unique pieces (drop dupes)
        Set<QwirklePiece> unique = new HashSet<>(pieces);

        // 1. compile list of all the shapes and colors that are represented
        Set<QwirkleColor> colors = new HashSet<>();
        Set<QwirkleShape> shapes = new HashSet<>();
        for (QwirklePiece piece : unique) {
            colors.add(piece.getColor());
            shapes.add(piece.getShape());
        }

        // 2. what is the largest number of the same shape?
        int max = 0;
        for (QwirkleShape shape : shapes) {
            int n = countShapes(unique, shape);
            if (n > max)
                max = n;
        }

        // 3. what is the largest number of the same color?
        for (QwirkleColor color : colors) {
            int n = countColors(unique, color);
            if (n > max)
                max = n;
        }

        return max;
    }

    private static int countShapes(Collection<QwirklePiece> pieces, QwirkleShape shape) {
        int result = 0;
        for (QwirklePiece p : pieces)
            if (p.getShape() == shape)
                ++result;
        return result;
    }

    private static int countColors(Collection<QwirklePiece> pieces, QwirkleColor color) {
        int result = 0;
        for (QwirklePiece p : pieces)
            if (p.getColor() == color)
                ++result;
        return result;
    }
}
