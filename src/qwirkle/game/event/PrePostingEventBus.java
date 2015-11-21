package qwirkle.game.event;

import com.google.common.eventbus.EventBus;

/** An EventBus that pre-posts events using {@link PreEvent} */
public class PrePostingEventBus extends EventBus {
    @Override
    public void post(Object event) {
        super.post(new PreEvent(event));
        super.post(event);
    }
}
