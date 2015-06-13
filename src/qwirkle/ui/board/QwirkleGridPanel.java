package qwirkle.ui.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.control.event.GameStarted;
import qwirkle.control.event.HighlightTurn;
import qwirkle.game.QwirkleGrid;
import qwirkle.game.QwirklePlacement;
import qwirkle.game.QwirkleTurn;

import javax.swing.*;

public class QwirkleGridPanel extends JPanel {
    private QwirkleGridLayout layout;
    private boolean blankIncluded;
    private QwirkleTurn lastTurn;
    private EventBus eventBus;

    public QwirkleGridPanel(EventBus bus) {
        this.eventBus = bus;
        layout = new QwirkleGridLayout();
        setLayout(layout);
        setBlankIncluded(true);
        bus.register(new Object() {
            /** Receive new turn notifications from the event bus. */
            @Subscribe public void nextTurn(QwirkleTurn turn) {
                lastTurn = turn;
                refresh();
            }

            /** Need this to catch new games, since a turn isn't posted right away. */
            @Subscribe public void gameStarted(GameStarted started) {
                lastTurn = null;
                refresh();
            }

            @Subscribe public void highlightTurn(HighlightTurn highlight) {
                setHighlighted(highlight.getTurn());
            }

//            @Subscribe public void gameOver(GameOver go) {
//                lastTurn = null;
//            }
        });
    }

    public void setHighlighted(QwirkleTurn turn) {

    }

    public EventBus getEventBus() { return eventBus; }

    /** Are blank spots included? Default true. */
    public void setBlankIncluded(boolean blankIncluded) {
        if (this.blankIncluded != blankIncluded) {
            this.blankIncluded = blankIncluded;
            layout.setMargin(blankIncluded ? 1 : 0);
            // refresh
            refresh();
        }
    }

    private QwirkleGrid getGrid() {
        return lastTurn == null ? null : lastTurn.getGrid();
    }

    public void refresh() {
        synchronized (getTreeLock()) {
            QwirkleGrid grid = getGrid();
            removeAll();
            layout.setGrid(grid);
            if (grid != null)
                if (blankIncluded) {
                    for (int y = grid.getYMin() - 1; y <= grid.getYMax() + 1; ++y)
                        for (int x = grid.getXMin() - 1; x <= grid.getXMax() + 1; ++x)
                            add(new QwirklePiecePanel(eventBus, grid, x, y, lastTurn.containsLocation(x, y)));
                } else {
                    for (QwirklePlacement p : grid.getPlacements())
                        add(new QwirklePiecePanel(eventBus, p, lastTurn.containsLocation(p.getLocation())));
                }
        }
        validate();
        repaint();
    }
}
