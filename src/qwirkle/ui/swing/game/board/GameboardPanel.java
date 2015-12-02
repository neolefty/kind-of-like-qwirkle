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
/** An active playing surface that facilitates playing a next turn by showing
 *  a hypothetical turn a player is building. */
public class GameboardPanel extends QwirkleGridPanel {
    // The play the player is currently making
    public HypotheticalPlay hypoPlay;

    public GameboardPanel(final QwirkleUIController control) {
        super(control.getEventBus(), DisplayType.gameboard);
        // When a placement is confirmed or canceled, update our display of the hypothetical play.
        control.register(new Object() {
            @Subscribe
            public void play(PlayPiece event) {
                // note: if a turn is confirmed, a QwirkleTurn will post and super will update
                if (event.isPhaseAccept())
                    setGrid(hypoPlay.getHypotheticalBoard());
                // on a cancel, we need to delay removing the piece from the board, since it will confuse Swing's drag-and-drop
                else if (event.isPhaseCancel()) {
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
        this.hypoPlay = control.getHypothetical();
    }

    /** The current board not including the hypothetical play. */
    public QwirkleBoard getBoard() {
        return hypoPlay == null
                ? (QwirkleBoard) super.getGrid()
                : hypoPlay.getBoard();
    }

    // TODO only if no discards
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
