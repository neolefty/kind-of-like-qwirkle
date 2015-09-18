package qwirkle.ui.swing.main;

import qwirkle.ui.swing.board.PlayerPanel;
import qwirkle.ui.swing.board.QwirkleGridPanel;
import qwirkle.ui.swing.util.LayoutBase;

import java.awt.*;
import java.util.LinkedHashSet;
import java.util.Set;

/** Lay out an entire Qwirkle game, including board and player panels.*/
public class QwirkleGameLayout extends LayoutBase {
    private Set<PlayerPanel> playerPanels = new LinkedHashSet<>();
    private QwirkleGridPanel gridPanel;
    private boolean vertical = false;

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            // 1. Review components
            playerPanels.clear(); // start from scratch each time, in case some have been removed
            gridPanel = null;
            for (int i = 0; i < parent.getComponentCount(); ++i) {
                Component comp = parent.getComponent(i);
                if (comp instanceof PlayerPanel)
                    playerPanels.add((PlayerPanel) comp);
                else if (comp instanceof QwirkleGridPanel)
                    gridPanel = (QwirkleGridPanel) comp;
            }

            Dimension outer = getFitInside(parent);
            // if this panel is vertical, make the player panels horizontal, and vice versa
            // give horizontal a little advantage because it's more space-efficient.
            vertical = outer.getWidth() < outer.getHeight() / 1.1;

            // 2. Lay out player panels
            int edge = 0; // start at the edge and walk left/down with panels
            for (PlayerPanel player : playerPanels) {
                player.setVertical(!vertical); // opposite of our orientation
                Dimension d;
                if (vertical) // if vertical, player panel is horizontal, same width as this
                    d = new Dimension(outer.width, (int) (outer.width / player.getAspectRatio()));
                else
                    d = new Dimension((int) (outer.height * player.getAspectRatio()), outer.height);
                if (vertical) // start at top and work down
                    player.setBounds(0, edge, d.width, d.height);
                else // start at left and work to the right
                    player.setBounds(edge, 0, d.width, d.height);
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

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Dimension result = new Dimension(0, 0);
        if (gridPanel != null)
            result.setSize(gridPanel.getMinimumSize());

        if (vertical) { // pad vertically -- figure out how tall player panels are based on width
            double h = result.width / PlayerPanel.HORIZONTAL_ASPECT_RATIO;
            result.height += playerPanels.size() * h;
        }
        else { // pad horizontally -- figure out width based on height
            double w = result.height * PlayerPanel.VERTICAL_ASPECT_RATIO;
            result.width += playerPanels.size() * w;
        }

        return result;
    }
}
