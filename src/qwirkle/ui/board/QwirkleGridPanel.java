package qwirkle.ui.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.control.event.GameStarted;
import qwirkle.game.QwirkleGrid;
import qwirkle.game.QwirkleLocation;
import qwirkle.game.QwirklePlacement;
import qwirkle.game.QwirkleTurn;

import javax.swing.*;
import java.awt.*;

public class QwirkleGridPanel extends JPanel implements QwirkleGridDisplay {
    private QwirkleGridLayout layout;
    private boolean blankIncluded;
    private QwirkleGrid grid;
    private QwirkleTurn lastTurn;
    private EventBus eventBus;

    private boolean draggable = false;

    public QwirkleGridPanel(EventBus bus) {
        this.eventBus = bus;
        layout = new QwirkleGridLayout();
        setLayout(layout);
        setBlankIncluded(true);
        bus.register(new Object() {
            /** Receive new turn notifications from the event bus. */
            @Subscribe public void nextTurn(QwirkleTurn turn) {
                lastTurn = turn;
                grid = lastTurn.getGrid();
                refresh();
            }

            /** Clear the board when a new game starts. */
            @Subscribe public void gameStarted(GameStarted started) {
                lastTurn = null;
                grid = started.getStatus().getBoard();
                refresh();
            }
        });
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

    public void refresh() {
        synchronized (getTreeLock()) {
            QwirkleGrid grid = getGrid();
            removeAll();
            layout.setGrid(grid);
            if (grid != null)
                if (blankIncluded) {
                    for (int y = grid.getYMin() - 1; y <= grid.getYMax() + 1; ++y)
                        for (int x = grid.getXMin() - 1; x <= grid.getXMax() + 1; ++x)
                            addPiecePanel(new QwirklePiecePanel(eventBus, grid, x, y, isInLastTurn(x, y)));
                } else {
                    for (QwirklePlacement p : grid.getPlacements()) {
                        QwirkleLocation loc = p.getLocation();
                        addPiecePanel(new QwirklePiecePanel(eventBus, grid, loc, isInLastTurn(loc)));
                    }
                }
        }
        validate();
        repaint();
    }

    private boolean isInLastTurn(int x, int y) {
        return lastTurn != null && lastTurn.containsLocation(x, y);
    }

    private boolean isInLastTurn(QwirkleLocation loc) {
        return isInLastTurn(loc.getX(), loc.getY());
    }

    private void addPiecePanel(QwirklePiecePanel pp) {
        if (draggable && pp.getPiece() != null)
            pp.setDraggable(true);
        add(pp);
    }

    /** Can drag-and-drop operations start from this grid? */
    public void setDraggable(boolean draggable) {
        synchronized (getTreeLock()) {
            this.draggable = draggable;
            for (Component c : getComponents()) {
                if (c instanceof QwirklePiecePanel)
                    ((QwirklePiecePanel) c).setDraggable(draggable);
            }
        }
    }

    @Override
    public QwirkleGrid getGrid() { return grid; }

    @Override
    public Dimension getPieceSize() {
        int square = layout.getPieceSize();
        return new Dimension(square, square);
    }

    @Override
    public QwirklePieceDisplay getPieceDisplay(int x, int y) {
        Component c = findComponentAt(x, y);
        if (c instanceof QwirklePieceDisplay)
            return (QwirklePieceDisplay) c;
        else
            return null;
    }
}
