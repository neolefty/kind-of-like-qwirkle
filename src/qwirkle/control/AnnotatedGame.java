package qwirkle.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.event.GameOver;
import qwirkle.event.PreEvent;
import qwirkle.event.TurnCompleted;
import qwirkle.game.*;

import java.util.*;

/** A Qwirkle board that keeps track of statistics of a single game,
 *  like who played what piece and what the best score has been.
 *  Internally, it uses events (GameStatus and QwirkleTurn) to keep track.
 *
 *  <p>Only records a single game. Quits when that game ends.</p> */
public class AnnotatedGame {
    private List<TurnCompleted> turns = new ArrayList<>(); // list of turns so far
    private List<TurnCompleted> turnsNoMod = null; // version of turns list that is unmodifiable
    private TurnCompleted bestTurn = null; // the turn with the best score so far
    private boolean finished = false; // has the game already finished?
    private Map<AsyncPlayer, Integer> scores = new HashMap<>(); // scores

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
                else if (pre.getEvent() instanceof TurnCompleted) {
                    try {
                        log((TurnCompleted) pre.getEvent());
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        e.fillInStackTrace();
                        throw e;
                    }
                }
            }
        });
    }

    private void log(TurnCompleted turn) {
        if (finished)
            throw new IllegalStateException("Game is already finished. Should have unregistered already.");

        // integrity check
        TurnCompleted prevTurn = getMostRecentTurn();
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
    public TurnCompleted getMostRecentTurn() {
        return (turns.size() == 0) ? null : turns.get(turns.size() - 1);
    }

    /** What is a player's score? 0 if not in the game. */
    public int getScore(AsyncPlayer player) {
        if (!scores.containsKey(player)) return 0;
        else return scores.get(player);
    }

    /** The turns in this game so far. */
    public List<TurnCompleted> getTurns() {
        if (turnsNoMod == null)
            turnsNoMod = Collections.unmodifiableList(turns);
        return turnsNoMod;
    }

    /** What is the turn with the best score so far? */
    public TurnCompleted getBestTurn() { return bestTurn; }

    /** What is the best move made so far by a particular player? */
    public TurnCompleted getBestTurn(AsyncPlayer player) {
        TurnCompleted best = null;
        for (TurnCompleted t : turns)
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
    public AsyncPlayer getLeader() {
        int max = -1;
        AsyncPlayer result = null;
        for (AsyncPlayer player : scores.keySet()) {
            int score = scores.get(player);
            if (score > max) {
                result = player;
                max = score;
            }
        }
        return result;
    }
}
