package piece;

import main.GamePanel;
import main.Type;

public class Knight extends Piece{

    public Knight(int color, int col, int row) {
        super(color, col, row);
        type=Type.KNIGHT;
        if(color==GamePanel.WHITE) {
            image=getImage("/piece/w-knight");
        }else {
            image=getImage("/piece/b-knight");
        }
    }
    
    public boolean canMove(int targetCol,int targetRow) {
        if(isWithinBoard(targetCol,targetRow)) {
            // Movimiento en L: 2 casillas en una dirección y 1 en la perpendicular
            if((Math.abs(targetCol-preCol)==2 && Math.abs(targetRow-preRow)==1) ||
               (Math.abs(targetCol-preCol)==1 && Math.abs(targetRow-preRow)==2)) {
                if(isValidSquare(targetCol,targetRow)) {
                    return true;
                }
            }
        }
        return false;
    }
}