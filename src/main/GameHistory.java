package main;

import java.io.Serializable;
import java.util.Date;

public class GameHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Date date;
    private String gameMode;
    private String opponent;
    private String result; // "win", "loss", "draw"
    private int xpChange;
    private int duration; // en segundos
    
    public GameHistory(String gameMode, String opponent, String result, int xpChange, int duration) {
        this.date = new Date();
        this.gameMode = gameMode;
        this.opponent = opponent;
        this.result = result;
        this.xpChange = xpChange;
        this.duration = duration;
    }
    
    // Getters
    public Date getDate() { return date; }
    public String getGameMode() { return gameMode; }
    public String getOpponent() { return opponent; }
    public String getResult() { return result; }
    public int getXpChange() { return xpChange; }
    public int getDuration() { return duration; }
}
