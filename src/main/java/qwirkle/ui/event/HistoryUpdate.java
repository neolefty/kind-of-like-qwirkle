package qwirkle.ui.event;

import qwirkle.game.control.GameHistory;

/** The MetaGameHistory has updated.
 *  Fires when a new game ends or is interrupted by a new game starting before the current one ends. */
public class HistoryUpdate {
    private GameHistory history;

    public HistoryUpdate(GameHistory history) {
        this.history = history;
    }

    public GameHistory getHistory() {
        return history;
    }
}
