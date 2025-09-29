package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class PieceSetManager {
    private HashMap<String, HashMap<String, BufferedImage>> pieceSets;
    private String currentPieceSet;
    
    public PieceSetManager() {
        pieceSets = new HashMap<>();
        currentPieceSet = "default";
        loadDefaultPieceSets();
    }
    
    private void loadDefaultPieceSets() {
        // Cargar sets de piezas por defecto
        loadPieceSet("default");
        loadPieceSet("classic");
        loadPieceSet("modern");
    }
    
    private void loadPieceSet(String setName) {
        HashMap<String, BufferedImage> set = new HashMap<>();
        String[] pieces = {"pawn", "rook", "knight", "bishop", "queen", "king"};
        
        for (String piece : pieces) {
            try {
                BufferedImage white = ImageIO.read(getClass().getResourceAsStream(
                    "/piece/" + setName + "/w-" + piece + ".png"));
                BufferedImage black = ImageIO.read(getClass().getResourceAsStream(
                    "/piece/" + setName + "/b-" + piece + ".png"));
                
                set.put("w-" + piece, white);
                set.put("b-" + piece, black);
            } catch (Exception e) {
                System.out.println("Error cargando pieza: " + piece + " del set: " + setName);
            }
        }
        
        pieceSets.put(setName, set);
    }
    
    public BufferedImage getPieceImage(String pieceKey) {
        HashMap<String, BufferedImage> set = pieceSets.get(currentPieceSet);
        if (set != null && set.containsKey(pieceKey)) {
            return set.get(pieceKey);
        }
        return null;
    }
    
    public void setCurrentPieceSet(String pieceSet) {
        if (pieceSets.containsKey(pieceSet)) {
            currentPieceSet = pieceSet;
        }
    }
    
    public String[] getAvailablePieceSets() {
        return pieceSets.keySet().toArray(new String[0]);
    }
}