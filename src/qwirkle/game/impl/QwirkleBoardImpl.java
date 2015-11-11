package qwirkle.game.impl;

import qwirkle.game.*;

import java.util.*;

// TODO implement as overwrite immutable so that we don't have to recreate all the QwirkleLine's each time
/** Game logic of a Qwirkle board. */
public class QwirkleBoardImpl extends QwirkleGridImpl implements QwirkleBoard {
    private Set<QwirkleLine> lines = null;
    private Collection<QwirklePlacement> lastPlay = null;
    private int lastScore = -1;
    private int turnCount = 0;
    private QwirkleBoard previous = null;
    private QwirkleSettings settings;

    /** Default constructor. */
    public QwirkleBoardImpl(QwirkleSettings settings) {
        initSettings(settings);
    }

    private QwirkleBoardImpl
            (Collection<QwirklePlacement> placements,
             QwirkleBoard previous,
             Collection<QwirklePlacement> lastPlay)
    {
        super(placements);
        initSettings(previous.getSettings());
        this.previous = previous;
        this.turnCount = previous.getTurnCount() + 1;
        this.lastPlay = lastPlay;
        this.lastScore = computeLastScore();
    }

    /** Copy <tt>board</tt>. */
    public QwirkleBoardImpl(QwirkleBoard board) {
        this(board.getPlacements(), board.getUndo(), board.getLastPlay());
    }

    @Override public QwirkleBoard getUndo() { return previous; }

    @Override public Collection<QwirklePlacement> getLastPlay() { return lastPlay; }

    @Override public int getTurnCount() { return turnCount; }

    @Override public Collection<QwirkleLine> getLines() {
        ensureLines();
        return Collections.unmodifiableSet(lines);
    }

    @Override
    public QwirkleSettings getSettings() { return settings; }

    private void initSettings(QwirkleSettings settings) {
        if (this.settings != null)
            throw new IllegalStateException("Settings already initialized: " + this.settings);
        if (settings == null)
            throw new NullPointerException("Null settings.");
        this.settings = settings;
    }

    /** If lines haven't been built, build them. */
    private void ensureLines() {
        if (lines == null)
            buildLines();
    }

    @Override
    public QwirkleBoardImpl play(QwirklePlacement next) {
        if (next == null)
            throw new NullPointerException("Null play.");
        List<QwirklePlacement> play = new ArrayList<>();
        play.add(next);
        List<QwirklePlacement> newPlacements
                = new ArrayList<>(getPlacements());
        newPlacements.add(next);
        return new QwirkleBoardImpl(newPlacements, this, play);
    }

    @Override
    public QwirkleBoard play(Collection<QwirklePlacement> play) {
        // null play has no effect
        if (play == null || play.isEmpty())
            return this;

        if (!isLegal(play))
            throw new IllegalStateException("Not a legal play: " + play);

        // find an order to play these in that works
        QwirkleBoardImpl scratch = findLegalSequence(play);
        if (scratch == null)
            throw new IllegalStateException("Not a legal move: " + play);
        scratch.previous = this;
        scratch.lastPlay = Collections.unmodifiableCollection
                (new ArrayList<>(play));
        scratch.turnCount = this.turnCount + 1;
        scratch.lastScore = scratch.computeLastScore();

        return scratch;
    }

    /** Build up the list of <tt>QwirkleLine</tt>s. */
    private void buildLines() {
        lines = new HashSet<>();
//        if (previous != null) {
//            lastPlay.
//            for (QwirkleLine line : previous.getLines())
//        }
        // include padding of one so that we'll find blank spaces after all lines and close the lines.
        buildLines(getHorizontalWalker(1));
        buildLines(getVerticalWalker(1));
    }

    private void buildLines(GridWalker walker) {
        for (LineWalker line : walker)
            buildLines(line);
    }

    private void buildLines(LineWalker walker) {
        QwirkleLine curLine = null;
        for (QwirkleLocation location : walker) {
            QwirklePlacement placement = getPlacement(location);
            // found an empty spot
            if (placement == null) {
                if (curLine != null) { // reached the end of a line
                    lines.add(curLine);
                    curLine = null;
                }
            }
            // found a piece
            else {
                if (curLine == null) // start a new line
                    curLine = new QwirkleLine(placement, settings);
                else // add it to the current line
                    curLine = curLine.augment(placement);
            }
        }
    }

