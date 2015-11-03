package qwirkle.ui.swing.game.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.event.GameStarted;
import qwirkle.game.AsyncPlayer;
import qwirkle.game.QwirkleGrid;
import qwirkle.game.QwirkleLocation;
import qwirkle.game.QwirklePlacement;
import qwirkle.event.TurnCompleted;
import qwirkle.ui.QwirkleGridDisplay;
import qwirkle.ui.QwirklePieceDisplay;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class QwirkleGridPanel extends JPanel implements QwirkleGridDisplay {
    private QwirkleGridLayout layout;
    private boolean blankIncluded;
    private QwirkleGrid grid;
    private TurnCompleted lastTurn;
    private EventBus eventBus;

    private boolean draggable = false;
    private AsyncPlayer dragPlayer = null;

    public QwirkleGridPanel(EventBus bus) {
        this.eventBus = bus;
        layout = new QwirkleGridLayout();
        setLayout(layout);
        setBlankIncluded(true);
        bus.register(this);
    }

    // listen for events
    /** Receive new turn notifications from the event bus. */
    @Subscribe public void nextTurn(TurnCompleted turn) {
        lastTurn = turn;
        setGrid(lastTurn.getGrid());
    }

    /** Clear the board when a new game starts. */
    @Subscribe public void gameStarted(GameStarted started) {
        lastTurn = null;
        setGrid(started.getStatus().getBoard());
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (getTreeLock()) {
                    QwirkleGrid grid = getGrid();
                    removeAll();
                    layout.setGrid(grid);
                    if (grid != null)
                        if (blankIncluded) {
                            for (int y = grid.getYMin() - 1; y <= grid.getYMax() + 1; ++y)
                                for (int x = grid.getXMin() - 1; x <= grid.getXMax() + 1; ++x)
                                    addPiecePanel(createPiecePanel(x, y));
                        } else {
                            for (QwirklePlacement p : grid.getPlacements()) {
                                QwirkleLocation loc = p.getLocation();
                                addPiecePanel(createPiecePanel(loc.getX(), loc.getY()));
                            }
                        }
                }
                validate();
                repaint();
            }
        });
    }

    public QwirklePiecePanel createPiecePanel(int x, int y) {
        return new QwirklePiecePanel(eventBus, grid, x, y, isInLastTurn(x, y));
    }

    private boolean isInLastTurn(int x, int y) {
        return lastTurn != null && lastTurn.containsLocation(x, y);
    }

    private Map<QwirkleLocation, QwirklePiecePanel> panelMap = new HashMap<>();

    @Override
    public void removeAll() {
        synchronized (getTreeLock()) {
            panelMap.clear();
            super.removeAll();
        }
    }

    private void addPiecePanel(QwirklePiecePanel pp) {
        if (draggable && pp.getPiece() != null)
            pp.makeDraggable(dragPlayer);
        panelMap.put(pp.getQwirkleLocation(), pp);
        add(pp);
    }

    /** Enable drag-and-drop operations starting from this grid. */
    public void makeDraggable(AsyncPlayer player) {
        synchronized (getTreeLock()) {
            this.dragPlayer = player;
            this.draggable = true;
            for (Component c : getComponents()) {
                if (c instanceof QwirklePiecePanel)
                    ((QwirklePiecePanel) c).makeDraggable(dragPlayer);
            }
        }
    }

    /** Disable drag-and-drop operations starting from this grid. */
    public void makeUndraggable() {
        synchronized (getTreeLock()) {
            this.dragPlayer = null;
            this.draggable = false;
            for (Component c : getComponents()) {
                if (c instanceof QwirklePiecePanel)
                    ((QwirklePiecePanel) c).makeUndraggable();
            }
        }
    }

    public void setGrid(QwirkleGrid grid) {
        if (grid != this.grid) {
            this.grid = grid;
            refresh();
        }
    }

    @Override public QwirkleGrid getGrid() { return grid; }

    @Override public int getPieceWidth() { return layout.getPieceSize(); }

    @Override public int getPieceHeight() { return layout.getPieceSize(); }

    public QwirklePiecePanel getPiecePanel(QwirkleLocation loc) {
        return panelMap.get(loc);
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
