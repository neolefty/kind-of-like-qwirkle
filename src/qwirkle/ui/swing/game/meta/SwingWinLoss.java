package qwirkle.ui.swing.game.meta;

import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.control.GameStatus;
import qwirkle.game.control.GameHistory;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Show a record of wins & losses for all players in history. */
class SwingWinLoss extends JPanel {
    public SwingWinLoss() {
        setLayout(new GridBagLayout());
    }

    public void update(GameHistory history) {
        // order the list of players: those in the current game first, then reverse order of the rest
        // (assume <tt>players</tt> was compiled dynamically as history was recorded)
        Set<QwirklePlayer> winLossPlayers = new HashSet<>(); // keep track of the players we've already recorded
        List<WinLossRecord> winLosses = new ArrayList<>();
        GameStatus cur = history.getCurrentIncompleteGame();
        if (cur != null)
            for (QwirklePlayer player : cur.getSettings().getPlayers()) {
                winLossPlayers.add(player);
                winLosses.add(new WinLossRecord(player, history));
            }
        List<QwirklePlayer> reversePlayers = new ArrayList<>(history.getAllPlayers());
        Collections.reverse(reversePlayers);
        for (QwirklePlayer player : reversePlayers) {
            if (!winLossPlayers.contains(player))
                winLosses.add(new SwingWinLoss.WinLossRecord(player, history));
        }

        // update contents of panel
        removeAll();
        GridBagConstraints constraints = new GridBagConstraints(0, 0, 0, 0, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0);

        // header
        constraints.gridx = 0;
        add(new JLabel(""), constraints);
        ++constraints.gridx;
        add(new JLabel("wins"), constraints);
        ++constraints.gridx;
        add(new JLabel("games"), constraints);
        ++constraints.gridy;

        // players
        for (WinLossRecord wl : winLosses) {
            constraints.gridx = 0;
            add(new JLabel(wl.player.getName()), constraints);
            constraints.anchor = GridBagConstraints.WEST;
            ++constraints.gridx;
            constraints.anchor = GridBagConstraints.CENTER;
            add(new JLabel("" + wl.win), constraints);
            ++constraints.gridx;
            add(new JLabel("" + wl.total), constraints);
            ++constraints.gridy;
        }

        validate();
    }

    class WinLossRecord {
        QwirklePlayer player;
        int win = 0, total = 0;

        WinLossRecord(QwirklePlayer player, GameHistory history) {
            this.player = player;
            for (GameStatus status : history.getCompletedGames())
                if (status.isFinished() && status.getSettings().getPlayers().contains(player)) {
                    ++total;
                    if (status.getAnnotated().getLeader() == player)
                        ++win;
                }
        }
    }
}
