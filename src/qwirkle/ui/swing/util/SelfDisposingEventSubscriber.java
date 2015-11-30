package qwirkle.ui.swing.util;

import com.google.common.eventbus.EventBus;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/** Automatically unregisters with its EventBus when it is removed from its Swing parent.
 *  Seems weird to remove when hidden as well as when removed, but it's the only
 *  event that I can find that you receive when you are removed from a container. */
public class SelfDisposingEventSubscriber {
    public SelfDisposingEventSubscriber(final EventBus bus, final JComponent home) {
        bus.register(this);
        home.addAncestorListener(new AncestorListener() {
            @Override public void ancestorRemoved(AncestorEvent event) {
                try {
//                    Class declaring = getClass().getDeclaringClass();
//                    System.out.println("class " + getClass().getCanonicalName() + " declared in "
//                            + (declaring == null ? "unknown" : declaring.getSimpleName()));
                    bus.unregister(SelfDisposingEventSubscriber.this);
                    System.out.println("--- " + home.getClass().getSimpleName()
                            + " hidden or removed from " + event.getAncestor().getClass().getSimpleName());
                }
                catch (IllegalArgumentException ignored) {} // sometimes double-removed because of events
            }
            @Override public void ancestorAdded(AncestorEvent event) {
                bus.register(SelfDisposingEventSubscriber.this);
                System.out.println("+++ " + home.getClass().getSimpleName()
                        + " visible or added to " + event.getAncestor().getClass().getSimpleName());
            }
            @Override public void ancestorMoved(AncestorEvent event) { }
        });
    }
}
