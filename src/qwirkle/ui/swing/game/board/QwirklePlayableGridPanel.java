package qwirkle.ui.swing.game.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.event.GameStarted;
import qwirkle.event.PiecePlay;
import qwirkle.event.QwirkleTurn;
import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;
import qwirkle.ui.swing.colors.HypotheticalPlayBgColors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** A {@link QwirkleGridPanel} that facilitates playing a next turn. */
public class QwirklePlayableGridPanel extends QwirkleGridPanel {
    // The hypothetical play that is being formed
    private List<QwirklePlacement> hypotheticalPlay;

    // The board including the hypothetical play
    private QwirkleBoard hypotheticalBoard;

    // The board not including the hypothetical play
    private QwirkleBoard board;

    public QwirklePlayableGridPanel(EventBus bus) {
        super(bus);
    }

    private void clearHypothetical() {
        if (hypotheticalPlay != null) {
            hypotheticalPlay = null;
            hypotheticalBoard = null;
            super.setGrid(board);
        }
    }

    public Collection<QwirklePlacement> getLegalMoves(QwirklePiece piece) {
        if (hypotheticalPlay == null)
            return board.getLegalPlacements(piece);
        else
            return board.getLegalPlacements(getHypotheticalPlay(), piece);
    }

    @Subscribe
    public void play(PiecePlay event) {
        playHypothetically(event.getPlacement());
    }

    /** Add to the hypothetical play we're making. */
    public void playHypothetically(QwirklePlacement placement) {
        if (hypotheticalPlay == null) {
            hypotheticalPlay = new ArrayList<>();
            board = (QwirkleBoard) super.getGrid();
            if (board == null)
                throw new NullPointerException("Board is null. Can't make hypothetical plays.");
        }
        hypotheticalPlay.add(placement);

        hypotheticalBoard = board.play(getHypotheticalPlay());
        super.setGrid(hypotheticalBoard);
    }

    /** The play currently under consideration -- null if none. */
    public List<QwirklePlacement> getHypotheticalPlay() {
        if (hypotheticalPlay != null)
            // copy and make immutable
            return Collections.unmodifiableList(new ArrayList<>(hypotheticalPlay));
        else
            return null;
    }

    @Override
    public QwirklePiecePanel createPiecePanel(int x, int y) {
        QwirklePiecePanel result = super.createPiecePanel(x, y);
        if (inHypotheticalPlay(x, y))
            result.getBackgroundManager()
                    .setColors(new HypotheticalPlayBgColors(getGrid().getPlacement(x, y)));
        return result;
    }

    @Override
    public void nextTurn(QwirkleTurn turn) {
        clearHypothetical();
        super.nextTurn(turn);
    }

    @Override
    public void gameStarted(GameStarted started) {
        clearHypothetical();
        super.gameStarted(started);
    }

    /** Is this location in the hypothetical play? */
    private boolean inHypotheticalPlay(int x, int y) {
        if (hypotheticalPlay != null)
            for (QwirklePlacement p : hypotheticalPlay)
                if (p.getLocation().equals(x, y))
                    return true;
        return false; // didn't find a match
    }
}