    /** Compute the score of the most recent play. */
    private int computeLastScore() {
        // the score is the sum of the scores for all of the lines that
        // the new pieces intersect.
        Set<QwirkleLine> lines = new HashSet<>();
        for (QwirklePlacement place : lastPlay)
            lines.addAll(getIntersectingLines(place.getLocation()));
        int result = 0;
        for (QwirkleLine line : lines)
            if (line.size() > 1) // ignore degenerate "line"s
                result += line.getScore();
        // on the first turn, if you only played one piece, you get a point
        if (result == 0 && lastPlay.size() > 0)
            result = 1;
        return result;
    }

    /** All the lines that intersect <tt>location</tt>. */
    private Set<QwirkleLine> getIntersectingLines(QwirkleLocation location) {
        ensureLines(); // ensure lines have been built
        Set<QwirkleLine> result = new HashSet<>();
        for (QwirkleLine line : lines)
            if (line.contains(location))
                result.add(line);
        return result;
    }

    @Override
    public int getLastScore() {
        return lastScore;
    }

    @Override
    public boolean isLegal(QwirklePlacement placement) {
        // first piece on the board? Always okay.
        if (size() == 0)
            return true;

        // occludes existing piece? not okay
        if (get(placement.getLocation()) != null)
            return false;

        // 1. check all existing lines that this would join
        // (including lines of length 1)
        Set<QwirkleLine> lines = getLinesWithEndpoint(placement.getLocation());
        if (lines.isEmpty()) return false; // no matches
        for (QwirkleLine line : lines) {
            // if it doesn't fit with one of the lines, it's not legal
            if (!line.isLegal(placement))
                return false;
        }
        for (QwirkleLine line : lines) {
            for (QwirkleLine other : lines) {
                // If this placement would combine two existing lines to make a new line, is it a legal line?
                if (line != other && line.isAlignedWith(other) && !line.canJoinWith(other, placement))
                    return false; // if the new line would not be legal, reject this placement
            }
        }

        // passed all tests!
        return true;
    }

    @Override
    public boolean isLegal(Collection<QwirklePlacement> play) {
        if (play.size() == 0)
            return true;

        if (!isInLineWithNoGaps(play))
            return false;

        QwirkleBoardImpl next = findLegalSequence(play);
        return next != null;
    }

    /** Find a legal sequence of moves to place <tt>play</tt>.
     *  If there is one, return the new board that results.
     *  Otherwise return null.
     *
     *  <p><strong>Warning:</strong> Doesn't set result's state correctly
     *  (previous, lastMove, etc) -- for that, see {@link #play}.</p>*/
    private QwirkleBoardImpl findLegalSequence(Collection<QwirklePlacement> play) {
        Set<QwirklePlacement> remaining = new HashSet<>(play);
        if (remaining.size() != play.size())
            throw new IllegalArgumentException
                    ("Duplicate placements: " + play);
        QwirkleBoardImpl scratch = this;
        // find an order that works, by playing them one at a time
        while (!remaining.isEmpty()) {
            // look for a piece that can be played next
            QwirklePlacement chosen = null;
            for (QwirklePlacement place : remaining) {
                if (scratch.isLegal(place)) {
                    chosen = place;
                    remaining.remove(chosen);
                    scratch = scratch.play(chosen);
                    // only play & remove one at a time to avoid concurrent mod exception
                    break;
                }
            }
            if (chosen == null)
                return null;
        }
        return scratch;
    }

    private boolean isInLineWithNoGaps(Collection<QwirklePlacement> play) {
        NavigableSet<QwirkleLocation> locations = new TreeSet<>();
        // put them in order of increasing x & y
        for (QwirklePlacement p : play)
            locations.add(p.getLocation());
        return isVerticalWithNoGaps(locations) || isHorizontalWithNoGaps(locations);
    }

