package qwirkle.ui.swing.game.board;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirkleGrid;
import qwirkle.game.base.QwirkleLocation;
import qwirkle.game.base.QwirklePlacement;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.base.impl.QwirkleGridImpl;
import qwirkle.game.base.impl.QwirkleGridTools;
import qwirkle.game.event.TurnStarting;
import qwirkle.ui.view.QwirkleGridDisplay;
import qwirkle.ui.view.QwirklePieceDisplay;
import qwirkle.ui.control.SelfDisposingEventSubscriber;
import qwirkle.ui.swing.util.SwingPlatformAttacher;

import javax.swing.*;
import java.awt.*;
import java.util.*;

// TODO animate transition
public class QwirkleGridPanel extends JPanel implements QwirkleGridDisplay {
    private QwirkleGridLayout layout;
    private boolean blankIncluded;
    private QwirkleGrid grid;
    private Collection<QwirkleLocation> highlight;
    private EventBus eventBus;
    private DisplayType displayType;
    private QwirklePlayer curPlayer;
    private final Map<QwirkleLocation, QwirklePiecePanel> panelMap = new HashMap<>();

    private final Object alwaysShownSync = new Object();
    private Set<QwirkleLocation> alwaysShown;

    private boolean draggable = false;
    private QwirklePlayer dragPlayer = null;

    public QwirkleGridPanel(EventBus bus, DisplayType displayType) {
        this.eventBus = bus;
        this.displayType = displayType;
        layout = new QwirkleGridLayout(this);
        setLayout(layout);
        setBlankIncluded(true);
        bus.register(new SelfDisposingEventSubscriber(bus, new SwingPlatformAttacher(this)) {
            @Subscribe public void updatePlayer(TurnStarting event) { curPlayer = event.getCurPlayer(); }
        });
    }

    /** Are blank spots included? Default true. */
    public void setBlankIncluded(boolean blankIncluded) {
        if (this.blankIncluded != blankIncluded) {
            this.blankIncluded = blankIncluded;
            layout.setMargin(blankIncluded ? 1 : 0);
            refresh();
        }
    }

    /** Highlight these locations. */
    @Override public void setHighlight(Collection<QwirkleLocation> highlight) { this.highlight = highlight; }

    private void refresh() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (getTreeLock()) {
                    QwirkleGrid grid = getGrid();
                    if (grid == null) grid = new QwirkleGridImpl(); // dummy grid to help with rendering
                    removeAll();
                    layout.setGrid(grid);
                    if (blankIncluded) {
                        int margin = layout.getMargin();
                        for (int y = grid.getYMin() - margin; y <= grid.getYMax() + margin; ++y)
                            for (int x = grid.getXMin() - margin; x <= grid.getXMax() + margin; ++x)
                                addPiecePanel(createPiecePanel(x, y));
                    } else {
                        for (QwirklePlacement p : grid.getPlacements())
                            addPiecePanel(p.getLocation());
                    }
                    // include the locations we've been asked to always show
                    synchronized (alwaysShownSync) {
                        if (alwaysShown != null)
                            for (QwirkleLocation loc : alwaysShown)
                                if (!panelMap.containsKey(loc))
                                    addPiecePanel(loc);
                    }
                }
                validate();
                repaint();
            }
        });
    }

    public QwirklePiecePanel createPiecePanel(int x, int y) {
        return new QwirklePiecePanel(eventBus, this, x, y, isHighlighted(x, y));
    }

    private boolean isHighlighted(int x, int y) {
        return highlight != null && highlight.contains(new QwirkleLocation(x, y));
    }

    @Override
    public void removeAll() {
        synchronized (getTreeLock()) {
            panelMap.clear();
            super.removeAll();
        }
    }

    /** Be sure to always show these locations.
     *  @param refresh if true, refresh the display immediately;
     *                 if false, wait until the next call to {@link #setGrid}. */
    public void setAlwayShown(Collection<QwirkleLocation> locations, boolean refresh) {
        synchronized (alwaysShownSync) {
            if (locations == null || locations.size() == 0)
                alwaysShown = null;
            else
                this.alwaysShown = Collections.unmodifiableSet(new HashSet<>(locations));
        }
        if (refresh)
            refresh();
    }

//    /** Locations that this is always sure to show. */
//    public Set<QwirkleLocation> getAlwaysShown() {
//        return alwaysShown;
//    }

    private void addPiecePanel(QwirkleLocation loc) {
        addPiecePanel(createPiecePanel(loc.getX(), loc.getY()));
    }

    private void addPiecePanel(QwirklePiecePanel pp) {
        synchronized (getTreeLock()) { // probably redundant
            if (draggable && pp.getPiece() != null)
                pp.makeDraggable(curPlayer);
            panelMap.put(pp.getQwirkleLocation(), pp);
            add(pp);
        }
    }

    /** Enable drag-and-drop operations starting from this grid. */
    public void makeDraggable(QwirklePlayer player) {
        if (!draggable || this.dragPlayer != player)
            synchronized (getTreeLock()) {
                this.dragPlayer = player;
                this.draggable = true;
                for (Component c : getComponents()) {
                    if (c instanceof QwirklePiecePanel)
                        ((QwirklePiecePanel) c).makeDraggable(curPlayer);
                }
            }
    }

    /** Disable drag-and-drop operations starting from this grid. */
    public void makeUndraggable() {
        if (draggable)
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
//    @Override public int getPieceWidth() { return layout.getPieceSize(); }
//    @Override public int getPieceHeight() { return layout.getPieceSize(); }
    @Override public DisplayType getDisplayType() { return displayType; }

    /** Least x-coordinate that is visible, including margin and all locations. */
    public int getXMin() {
        int result = grid == null ? 0 : grid.getXMin();
        synchronized (alwaysShownSync) {
            if (alwaysShown != null)
                result = Math.min(result, QwirkleGridTools.getXMin(alwaysShown));
        }
        return result - layout.getMargin();
    }

    /** Least y-coordinate that is visible, including margin and all locations. */
    public int getYMin() {
        int result = grid == null ? 0 : grid.getYMin();
        synchronized (alwaysShownSync) {
            if (alwaysShown != null)
                result = Math.min(result, QwirkleGridTools.getYMin(alwaysShown));
        }
        return result - layout.getMargin();
    }

    /** Greatest x-coordinate that is visible, including margin and all locations. */
    public int getXMax() {
        int result = grid == null ? 0 : grid.getXMax();
        synchronized (alwaysShownSync) {
            if (alwaysShown != null)
                result = Math.max(result, QwirkleGridTools.getXMax(alwaysShown));
        }
        return result + layout.getMargin();
    }

    /** Greatest y-coordinate that is visible, including margin and all locations. */
    public int getYMax() {
        int result = grid == null ? 0 : grid.getYMax();
        synchronized (alwaysShownSync) {
            if (alwaysShown != null)
                result = Math.max(result, QwirkleGridTools.getYMax(alwaysShown));
        }
        return result + layout.getMargin();
    }

    /** Get the piece panel at a board location. */
    public QwirklePiecePanel getPiecePanel(QwirkleLocation loc) {
        synchronized (getTreeLock()) {
            return panelMap.get(loc);
        }
    }

    @Override
    public QwirklePieceDisplay getPieceDisplay(QwirkleLocation location) {
        return getPiecePanel(location);
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
