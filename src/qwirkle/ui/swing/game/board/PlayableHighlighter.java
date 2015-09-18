package qwirkle.ui.swing.game.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.event.PieceDrag;
import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirkleGrid;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirklePlacement;
import qwirkle.ui.swing.colors.ColorSet;
import qwirkle.ui.swing.colors.HypotheticalPlayBgColors;

import java.util.Collection;

// TODO make playable spots pulse
// TODO abstract this from Swing and move to qwirkle.control, like PieceDropWatcher?
/** A listener that highlights playable spots on a board. */
public class PlayableHighlighter {
    private QwirklePlayableGridPanel gridPanel;

    public PlayableHighlighter(EventBus bus, QwirklePlayableGridPanel gridPanel, ColorSet playableColors) {
        bus.register(this);
        this.gridPanel = gridPanel;
    }

    @Subscribe
    public void drag(PieceDrag event) {
        if (event.isPickup())
            highlightPlayable(event);
        else if (event.isCancel() || event.isDrop())
            unhighlight(event);
    }

    private void unhighlight(PieceDrag event) {
        forEachLegalQPP(event.getPiece(), new QPPer() {
            @Override
            public void go(QwirklePiecePanel panel) {
                panel.getBackgroundManager().popColors();
            }
        });
    }

    // TODO draw inverted shape in each possible square
    private void highlightPlayable(PieceDrag event) {
        final ColorSet colors = new HypotheticalPlayBgColors(event.getPiece());
        forEachLegalQPP(event.getPiece(), new QPPer() {
            @Override
            public void go(QwirklePiecePanel panel) {
                panel.getBackgroundManager().pushColors(colors);
            }
        });
    }

    /** Lambda. */
    private interface QPPer {
        void go(QwirklePiecePanel panel);
    }

    /** Loop over all the legal placements for <tt>goer</tt>
     *  and do something to each corresponding {@link QwirklePiecePanel}. */
    private void forEachLegalQPP(QwirklePiece piece, QPPer goer) {
        QwirkleBoard board = getBoard();
        if (board != null) {
            Collection<QwirklePlacement> legalMoves = gridPanel.getLegalMoves(piece);
            for (QwirklePlacement move : legalMoves) {
                // do we need to check for null? hypothetically no ...
                QwirklePiecePanel panel = gridPanel.getPiecePanel(move.getLocation());
                goer.go(panel);
            }
        }
    }

    private QwirkleBoard getBoard() {
        QwirkleGrid grid = gridPanel.getGrid();
        if (grid == null)
            return null;
        else if (grid instanceof QwirkleBoard)
            return (QwirkleBoard) grid;
        else
            // could wrap it in a QwirkleBoard, but why are we playing on a non-board?
            throw new UnsupportedOperationException(grid.getClass().getName()
                    + " doesn't implement QwirkleBoard.");
    }
}
