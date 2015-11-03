package qwirkle.ui.swing.util;

import qwirkle.game.AsyncPlayer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/** Supports dragging stuff. */
public class DragHelper {
    // is dragging currently possible?
    private boolean draggable = true;

    // are we mid-drag?
    private boolean dragging = false;

    // pixel distance required to initiate a drag
    private double dragActivateDistance = 5;

    // the last place a mouse button was pressed
    private Point latestMousePress = null;

    private DragHandler handler;

    // the player who is acting
    private AsyncPlayer player;

    /** Implement this and pass it to {@link #DragHelper}. */
    public interface DragHandler {
        /** Called when a drag is started (mouse is clicked and dragged beyond a certain threshold. */
        void startDrag(MouseEvent e);

        /** Called when the mouse moves during an ongoing drag. */
        void keepDragging(MouseEvent e);

        /** Called when a drag ends (mouse is released). */
        void endDrag(MouseEvent e);

        /** Called when a drag is canceled (currently only if dragging is turned off mid-drag). */
        void cancelDrag();
    }

    public DragHelper(Component listenTo, final DragHandler handler) {
        if (handler == null) throw new NullPointerException("handler is null");
        this.handler = handler;

        listenTo.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDraggable()) {
                    // if we're already dragging, keep going
                    if (isDragging())
                        handler.keepDragging(e);
                    // have we moved the mouse far enough to initiate a drag?
                    else if (latestMousePress != null && e.getPoint().distance(latestMousePress) >= dragActivateDistance) {
                        setDragging(true);
                        handler.startDrag(e);
                    }
                }
            }
        });

        listenTo.addMouseListener(new MouseAdapter() {
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
                if (isDragging()) {
                    setDragging(false);
                    handler.endDrag(e);
                }
            }
        });
    }

    /** Can a drag-and-drop be initiated over this panel? Default false.
     *  If true, be sure to override {@link DragHandler#startDrag} etc. */
    public boolean isDraggable() { return draggable; }

    /** Are we currently mid-drag? */
    public boolean isDragging() { return dragging; }

    /** The player making an action. */
    public AsyncPlayer getPlayer() { return player; }

    /** Are we currently mid-drag? */
    private void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    /** Make this draggable. */
    public void makeDraggable(AsyncPlayer player) {
        this.player = player;
        this.draggable = true;
    }

    /** Make this no longer draggable. */
    public void makeUndraggable() {
        // need to cancel if we turn off draggability while currently dragging
        if (isDragging()) {
            handler.cancelDrag();
            dragging = false;
        }
        this.player = null;
        this.draggable = false;
    }
}
