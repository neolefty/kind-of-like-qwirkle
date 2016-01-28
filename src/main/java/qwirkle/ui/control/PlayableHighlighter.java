package qwirkle.ui.control;

import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirkleBoard;
import qwirkle.game.base.QwirkleGrid;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.view.QwirkleGridDisplay;
import qwirkle.ui.view.QwirklePieceDisplay;
import qwirkle.ui.view.colors.ColorSet;
import qwirkle.ui.view.colors.HypotheticalPlayBgColors;

import java.util.Collection;
import java.util.Collections;

// TODO make playable spots pulse
// TODO abstract this from Swing and move to qwirkle.game.control, like PieceDropWatcher
/** A listener that highlights playable spots on a board. */
public class PlayableHighlighter {
    private QwirkleGridDisplay display;
    private HypotheticalPlayController hypo;

    public PlayableHighlighter(QwirkleUIController control, QwirkleGridDisplay display) {
        control.register(this);
        this.display = display;
        this.hypo = control.getInteraction().getHypotheticalPlay();
    }

    @Subscribe
    public void drag(DragPiece event) {
        if (event.isActionPickup())
            highlightPlayable(event);
        else if (event.isActionCancel() || event.isActionDrop())
            unhighlight(event);
    }

    private void unhighlight(DragPiece event) {
        forEachLegalQPP(event.getPiece(), false, new QPDer() {
            @Override
            public void go(QwirklePieceDisplay display) {
                display.getBackgroundManager().popColors();
            }
        });
    }

    // TODO draw inverted shape in each possible square
    private void highlightPlayable(DragPiece event) {
        final ColorSet colors = new HypotheticalPlayBgColors(event.getPiece());
        forEachLegalQPP(event.getPiece(), true, new QPDer() {
            @Override
            public void go(QwirklePieceDisplay display) {
                display.getBackgroundManager().pushColors(colors);
            }
        });
    }

    /** Lambda. */
    private interface QPDer {
        void go(QwirklePieceDisplay pieceDisplay);
    }

    /** Loop over all the legal placements for <tt>goer</tt>
     *  and do something to each corresponding {@link QwirklePieceDisplay}.
     *  @param strict If true, do this for every legal move and throw a NPE if the panel can't be found.
     *                If false, just skip it if the panel can't be found. */
    private void forEachLegalQPP(QwirklePiece piece, boolean strict, QPDer goer) {
        QwirkleBoard board = getBoard();
        if (board != null) {
            Collection<QwirklePlacement> legalMoves = hypo.getLegalMoves(piece);
            // special case: empty board but not zero-centered (like if we've walked a pair of pieces off-center)
            if (display.getGrid().size() == 1) {
                QwirklePlacement placement = display.getGrid().getPlacements().iterator().next();
                if (placement.getPiece() == piece)
                    legalMoves = Collections.singletonList(placement);
            }
            for (QwirklePlacement move : legalMoves) {
                // do we need to check for null? yes because of race conditions
                // (that hopefully haven't messed up legality -- practice they're related
                // to re-zeroing the board by dragging the very first piece.)
                QwirklePieceDisplay pieceDisplay = display.getPieceDisplay(move.getLocation());
                if (strict)
                    goer.go(pieceDisplay);
                else if (pieceDisplay != null)
                    goer.go(pieceDisplay);
            }
        }
    }

    private QwirkleBoard getBoard() {
        QwirkleGrid grid = display.getGrid();
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
