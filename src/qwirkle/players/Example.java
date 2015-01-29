package qwirkle.players;

import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Example {
    /** Find a line of pieces that we can play. */
    List<QwirklePlacement> findLine(QwirkleBoard board, List<QwirklePiece> wantToPlay) {
        return findLineRecursive(board, wantToPlay, new ArrayList<QwirklePlacement>());
    }

    /** Recursive function to add pieces to a line. */
    List<QwirklePlacement> findLineRecursive(
            QwirkleBoard board,
            List<QwirklePiece> wantToPlay,
            List<QwirklePlacement> line)
    {
        // search through all the pieces that we want to play
        for (QwirklePiece piece : wantToPlay) {
            // find one that is in line with the pieces we have already played
            Collection<QwirklePlacement> placements = board.getLegalPlacements(piece);
            for (QwirklePlacement placement : placements) {
                if (isInLine(placement, line)) {
                    // find more pieces in the line
                    line.add(placement);
                    wantToPlay.remove(placement.getPiece());
                    return findLineRecursive(board.play(placement), wantToPlay, line);
                }
            }
        }
        // didn't find any that we can add to our line
        return line;
    }

    private boolean isInLine(QwirklePlacement placement, List<QwirklePlacement> list) {
        ArrayList<QwirklePlacement> bigLine = new ArrayList<>(list);
        bigLine.add(placement);
        return isInLine(bigLine);
    }

    private boolean isInLine(ArrayList<QwirklePlacement> list) {
        return isHorizontal(list) || isVertical(list);
    }

    private boolean isHorizontal(ArrayList<QwirklePlacement> list) {
        if (list.size() <= 1) return true;
        else {
            int y = list.get(0).getY();
            for (QwirklePlacement p : list)
                if (p.getY() != y)
                    return false;
            return true;
        }
    }

    private boolean isVertical(ArrayList<QwirklePlacement> list) {
        if (list.size() <= 1) return true;
        else {
            int x = list.get(0).getX();
            for (QwirklePlacement p : list)
                if (p.getX() != x)
                    return false;
            return true;
        }
    }
}
