package qwirkle.ui.swing.game.board;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameController;
import qwirkle.control.HypotheticalPlay;
import qwirkle.event.PiecePlay;
import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirkleLocation;
import qwirkle.ui.swing.colors.HypotheticalPlayBgColors;

/** A {@link QwirkleGridPanel} that facilitates playing a next turn. */
public class QwirklePlayableGridPanel extends QwirkleGridPanel {
    // The play the player is currently making
    public HypotheticalPlay hypoPlay;

    public QwirklePlayableGridPanel(GameController control) {
        super(control.getEventBus());
        control.register(new Object() {
            @Subscribe
            public void play(PiecePlay event) {
                // refresh if a placement is accepted or canceled
                // note: if a turn is confirmed, a QwirkleTurn will post and super will update
                if (event.isAccept() || event.isCancel())
                    setGrid(hypoPlay.getHypotheticalBoard());
            }
        });
        this.hypoPlay = control.getEventsController().getHypotheticalPlay();
    }

    /** The current board not including the hypothetical play. */
    public QwirkleBoard getBoard() {
        return hypoPlay == null
                ? (QwirkleBoard) super.getGrid()
                : hypoPlay.getBoard();
    }

    /** Highlight the hypothetical play. */
    @Override
    public QwirklePiecePanel createPiecePanel(int x, int y) {
        // normal ...
        QwirklePiecePanel result = super.createPiecePanel(x, y);
        // ... except if it's part of the hypothetical play
        if (inHypotheticalPlay(new QwirkleLocation(x, y)))
            result.getBackgroundManager()
                    .setColors(new HypotheticalPlayBgColors(getGrid().getPlacement(x, y)));
        return result;
    }

    /** Is this location in the hypothetical play? */
    private boolean inHypotheticalPlay(QwirkleLocation loc) {
        return hypoPlay.containsPlacement(loc);
    }
}
