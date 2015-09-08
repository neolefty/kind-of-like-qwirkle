package qwirkle.players;

import qwirkle.control.ThreadingStrategy;
import qwirkle.game.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Wrap a regular {@link QwirklePlayer} inside an {@link AsyncPlayer}. */
public class AsyncPlayerWrapper extends AsyncPlayer {
    private QwirklePlayer player;

    public AsyncPlayerWrapper(QwirklePlayer player) {
        if (player == null)
            throw new NullPointerException("Wrapped player is null.");
        this.player = player;
    }

    @Override
    public void awaitDecision(final QwirkleBoard board, final List<QwirklePiece> hand,
                              ThreadingStrategy strategy, final PlayOrDiscard callback)
    {
        strategy.execute(new Runnable() {
            @Override
            public void run() {
                Collection<QwirklePlacement> play = player.play(board, hand);
                if (play != null && !play.isEmpty())
                    callback.play(play);
                else
                    callback.discard(player.discard(board, hand));
            }
        });
    }

    @Override
    public String getName() {
        return player.getName();
    }

    /** Wrap {@link QwirklePlayer}s inside {@link AsyncPlayerWrapper}s. */
    public static List<AsyncPlayer> wrap(Collection<QwirklePlayer> players) {
        List<AsyncPlayer> result = new ArrayList<>();
        if (players != null)
            for (QwirklePlayer player : players)
                result.add(new AsyncPlayerWrapper(player));
        return result;
    }
}
