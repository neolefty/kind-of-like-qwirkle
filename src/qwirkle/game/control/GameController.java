package qwirkle.game.control;

import com.google.common.eventbus.EventBus;
import qwirkle.game.base.*;
import qwirkle.game.base.impl.QwirkleBoardImpl;
import qwirkle.game.event.*;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** Model a group of Qwirkle players playing a game.
 *  To receive updates, register for events on EventBus.
 *
 *  <p>Events:</p>
 *
 *  <ul>
 *      <li>Game starts: {@link GameStarted}</li>
 *      <li>Turn is taken: {@link TurnCompleted}</li>
 *      <li>A player drew some pieces at the end of their turn: {@link DrawPieces}</li>
 *      <li>Board changes: {@link QwirkleBoard}</li>
 *      <li>Ready to start next turn: {@link TurnStarting}</li>
 *      <li>Game ends: {@link GameOver}</li>
 *  </ul>
 *
 *  <p>Events are posted to the {@link EventBus} in two stages.
 *  First, wrapped in a {@link qwirkle.game.event.PreEvent} to allow setup by internal objects
 *  such as AnnotatedGame and second, normally, for GUI etc.</p>*/
public class GameController {
    // long-lived things
    private AnnotatedGame annotated;
    private EventBus bus;
    private ThreadingStrategy threading;

    // game settings -- can only be changed when a new game is started
    private QwirkleSettings settings = new QwirkleSettings();

    // Map of players to their hands. Current player is always the first one in the map.
    // note that this is also the official list of players
    private final LinkedHashMap<QwirklePlayer, List<QwirklePiece>> playerHands = new LinkedHashMap<>();

    // References to board, current player, finished message
    private QwirkleBoard board;
    private QwirklePlayer curPlayer;
    private String finishedLong, finishedShort;
    private List<QwirklePiece> deck;

    private int nPasses; // number of passes in a row

    private static final Random r = new Random();
    private boolean randomDeal = true; // by default, deal randomly

    public GameController(EventBus bus, QwirkleSettings settings, ThreadingStrategy threading) {
        this.bus = bus;
        this.settings = settings;
        this.threading = threading;
        deck = new ArrayList<>();
    }

    /** Initialize with default game settings. */
    public GameController(EventBus bus, ThreadingStrategy threading) {
        this(bus, new QwirkleSettings(), threading);
    }

    /** The event bus. */
    public EventBus getEventBus() { return bus; }

    /** What are the settings currently being used? */
    public QwirkleSettings getSettings() { return settings; }

    public List<QwirklePiece> getDeck() { return deck; }

    /** For testing. By default, deal randomly. But for testing we want to script things. */
    protected void setRandomDealing(boolean random) { this.randomDeal = random; }

    /** Short summary of how the game finished. Null if not finished. */
    public String getFinishedMessageShort() { return finishedShort; }
    /** Detailed description of how the game finished. Null if not finished. */
    public String getFinishedMessageLong() { return finishedLong; }

