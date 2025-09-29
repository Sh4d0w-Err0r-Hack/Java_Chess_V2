package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String password;
    private int xp;
    private HashMap<String, String> preferences;
    private ArrayList<GameHistory> gameHistory;
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.xp = 0;
        this.preferences = new HashMap<>();
        this.gameHistory = new ArrayList<>();
        setDefaultPreferences();
    }
    
    private void setDefaultPreferences() {
        preferences.put("boardTheme", "Clásico");
        preferences.put("pieceSet", "default");
        preferences.put("musicVolume", "0.8");
    }
    
    // Getters y setters
    public String getUsername() { return username; }
    public int getXp() { return xp; }
    public void addXp(int amount) { this.xp += amount; }
    public HashMap<String, String> getPreferences() { return preferences; }
    public ArrayList<GameHistory> getGameHistory() { return gameHistory; }
    
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
    
    public void addGameHistory(GameHistory history) {
        gameHistory.add(history);
        if (gameHistory.size() > 50) { // Mantener solo las últimas 50 partidas
            gameHistory.remove(0);
        }
    }
}