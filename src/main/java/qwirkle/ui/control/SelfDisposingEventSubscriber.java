package qwirkle.ui.control;

import com.google.common.eventbus.EventBus;

/** Automatically unregisters with its EventBus on a dispose event and
 *  re-registers on undispose.
 *  Seems weird to remove when hidden as well as when removed, but in Swing
 *  I couldn't separate being made invisible from being removed from a container. */
public class SelfDisposingEventSubscriber {
    /** Implement this for each platform to dispose and undispose a resource that is
     *  attached to a short-lived UI element, based on UI changes. */
    public interface PlatformAttacher {
        void register(DisposeUndisposer delegate);
    }

    public interface DisposeUndisposer {
        void dispose();
        void undispose();
    }

    public SelfDisposingEventSubscriber(final EventBus bus, final PlatformAttacher du) {
        bus.register(this);
        if (du != null) {
            du.register(new DisposeUndisposer() {
                @Override
                public void dispose() {
                    bus.unregister(SelfDisposingEventSubscriber.this);
                    //                    System.out.println("--- " + home.getClass().getSimpleName()
                    //                            + " hidden or removed from " + event.getAncestor().getClass().getSimpleName());
                }

                @Override
                public void undispose() {
                    bus.register(SelfDisposingEventSubscriber.this);
                    //                System.out.println("+++ " + home.getClass().getSimpleName()
                    //                        + " visible or added to " + event.getAncestor().getClass().getSimpleName());
                }
            });
        }
    }
}
