package main;

import java.util.ArrayList;
import java.util.Random;
import piece.*;

public class AIPlayer {
    private int color;
    private Random random;
    
    private static final int WHITE = 0;
    private static final int BLACK = 1;
    
    public AIPlayer(int color) {
        this.color = color;
        this.random = new Random();
    }
    
    public AIMove findBestMove(ArrayList<Piece> pieces, int currentColor) {
    	
    	boolean opponentKingExists = false;
        boolean myKingExists = false;
        
        for (Piece piece : pieces) {
            if (piece.type == Type.KING) {
                if (piece.color == this.color) {
                    myKingExists = true;
                } else {
                    opponentKingExists = true;
                }
            }
        }
        
        if (!myKingExists || !opponentKingExists) {
            return null; // No hacer movimientos si faltan reyes
        }
        ArrayList<AIMove> allPossibleMoves = generateAllPossibleMoves(pieces);
        
        if (allPossibleMoves.isEmpty()) {
            return null;
        }
        
        // Priorizar movimientos de captura
        ArrayList<AIMove> capturingMoves = new ArrayList<>();
        ArrayList<AIMove> safeMoves = new ArrayList<>();
        ArrayList<AIMove> allMoves = new ArrayList<>();
        
        for (AIMove move : allPossibleMoves) {
            if (isCapturingMove(move, pieces)) {
                capturingMoves.add(move);
            } else if (isSafeSquare(move.targetCol, move.targetRow, pieces)) {
                safeMoves.add(move);
            } else {
                allMoves.add(move);
            }
        }
        
        // Prioridad: capturas > movimientos seguros > otros movimientos
        if (!capturingMoves.isEmpty()) {
            return getRandomMove(capturingMoves);
        }
        
        if (!safeMoves.isEmpty()) {
            return getRandomMove(safeMoves);
        }
        
        return getRandomMove(allMoves);
    }
    
    private ArrayList<AIMove> generateAllPossibleMoves(ArrayList<Piece> pieces) {
        ArrayList<AIMove> allMoves = new ArrayList<>();
        
        for (Piece piece : pieces) {
            if (piece.color == this.color) {
                ArrayList<AIMove> pieceMoves = generateMovesForPiece(piece, pieces);
                allMoves.addAll(pieceMoves);
            }
        }
        
        return allMoves;
    }
    
    private ArrayList<AIMove> generateMovesForPiece(Piece piece, ArrayList<Piece> allPieces) {
        ArrayList<AIMove> moves = new ArrayList<>();
        
        // Probar todas las casillas del tablero
        for (int targetCol = 0; targetCol < 8; targetCol++) {
            for (int targetRow = 0; targetRow < 8; targetRow++) {
                
                // Saltar si es la misma casilla
                if (piece.col == targetCol && piece.row == targetRow) {
                    continue;
                }
                
                // Verificar si el movimiento es válido según las reglas de la pieza
                if (piece.canMove(targetCol, targetRow)) {
                    
                    // Verificar que no sea una pieza del mismo color
                    boolean canCapture = true;
                    for (Piece p : allPieces) {
                        if (p.col == targetCol && p.row == targetRow && p.color == this.color) {
                            canCapture = false;
                            break;
                        }
                    }
                    
                    // Verificar si el movimiento es legal (no deja al rey en jaque)
                    if (canCapture && isMoveLegal(piece, targetCol, targetRow, allPieces)) {
                        moves.add(new AIMove(piece, targetCol, targetRow));
                    }
                }
            }
        }
        
        return moves;
    }
    
    private boolean isMoveLegal(Piece piece, int targetCol, int targetRow, ArrayList<Piece> allPieces) {
        // Crear una copia simulada del estado del tablero
        ArrayList<Piece> simulatedPieces = new ArrayList<>();
        for (Piece p : allPieces) {
            // Crear una nueva pieza con las mismas propiedades
            Piece copy = createPieceCopy(p);
            simulatedPieces.add(copy);
        }
        
        // Encontrar la pieza correspondiente en la simulación
        Piece simPiece = null;
        for (Piece p : simulatedPieces) {
            if (p.col == piece.col && p.row == piece.row && p.color == piece.color && p.type == piece.type) {
                simPiece = p;
                break;
            }
        }
        
        if (simPiece == null) return false;
        
        // Remover pieza capturada si existe
        Piece captured = null;
        for (Piece p : simulatedPieces) {
            if (p != simPiece && p.col == targetCol && p.row == targetRow) {
                captured = p;
                break;
            }
        }
        if (captured != null) {
            simulatedPieces.remove(captured);
        }
        
        // Mover la pieza
        simPiece.col = targetCol;
        simPiece.row = targetRow;
        
        // Verificar si el rey está en jaque después del movimiento
        return !isKingInCheck(simulatedPieces, this.color);
    }
    
    private Piece createPieceCopy(Piece original) {
        Piece copy = null;
        
        switch (original.type) {
            case PAWN:
                copy = new Pawn(original.color, original.col, original.row);
                break;
            case ROOK:
                copy = new Rook(original.color, original.col, original.row);
                break;
            case KNIGHT:
                copy = new Knight(original.color, original.col, original.row);
                break;
            case BISHOP:
                copy = new Bishop(original.color, original.col, original.row);
                break;
            case QUEEN:
                copy = new Queen(original.color, original.col, original.row);
                break;
            case KING:
                copy = new King(original.color, original.col, original.row);
                break;
        }
        
        if (copy != null) {
            copy.moved = original.moved;
            copy.preCol = original.preCol;
            copy.preRow = original.preRow;
        }
        
        return copy;
    }
    
    private boolean isKingInCheck(ArrayList<Piece> pieces, int kingColor) {
        Piece king = null;
        for (Piece p : pieces) {
            if (p.type == Type.KING && p.color == kingColor) {
                king = p;
                break;
            }
        }
        
        if (king == null) return true;
        
        for (Piece p : pieces) {
            if (p.color != kingColor && p.canMove(king.col, king.row)) {
                // Para piezas de largo alcance, verificar que el camino no esté bloqueado
                if (p.type == Type.ROOK || p.type == Type.BISHOP || p.type == Type.QUEEN) {
                    if (!isPathBlocked(pieces, p, king.col, king.row)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean isPathBlocked(ArrayList<Piece> pieces, Piece attacker, int targetCol, int targetRow) {
        int colDiff = targetCol - attacker.col;
        int rowDiff = targetRow - attacker.row;
        
        int colStep = Integer.signum(colDiff);
        int rowStep = Integer.signum(rowDiff);
        
        int steps = Math.max(Math.abs(colDiff), Math.abs(rowDiff));
        
        for (int i = 1; i < steps; i++) {
            int checkCol = attacker.col + colStep * i;
            int checkRow = attacker.row + rowStep * i;
            
            for (Piece p : pieces) {
                if (p.col == checkCol && p.row == checkRow) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean isCapturingMove(AIMove move, ArrayList<Piece> pieces) {
        for (Piece piece : pieces) {
            if (piece.col == move.targetCol && piece.row == move.targetRow && 
                piece.color != this.color) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSafeSquare(int col, int row, ArrayList<Piece> pieces) {
        for (Piece piece : pieces) {
            if (piece.color != this.color && piece.canMove(col, row)) {
                return false;
            }
        }
        return true;
    }
    
    private AIMove getRandomMove(ArrayList<AIMove> moves) {
        if (moves.isEmpty()) return null;
        return moves.get(random.nextInt(moves.size()));
    }
}