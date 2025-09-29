package main;

import java.io.*;
import java.net.*;
import java.util.Random;

public class OnlineManager {
    private static final int[] PORTS = {12345, 12346, 12347, 12348, 12349}; // Múltiples puertos
    private ServerSocket serverSocket;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    
    private String gameCode;
    private boolean isHost;
    private boolean connected;
    private int currentPort;
    
    public OnlineManager() {
        this.gameCode = generateGameCode();
        this.connected = false;
        this.currentPort = PORTS[0];
    }
    
    private String generateGameCode() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }
    
    public String getGameCode() { return gameCode; }
    public boolean isHost() { return isHost; }
    public boolean isConnected() { return connected; }
    public int getCurrentPort() { return currentPort; }
    
    public boolean createGame() {
        for (int port : PORTS) {
            try {
                serverSocket = new ServerSocket(port);
                currentPort = port;
                isHost = true;
                
                // Esperar conexión con timeout
                serverSocket.setSoTimeout(30000); // 30 segundos timeout
                
                socket = serverSocket.accept();
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                connected = true;
                
                return true;
                
            } catch (SocketTimeoutException e) {
                disconnect();
                return false;
            } catch (IOException e) {
                 continue; // Probar siguiente puerto
            }
        }
        
        return false;
    }
    
    public boolean joinGame(String code, String host) {
        // Probar todos los puertos posibles
        for (int port : PORTS) {
            try {
                socket = new Socket(host, port);
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                isHost = false;
                connected = true;
                this.gameCode = code;
                this.currentPort = port;
                
                return true;
                
            } catch (IOException e) {
                continue; // Probar siguiente puerto
            }
        }
        
        return false;
    }
    
    public void sendData(String data) {
        if (connected && output != null) {
            try {
                output.writeObject(data);
                output.flush();
                } catch (IOException e) {
               connected = false;
            }
        }
    }
    
 // Mejora el método receiveData para evitar bloqueos:
    public String receiveData() {
        if (connected && input != null) {
            try {
                // Configurar timeout para no bloquear indefinidamente
                socket.setSoTimeout(100); // 100ms timeout
                String data = (String) input.readObject();
                socket.setSoTimeout(0); // Restaurar a bloqueo normal
                return data;
            } catch (SocketTimeoutException e) {
                // Timeout esperado, no es un error
                return null;
            } catch (IOException e) {
                connected = false;
            } catch (ClassNotFoundException e) {
                } catch (Exception e) {
                }
        }
        return null;
    }
    
    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
            connected = false;
            } catch (IOException e) {
            
        }
    }
}