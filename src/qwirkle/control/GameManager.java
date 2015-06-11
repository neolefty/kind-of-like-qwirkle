package qwirkle.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import qwirkle.control.event.GameOver;
import qwirkle.control.event.GameStarted;
import qwirkle.control.event.PreEvent;
import qwirkle.game.*;
import qwirkle.game.impl.QwirkleBoardImpl;

import java.util.*;

/** Manage a group of Qwirkle players playing a game.
 *  To receive updates, register for {@link GameStatus}, {@link QwirkleBoard},
 *  {@link QwirkleTurn}, or {@link AnnotatedGame} on {@link #getEventBus}.
 *
 *  <p>Events:</p>
 *
 *  <ul>
 *      <li>Game starts: {@link qwirkle.control.event.GameStarted}</li>
 *      <li>Turn is taken: {@link QwirkleTurn}</li>
 *      <li>A player drew some pieces at the end of their turn: {@link QwirkleDraw}</li>
 *      <li>Board changes: {@link QwirkleBoard}</li>
 *      <li>Game ends: {@link qwirkle.control.event.GameOver}</li>
 *  </ul>
 *
 *  <p>Events are posted to the {@link EventBus} in two stages.
 *  First, wrapped in a {@link qwirkle.control.event.PreEvent} to allow setup by internal objects
 *  such as AnnotatedGame and second, normally, for GUI etc.</p>*/
public class GameManager {
    private static final int[] eventBusSerial = { 0 }; // serial number for event bus

    // long-lived things
    private GameStatus status;
    private EventBus bus;

    // game settings -- can only be changed when a new game is started
    private QwirkleSettings settings = new QwirkleSettings();

    // Map of players to their hands. Current player is always the first one in the map.
    private final LinkedHashMap<QwirklePlayer, List<QwirklePiece>> playerHands = new LinkedHashMap<>();

    // References to board, current player, finished message
    private QwirkleBoard board;
    private QwirklePlayer curPlayer;
    private String finishedMessage;
    private List<QwirklePiece> deck;

    private int nPasses; // number of passes in a row

    private static final Random r = new Random();
    private boolean randomDeal = true; // by default, deal randomly

    public GameManager(QwirkleSettings settings) {
        this.settings = settings;
        synchronized (eventBusSerial) {
            bus = new EventBus(new SubscriberExceptionHandler() {
                @Override
                public void handleException(Throwable exception, SubscriberExceptionContext context) {
                    System.out.println(context);
                    exception.printStackTrace(System.out);
                }
            });
        }
        status = new GameStatus(this);
        deck = new ArrayList<>();
    }

    /** Initialize with default game settings. */
    public GameManager() {
        this(new QwirkleSettings());
    }

    public GameStatus getStatus() { return status; }

    /** What are the settings currently being used? */
    public QwirkleSettings getSettings() { return settings; }

    public EventBus getEventBus() { return bus; }

    public List<QwirklePiece> getDeck() { return deck; }

    /** For testing. By default, deal randomly. But for testing we want to script things. */
    protected void setRandomDealing(boolean random) { this.randomDeal = random; }

    public String getFinishedMessage() { return finishedMessage; }

    public QwirkleBoard getBoard() { return board; }

    private void setBoard(QwirkleBoard board) {
        if (this.board != board) {
            this.board = board;
            post(board);
        }
    }

    /** If everyone has passed three times, it's over.
     *  Or if everyone has passed and there are no cards left to draw,
     *  it's because no moves are possible, and, the game is over. */
    public boolean isStalled() {
        return nPasses >= playerHands.size() * 3
                || (nPasses == playerHands.size() && deck.isEmpty());
    }

    private void checkStalled() {
        if (!isFinished()) {
            if (nPasses >= playerHands.size() * 3)
                finished("All players have passed 3 in a row. Game ends from stalling.");
            else if (deck.isEmpty() && nPasses == playerHands.size())
                finished("No more tiles to draw, and all players have passed. Game ends.");
        }
    }

    /** Mark the game as over. */
    private void finished(String reason) {
        if (finishedMessage != null)
            throw new IllegalStateException("Already finished: " + finishedMessage
                    + "; can't finish again (" + reason + ").");
        else if (reason == null)
            throw new NullPointerException("Reason is null.");
        else
            finishedMessage = reason;
        post(new GameOver(status));
    }

    /** Has the current game finished? */
    public boolean isFinished() { return finishedMessage != null; }

    /** The number of passes in a row. Resets to zero whenever a player
     *  makes a normal turn and doesn't pass.  */
    public int getNPasses() { return nPasses; }

