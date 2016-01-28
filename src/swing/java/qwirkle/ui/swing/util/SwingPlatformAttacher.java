package qwirkle.ui.swing.util;

import qwirkle.ui.control.SelfDisposingEventSubscriber;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/** Swing implementation of DisposeUndisposer.
 *  Disposes when home is removed or hidden. */
public class SwingPlatformAttacher implements SelfDisposingEventSubscriber.PlatformAttacher {
    private JComponent home;

    public SwingPlatformAttacher(JComponent home) {
        this.home = home;
    }

    @Override
    public void register(final SelfDisposingEventSubscriber.DisposeUndisposer delegate) {
        home.addAncestorListener(new AncestorListener() {
            @Override public void ancestorRemoved(AncestorEvent event) {
                try {
                    delegate.dispose();
                }
                catch (IllegalArgumentException ignored) {} // sometimes double-removed because of events
            }
            @Override public void ancestorAdded(AncestorEvent event) {
                delegate.undispose();
            }
            @Override public void ancestorMoved(AncestorEvent event) { }
        });
    }
}