    /** The annotated version of this game, kept live.
     *  It will stop updating when the current game ends
     *  (and you'll have to make a new call to getAnnotated()). */
    public AnnotatedGame getAnnotated() { return annotated; }

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
        synchronized (playerHands) {
            return nPasses >= playerHands.size() * 3
                    || (nPasses == playerHands.size() && deck.isEmpty());
        }
    }

    private void checkStalled() {
        if (!isFinished()) {
            String longMsg = null, shortMsg = null;

            synchronized (playerHands) {
                if (nPasses >= playerHands.size() * 3) {
                    longMsg = "All players passed 3 times in a row. Game is stalled.";
                    shortMsg = longMsg;
                }
                else if (deck.isEmpty() && nPasses == playerHands.size()) {
                    longMsg = "No more tiles to draw, and all players have passed.";
                    shortMsg = longMsg;
                }
            }

            if (longMsg != null) finished(longMsg, shortMsg);
        }
    }

    /** Mark the game as over. */
    private void finished(String longMessage, String shortMessage) {
        //noinspection StringEquality
        if (this.finishedLong != null || this.finishedShort != null)
            throw new IllegalStateException("Already finished: "
                    + longMessage + " / " + shortMessage
                    + "; can't finish again (" + longMessage + ").");
        else if (longMessage == null || shortMessage == null)
            throw new NullPointerException("Message is null.");
        else {
            finishedLong = longMessage;
            finishedShort = shortMessage;
        }
        post(new GameOver(new GameStatus(this)));
    }

    /** Has the current game finished? */
    public boolean isFinished() { return finishedLong != null; }

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
            finished("The game was cancelled.", "The game was cancelled.");

        this.settings = settings;
        if (settings == null)
            throw new NullPointerException("GameSettings is null.");

        // set up players' hands
        synchronized (playerHands) {
            for (List<QwirklePiece> hand : playerHands.values())
                hand.clear();
            playerHands.clear();
            for (QwirklePlayer player : settings.getPlayers())
                playerHands.put(player, new ArrayList<QwirklePiece>());
        }

        nPasses = 0;
        clearFinished();

        // set up new game
        this.deck.clear();
        setBoard(new QwirkleBoardImpl(settings));
        annotated = new AnnotatedGame(bus);
        this.deck.addAll(settings.generate());

        // game is ready to start -- make sure everybody knows
        post(new GameStarted(new GameStatus(this)));

        // deal first cards -- propagated as a QwirkleTurn
        deal();
        chooseFirstPlayer();
    }

    private void clearFinished() {
        finishedLong = null;
        finishedShort = null;
    }

    /** Has this game started? True if any pieces have been dealt to players or if anything is played on the board. */
    public boolean isStarted() {
        if (board != null && board.size() > 0)
            return true;
        else
            synchronized (playerHands) {
                for (List<QwirklePiece> hand : playerHands.values())
                    if (!hand.isEmpty())
                        return true;
            }
        return false;
    }

    /** Choose who should go first, based on who has the best possible start
     *  (most pieces that can be played all together).
     *  Post an event announcing that their turn is starting. */
    private void chooseFirstPlayer() {
        int max = -1;
        QwirklePlayer best = getCurrentPlayer();
        // figure out who has the best initial play
        synchronized (playerHands) {
            for (QwirklePlayer p : playerHands.keySet()) {
                int matches = QwirkleKit.countMatches(getHand(p));
                if (matches > max) {
                    max = matches;
                    best = p;
                }
            }
        }
        // advance until they're the first player
        while (getCurrentPlayer() != best)
            advance(false);
        // okay, we found the right one. Announce their turn.
        postTurnStarting();
    }

    private final Semaphore aiRunning = new Semaphore(1);

    /** The current player takes a turn, draws to fill hand, then advance to the next player.
     *  Note: the AI is run asynchronously using ThreadingStrategy,
     *  since AIs can take a while to think, so don't call this again
     *  until the turn finishes ({@link TurnCompleted} event). */
    public void stepAI() {
        final QwirklePlayer cur = getCurrentPlayer();
        if (cur.isHuman())
            throw new IllegalStateException("Must wait for human to take a turn.");

//            try {
//                Collection<QwirklePlacement> placements = cur.getAi().play(getBoard(), getHand(cur));
//                if (placements == null || placements.size() == 0)
//                    // an empty play means pass, which means give a chance to discard and re-draw
//                    stepDiscard(cur, cur.getAi().discard(getBoard(), getHand(cur)));
//                else
//                    play(cur, placements);
//            } catch(IllegalStateException e) {
//                e.printStackTrace();
//                System.err.println(board);
//                e.fillInStackTrace();
//                throw e;
//            }

        try {
            // make sure it's not called again while the AI is thinking
            aiRunning.tryAcquire(1, 0, TimeUnit.DAYS);
            threading.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Collection<QwirklePlacement> placements = cur.getAi().play(getBoard(), getHand(cur));
                        if (placements == null || placements.size() == 0)
                            // an empty play means pass, which means give a chance to discard and re-draw
                            stepDiscard(cur, cur.getAi().discard(getBoard(), getHand(cur)));
                        else
                            play(cur, placements);
                    } catch(IllegalStateException e) {
                        e.printStackTrace();
                        System.err.println(board);
                        e.fillInStackTrace();
                        throw e;
                    } finally {
                        aiRunning.release();
                    }
                }
            });
        }
        catch(InterruptedException e) { // somebody got impatient calling stepAI
            throw new IllegalStateException("stepAI called before turn completed.", e);
        }
    }

    /** A player takes a turn by playing some pieces, and the game advances. */
    public void play(QwirklePlayer cur, Collection<QwirklePlacement> play) {
        checkCurrent(cur, "play");
        List<QwirklePiece> hand;
        synchronized (playerHands) {
            hand = playerHands.get(cur);
        }
        TurnCompleted turn; // the turn object -- used for event signalling
        if (play == null || play.isEmpty())
            throw new IllegalStateException("Play is empty (" + play + ").");

        nPasses = 0; // reset the pass counter
        validatePlay(cur, play); // make sure the player actually has these pieces
        for (QwirklePlacement placement : play)
            hand.remove(placement.getPiece()); // remove played pieces from the player's hand
        setBoard(board.play(play)); // update the board
        // initialize the turn object, for event signalling
        turn = TurnCompleted.play(new GameStatus(this), play, cur, board.getLastScore());

        stepFinish(cur, turn);
    }

    /** The part of a step after the QwirklePlayer callback if the player chose to discard. */
    private void stepDiscard(QwirklePlayer cur, Collection<QwirklePiece> discards) {
        checkCurrent(cur, "discard");
        ++nPasses; // increment the pass counter
        if (discards != null && discards.size() > 0) { // do the discard
            List<QwirklePiece> hand;
            synchronized (playerHands) {
                hand = playerHands.get(cur);
            }
            if (!hand.containsAll(discards)) // validate discards
                throw new IllegalArgumentException(cur.getName() + " cannot discard "
                        + QwirklePiece.abbrev(discards) + " from hand (" + QwirklePiece.abbrev(hand) + ").");
            discards = new ArrayList<>(discards); // just in case they passed us their hand as their discards
            hand.removeAll(discards); // remove them from the player's hand
            deck.addAll(discards); // put them back into the deck
        }

        // initialize the turn object, for event signalling
        TurnCompleted turn = TurnCompleted.discard(new GameStatus(this), cur, discards == null ? 0 : discards.size());

        stepFinish(cur, turn);
    }

    /** The part of a step after a play or discard is created. */
    private void stepFinish(QwirklePlayer cur, TurnCompleted turn) {
        // 2. did the game end?
        boolean itsOver = false;
        int bonus = 0;
        // if the current player ran out of tiles and can't draw more, then the game is over
        if (getHand(cur).isEmpty() && deck.isEmpty()) {
            itsOver = true;
            // bonus points for other players' remaining pieces
            synchronized (playerHands) {
                for (List<QwirklePiece> otherHand : playerHands.values())
                    bonus += otherHand.size();
            }
            turn = turn.bonus(bonus);
        }

        // 3. Post events
        // post the new turn before we broadcast any further changes
        post(turn);
        // announce the end only after we broadcast the last turn
        if (itsOver)
            finished(cur.getName() + " plays " + turn.getPlacements().size() + " to go out. Bonus "
                    + bonus + " for other players' tiles. Total " + turn.getScore() + " points."
                    , cur.getName() + " goes out for " + turn.getScore());

        // did the game stall (broadcasts game finished events)
        checkStalled();

        // 4. hand out new tiles
        deal();

        // 5. advance to the next player
        advance(true);
    }

    /** Check that <tt>player</tt> is the current player. */
    private void checkCurrent(QwirklePlayer player, String verb) {
        if (player != getCurrentPlayer())
            throw new IllegalStateException(player.getName()
                    + " tried to " + verb + " when the current player is " + getCurrentPlayer().getName());
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

    /** Deal tiles to the players, filling their hands up to <tt>handSize</tt>, if
     * there are enough tiles, starting with the current player (the first in the map). */
    public void deal() {
        // save event broadcasting for later to avoid concurrent mod exceptions
        List<DrawPieces> events = new ArrayList<>();
        synchronized (playerHands) {
            for (QwirklePlayer player : playerHands.keySet()) {
                List<QwirklePiece> dealt = pickToDeal(player, getHand(player));
                if (dealt.size() > 0) {
                    playerHands.get(player).addAll(dealt);
                    events.add(new DrawPieces(player, dealt));
                }
            }
        }
        // do this outside the synchronized section
        for (DrawPieces event : events)
            post(event);
    }

    /** Choose as many new tiles as needed to <tt>player</tt> and remove them from the deck.
     *  Would be private except it's used in testing.
     *  @param hand the player's current hand.
     *  @return the cards to be dealt, empty if none. */
    protected List<QwirklePiece> pickToDeal(QwirklePlayer player, Collection<QwirklePiece> hand) {
        List<QwirklePiece> result = new ArrayList<>();
        if (deck.size() > 0 && hand.size() < settings.getHandSize()) {
            int nToDeal = settings.getHandSize() - hand.size();
            for (int i = 0; i < nToDeal && deck.size() > 0; ++i) {
                int x = randomDeal ? r.nextInt(deck.size()) : 0;
                result.add(deck.remove(x));
            }
        }
        return result;
    }

    private void post(Object event) {
        bus.post(new PreEvent(event));
        bus.post(event);
    }

    /** The pieces <tt>player</tt> currently holds. */
    public List<QwirklePiece> getHand(QwirklePlayer player) {
        if (!getPlayers().contains(player))
            throw new IllegalArgumentException(player + " is not in this game.");
        List<QwirklePiece> hand;
        synchronized (playerHands) {
            hand = playerHands.get(player);
        }
        return (hand == null) ? null : Collections.unmodifiableList(hand);
    }

    public QwirklePlayer getCurrentPlayer() {
        if (curPlayer == null)
            curPlayer = findCurrentPlayer();
        return curPlayer;
    }

    /** Look in the map for the player currently first in the sequence. */
    private QwirklePlayer findCurrentPlayer() {
        synchronized (playerHands) {
            return playerHands.keySet().iterator().next();
        }
    }

    /** Move the current player to the end of the line. */
    private void advance(boolean postEvent) {
        if (!isFinished()) {
            QwirklePlayer cur = getCurrentPlayer();
            // move current player to the back of the map
            synchronized (playerHands) {
                playerHands.put(cur, playerHands.remove(cur));
            }
            // update the reference to the current player
            curPlayer = findCurrentPlayer();
            // notify everyone that we're ready for a new turn
            if (postEvent)
                postTurnStarting();
        }
    }

    /** Post that a turn is starting for the current player. */
    private void postTurnStarting() {
        post(new TurnStarting(new GameStatus(this)));
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
            result.append(finishedLong + "\n");
        else
            result.append("  - Next player: " + getCurrentPlayer().getName() + "\n");
        TurnCompleted best = annotated.getBestTurn();
        if (best != null) {
            result.append("  - Best play (" + best.getScore() + "): " + best.getPlacements() + "\n");
            QwirklePlayer leader = annotated.getLeader();
            result.append((isFinished() ? "Winner" : "Current leader") + ": "
                    + leader.getName() + " (" + annotated.getScore(leader) + ")\n");
        }

        // 3. Players - score & current pieces in hand
        for (QwirklePlayer player : getPlayers()) {
            List<QwirklePiece> hand = getHand(player);
            result.append("  - " + player.getName() + ": " + annotated.getScore(player) + " points");
            result.append(" - holds " + ((hand == null || hand.size() == 0) ? "nothing" : QwirklePiece.abbrev(hand)));
            result.append("\n");
        }
        result.append(" -- Total of all scores: " + annotated.getTotalScore() + "\n");

        // 4. Is the game stalled?
        if (isStalled()) {
            result.append("-------------------\n")
                    .append("--- Stalled (" + getNPasses() + ") ---\n")
                    .append("-------------------\n");
        }

        return result.toString();
    }
}