    public Collection<QwirklePlayer> getPlayers() {
        return settings.getPlayers();
    }

    /** Start a game using the existing settings. */
    public void start() { start(settings); }

    /** Start a new game. */
    public void start(QwirkleSettings settings) {
        // end the old game
        if (isStarted() && !isFinished())
            finished("The game was cancelled.");

        this.settings = settings;
        if (settings == null)
            throw new NullPointerException("GameSettings is null.");

        // clear old game state
        for (List<QwirklePiece> hand : playerHands.values())
            hand.clear();
        playerHands.clear();
        nPasses = 0;
        finishedMessage = null;
        this.deck.clear();

        // set up new game
        setBoard(new QwirkleBoardImpl(settings));
        this.deck.addAll(settings.generate());
        for (QwirklePlayer player : settings.getPlayers())
            addPlayer(player);

        // game is ready to start -- make sure everybody knows
        post(new GameStarted(status));

        // deal first cards -- propagated as a QwirkleTurn
        deal();
        chooseFirstPlayer();
    }

    /** Has this game started? True if any pieces have been dealt to players or if anything is played on the board. */
    public boolean isStarted() {
        if (board != null && board.size() > 0)
            return true;
        else
            for (List<QwirklePiece> hand : playerHands.values())
                if (!hand.isEmpty())
                    return true;
        return false;
    }

    /** Choose who should go first, based on who has the best possible start
     *  (most pieces that can be played all together). */
    private void chooseFirstPlayer() {
        int max = -1;
        QwirklePlayer best = getCurrentPlayer();
        // figure out who has the best initial play
        for (QwirklePlayer p : playerHands.keySet()) {
            int matches = QwirkleKit.countMatches(getHand(p));
            if (matches > max) {
                max = matches;
                best = p;
            }
        }
        // advance until they're the first player
        while (getCurrentPlayer() != best)
            advance();
    }

    /** The current player take a turn, draws to fill hand, then advance to the next player. */
    public void step() {
        QwirklePlayer cur = getCurrentPlayer();
        List<QwirklePiece> hand = playerHands.get(cur);
        try {
            // 1. Let the current player play
            QwirkleTurn turn; // the turn object -- used for event signalling
            Collection<QwirklePlacement> play = cur.play(board, getHand(cur));

            // 1a. If a pass, give a chance to discard
            if (play == null || play.isEmpty()) {
                ++nPasses; // increment the pass counter
                // give the player a chance to discard
                Collection<QwirklePiece> discards = cur.discard(board, getHand(cur));
                if (discards != null && discards.size() > 0) { // do the discard
                    if (!hand.containsAll(discards)) // validate discards
                        throw new IllegalArgumentException(cur.getName() + " cannot discard "
                                + QwirklePiece.abbrev(discards) + " from hand (" + QwirklePiece.abbrev(hand) + ").");
                    hand.removeAll(discards); // remove them from the player's hand
                    deck.addAll(discards); // put them back into the deck
                }
                // initialize the turn object, for event signalling
                turn = QwirkleTurn.discard(status, cur, discards == null ? 0 : discards.size());
            }

            // 1b. If not a pass, record the play
            else { // note: we know play is non-empty
                nPasses = 0; // reset the pass counter
                validatePlay(cur, play); // make sure the player actually has these pieces
                for (QwirklePlacement placement : play)
                    hand.remove(placement.getPiece()); // remove played pieces from the player's hand
                setBoard(board.play(play)); // update the board
                // initialize the turn object, for event signalling
                turn = QwirkleTurn.play(status, play, cur, board.getLastScore());
            }

            // 2. did the game end?
            boolean itsOver = false;
            int bonus = 0;
            // if the current player ran out of tiles and can't draw more, then the game is over
            if (getHand(cur).isEmpty() && deck.isEmpty()) {
                itsOver = true;
                // bonus points for other players' remaining pieces
                for (List<QwirklePiece> otherHand : playerHands.values())
                    bonus += otherHand.size();
                turn = turn.bonus(bonus);
            }

            // 3. Post events
            // post the new turn before we broadcast any further changes
            post(turn);
            // announce the end only after we broadcast the last turn
            if (itsOver)
                finished(cur.getName() + " played their last tile. Bonus " + bonus + " for other players' remaining tiles.");
            // did the game stall (broadcasts game finished events)
            checkStalled();

            // 4. hand out new tiles
            deal();

            // 5. advance to the next player
            advance();

            // 6. make sure the game status is posted after all updates (messy, but not causing problems ... yet)
            status.post();
        } catch(IllegalStateException e) {
            e.printStackTrace();
            System.err.println(board);
            e.fillInStackTrace();
            throw e;
        }
    }

