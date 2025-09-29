package main;

import piece.Piece;

public class AIMove {
    public Piece piece;
    public int targetCol;
    public int targetRow;
    
    public AIMove(Piece piece, int targetCol, int targetRow) {
        this.piece = piece;
        this.targetCol = targetCol;
        this.targetRow = targetRow;
    }
}