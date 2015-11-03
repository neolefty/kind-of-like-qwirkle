package qwirkle.game;

import qwirkle.control.ThreadingStrategy;

import java.util.Collection;
import java.util.List;

/** A QwirklePlayer that works asynchronously. To enable human players. */
public abstract class AsyncPlayer {
    public interface PlayOrDiscard {
        void play(Collection<QwirklePlacement> placements);
        void discard(Collection<QwirklePiece> pieces);
    }

    abstract public void awaitDecision
            (QwirkleBoard board, List<QwirklePiece> hand,
             ThreadingStrategy threading, PlayOrDiscard callback);

    abstract public String getName();
}
