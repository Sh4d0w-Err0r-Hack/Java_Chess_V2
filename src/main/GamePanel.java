package main;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ObjectOutputStream;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import piece.*;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();
    MusicPlayer musicPlayer = new MusicPlayer();
    private Rectangle nextSongButton, prevSongButton, playlistButton;
    
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
    Piece activeP, checkingP;
    public static Piece castlingP;
    
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;
    boolean promotion;
    boolean gameOver;
    boolean stalemate;
    
    private boolean gameStarted = false;
    private boolean showMenu = true;
    private boolean isMaximized = false;
    private JFrame parentFrame;
    
    private AIPlayer aiPlayer;
    private boolean aiThinking = false;
    private long lastAIMoveTime = 0;
    private static final long AI_MOVE_DELAY = 1000;
    
    
    private Rectangle createRoomButton, joinRoomButton, codeInputField;
    private String inputCode = "";
    
    
    private OnlineManager onlineManager;
    private boolean onlineMode;
    private boolean isOnlineHost;
    private boolean waitingForPlayer;
    
    private Rectangle startButton, themeButton, maximizeButton, musicButton, backButton;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        
        aiPlayer = new AIPlayer(BLACK);
        onlineManager = new OnlineManager(); // Nuevo manager
        onlineMode = false;
        
        setPieces();
        copyPieces(pieces, simPieces);
        
        musicPlayer.play();
        initializeButtons();
    }

    private void initializeButtons() {
        // Botones principales - VERIFICAR POSICIONES
        startButton = new Rectangle(450, 250, 200, 50);        // Jugar vs IA
        createRoomButton = new Rectangle(450, 320, 200, 50);   // Crear Sala Online  
        joinRoomButton = new Rectangle(450, 390, 200, 50);     // Unirse a Sala
        
        // Botones de configuración
        themeButton = new Rectangle(450, 460, 200, 50);
        maximizeButton = new Rectangle(450, 530, 200, 50);
        musicButton = new Rectangle(450, 600, 200, 50);
        
        backButton = new Rectangle(850, 700, 200, 50);
        
        nextSongButton = new Rectangle(450, 660, 95, 30);
        prevSongButton = new Rectangle(555, 660, 95, 30);
        playlistButton = new Rectangle(450, 700, 200, 30);
        
    }
    
    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }
    
    
    
    
    private void update() {
        // Verificación temprana: si faltan reyes, terminar el juego
        if (!bothKingsPresent() && !gameOver && !stalemate) {
            checkGameState();
            return;
        }
        
        if (showMenu) {
            checkMenuClicks();
        } else if (gameStarted) {
            if (onlineMode) {
                checkOnlineGame();
            } else {
                checkLocalGame();
            }
        }
    }
    
    private void checkLocalGame() {
        if (backButton.contains(mouse.x, mouse.y) && mouse.pressed) {
            showMenu = true;
            gameStarted = false;
            mouse.pressed = false;
            return;
        }
        
        if (promotion) {
            promoting();
        } else if (!gameOver && !stalemate) {
            handleLocalGameplay();
        }
    }

    private void handleLocalGameplay() {
        if (mouse.pressed) {
            if (activeP == null) {
                for (Piece piece : simPieces) {
                    if (piece.color == currentColor && 
                        piece.col == mouse.x / Board.SQUARE_SIZE && 
                        piece.row == mouse.y / Board.SQUARE_SIZE) {
                        activeP = piece;
                        break;
                    }
                }
            } else {
                simulate();
            }
        }
        
        if (mouse.pressed == false && activeP != null) {
            if (isValidMove(activeP, activeP.col, activeP.row)) {
                executeMove();
            } else {
                copyPieces(pieces, simPieces);
                activeP.resetPosition();
                activeP = null;
            }
        }
        
        if (currentColor == BLACK && !aiThinking && !gameOver && !stalemate && !promotion && 
            activeP == null && System.currentTimeMillis() - lastAIMoveTime > AI_MOVE_DELAY) {
            if (bothKingsPresent()) {
                startAITurn();
            }
        }
    }
   
    

    private void checkOnlineGame() {
        // Procesar mensajes primero
        processOnlineMessages();
        
        if (backButton.contains(mouse.x, mouse.y) && mouse.pressed) {
            returnToMenu();
            mouse.pressed = false;
            return;
        }
        
        if (waitingForPlayer) {
            return;
        }
        
        if (onlineManager == null || !onlineManager.isConnected()) {
            return;
        }
        
        // Debug del estado cada 5 segundos
        if (System.currentTimeMillis() % 5000 < 50) {
            debugGameState();
        }
        
        if (promotion) {
            promoting();
        } else if (!gameOver && !stalemate) {
            handleOnlineTurn();
        }
    }
    
    private void processOnlineMessages() {
        if (onlineManager == null) return;
        
        try {
            String data = onlineManager.receiveData();
            if (data != null && !data.isEmpty()) {
                
                if (data.startsWith("MOVE:")) {
                    processReceivedMove(data);
                } else if (data.startsWith("PROMO:")) {
                    processReceivedPromotion(data);
                } else {
                    
                }
            }
        } catch (Exception e) {
            }
    }
    
    
    private void processReceivedPromotion(String data) {
        try {
            // Formato: "PROMO:col,row,pieceType"
            String[] parts = data.substring(6).split(",");
            int col = Integer.parseInt(parts[0]);
            int row = Integer.parseInt(parts[1]);
            String pieceType = parts[2];
            
           
            // Encontrar el peón que se promociona
            Piece pawn = findPieceAt(col, row);
            if (pawn != null && pawn.type == Type.PAWN) {
                // Crear la nueva pieza
                Piece newPiece = createPieceFromType(pieceType, pawn.color, col, row);
                
                // Reemplazar el peón
                simPieces.remove(pawn);
                pieces.remove(pawn);
                simPieces.add(newPiece);
                pieces.add(newPiece);
                
                
                // Cambiar turno después de la promoción
                currentColor = (currentColor == WHITE) ? BLACK : WHITE;
            }
            
        } catch (Exception e) {
            }
    }

    // Método auxiliar para crear piezas desde string
    private Piece createPieceFromType(String pieceType, int color, int col, int row) {
        switch (pieceType) {
            case "ROOK": return new Rook(color, col, row);
            case "KNIGHT": return new Knight(color, col, row);
            case "BISHOP": return new Bishop(color, col, row);
            case "QUEEN": 
            default: return new Queen(color, col, row);
        }
    }

    private void processReceivedMove(String data) {
        try {
            String[] parts = data.substring(5).split(",");
            int fromCol = Integer.parseInt(parts[0]);
            int fromRow = Integer.parseInt(parts[1]);
            int toCol = Integer.parseInt(parts[2]);
            int toRow = Integer.parseInt(parts[3]);
            int senderTurn = Integer.parseInt(parts[4]); // Turno del que envió
            
            
            // SINCRONIZAR estado actual
            copyPieces(pieces, simPieces);
            
            // Buscar la pieza del OPONENTE que se mueve
            Piece movingPiece = null;
            for (Piece piece : simPieces) {
                if (piece.col == fromCol && piece.row == fromRow) {
                    // Verificar que sea del color del OPONENTE
                    boolean isOpponentPiece = (isOnlineHost && piece.color == BLACK) || (!isOnlineHost && piece.color == WHITE);
                    if (isOpponentPiece) {
                        movingPiece = piece;
                         break;
                    }
                }
            }
            
            if (movingPiece != null) {
                // CAPTURAR pieza en la posición destino (si existe)
                Piece capturedPiece = null;
                for (Piece piece : simPieces) {
                    if (piece.col == toCol && piece.row == toRow && piece != movingPiece) {
                        capturedPiece = piece;
                         break;
                    }
                }
                
                if (capturedPiece != null) {
                    simPieces.remove(capturedPiece);
                    pieces.remove(capturedPiece);
                }
                
                // MOVER la pieza del oponente
                movingPiece.col = toCol;
                movingPiece.row = toRow;
                movingPiece.updatePosition();
                
                
                // Verificar promoción automática
                if (movingPiece.type == Type.PAWN && 
                    ((movingPiece.color == WHITE && toRow == 0) || 
                     (movingPiece.color == BLACK && toRow == 7))) {
                    performPromotion(toCol, toRow, Type.QUEEN);
                }
                
                // SINCRONIZAR después del movimiento
                copyPieces(simPieces, pieces);
                
                // CAMBIAR TURNO basado en el turno del remitente
                // Si el remitente movió con BLANCAS, ahora es turno de NEGRAS
                // Si el remitente movió con NEGRAS, ahora es turno de BLANCAS
                currentColor = (senderTurn == WHITE) ? BLACK : WHITE;
                
                // Verificar estado del juego
                checkGameState();
                
            } else {
                }
            
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }

    // AÑADE este método para forzar el cambio de turno si hay problemas:
    private void forceTurnChange() {
        currentColor = (currentColor == WHITE) ? BLACK : WHITE;
        }

    private Piece findPieceAt(int col, int row) {
        for (Piece piece : simPieces) {
            if (piece.col == col && piece.row == row) {
                return piece;
            }
        }
        return null;
    }
    
    
    private void resetBoardForOnline() {
        
        pieces.clear();
        simPieces.clear();
        
        // White Team - Fila 7 (abajo)
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Queen(WHITE, 3, 7));  // Reina en d1 (3,7)
        pieces.add(new King(WHITE, 4, 7));   // Rey en e1 (4,7)
        
        // Black Team - Fila 0 (arriba)  
        pieces.add(new Pawn(BLACK, 0, 1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Queen(BLACK, 3, 0));  // Reina en d8 (3,0)
        pieces.add(new King(BLACK, 4, 0));   // Rey en e8 (4,0)
        
        copyPieces(pieces, simPieces);
        
        // VERIFICAR que los reyes estén en posiciones correctas
        boolean whiteKingFound = false;
        boolean blackKingFound = false;
        
        for (Piece piece : pieces) {
            if (piece.type == Type.KING) {
                if (piece.color == WHITE) {
                    whiteKingFound = true;
                    } else {
                    blackKingFound = true;
                    }
            }
        }
        
        if (!whiteKingFound || !blackKingFound) {
        }
        
        }
    

    private void returnToMenu() {
        showMenu = true;
        gameStarted = false;
        onlineMode = false;
        if (onlineManager != null) {
            onlineManager.disconnect();
        }
        mouse.pressed = false;
    }

    private void handleOnlineTurn() {
        boolean isMyTurn = (isOnlineHost && currentColor == WHITE) || (!isOnlineHost && currentColor == BLACK);
        
        
        if (!isMyTurn) {
            return;
        }
        
        if (onlineManager == null || !onlineManager.isConnected()) {
            return;
        }
        
        
        if (promotion) {
            promoting();
            return;
        }
        
        if (mouse.pressed) {
            if (activeP == null) {
                // Seleccionar pieza
                int mouseCol = mouse.x / Board.SQUARE_SIZE;
                int mouseRow = mouse.y / Board.SQUARE_SIZE;
                
                for (Piece piece : simPieces) {
                    if (piece.color == currentColor && 
                        piece.col == mouseCol && 
                        piece.row == mouseRow) {
                        activeP = piece;
                         break;
                    }
                }
            } else {
                simulate();
            }
        }
        
        if (mouse.pressed == false && activeP != null) {
            if (isValidMove(activeP, activeP.col, activeP.row)) {
                
                int fromCol = activeP.preCol;
                int fromRow = activeP.preRow;
                int toCol = activeP.col;
                int toRow = activeP.row;
                
                // Ejecutar movimiento LOCALMENTE
                executeMove();
                
                // ENVIAR movimiento al oponente (INCLUYENDO el turno actual)
                String moveData = "MOVE:" + fromCol + "," + fromRow + "," + toCol + "," + toRow + "," + currentColor;
                System.out.println("📤 ENVIANDO movimiento: " + moveData);
                onlineManager.sendData(moveData);
                
                activeP = null;
                
            } else {
                copyPieces(pieces, simPieces);
                activeP.resetPosition();
                activeP = null;
            }
            
            mouse.pressed = false;
        }
    }

    // MÉTODO AUXILIAR: Forzar promoción a reina (por si falla la selección)
    private void forcePromotionToQueen(int col, int row) {
        // Encontrar el peón
        Piece pawnToPromote = null;
        for (Piece piece : simPieces) {
            if (piece.col == col && piece.row == row && piece.type == Type.PAWN) {
                pawnToPromote = piece;
                break;
            }
        }
        
        if (pawnToPromote != null) {
            Piece newQueen = new Queen(pawnToPromote.color, col, row);
            simPieces.remove(pawnToPromote);
            simPieces.add(newQueen);
            copyPieces(simPieces, pieces);
        }
    }

    private void processOnlineMove(String moveData) {
        try {
            String[] parts = moveData.split(",");
            int fromCol = Integer.parseInt(parts[0]);
            int fromRow = Integer.parseInt(parts[1]);
            int toCol = Integer.parseInt(parts[2]);
            int toRow = Integer.parseInt(parts[3]);
            int receivedTurn = Integer.parseInt(parts[4]);
            
            
            
            // Sincronizar primero
            copyPieces(pieces, simPieces);
            
            // Encontrar la pieza que se mueve (debe ser del oponente)
            Piece movingPiece = null;
            for (Piece piece : simPieces) {
                if (piece.col == fromCol && piece.row == fromRow) {
                    // Verificar que sea una pieza del oponente
                    boolean isOpponentPiece = (isOnlineHost && piece.color == BLACK) || (!isOnlineHost && piece.color == WHITE);
                    if (isOpponentPiece) {
                        movingPiece = piece;
                        break;
                    }
                }
            }
            
            if (movingPiece != null) {
                // Remover pieza en la posición destino (captura)
                Iterator<Piece> iterator = simPieces.iterator();
                while (iterator.hasNext()) {
                    Piece piece = iterator.next();
                    if (piece.col == toCol && piece.row == toRow && piece != movingPiece) {
                        iterator.remove();
                        break;
                    }
                }
                
                // Mover la pieza del oponente
                movingPiece.col = toCol;
                movingPiece.row = toRow;
                movingPiece.updatePosition();
                
                // Verificar promoción automática
                if (movingPiece.type == Type.PAWN && 
                    ((movingPiece.color == WHITE && toRow == 0) || 
                     (movingPiece.color == BLACK && toRow == 7))) {
                    performPromotion(toCol, toRow, Type.QUEEN);
                } else {
                    // Sincronizar con pieces
                    copyPieces(simPieces, pieces);
                }
                
                // Sincronizar el turno - IMPORTANTE
                currentColor = receivedTurn;
                
                checkGameState();
                
            } else {
                
            }
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }
    
    private void processOnlinePromotion(String promotionData) {
        try {
            String[] parts = promotionData.split(",");
            int col = Integer.parseInt(parts[1]);
            int row = Integer.parseInt(parts[2]);
            String pieceType = parts[3];
            int promotionColor = Integer.parseInt(parts[4]);
            
            
            // Realizar la promoción
            performPromotion(col, row, Type.valueOf(pieceType));
            
            // Sincronizar el turno
            currentColor = (promotionColor == WHITE) ? BLACK : WHITE;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleGameplay() {
    	if(gameOver||stalemate||!bothKingsPresent()) {
    		return;
    	}
    	
        if (mouse.pressed) {
            if (activeP == null) {
                for (Piece piece : simPieces) {
                    if (piece.color == currentColor && 
                        piece.col == mouse.x / Board.SQUARE_SIZE && 
                        piece.row == mouse.y / Board.SQUARE_SIZE) {
                        activeP = piece;
                        break;
                    }
                }
            } else {
                simulate();
            }
        }
        
        if (mouse.pressed == false && activeP != null) {
            if (isValidMove(activeP, activeP.col, activeP.row)) {
                executeMove();
            } else {
                copyPieces(pieces, simPieces);
                activeP.resetPosition();
                activeP = null;
            }
        }
        
        if (currentColor == BLACK && !aiThinking && !gameOver && !stalemate && !promotion && 
            activeP == null && System.currentTimeMillis() - lastAIMoveTime > AI_MOVE_DELAY) {
            if(bothKingsPresent()) {
            	startAITurn();
            }
        }
    }
    
    private boolean bothKingsPresent() {
        boolean whiteKingExists = false;
        boolean blackKingExists = false;
        
        for (Piece piece : pieces) {
            if (piece.type == Type.KING) {
                if (piece.color == WHITE) {
                    whiteKingExists = true;
                } else {
                    blackKingExists = true;
                }
            }
        }
        
        return whiteKingExists && blackKingExists;
    }
    
    private void checkMenuClicks() {
        if (mouse.pressed) {
            
            if (startButton.contains(mouse.x, mouse.y)) {
                startLocalGame();
            } else if (createRoomButton.contains(mouse.x, mouse.y)) {
                createOnlineRoom();
            } else if (joinRoomButton.contains(mouse.x, mouse.y)) {
                showJoinRoomDialog();
            } else if (themeButton.contains(mouse.x, mouse.y)) {
                board.showThemeSelector();
            } else if (maximizeButton.contains(mouse.x, mouse.y)) {
                toggleMaximize();
            } else if (musicButton.contains(mouse.x, mouse.y)) {
                musicPlayer.toggleMute();
            } else if (nextSongButton.contains(mouse.x, mouse.y)) {
                musicPlayer.playNextSong();
            } else if (prevSongButton.contains(mouse.x, mouse.y)) {
                musicPlayer.playPreviousSong();
            } else if (playlistButton.contains(mouse.x, mouse.y)) {
                showPlaylistSelector();
            } else {
            }
            mouse.pressed = false;
        }
    }
    
    

    private void startLocalGame() {
        showMenu = false;
        gameStarted = true;
        onlineMode = false;
        resetGame();
    }

    private void createOnlineRoom() {
        // Mostrar mensaje inmediato con el código
        String code = onlineManager.getGameCode();
        JOptionPane.showMessageDialog(this, 
            "Creando sala...\nCódigo: " + code + 
            "\nEste código expira en 30 segundos", 
            "Creando Sala", 
            JOptionPane.INFORMATION_MESSAGE);
        
        new Thread(() -> {
            boolean success = onlineManager.createGame();
            
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    setupOnlineGame(true, WHITE);
                    JOptionPane.showMessageDialog(this, 
                        "✅ ¡Jugador conectado!\nEres las BLANCAS.\n¡La partida puede comenzar!", 
                        "Conexión Exitosa", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "❌ No se conectó ningún jugador en 30 segundos\no todos los puertos están ocupados.", 
                        "Timeout", 
                        JOptionPane.WARNING_MESSAGE);
                    // Volver al menú
                    showMenu = true;
                    gameStarted = false;
                    onlineMode = false;
                }
            });
        }).start();
    }

    private void showJoinRoomDialog() {
        
        String code = JOptionPane.showInputDialog(
            this, 
            "Ingresa el código de 4 dígitos de la sala:", 
            "Unirse a Sala Online", 
            JOptionPane.QUESTION_MESSAGE
        );
        
        System.out.println("Código ingresado: " + code);
        
        if (code != null && !code.trim().isEmpty()) {
            String trimmedCode = code.trim();
            if (trimmedCode.length() == 4 && trimmedCode.matches("\\d{4}")) {
                System.out.println("✅ Código válido: " + trimmedCode);
                joinOnlineRoom(trimmedCode);
            } else {
                System.out.println("❌ Código inválido: " + trimmedCode);
                JOptionPane.showMessageDialog(
                    this, 
                    "El código debe ser de 4 dígitos numéricos.\nEjemplo: 1234", 
                    "Código Inválido", 
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } else {
        }
    }

    private void joinOnlineRoom(String code) {
        if (code == null || code.trim().isEmpty()) {
            return;
        }
        
        JOptionPane.showMessageDialog(this, 
            "Conectando a la sala " + code + "...", 
            "Conectando", 
            JOptionPane.INFORMATION_MESSAGE);
        
        new Thread(() -> {
            boolean success = onlineManager.joinGame(code.trim(), "localhost");
            
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    setupOnlineGame(false, BLACK);
                    JOptionPane.showMessageDialog(this, 
                        "✅ ¡Conectado a la sala!\nEres las NEGRAS.", 
                        "Conexión Exitosa", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "❌ No se pudo conectar a la sala " + code + 
                        "\nVerifica:\n- El código es correcto\n- El host está esperando\n- Tu conexión a internet", 
                        "Error de Conexión", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }
    
    private void setupOnlineGame(boolean isHost, int startingColor) {
        isOnlineHost = isHost;
        onlineMode = true;
        waitingForPlayer = false;
        showMenu = false;
        gameStarted = true;
        
        // Reset del juego
        resetBoardForOnline();
        
        // CORRECCIÓN: El host siempre juega con BLANCAS y empieza primero
        // El invitado siempre juega con NEGRAS y espera
        if (isHost) {
            currentColor = WHITE;
        } else {
            currentColor = BLACK; 
            
        }
        
        // DIAGNÓSTICO
        diagnoseGameStart();
        
        // RESETAR estados de juego
        gameOver = false;
        stalemate = false;
        promotion = false;
        activeP = null;
        checkingP = null;
        castlingP = null;
        
        }

	private void showPlaylistSelector() {
        if (!musicPlayer.hasSongs()) {
            JOptionPane.showMessageDialog(this, "No hay canciones disponibles", "Playlist", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JFrame playlistFrame = new JFrame("Seleccionar Canción");
        playlistFrame.setSize(400, 300);
        playlistFrame.setLocationRelativeTo(this);
        playlistFrame.setLayout(new BorderLayout());
        
        ArrayList<String> songNames = musicPlayer.getPlaylistNames();
        JList<String> songList = new JList<>(songNames.toArray(new String[0]));
        songList.setSelectedIndex(musicPlayer.getCurrentSongIndex());
        
        JScrollPane scrollPane = new JScrollPane(songList);
        playlistFrame.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton selectButton = new JButton("Seleccionar");
        JButton cancelButton = new JButton("Cancelar");
        
        selectButton.addActionListener(e -> {
            int selectedIndex = songList.getSelectedIndex();
            if (selectedIndex >= 0) {
                musicPlayer.setSong(selectedIndex);
                playlistFrame.dispose();
            }
        });
        
        cancelButton.addActionListener(e -> playlistFrame.dispose());
        
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        playlistFrame.add(buttonPanel, BorderLayout.SOUTH);
        
        playlistFrame.setVisible(true);
    }
    
    private void startGame() {
        showMenu = false;
        gameStarted = true;
        resetGame();
    }
    
    private void toggleMaximize() {
        if (parentFrame != null) {
            if (isMaximized) {
                parentFrame.setExtendedState(JFrame.NORMAL);
                parentFrame.setSize(WIDTH, HEIGHT);
                parentFrame.setLocationRelativeTo(null);
            } else {
                parentFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            isMaximized = !isMaximized;
        }
    }
    
    private void resetGame() {
        pieces.clear();
        simPieces.clear();
        setPieces();
        copyPieces(pieces, simPieces);
        
        currentColor = WHITE;
        gameOver = false;
        stalemate = false;
        promotion = false;
        activeP = null;
        checkingP = null;
        castlingP = null;
        
        }
    
    public void setPieces() {
        // White Team
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        pieces.add(new King(WHITE, 4, 7));
        
        // Black Team
        pieces.add(new Pawn(BLACK, 0, 1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Queen(BLACK, 4, 0));
        pieces.add(new King(BLACK, 3, 0));
    }
    
    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();
        for (Piece piece : source) {
            target.add(piece);
        }
    }
    
    private void simulate() {
        
        copyPieces(pieces, simPieces);
        
        if (castlingP != null) {
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }
        
        // Actualizar posición de la pieza activa según el mouse
        activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);
        
        
        // Manejar capturas
        if (activeP.hittingP != null) {
            Piece pieceToRemove = null;
            for (Piece piece : simPieces) {
                if (piece == activeP.hittingP) {
                    pieceToRemove = piece;
                    break;
                }
            }
            if (pieceToRemove != null) {
                simPieces.remove(pieceToRemove);
            }
            activeP.hittingP = null;
        }
        
        checkCastling();
    }
    
    private boolean isValidMove(Piece piece, int targetCol, int targetRow) {
        
        
        if (!piece.canMove(targetCol, targetRow)) {
            return false;
        }
        
        if (piece.hittingP != null && piece.hittingP.color == piece.color) {
            return false;
        }
        
        boolean legal = isMoveLegal(piece, targetCol, targetRow);
        
        return legal;
    }
    
    private void executeMove() {
        if (activeP != null) {
            // Capturar pieza si existe
            Piece capturedPiece = null;
            for (Piece piece : pieces) {
                if (piece != activeP && piece.col == activeP.col && piece.row == activeP.row) {
                    capturedPiece = piece;
                    break;
                }
            }
            
            if (capturedPiece != null) {
                pieces.remove(capturedPiece);
                simPieces.remove(capturedPiece);
            }
            
            activeP.updatePosition();
            
            // Sincronizar
            copyPieces(pieces, simPieces);
            
            if (canPromote()) {
                promotion = true;
                }
            
            checkGameState();
        }
        
        if (castlingP != null) {
            castlingP.updatePosition();
            castlingP = null;
        }
    }

    // AÑADE este método para debuggear el estado del juego:
    private void debugGameState() {
        System.out.println("=== DEBUG ESTADO DEL JUEGO ===");
        System.out.println("Online Mode: " + onlineMode);
        System.out.println("Is Host: " + isOnlineHost);
        System.out.println("Current Color: " + (currentColor == WHITE ? "WHITE" : "BLACK"));
        System.out.println("Connected: " + (onlineManager != null && onlineManager.isConnected()));
        System.out.println("My Turn: " + ((isOnlineHost && currentColor == WHITE) || (!isOnlineHost && currentColor == BLACK)));
        System.out.println("Pieces count: " + pieces.size());
        System.out.println("=============================");
    }
    
    
 // Método para verificar que ambos jugadores tienen el mismo estado
    private void verifyBoardSync() {
        
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                int count = 0;
                for (Piece piece : simPieces) {
                    if (piece.col == col && piece.row == row) {
                        count++;
                        }
                }
                if (count > 1) {
                    
                }
            }
        }
    }
    
    private void checkGameState() {
        // PRIMERO verificar que ambos reyes existan
        if (!bothKingsPresent()) {
            gameOver = true;
            determineWinnerByMissingKing();
            return;
        }
        
        int opponentColor = (currentColor == WHITE ? BLACK : WHITE);
        
        
        // Verificar jaque mate
        if (isCheckMate(opponentColor)) {
            gameOver = true;
            String winner = (currentColor == WHITE) ? "Blancas" : "Negras";
        } 
        // Verificar tablas por ahogado
        else if (isStalemate(opponentColor)) {
            stalemate = true;
        } 
        // Verificar jaque
        else if (isKingInCheck(opponentColor)) {
            } else {
            
        }
    }

    // NUEVO MÉTODO: Determinar ganador cuando falta un rey
    private void determineWinnerByMissingKing() {
        boolean whiteKingExists = false;
        boolean blackKingExists = false;
        
        for (Piece piece : pieces) {
            if (piece.type == Type.KING) {
                if (piece.color == WHITE) {
                    whiteKingExists = true;
                } else {
                    blackKingExists = true;
                }
            }
        }
        
        if (!whiteKingExists && !blackKingExists) {
        } else if (!whiteKingExists) {
        } else if (!blackKingExists) {
        }
    }
    
    private boolean isCheckMate(int kingColor) {
        
        // Si el rey no existe, no es jaque mate (ya se manejó antes)
        if (!kingExists(kingColor)) {
             return false;
        }
        
        // Si el rey no está en jaque, no es jaque mate
        if (!isKingInCheck(kingColor)) {
             return false;
        }
        
        // Verificar si hay movimientos legales
        boolean hasMoves = hasLegalMoves(kingColor);
        
        return !hasMoves;
    }

    private boolean isKingCaptured(int kingColor) {
        return !kingExists(kingColor);
    }

    private boolean kingExists(int kingColor) {
        for (Piece piece : pieces) {
            if (piece.type == Type.KING && piece.color == kingColor) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isStalemate(int kingColor) {
        if (isKingInCheck(kingColor)) {
            return false;
        }
        return !hasLegalMoves(kingColor);
    }
    
    private boolean hasLegalMoves(int color) {
        // Si no hay rey, no hay movimientos legales
        if (!kingExists(color)) {
            return false;
        }
        
        for (Piece piece : simPieces) {
            if (piece.color == color) {
                for (int col = 0; col < 8; col++) {
                    for (int row = 0; row < 8; row++) {
                        if (piece.canMove(col, row) && isMoveLegal(piece, col, row)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    
    
    private void diagnoseGameStart() {
        
        // Verificar reyes
        Piece whiteKing = null;
        Piece blackKing = null;
        
        for (Piece piece : pieces) {
            if (piece.type == Type.KING) {
                if (piece.color == WHITE) {
                    whiteKing = piece;
                } else {
                    blackKing = piece;
                }
            }
        }
      
    }
    
    
    
    private void testMovement() {
        
        // Intentar mover el primer peón disponible
        for (Piece piece : simPieces) {
            if (piece.color == currentColor && piece.type == Type.PAWN) {
                
                // Intentar mover hacia adelante
                int targetCol = piece.col;
                int targetRow = piece.color == WHITE ? piece.row - 1 : piece.row + 1;
                
                
                if (piece.canMove(targetCol, targetRow)) {
                    activeP = piece;
                    activeP.col = targetCol;
                    activeP.row = targetRow;
                    
                    if (isValidMove(activeP, targetCol, targetRow)) {
                        executeMove();
                        return;
                    } else {
                        }
                } else {
                    }
                break;
            }
        }
    }
    
    
    private boolean isMoveLegal(Piece piece, int targetCol, int targetRow) {
        ArrayList<Piece> tempPieces = createDeepCopy(simPieces);
        
        Piece tempPiece = null;
        Piece tempHittingP = null;
        
        for (Piece p : tempPieces) {
            if (p.col == piece.col && p.row == piece.row && 
                p.color == piece.color && p.type == piece.type) {
                tempPiece = p;
            }
            if (p.col == targetCol && p.row == targetRow && p.color != piece.color) {
                tempHittingP = p;
            }
        }
        
        if (tempPiece == null) return false;
        
        if (tempHittingP != null) {
            tempPieces.remove(tempHittingP);
        }
        tempPiece.col = targetCol;
        tempPiece.row = targetRow;
        
        // Si después del movimiento no hay rey, el movimiento es ilegal (no debería pasar)
        return isKingInCheck(tempPieces, piece.color) == false;
    }
    
    private ArrayList<Piece> createDeepCopy(ArrayList<Piece> original) {
        ArrayList<Piece> copy = new ArrayList<>();
        for (Piece p : original) {
            Piece newPiece = new Piece(p.color, p.col, p.row);
            newPiece.type = p.type;
            newPiece.preCol = p.preCol;
            newPiece.preRow = p.preRow;
            newPiece.moved = p.moved;
            copy.add(newPiece);
        }
        return copy;
    }

    private boolean isKingInCheck(int kingColor) {
        // Encontrar el rey
        Piece king = null;
        for (Piece piece : simPieces) {
            if (piece.type == Type.KING && piece.color == kingColor) {
                king = piece;
                break;
            }
        }
        
        if (king == null) {
            return true; // Si no hay rey, está en "jaque"
        }
        
        // Verificar si alguna pieza enemiga puede capturar al rey
        for (Piece piece : simPieces) {
            if (piece.color != kingColor && piece.canMove(king.col, king.row)) {
                // Para piezas de largo alcance, verificar que el camino no esté bloqueado
                if (piece.type == Type.ROOK || piece.type == Type.BISHOP || piece.type == Type.QUEEN) {
                    if (!isPathBlocked(simPieces, piece, king.col, king.row)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        
        return false;
    }

    private boolean isKingInCheck(ArrayList<Piece> pieceList, int kingColor) {
        // Primero verificar si el rey existe
        Piece king = getKing(kingColor, pieceList);
        if (king == null) {
            return true; // Si no hay rey, está en "jaque" (rey capturado)
        }
        
        for (Piece piece : pieceList) {
            if (piece.color != king.color && piece.canMove(king.col, king.row)) {
                if (piece.type == Type.ROOK || piece.type == Type.BISHOP || piece.type == Type.QUEEN) {
                    if (!isPathBlocked(pieceList, piece, king.col, king.row)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isPathBlocked(ArrayList<Piece> pieceList, Piece attacker, int targetCol, int targetRow) {
        int colDiff = targetCol - attacker.col;
        int rowDiff = targetRow - attacker.row;
        
        int colStep = Integer.signum(colDiff);
        int rowStep = Integer.signum(rowDiff);
        
        int steps = Math.max(Math.abs(colDiff), Math.abs(rowDiff));
        
        for (int i = 1; i < steps; i++) {
            int checkCol = attacker.col + colStep * i;
            int checkRow = attacker.row + rowStep * i;
            
            for (Piece p : pieceList) {
                if (p.col == checkCol && p.row == checkRow) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private Piece getKing(int kingColor, ArrayList<Piece> pieceList) {
        for (Piece piece : pieceList) {
            if (piece.type == Type.KING && piece.color == kingColor) {
                return piece;
            }
        }
        return null; // Rey no encontrado (capturado)
    }
    
    private void startAITurn() {
        if (!aiThinking) {
            aiThinking = true;
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                    
                    SwingUtilities.invokeLater(() -> {
                        makeAIMove();
                        aiThinking = false;
                        lastAIMoveTime = System.currentTimeMillis();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    private void makeAIMove() {
        
        long startTime = System.currentTimeMillis();
        
        try {
            AIMove aiMove = aiPlayer.findBestMove(simPieces, currentColor);
            
            if (aiMove != null) {
                executeAIMove(aiMove);
                changePlayer();
            } else {
                changePlayer();
            }
            
        } catch (Exception e) {
            changePlayer();
        }
        
        long endTime = System.currentTimeMillis();
    }
    
    private void executeAIMove(AIMove aiMove) {
        try {
            // Buscar la pieza real en simPieces
            Piece realPiece = null;
            for (Piece p : simPieces) {
                if (p.col == aiMove.piece.col && p.row == aiMove.piece.row && 
                    p.color == aiMove.piece.color && p.type == aiMove.piece.type) {
                    realPiece = p;
                    break;
                }
            }
            
            if (realPiece == null) {
                return;
            }
            
            activeP = realPiece;
            
            // CORREGIDO: Remover pieza capturada tanto de simPieces como de pieces
            Piece capturedPiece = null;
            
            // Buscar en simPieces
            for (Piece p : simPieces) {
                if (p != activeP && p.col == aiMove.targetCol && p.row == aiMove.targetRow) {
                    capturedPiece = p;
                    break;
                }
            }
            
            if (capturedPiece != null) {
                simPieces.remove(capturedPiece);
                // También remover de pieces
                Piece capturedInPieces = null;
                for (Piece p : pieces) {
                    if (p.col == capturedPiece.col && p.row == capturedPiece.row && 
                        p.color == capturedPiece.color && p.type == capturedPiece.type) {
                        capturedInPieces = p;
                        break;
                    }
                }
                if (capturedInPieces != null) {
                    pieces.remove(capturedInPieces);
                }
            }
            
            // Mover la pieza
            activeP.col = aiMove.targetCol;
            activeP.row = aiMove.targetRow;
            activeP.updatePosition();
            
        } catch (Exception e) {
            e.printStackTrace();
            // Restaurar estado sincronizando ambas listas
            copyPieces(pieces, simPieces);
        }
    }
    
    private void syncPiecesLists() {
        // Asegurar que simPieces refleje exactamente el estado de pieces
        copyPieces(pieces, simPieces);
    }
    
    private void checkCastling() {
        if (castlingP != null) {
            if (castlingP.col == 0) {
                castlingP.col += 3;
            } else if (castlingP.col == 7) {
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }
    
    private void changePlayer() {
        currentColor = (currentColor == WHITE) ? BLACK : WHITE;
        activeP = null;
        castlingP = null;
      }
    
    private boolean canPromote() {
        if (activeP != null && activeP.type == Type.PAWN) {
            if ((currentColor == WHITE && activeP.row == 0) || 
                (currentColor == BLACK && activeP.row == 7)) {
                promoPieces.clear();
                promoPieces.add(new Rook(currentColor, 9, 2));
                promoPieces.add(new Knight(currentColor, 9, 3));
                promoPieces.add(new Bishop(currentColor, 9, 4));
                promoPieces.add(new Queen(currentColor, 9, 5));
                return true;
            }
        }
        return false;
    }
    
    private void promoting() {
        if (mouse.pressed) {
            int panelX = 8 * Board.SQUARE_SIZE + 20;
            int panelY = 100;
            int optionSize = Board.SQUARE_SIZE - 20;
            
            if (mouse.x >= panelX && mouse.x <= panelX + optionSize + 150 && 
                mouse.y >= panelY && mouse.y <= panelY + (optionSize * 4 + 30)) {
                
                int mouseRow = (mouse.y - panelY) / (optionSize + 10);
                
                if (mouseRow >= 0 && mouseRow < 4) {
                    Type selectedType = null;
                    String typeString = "";
                    
                    switch (mouseRow) {
                        case 0: selectedType = Type.ROOK; typeString = "ROOK"; break;
                        case 1: selectedType = Type.KNIGHT; typeString = "KNIGHT"; break;
                        case 2: selectedType = Type.BISHOP; typeString = "BISHOP"; break;
                        case 3: selectedType = Type.QUEEN; typeString = "QUEEN"; break;
                    }
                    
                    if (selectedType != null) {
                        // Encontrar el peón que promociona
                        Piece pawnToPromote = null;
                        for (Piece piece : simPieces) {
                            if (piece.type == Type.PAWN && 
                                ((currentColor == WHITE && piece.row == 0) || 
                                 (currentColor == BLACK && piece.row == 7))) {
                                pawnToPromote = piece;
                                break;
                            }
                        }
                        
                        if (pawnToPromote != null) {
                            // Realizar promoción local
                            performPromotion(pawnToPromote.col, pawnToPromote.row, selectedType);
                            
                            // Enviar promoción al oponente si estamos en online
                            if (onlineMode && onlineManager.isConnected()) {
                                onlineManager.sendData("PROMO:" + pawnToPromote.col + "," + pawnToPromote.row + "," + typeString);
                            }
                        }
                    }
                }
            }
            mouse.pressed = false;
        }
    }

    private void performPromotion(int col, int row, Type pieceType) {
        // Encontrar el peón exacto
        Piece pawnToPromote = null;
        for (Piece piece : simPieces) {
            if (piece.col == col && piece.row == row && piece.type == Type.PAWN) {
                pawnToPromote = piece;
                break;
            }
        }
        
        if (pawnToPromote != null) {
            int color = pawnToPromote.color;
            
            // Crear la nueva pieza
            Piece newPiece;
            switch (pieceType) {
                case ROOK: newPiece = new Rook(color, col, row); break;
                case KNIGHT: newPiece = new Knight(color, col, row); break;
                case BISHOP: newPiece = new Bishop(color, col, row); break;
                case QUEEN:
                default: newPiece = new Queen(color, col, row); break;
            }
            
            // Reemplazar el peón
            simPieces.remove(pawnToPromote);
            pieces.remove(pawnToPromote);
            simPieces.add(newPiece);
            pieces.add(newPiece);
            
            // Limpiar estado
            promotion = false;
            promoPieces.clear();
            activeP = null;
            
            // Cambiar turno (solo en local, en online se maneja por mensajes)
            if (!onlineMode) {
                changePlayer();
            }
            
        }
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (showMenu) {
            drawMenu(g2);
        } else {
            board.draw(g2);
            
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            for (int col = 0; col < 8; col++) {
                for (int row = 0; row < 8; row++) {
                    g2.drawString(col + "," + row, 
                        col * Board.SQUARE_SIZE + 5, 
                        row * Board.SQUARE_SIZE + 15);
                }
            }
            
            for (Piece p : simPieces) {
                p.draw(g2);
            }
            
            if (activeP != null) {
                g2.setColor(new Color(0, 255, 0, 100));
                g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, 
                           Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                activeP.draw(g2);
            }
            
            drawGameInfo(g2);
            
            if (promotion) {
                drawPromotionOptions(g2);
            }
            
            // Mostrar información de conexión online
            if (onlineMode) {
                drawOnlineInfo(g2);
            }
        }
    }

    private void drawOnlineInfo(Graphics2D g2) {
        int panelX = 8 * Board.SQUARE_SIZE + 20;
        
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        
        if (waitingForPlayer) {
            g2.setColor(Color.YELLOW);
            g2.drawString("Esperando jugador...", panelX, 100);
            g2.drawString("Código: " + onlineManager.getGameCode(), panelX, 120);
        } else if (onlineManager.isConnected()) {
            g2.setColor(Color.GREEN);
            g2.drawString("Conectado", panelX, 100);
            g2.drawString("Modo: " + (isOnlineHost ? "Anfitrión (Blancas)" : "Invitado (Negras)"), panelX, 120);
        } else {
            g2.setColor(Color.RED);
            g2.drawString("Desconectado", panelX, 100);
        }
    }
    
    private void drawPromotionOptions(Graphics2D g2) {
        int panelX = 8 * Board.SQUARE_SIZE + 20;
        int panelY = 100;
        int optionSize = Board.SQUARE_SIZE - 20;
        
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRoundRect(panelX - 10, panelY - 10, optionSize + 30, optionSize * 4 + 50, 20, 20);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Elige pieza:", panelX + 5, panelY - 20);
        
        String[] pieceNames = {"Torre", "Caballo", "Alfil", "Reina"};
        
        for (int i = 0; i < 4; i++) {
            int y = panelY + i * (optionSize + 10);
            
            g2.setColor(new Color(60, 60, 80));
            g2.fillRoundRect(panelX, y, optionSize, optionSize, 10, 10);
            
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(panelX, y, optionSize, optionSize, 10, 10);
            
            BufferedImage pieceImage = getPromotionPieceImage(i, currentColor);
            if (pieceImage != null) {
                g2.drawImage(pieceImage, panelX + 5, y + 5, optionSize - 10, optionSize - 10, null);
            }
            
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(pieceNames[i], panelX + optionSize + 10, y + optionSize / 2);
        }
    }
    
    private BufferedImage getPromotionPieceImage(int index, int color) {
        try {
            String colorPrefix = (color == WHITE) ? "w-" : "b-";
            String[] pieceTypes = {"rook", "knight", "bishop", "queen"};
            
            return ImageIO.read(getClass().getResourceAsStream("/piece/" + colorPrefix + pieceTypes[index] + ".png"));
        } catch (Exception e) {
            return null;
        }
    }

    private void drawMenu(Graphics2D g2) {
        g2.setColor(new Color(30, 30, 50));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Título
        g2.setFont(new Font("Arial", Font.BOLD, 60));
        g2.setColor(Color.WHITE);
        g2.drawString("AJEDREZ MAESTRO", 350, 150);
        
        // DEBUG: Dibujar áreas de botones (temporal)
        g2.setColor(new Color(255, 0, 0, 50));
        g2.fillRect(startButton.x, startButton.y, startButton.width, startButton.height);
        g2.fillRect(createRoomButton.x, createRoomButton.y, createRoomButton.width, createRoomButton.height);
        g2.fillRect(joinRoomButton.x, joinRoomButton.y, joinRoomButton.width, joinRoomButton.height);
        
        // Botones principales
        drawButton(g2, startButton, "Jugar vs IA", new Color(70, 130, 180));
        drawButton(g2, createRoomButton, "Crear Sala Online", new Color(80, 180, 80));
        drawButton(g2, joinRoomButton, "Unirse a Sala", new Color(180, 80, 80));
        
        // Botones de configuración
        drawButton(g2, themeButton, "Elegir Tema", new Color(80, 140, 190));
        drawButton(g2, maximizeButton, isMaximized ? "Restaurar" : "Maximizar", new Color(90, 150, 200));
        
        String musicText = musicPlayer.isMuted() ? "🔇 Activar Música" : "🔊 Desactivar Música";
        drawButton(g2, musicButton, musicText, 
                  musicPlayer.isMuted() ? new Color(180, 70, 70) : new Color(70, 180, 70));
        
        if (musicPlayer.hasSongs()) {
            drawMusicControls(g2);
        }
        
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Selecciona una opción para comenzar", 420, 750);
    }
    
    private void drawMusicControls(Graphics2D g2) {
        drawSmallButton(g2, prevSongButton, "Anterior", new Color(100, 100, 150));
        drawSmallButton(g2, nextSongButton, "Siguiente", new Color(100, 100, 150));
        drawSmallButton(g2, playlistButton, "Seleccionar Canción", new Color(120, 120, 180));
        
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(Color.YELLOW);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.setColor(Color.LIGHT_GRAY);
    }
    
    private void drawSmallButton(Graphics2D g2, Rectangle rect, String text, Color color) {
        g2.setColor(color);
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);
        
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);
        
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = rect.x + (rect.width - textWidth) / 2;
        int y = rect.y + (rect.height - fm.getHeight()) / 2 + fm.getAscent();
        
        g2.drawString(text, x, y);
    }
    
    private void drawButton(Graphics2D g2, Rectangle rect, String text, Color color) {
        // Fondo del botón
        g2.setColor(color);
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
        
        // Borde
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
        
        // Texto centrado
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        int x = rect.x + (rect.width - textWidth) / 2;
        int y = rect.y + (rect.height - textHeight) / 2 + fm.getAscent();
        
        g2.drawString(text, x, y);
        
        // DEBUG: Dibujar coordenadas (temporal)
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(Color.YELLOW);
        g2.drawString(rect.x + "," + rect.y, rect.x + 5, rect.y + 12);
    }
    
    private void drawGameInfo(Graphics2D g2) {
        int panelX = 8 * Board.SQUARE_SIZE + 20;
        
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        
        if (onlineMode) {
            // Mostrar información de conexión
            g2.setColor(Color.CYAN);
            g2.drawString("MODO ONLINE", panelX, 30);
            
            g2.setFont(new Font("Arial", Font.PLAIN, 16));
            if (onlineManager.isConnected()) {
                g2.setColor(Color.GREEN);
                g2.drawString("Conectado - " + (isOnlineHost ? "Anfitrión" : "Invitado"), panelX, 55);
            } else {
                g2.setColor(Color.RED);
                g2.drawString("Desconectado", panelX, 55);
            }
            
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            if (waitingForPlayer) {
                g2.setColor(Color.YELLOW);
                g2.drawString("Esperando jugador...", panelX, 85);
            } else {
                // Verificar si es nuestro turno
                boolean isMyTurn = (isOnlineHost && currentColor == WHITE) || (!isOnlineHost && currentColor == BLACK);
                
                if (isMyTurn) {
                    g2.setColor(Color.GREEN);
                    g2.drawString("¡TU TURNO!", panelX, 85);
                } else {
                    g2.setColor(Color.ORANGE);
                    g2.drawString("Turno del oponente", panelX, 85);
                }
                
                g2.setColor(Color.WHITE);
                g2.drawString("Turno: " + (currentColor == WHITE ? "Blancas ♔" : "Negras ♚"), panelX, 115);
            }
        } else {
            // Modo local vs IA
            g2.setColor(Color.WHITE);
            if (currentColor == WHITE) {
                g2.drawString("Turno: Blancas ♔", panelX, 50);
            } else {
                g2.drawString("Turno: Negras (IA) ♚", panelX, 50);
            }
        }
        
        drawButton(g2, backButton, "Volver al Menú", new Color(180, 70, 70));
        
        if (gameOver) {
            String message = getGameOverMessage();
            drawCenteredMessage(g2, message, Color.RED, 36);
        } else if (stalemate) {
            drawCenteredMessage(g2, "¡Tablas por Ahogado!", Color.BLUE, 40);
        }
    }

    // NUEVO MÉTODO: Obtener mensaje de fin de juego apropiado
    private String getGameOverMessage() {
        boolean whiteKingExists = kingExists(WHITE);
        boolean blackKingExists = kingExists(BLACK);
        
        if (!whiteKingExists && !blackKingExists) {
            return "¡Tablas! Ambos reyes capturados";
        } else if (!whiteKingExists) {
            return "¡Rey Blanco Capturado! Ganaron las Negras ♚";
        } else if (!blackKingExists) {
            return "¡Rey Negro Capturado! Ganaron las Blancas ♔";
        } else {
            // Fue jaque mate
            String winner = (currentColor == WHITE) ? "Blancas ♔" : "Negras ♚";
            return "¡Jaque Mate! Ganaron las " + winner;
        }
    }

    private void drawCenteredMessage(Graphics2D g2, String message, Color color, int fontSize) {
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(message);
        int x = (8 * Board.SQUARE_SIZE - textWidth) / 2;
        int y = 4 * Board.SQUARE_SIZE;
        
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRoundRect(x - 20, y - 40, textWidth + 40, 80, 20, 20);
        
        g2.setColor(color);
        g2.drawString(message, x, y);
        
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x - 20, y - 40, textWidth + 40, 80, 20, 20);
    }
    
    public void launchGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        
        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }
}