package qwirkle.ui.swing.game.board;

import com.google.common.eventbus.Subscribe;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.control.HypotheticalPlay;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.event.PlayPiece;
import qwirkle.game.base.QwirkleBoard;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.ui.swing.colors.HypotheticalPlayBgColors;

// TODO show points of hypothetical placement in highlight
/** A {@link QwirkleGridPanel} that facilitates playing a next turn by showing
 *  a hypothetical turn a player is building. */
public class QwirklePlayableGridPanel extends QwirkleGridPanel {
    // The play the player is currently making
    public HypotheticalPlay hypoPlay;

    public QwirklePlayableGridPanel(final QwirkleUIController control) {
        super(control.getEventBus());
        control.register(new Object() {
            /** When a placement is confirmed or canceled, update our display of the hypothetical play. */
            @Subscribe
            public void play(PlayPiece event) {
                // note: if a turn is confirmed, a QwirkleTurn will post and super will update
                if (event.isAccept())
                    setGrid(hypoPlay.getHypotheticalBoard());
                // on a cancel, we need to delay removing the piece from the board, since it will confuse Swing's drag-and-drop
                else if (event.isCancel()) {
                    control.register(new Object() {
                        @Subscribe public void drop(DragPiece event) {
                            if (event.isDrop()) {
                                control.unregister(this);
                                setGrid(hypoPlay.getHypotheticalBoard());
                            }
                        }
                    });
                }
            }
        });
        this.hypoPlay = control.getInteraction().getHypotheticalPlay();
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
        QwirklePlacement placement = getGrid().getPlacement(x, y);
        // ... except if it's part of the hypothetical play
        if (hypoPlay.containsPlacement(placement)) {
            result.getBackgroundManager()
                    .setColors(new HypotheticalPlayBgColors(getGrid().getPlacement(x, y)));
            if (hypoPlay.isRemovable(placement))
                result.makeDraggable(hypoPlay.getCurrentPlayer(), hypoPlay.getAcceptedPlay(placement));
        }
        return result;
    }
}
