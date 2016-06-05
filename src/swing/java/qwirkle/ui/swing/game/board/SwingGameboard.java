package qwirkle.ui.swing.game.board;

import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirkleBoard;
import qwirkle.game.base.QwirkleGrid;
import qwirkle.game.base.QwirkleKit;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.base.impl.QwirkleBoardImpl;
import qwirkle.game.event.GameStarted;
import qwirkle.game.event.TurnCompleted;
import qwirkle.ui.control.HypotheticalPlayController;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.event.PlayPiece;
import qwirkle.ui.view.colors.HypotheticalPlayBgColors;

// TODO show points of hypothetical placement in highlight
/** An active playing surface that facilitates playing a next turn by showing
 *  a hypothetical turn a player is building. */
public class SwingGameboard extends SwingGrid {
    // The play the player is currently making
    public HypotheticalPlayController hypoPlay;
    public QwirkleUIController control;

    public SwingGameboard(final QwirkleUIController control) {
        super(control.getEventBus(), DisplayType.gameboard);
        this.control = control;
        // When a placement is confirmed or canceled, update our display of the hypothetical play.
        control.register(new Object() {
            /** Clear the board when a new game starts. */
            @Subscribe public void gameStarted(GameStarted started) {
                setHighlight(null);
                setGrid(null); // force a refresh
            }

            /** Receive new turn notifications from the event bus. */
            @Subscribe public void nextTurn(TurnCompleted turn) {
                setHighlight(QwirkleKit.placementsToLocations(turn.getPlacements()));
                setGrid(turn.getGrid());
            }

            /** Show hypothetical play as it develops. */
            @Subscribe public void play(PlayPiece event) {
                // note: if a turn is confirmed, a QwirkleTurn will post and super will update
                if (event.isPhaseAccept())
                    setGrid(hypoPlay.getHypotheticalBoard());
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

    @Override
    public void setGrid(QwirkleGrid grid) {
        if (grid == null)
            //noinspection unchecked
            grid = new QwirkleBoardImpl(control.getGame().getSettings());
        super.setGrid(grid);
    }

    /** Highlight the hypothetical play. */
    @Override
    public SwingPiece createPiecePanel(int x, int y) {
        // normal ...
        SwingPiece result = super.createPiecePanel(x, y);
        QwirkleGrid grid = getGrid();
        QwirklePlacement placement = grid == null ? null : grid.getPlacement(x, y);
        // ... except if it's part of the hypothetical play
        if (placement != null && hypoPlay.containsPlacement(placement)) {
            result.getBackgroundManager()
                    .setColors(new HypotheticalPlayBgColors(getGrid().getPlacement(x, y)));
            if (hypoPlay.isRemovable(placement))
                result.makeDraggable(hypoPlay.getCurrentPlayer());
        }
        return result;
    }
}
