package qwirkle.ui.swing.util;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.event.TurnStarting;
import qwirkle.ui.QwirklePieceDisplay;
import qwirkle.ui.control.SelfDisposingEventSubscriber;
import qwirkle.ui.event.DragPiece;
import qwirkle.ui.event.PassOver;
import qwirkle.ui.swing.impl.SwingPlatformAttacher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Supports dragging pieces by creating PieceDrag events from Swing internals. */
public class SwingDragHelper {
    private EventBus eventBus;

    // is dragging currently possible?
    private boolean draggable = true;

    // pixel distance required to initiate a drag
    private double dragActivateDistance = 5;

    // the last place a mouse button was pressed -- transient but never nulled
    private Point latestMousePress = null;

    // the piece display we are helping -- never changes
    private QwirklePieceDisplay pieceDisplay;

    // the current QwirklePieceDisplay this is over
    private QwirklePieceDisplay transientCurrentlyOver;

    // The current player
    private QwirklePlayer transientCurPlayer;

    // The current active drag event
    private DragPiece transientPickup;

    public SwingDragHelper
            (final JComponent component, QwirklePlayer curPlayer, EventBus eventBus, QwirklePieceDisplay display)
    {
        this.eventBus = eventBus;
        this.pieceDisplay = display;
        this.transientCurPlayer = curPlayer;

        // keep track of some state: last display that was passed over & current player
        eventBus.register(new SelfDisposingEventSubscriber(eventBus, new SwingPlatformAttacher(component)) {
            @Subscribe public void passOver(PassOver event) {
                if (event.isEnter())
                    transientCurrentlyOver = event.getDisplay();
                else
                    transientCurrentlyOver = null;
            }
            @Subscribe public void turnStart(TurnStarting event) {
                transientCurPlayer = event.getCurPlayer();
            }
        });

        component.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDraggable()) {
                    // if we're already dragging, keep going
                    if (isDragging())
                        sustain();
                        // have we moved the mouse far enough to initiate a drag?
                    else if (latestMousePress != null && e.getPoint().distance(latestMousePress) >= dragActivateDistance) {
                        pickup();
                    }
                }
            }
        });

        component.addMouseListener(new MouseAdapter() {
            /** Note that once a mouse is pressed in this Component, it will receive all mouse events
             *  until it is released. Therefore, events need to be forwarded if a different Component
             *  is supposed to get them.
             *  <p>See {@link MouseEvent} for details.</p> */
            @Override
            public void mousePressed(MouseEvent e) {
                latestMousePress = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                latestMousePress = null;
                dragDebug("released");
                drop();
            }
        });

        component.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { cancel(); }
        });
    }

    /** Can a drag-and-drop currently be initiated over this panel? */
    public boolean isDraggable() { return draggable; }

    /** Make this draggable or undraggable.
     *  If we're currently dragging and we turn it off, cancel the drag. */
    public void setDraggable(boolean draggable) {
        if (!draggable)
            cancel();
        this.draggable = draggable;
    }

    /** Are we currently mid-drag? */
    public boolean isDragging() { return transientPickup != null; }

    /** A drag is started (mouse is clicked and dragged beyond a certain threshold. */
    private void pickup() {
        if (isDragging())
            cancel();
        DragPiece pickup = DragPiece.createPickup(transientCurPlayer, pieceDisplay);
        this.transientPickup = pickup; // avoid concurrency problems
        eventBus.post(pickup);
    }

    /** The mouse moves during an ongoing drag. */
    private void sustain() {
        DragPiece tmpPickup = transientPickup; // avoid concurrency problems
        if (tmpPickup != null)
            // okay if transientCurrentlyOver changes or is null
            eventBus.post(tmpPickup.sustain(transientCurrentlyOver));
    }

    /** A drag ends (mouse is released). If not over a piece display, cancel instead.
     *  No effect if not currently dragging. */
    private void drop() {
        QwirklePieceDisplay curDisplay = transientCurrentlyOver; // avoid concurrency problems
        if (curDisplay == null)
            cancel();
        else {
            DragPiece curPickup = transientPickup;
            this.transientPickup = null;
            if (curPickup != null)
                eventBus.post(curPickup.drop(curDisplay));
        }
    }

    /** Cancel dragging, if we're dragging -- otherwise no effect. */
    private void cancel() {
        DragPiece tmp = transientPickup;
        this.transientPickup = null;
        if (tmp != null)
            eventBus.post(tmp.cancel());
    }
    private static final boolean DEBUG = false;

    private void dragDebug(String msg) {
        if (isDragging())
            if (DEBUG)
                System.out.println("**** " + msg + " ****");
    }
}
