package qwirkle.ui.swing.util;

import qwirkle.ui.control.DisposeUndisposer;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/** Swing implementation of DisposeUndisposer.
 *  Disposes when home is removed or hidden. */
public class SwingDisposeUndisposer implements DisposeUndisposer {
    private JComponent home;

    public SwingDisposeUndisposer(JComponent home) {
        this.home = home;
    }

    @Override
    public void register(final Worker worker) {
        home.addAncestorListener(new AncestorListener() {
            @Override public void ancestorRemoved(AncestorEvent event) {
                try {
                    worker.dispose();
                }
                catch (IllegalArgumentException ignored) {} // sometimes double-removed because of events
            }
            @Override public void ancestorAdded(AncestorEvent event) {
                worker.undispose();
            }
            @Override public void ancestorMoved(AncestorEvent event) { }
        });
    }
}