    private boolean isVerticalWithNoGaps(NavigableSet<QwirkleLocation> play) {
        Iterator<QwirkleLocation> i = play.iterator();
        QwirkleLocation here = i.next();
        int y = here.getY(), x = here.getX();
        // walk from one end of the play to the other
        // each position must be either already on the board
        // or in the list of pieces to be played
        while (i.hasNext()) {
            QwirkleLocation next = i.next();
            if (next.getX() != x) // is it vertical (all x's same)?
                return false;
            ++y;
            while (y < next.getY())
                if (get(new QwirkleLocation(x, y)) == null)
                    return false;
                else
                    ++y;
        }
        return true;
    }

    private boolean isHorizontalWithNoGaps(NavigableSet<QwirkleLocation> play) {
        Iterator<QwirkleLocation> i = play.iterator();
        QwirkleLocation here = i.next();
        int y = here.getY(), x = here.getX();
        // walk from one end of the play to the other
        // each position must be either already on the board
        // or in the list of pieces to be played
        while (i.hasNext()) {
            QwirkleLocation next = i.next();
            if (next.getY() != y) // is it horizontal (all y's same)?
                return false;
            ++x;
            while (x < next.getX())
                if (get(new QwirkleLocation(x, y)) == null)
                    return false;
                else
                    ++x;
        }
        return true;
    }

    /** All open endpoints of lines. */
    private Set<QwirkleLocation> getAllEndpoints() {
        ensureLines();
        Set<QwirkleLocation> result = new HashSet<>();
        for (QwirkleLine line : lines)
            for (QwirkleLocation location : line.getEnds())
                // single-length lines have 4 endpoints, which often overlap other lines
                if (!hasPieceAt(location))
                    result.add(location);
        return result;
    }

    /** The lines with this point as an endpoint. */
    private Set<QwirkleLine> getLinesWithEndpoint(QwirkleLocation location) {
        ensureLines();
        Set<QwirkleLine> result = new HashSet<>();
        boolean containsSingles = false, containsBigs = false;
        for (QwirkleLine line : lines)
            if (line.getEnds().contains(location)) {
                result.add(line);
                if (line.size() == 1) containsSingles = true;
                else if (line.size() > 1) containsBigs = true;
            }
        // remove single-length lines that are subsets of longer lines in the result
        if (containsSingles && containsBigs) {
            List<QwirkleLine> resultCopy = new ArrayList<>(result);
            for (QwirkleLine single : resultCopy) {
                if (single.size() == 1) {
                    QwirklePlacement place = single.iterator().next();
                    for (QwirkleLine bigger : result) {
                        if (bigger.size() > 1 && bigger.contains(place)) {
                            result.remove(single);
                            break; // go on to the next single (and avoid a ConcurrentModificationException)
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Collection<QwirklePlacement> getLegalPlacements
            (QwirklePiece piece)
    {
        Set<QwirklePlacement> result = new HashSet<>();
        // empty board: place at 0, 0
        if (size() == 0)
            result.add(new QwirklePlacement(piece, 0, 0));
        else {
            // check each open space
            for (QwirkleLocation location : getAllEndpoints()) {
                QwirklePlacement placement = new QwirklePlacement(piece, location);
                if (isLegal(placement))
                    result.add(placement);
/*
                // is that open space okay for all lines that touch it?
                boolean allLegal = true;
                Set<QwirkleLine> lines = getLinesWithEndpoint(location);
                for (QwirkleLine line : lines) {
                    if (!line.isLegal(placement)) {
                        allLegal = false;
                        break;
                    }
                }
                if (allLegal) {
                    if (lines.size() == 1)
                        result.add(placement);
                    else if (lines.size() > 1 && isLegal(placement))
                    // if more than one line, would the new line that is created be a legal line too?
                    if (lines.size() < 2 || isLegal(placement))
                        result.add(placement);
                }
*/
            }
        }
        return result;
    }

    @Override
    public Collection<QwirklePlacement> getLegalPlacements
            (Collection<QwirklePlacement> play, QwirklePiece piece)
    {
        if (play == null || play.isEmpty())
            return getLegalPlacements(piece);
        else {
            Collection<QwirklePlacement> candidates = play(play).getLegalPlacements(piece);
            List<QwirklePlacement> result = new ArrayList<>();
            List<QwirklePlacement> scratch = new ArrayList<>(play);
            for (QwirklePlacement candidate : candidates) {
                scratch.add(candidate);
                if (isLegal(scratch))
                    result.add(candidate);
                scratch.remove(candidate);
            }
            return result;
        }
    }
}
