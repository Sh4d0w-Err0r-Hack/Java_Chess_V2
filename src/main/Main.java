package main;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Ajedrez Maestro - Multijugador");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GamePanel gp = new GamePanel();
        window.add(gp);
        window.pack();
        
        window.setResizable(true);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        
        gp.setParentFrame(window);
        
        // Manejar cierre correctamente
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.out.println("Cerrando aplicación...");
                // Aquí desconectarías si es necesario
            }
        });
        
        gp.launchGame();
    }
}