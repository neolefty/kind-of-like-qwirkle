package qwirkle.ui.swing.game;

import qwirkle.ui.swing.game.board.SwingGrid;
import qwirkle.ui.swing.game.player.SwingPlayer;
import qwirkle.ui.swing.util.LayoutBase;
import qwirkle.ui.view.HasAspectRatio;

import java.awt.*;
import java.util.ArrayList;

/** Lay out an entire Qwirkle game, including board and player panels.*/
public class SwingGameLayout extends LayoutBase {
    private SwingGrid gridPanel;
    private java.util.List<HasAspectRatio> stackOfPanels = new ArrayList<>();
    private boolean vertical = false;

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            // 1. Review components
            java.util.List<SwingPlayer> playerPanels = new ArrayList<>();
            gridPanel = null;
            java.util.List<HasAspectRatio> extraPanels = new ArrayList<>();
            for (int i = 0; i < parent.getComponentCount(); ++i) {
                Component comp = parent.getComponent(i);
                // list player panels separately
                if (comp instanceof SwingPlayer)
                    playerPanels.add((SwingPlayer) comp);
                else if (comp instanceof SwingGrid)
                    gridPanel = (SwingGrid) comp;
                else if (comp instanceof HasAspectRatio)
                    extraPanels.add((HasAspectRatio) comp);
                else
                    throw new UnsupportedOperationException("Unknown component: "
                            + comp.getClass().getSimpleName() + ": " + comp);
            }
            stackOfPanels.clear();
            stackOfPanels.addAll(extraPanels);
            stackOfPanels.addAll(playerPanels);

            Dimension outer = getFitInside(parent);
            // if this panel is vertical, make the player panels horizontal, and vice versa
            // give horizontal a little advantage because it's more space-efficient.
            vertical = outer.getWidth() < outer.getHeight() / 1.1;

            // 2. Lay out player panels & discard panel
            int edge = 0; // start at the edge and walk left/down with panels
            for (HasAspectRatio panel : stackOfPanels) {
                panel.setVertical(!vertical); // opposite of our orientation
                Dimension d = getDimensions(outer, panel);
                if (vertical) // vertical: start at top and work down
                    ((Component) panel).setBounds(0, edge, d.width, d.height);
                else // horizontal: start at left and work to the right
                    ((Component) panel).setBounds(edge, 0, d.width, d.height);
                edge += vertical ? d.height : d.width;
            }

            // 3. Lay out grid panel in the remaining space
            if (gridPanel != null) {
                if (vertical)
                    gridPanel.setBounds(0, edge, outer.width, outer.height - edge);
                else
                    gridPanel.setBounds(edge, 0, outer.width - edge, outer.height);
            }
        }
    }

    /** The dimensions of a sub-panel like a player panel or discard panel.
     *  @param outer the outer dimensions everything is fitting into
     *  @param panel the panel to be dimensioned */
    private Dimension getDimensions(Dimension outer, HasAspectRatio panel) {
        if (panel.isVertical()) // vertical -- same height as outer
            return new Dimension((int) (outer.height * panel.getAspectRatio()), outer.height);
        else // horizontal -- same width as outer
            return new Dimension(outer.width, (int) (outer.width / panel.getAspectRatio()));

    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Dimension result = new Dimension(0, 0);

        // main grid panel
        if (gridPanel != null)
            result.setSize(gridPanel.getMinimumSize());

        // the rest of the panels
        // (player panels & discard panel -- all implement HasAspectRatio)
        for (HasAspectRatio panel : stackOfPanels)
            if (vertical) // pad vertically -- figure out how tall player panels are based on width
                result.height += result.width / panel.getAspectRatio();
            else // pad horizontally -- figure out width based on height
                result.width += result.height * panel.getAspectRatio();

        return result;
    }
}