    private void validatePlay(QwirklePlayer cur, Collection<QwirklePlacement> play) {
        List<QwirklePiece> handScratch = new ArrayList<>(getHand(cur));
        // One by one, remove the played pieces from a scratch copy of the player's hand.
        // if we find one that isn't in their hand, complain.
        for (QwirklePlacement place : play)
            if (!handScratch.remove(place.getPiece()))
                    throw new IllegalStateException(cur.getName() + " cannot play " + play
                            + " because their hand only has " + getHand(cur));
    }

    /** Add a player and return a changer that will monitor its hand. */
    private void addPlayer(QwirklePlayer player) {
        playerHands.put(player, new ArrayList<QwirklePiece>());
    }

    /** Deal tiles to the players, filling their hands up to <tt>handSize</tt>, if
     * there are enough tiles, starting with the current player (the first in the map). */
    public void deal() {
        for (QwirklePlayer player : playerHands.keySet()) {
            List<QwirklePiece> hand = playerHands.get(player);
            if (deck.size() > 0 && hand.size() < settings.getHandSize()) {
                List<QwirklePiece> dealt = new ArrayList<>();
                while (deck.size() > 0 && hand.size() < settings.getHandSize()) {
                    int i = randomDeal ? r.nextInt(deck.size()) : 0;
                    QwirklePiece piece = deck.remove(i);
                    hand.add(piece);
                    dealt.add(piece);
                }
                post(new QwirkleDraw(player, dealt));
            }
        }
    }

    private void post(Object event) {
        bus.post(new PreEvent(event));
        bus.post(event);
    }

    /** The pieces <tt>player</tt> currently holds. */
    public List<QwirklePiece> getHand(QwirklePlayer player) {
        if (!getPlayers().contains(player))
            throw new IllegalArgumentException(player + " is not in this game.");
        List<QwirklePiece> hand = playerHands.get(player);
        return (hand == null) ? null : Collections.unmodifiableList(hand);
    }

    public QwirklePlayer getCurrentPlayer() {
        if (curPlayer == null)
            curPlayer = findCurrentPlayer();
        return curPlayer;
    }

    /** Look in the map for the player currently first in the sequence. */
    private QwirklePlayer findCurrentPlayer() {
        return playerHands.keySet().iterator().next();
    }

    /** Move the current player to the end of the line. */
    private void advance() {
        if (!isFinished()) {
            QwirklePlayer cur = getCurrentPlayer();
            // move current player to the back of the map
            playerHands.put(cur, playerHands.remove(cur));
            // update the reference to the current player
            curPlayer = findCurrentPlayer();
        }
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        // 1. Board
        result.append("Board:");
        if (board != null) {
            result.append("\n").append(board.toString("  - "));
            int turns = board.getTurnCount(), pieces = board.size();
            result.append("Turns played: " + turns
                    + "; pieces played: " + pieces
                    + "; average pieces per turn: " + (((double) pieces) / turns) + "\n");
        }
        else result.append(" none\n");

        // 2. Some statistics -- whose turn, how many pieces played so far, etc.
        if (isFinished())
            result.append(finishedMessage + "\n");
        else
            result.append("  - Next player: " + getCurrentPlayer().getName() + "\n");
        AnnotatedGame noted = status.getAnnotatedGame();
        QwirkleTurn best = noted.getBestTurn();
        if (best != null) {
            result.append("  - Best play (" + best.getScore() + "): " + best.getPlacements() + "\n");
            QwirklePlayer leader = noted.getLeader();
            result.append((isFinished() ? "Winner" : "Current leader") + ": "
                    + leader.getName() + " (" + noted.getScore(leader) + ")\n");
        }

        // 3. Players - score & current pieces in hand
        for (QwirklePlayer player : getPlayers()) {
            List<QwirklePiece> hand = getHand(player);
            result.append("  - " + player.getName() + ": " + noted.getScore(player) + " points");
            result.append(" - holds " + ((hand == null || hand.size() == 0) ? "nothing" : QwirklePiece.abbrev(hand)));
            result.append("\n");
        }
        result.append(" -- Total of all scores: " + noted.getTotalScore() + "\n");

        // 4. Is the game stalled?
        if (isStalled()) {
            result.append("-------------------\n")
                    .append("--- Stalled (" + getNPasses() + ") ---\n")
                    .append("-------------------\n");
        }

        return result.toString();
    }
}
