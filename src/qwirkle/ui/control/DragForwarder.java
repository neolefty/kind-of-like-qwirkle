package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.event.PassOver;

/** Forward drag events from one EventBus to another, so that a
 *  drag that starts in one can pass over and conclude with another.
 *  Generally need to forward from local each GridPanel EventBus to
 *  the gameboard's EventBus, which is the "main" one. */
public class DragForwarder extends SelfDisposingEventSubscriber {
    private EventBus externalBus;

    /** Forward drag events from <tt>localBus</tt> to <tt>externalBus</tt>.
     *  @param du if disposed, stop forwarding */
    public DragForwarder(EventBus localBus, PlatformAttacher du, EventBus externalBus) {
        super(localBus, du);
        this.externalBus = externalBus;
    }

    @Subscribe public void dragPosted(DragPiece event) { externalBus.post(event); }
    @Subscribe public void passedOver(PassOver event) { externalBus.post(event); }
}
