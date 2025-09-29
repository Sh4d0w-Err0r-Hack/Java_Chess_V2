package piece;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Board;
import main.GamePanel;
import main.Type;

public class Piece {
    public Type type;
    public BufferedImage image;
    public int x,y;
    public int col,row,preCol,preRow;
    public int color;
    public Piece hittingP;
    public boolean moved,twoStepped;
    
    public Piece(int color,int col,int row) {
        this.color=color;
        this.col=col;
        this.row=row;
        x=getX(col);
        y=getY(row);
        preCol=col;
        preRow=row;
    }
    
    public BufferedImage getImage(String imagePath) {
        BufferedImage image=null;
        try {
            image=ImageIO.read(getClass().getResourceAsStream(imagePath+".png"));
        }catch(IOException e) {
            e.printStackTrace();
        }
        return image;
    }
    
    public int getX(int col) {
        return col*Board.SQUARE_SIZE;
    }
    public int getY(int row) {
        return row*Board.SQUARE_SIZE;
    }
    
    public int getCol(int x) {
        return((x+Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE);
    }
    public int getRow(int y) {
        return((y+Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE);
    }
    
    public int getIndex() {
        for(int index=0;index<GamePanel.simPieces.size();index++) {
            if(GamePanel.simPieces.get(index)==this) {
                return index;
            }
        }
        return 0;
    }
    
    public void updatePosition() {
        x=getX(col);
        y=getY(row);
        preCol=getCol(x);
        preRow=getRow(y);
        moved=true;
    }
    
    public void resetPosition() {
        col=preCol;
        row=preRow;
        x=getX(col);
        y=getY(row);
    }
    
    public boolean canMove(int targetCol,int targetRow) {
        return false;
    }
    
    public boolean isWithinBoard(int targetCol,int targetRow) {
        if(targetCol>=0 && targetCol<=7 && targetRow>=0 && targetRow<=7) {
            return true;
        }
        return false;
    }
    
    public boolean isSameSquare(int targetCol,int targetRow) {
        if(targetCol==preCol && targetRow==preRow) {
            return true;
        }
        return false;
    }
    
    public Piece getHittingP(int targetCol,int targetRow) {
        for(Piece piece:GamePanel.simPieces) {
            if(piece.col==targetCol&&piece.row==targetRow && piece!=this) {
                return piece;
            }
        }
        return null;
    }
    
    public boolean isValidSquare(int targetCol,int targetRow) {
        hittingP = getHittingP(targetCol,targetRow);
        if(hittingP == null) {
            return true;
        } else {
            // CORREGIDO: Verificar claramente que es una pieza enemiga
            if(hittingP.color != this.color) {
                return true; // Puede capturar
            } else {
                hittingP = null; // Pieza aliada, no puede capturar
                return false;
            }
        }
    }
    
    // MÉTODO MEJORADO - no marca piezas en la casilla destino como bloqueo
    public boolean isPathBlocked(int targetCol, int targetRow) {
        int colDiff = targetCol - preCol;
        int rowDiff = targetRow - preRow;
        
        // Si no es movimiento en línea recta o diagonal, no hay camino que verificar
        if (Math.abs(colDiff) != Math.abs(rowDiff) && colDiff != 0 && rowDiff != 0) {
            return false; // Movimientos de caballo no tienen camino que verificar
        }
        
        int colStep = Integer.signum(colDiff);
        int rowStep = Integer.signum(rowDiff);
        
        int steps = Math.max(Math.abs(colDiff), Math.abs(rowDiff));
        
        // Verificar solo las casillas INTERMEDIAS, no la destino
        for (int i = 1; i < steps; i++) {
            int checkCol = preCol + colStep * i;
            int checkRow = preRow + rowStep * i;
            
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == checkCol && piece.row == checkRow) {
                    hittingP = piece;
                    return true;
                }
            }
        }
        return false;
    }
    
    public void draw(Graphics2D g2) {
        if (image != null) {
            g2.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
        }
    }
}