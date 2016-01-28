package qwirkle.game.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.event.GameOver;
import qwirkle.game.event.GameStarted;
import qwirkle.game.event.TurnCompleted;
import qwirkle.ui.event.HistoryUpdate;

import java.util.*;

/** Multi-game history. Fires {@link HistoryUpdate} events. */
public class GameHistory {
    private EventBus bus;

    /** All games, including those that were not completed. */
    private final List<GameStatus> fullHistory = new ArrayList<>();

    /** All completed games. */
    private final List<GameStatus> completeGames = new ArrayList<>();

    /** All players in the history. */
    private Set<QwirklePlayer> allPlayers = new LinkedHashSet<>();

    /** The status as of the end of the last turn. Keep in mind in case a game is interrupted
     *  (a new game starts before the current one ends). Nulled whenever a game ends. */
    private GameStatus currentIncompleteGame;

    public GameHistory(EventBus bus) {
        this.bus = bus;
        bus.register(new Object() {
            @Subscribe public void gameEnded(GameOver ended) {
                GameStatus status = ended.getStatus();
                fullHistory.add(status);
                completeGames.add(status);
                currentIncompleteGame = null;
                fireUpdate();
            }

            @Subscribe public void gameTurn(TurnCompleted turn) {
                currentIncompleteGame = turn.getStatus();
            }

            @Subscribe public void gameStart(GameStarted start) {
                allPlayers.addAll(start.getSettings().getPlayers());
                if (currentIncompleteGame != null) {
                    if (currentIncompleteGame.isFinished()) throw new IllegalStateException
                            ("Current game is finished but didn't receive GameOver event: " + currentIncompleteGame);
                    fullHistory.add(currentIncompleteGame);
                    currentIncompleteGame = null;
                    fireUpdate();
                }
            }
        });
    }

    private void fireUpdate() {
        bus.post(new HistoryUpdate(this));
    }

    /** The current game that is in progress but not finished. */
    public GameStatus getCurrentIncompleteGame() {
        return currentIncompleteGame;
    }

    /** An ordered set of all the players who have played so far,
     *  in the order in which they appear in the history. */
    public Set<QwirklePlayer> getAllPlayers() {
        return Collections.unmodifiableSet(allPlayers);
    }

    /** All the games that have actually run to completion (not been interrupted). */
    public List<GameStatus> getCompletedGames() {
        return Collections.unmodifiableList(completeGames);
    }

    /** All games, in the order in which they were played, including ones
     *  that didn't finish because they were interrupted.
     *  Doesn't include {@link #getCurrentIncompleteGame}. */
    public List<GameStatus> getAllGames() {
        return Collections.unmodifiableList(fullHistory);
    }
}
