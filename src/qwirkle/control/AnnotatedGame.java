package qwirkle.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirkleGrid;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleTurn;

import java.util.*;

/** A Qwirkle board that keeps track of statistics of a single game,
 *  like who played what piece and what the best score has been.
 *  Internally, it uses events (GameStatus and QwirkleTurn) to keep track.
 *
 *  <p>Only records a single game. Quits when that game ends.</p> */
public class AnnotatedGame {
    private List<QwirkleTurn> turns = new ArrayList<>(); // list of turns so far
    private List<QwirkleTurn> turnsNoMod = null; // version of turns list that is unmodifiable
    private QwirkleTurn bestTurn = null; // the turn with the best score so far
    private boolean finished = false; // has the game already finished?
    private Map<QwirklePlayer, Integer> scores = new HashMap<>(); // scores

    public AnnotatedGame(final EventBus bus) {
        bus.register(new Object() {
            @Subscribe
            public void update(PreEvent pre) {
                // unregister when the game ends
                if (pre.getEvent() instanceof GameOver) {
                    bus.unregister(this);
                    finished = true;
                }
                // log turns as they arrive
                else if (pre.getEvent() instanceof QwirkleTurn) {
                    try {
                        log((QwirkleTurn) pre.getEvent());
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        e.fillInStackTrace();
                        throw e;
                    }
                }
            }
        });
    }

    private void log(QwirkleTurn turn) {
        if (finished)
            throw new IllegalStateException("Game is already finished. Should have unregistered already.");

        // integrity check
        QwirkleTurn prevTurn = getMostRecentTurn();
        QwirkleGrid curGrid = turn.getGrid();
        QwirkleBoard prevBoard = null;
        if (curGrid instanceof QwirkleBoard)
            prevBoard = ((QwirkleBoard) curGrid).getUndo();
        if (turns.size() == 0) { // if first turn, board should previously be empty
            if (prevBoard != null && prevBoard.size() > 0)
                throw new IllegalStateException("Received first turn, but game was already underway: " + prevBoard);
        } else if (turn.isDiscard()) { // on a pass, board shouldn't change
            if (prevTurn.getGrid() != turn.getGrid())
                throw new IllegalStateException("Pass, but game changed: \n" + prevBoard + " to \n" + turn.getGrid());
        } else { // regular play: previous state should match previous play
            if (turn.getPlacements().size() == 0)
                throw new IllegalStateException("Unknown turn type: not a pass, but no placements: " + turn);
            if (prevBoard != prevTurn.getGrid())
                throw new IllegalStateException
                        ("Was a turn missed? Last turn's board doesn't match this turn's "
                                + "previous board. Last turn's board: \n" + prevTurn.getGrid()
                                + "This turn's previous board: " + prevBoard);
        }

        // record this turn
        turns.add(turn);
        if (bestTurn == null || bestTurn.getScore() < turn.getScore())
            bestTurn = turn;

        // update scores
        int oldScore = getScore(turn.getPlayer());
        scores.put(turn.getPlayer(), oldScore + turn.getScore());
    }

    /** The most recent turn. Null if none yet. */
    public QwirkleTurn getMostRecentTurn() {
        return (turns.size() == 0) ? null : turns.get(turns.size() - 1);
    }

    /** What is a player's score? 0 if not in the game. */
    public int getScore(QwirklePlayer player) {
        if (!scores.containsKey(player)) return 0;
        else return scores.get(player);
    }

    /** The turns in this game so far. */
    public List<QwirkleTurn> getTurns() {
        if (turnsNoMod == null)
            turnsNoMod = Collections.unmodifiableList(turns);
        return turnsNoMod;
    }

    /** What is the turn with the best score so far? */
    public QwirkleTurn getBestTurn() { return bestTurn; }

    /** What is the best move made so far by a particular player? */
    public QwirkleTurn getBestTurn(QwirklePlayer player) {
        QwirkleTurn best = null;
        for (QwirkleTurn t : turns)
            if (t.getPlayer() == player && t.getScore() > 0
                    && (best == null || t.getScore() > best.getScore()))
                best = t;
        return best;
    }

    /** The total of all scores. */
    public int getTotalScore() {
        int result = 0;
        for (Integer score : scores.values())
            result += score;
        return result;
    }

    /** The player with the highest score. */
    public QwirklePlayer getLeader() {
        int max = -1;
        QwirklePlayer result = null;
        for (QwirklePlayer player : scores.keySet()) {
            int score = scores.get(player);
            if (score > max) {
                result = player;
                max = score;
            }
        }
        return result;
    }
}
