package main;

import java.io.*;
import java.net.*;
import java.util.Random;

public class OnlineGame {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String gameCode;
    private boolean isHost;
    private boolean connected;
    
    public OnlineGame() {
        this.gameCode = generateGameCode();
        this.connected = false;
    }
    
    private String generateGameCode() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }
    
    public String getGameCode() {
        return gameCode;
    }
    
    public boolean isHost() {
        return isHost;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public boolean createGame() {
        try {
            serverSocket = new ServerSocket(PORT);
            isHost = true;
            System.out.println("Sala creada con código: " + gameCode);
            
            new Thread(() -> {
                try {
                    System.out.println("Esperando jugador...");
                    clientSocket = serverSocket.accept();
                    output = new ObjectOutputStream(clientSocket.getOutputStream());
                    input = new ObjectInputStream(clientSocket.getInputStream());
                    connected = true;
                    System.out.println("Jugador conectado!");
                } catch (IOException e) {
                    System.out.println("Error en la conexión: " + e.getMessage());
                }
            }).start();
            
            return true;
        } catch (IOException e) {
            System.out.println("Error creando sala: " + e.getMessage());
            return false;
        }
    }
    
    public boolean joinGame(String code, String host) {
        try {
            clientSocket = new Socket(host, PORT);
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            input = new ObjectInputStream(clientSocket.getInputStream());
            isHost = false;
            connected = true;
            this.gameCode = code;
            System.out.println("Conectado a la sala: " + code);
            return true;
        } catch (IOException e) {
            System.out.println("Error uniéndose a sala: " + e.getMessage());
            return false;
        }
    }
    
    public void sendMove(int fromCol, int fromRow, int toCol, int toRow, int currentTurn) {
        if (connected && output != null) {
            try {
                String move = fromCol + "," + fromRow + "," + toCol + "," + toRow + "," + currentTurn;
                output.writeObject(move);
                output.flush();
                System.out.println("Movimiento enviado: " + move);
            } catch (IOException e) {
                System.out.println("Error enviando movimiento: " + e.getMessage());
            }
        }
    }
    
    // CORREGIDO: El color debe pasarse como parámetro
    public void sendPromotion(int col, int row, String pieceType, int promotionColor) {
        if (connected && output != null) {
            try {
                String promotionData = "PROMOTION," + col + "," + row + "," + pieceType + "," + promotionColor;
                output.writeObject(promotionData);
                output.flush();
                System.out.println("Promoción enviada: " + promotionData);
            } catch (IOException e) {
                System.out.println("Error enviando promoción: " + e.getMessage());
            }
        }
    }
    
    public String receiveMove() {
        if (connected && input != null) {
            try {
                return (String) input.readObject();
            } catch (Exception e) {
                System.out.println("Error recibiendo movimiento: " + e.getMessage());
            }
        }
        return null;
    }
    
    public void disconnect() {
        try {
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
            connected = false;
        } catch (IOException e) {
            System.out.println("Error desconectando: " + e.getMessage());
        }
    }
}