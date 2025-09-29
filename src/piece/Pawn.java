package piece;
import main.GamePanel;
import main.Type;

public class Pawn extends Piece{
    public Pawn(int color,int col,int row) {
        super(color,col,row);
        
        type=Type.PAWN;
        
        if(color==GamePanel.WHITE) {
            image=getImage("/piece/w-pawn");
        }else {
            image=getImage("/piece/b-pawn");
        }
    }
    
    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBoard(targetCol,targetRow) && !isSameSquare(targetCol,targetRow)) {
            int moveValue;
            if(color==GamePanel.WHITE) {
                moveValue=-1;
            }else {
                moveValue=1;
            }
            
            hittingP=getHittingP(targetCol,targetRow);
            
            // Movimiento hacia adelante (solo si la casilla está vacía)
            if(targetCol==preCol && targetRow==preRow+moveValue && hittingP==null) {
                return true;
            }
            
            // Movimiento inicial de dos casillas (solo si ambas casillas están vacías)
            if(targetCol==preCol && targetRow==preRow + moveValue*2 && moved==false && hittingP==null) {
                // Verificar que la casilla intermedia esté vacía
                int intermediateRow = preRow + moveValue;
                if(getHittingP(targetCol, intermediateRow) == null) {
                    return true;
                }
            }
            
            // Captura en diagonal (solo si hay una pieza enemiga)
            if(Math.abs(targetCol-preCol)==1 && targetRow==preRow+moveValue) {
                if(hittingP!=null && hittingP.color !=color) {
                    return true;
                }
            }
        }
        return false;
    }
}