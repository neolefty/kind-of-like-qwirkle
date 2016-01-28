package qwirkle.ui.swing.piece.impl;

import qwirkle.game.base.QwirkleShape;

public class PaintPolygon extends PaintStar {
    public PaintPolygon(int points, QwirkleShape shape) {
        super(points, shape, 1 / Math.cos(Math.PI / points));
    }
}
