package main;

public class GameConfig {
    private GameMode gameMode;
    private boolean playerStartsAsBlack;
    private String boardTheme;
    private String pieceSet;
    private boolean timedGame;
    private int timePerPlayer; // en minutos
    
    public GameConfig() {
        this.gameMode = GameMode.VS_AI_WHITE;
        this.playerStartsAsBlack = false;
        this.boardTheme = "Clásico";
        this.pieceSet = "default";
        this.timedGame = false;
        this.timePerPlayer = 10;
    }
    
    // Getters y setters
    public GameMode getGameMode() { return gameMode; }
    public void setGameMode(GameMode gameMode) { this.gameMode = gameMode; }
    
    public boolean isPlayerStartsAsBlack() { return playerStartsAsBlack; }
    public void setPlayerStartsAsBlack(boolean playerStartsAsBlack) { 
        this.playerStartsAsBlack = playerStartsAsBlack; 
    }
    
    public String getBoardTheme() { return boardTheme; }
    public void setBoardTheme(String boardTheme) { this.boardTheme = boardTheme; }
    
    public String getPieceSet() { return pieceSet; }
    public void setPieceSet(String pieceSet) { this.pieceSet = pieceSet; }
    
    public boolean isTimedGame() { return timedGame; }
    public void setTimedGame(boolean timedGame) { this.timedGame = timedGame; }
    
    public int getTimePerPlayer() { return timePerPlayer; }
    public void setTimePerPlayer(int timePerPlayer) { this.timePerPlayer = timePerPlayer; }
}