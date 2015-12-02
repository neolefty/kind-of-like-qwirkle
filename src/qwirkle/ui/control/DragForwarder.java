package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.event.PassOver;
import qwirkle.ui.swing.util.SelfDisposingEventSubscriber;

import javax.swing.*;

/** Forward drag events from one EventBus to another, so that a
 *  drag that starts in one can pass over and conclude with another.
 *  Generally need to forward from local each GridPanel EventBus to
 *  the gameboard's EventBus, which is the "main" one. */
public class DragForwarder extends SelfDisposingEventSubscriber {
    private EventBus externalBus;

    /** Forward drag events from <tt>localBus</tt> to <tt>externalBus</tt>.
     *  @param home if ever removed from its parent, stop forwarding (also while it is hidden) */
    public DragForwarder(EventBus localBus, JComponent home, EventBus exernalBus) {
        super(localBus, home);
        this.externalBus = exernalBus;
    }

    @Subscribe public void dragPosted(DragPiece event) { externalBus.post(event); }
    @Subscribe public void passedOver(PassOver event) { externalBus.post(event); }
}
