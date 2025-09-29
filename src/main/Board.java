package main;
import java.awt.*;
import javax.swing.*;

public class Board {
    final int MAX_COL = 8;
    final int MAX_ROW = 8;
    public static int SQUARE_SIZE = 100; // Hacerlo no-final para poder cambiarlo
    public static int HALF_SQUARE_SIZE = SQUARE_SIZE / 2;

    private Color lightColor = new Color(210,165,125);
    private Color darkColor  = new Color(175,115,70);

    public Board() {
        // Constructor vacío
    }

    // Método para cambiar el tamaño de las casillas
    public void setSquareSize(int size) {
        SQUARE_SIZE = size;
        HALF_SQUARE_SIZE = size / 2;
    }

    public void showThemeSelector() {
        JFrame frame = new JFrame("Selecciona tema");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 150); // Un poco más grande
        frame.setLocationRelativeTo(null);

        String[] temas = {
            "Bosque", "Oro envejecido", "Clásico", "Marino", "Gris industrial", 
            "Pastel", "Nocturno", "Rojo elegante", "Esmeralda", "Nieve",
            "Fuego", "Arena", "Violeta", "Océano profundo", "Desierto dorado",
            "Selva tropical", "Atardecer", "Hielo ártico", "Chocolate", "Noche estrellada",
            "Primavera", "Retro", "Metálico", "Pixel Retro", "Cyberpunk", "Mario Bros",
            "Matrix", "Neón", "Ciberpunk oscuro", "Galaxia", "Planeta rojo", "Nebulosa",
            "Chocolate y vainilla", "Fresa con crema", "Caramelo", "Furia", "Tranquilidad", "Misterio"
        };

        JComboBox<String> combo = new JComboBox<>(temas);
        combo.setPreferredSize(new Dimension(200, 30)); // Tamaño fijo para el combo
        combo.addActionListener(e -> {
            String tema = (String) combo.getSelectedItem();
            setTheme(tema);
            frame.dispose();
        });

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
        panel.add(new JLabel("Tema:"));
        panel.add(combo);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void setTheme(String themeName) {
        switch (themeName) {
            case "Bosque":
                lightColor = new Color(170,215,81);
                darkColor  = new Color(100,140,50);
                break;
            case "Oro envejecido":
                lightColor = new Color(210,165,125);
                darkColor  = new Color(175,115,70);
                break;
            case "Clásico":
                lightColor = new Color(240,217,181);
                darkColor  = new Color(181,136,99);
                break;
            case "Marino":
                lightColor = new Color(173,216,230);
                darkColor  = new Color(25,25,112);
                break;
            case "Gris industrial":
                lightColor = new Color(200,200,200);
                darkColor  = new Color(80,80,80);
                break;
            case "Pastel":
                lightColor = new Color(255,228,225);
                darkColor  = new Color(221,160,221);
                break;
            case "Nocturno":
                lightColor = new Color(60,63,65);
                darkColor  = new Color(30,30,30);
                break;
            case "Rojo elegante":
                lightColor = new Color(255,204,203);
                darkColor  = new Color(139,0,0);
                break;
            case "Esmeralda":
                lightColor = new Color(144,238,144);
                darkColor  = new Color(0,100,0);
                break;
            case "Nieve":
                lightColor = new Color(255,255,255);
                darkColor  = new Color(180,180,180);
                break;
            case "Fuego":
                lightColor = new Color(255,165,0);
                darkColor  = new Color(178,34,34);
                break;
            case "Arena":
                lightColor = new Color(244,164,96);
                darkColor  = new Color(139,69,19);
                break;
            case "Violeta":
                lightColor = new Color(216,191,216);
                darkColor  = new Color(148,0,211);
                break;
            case "Océano profundo":
                lightColor = new Color(0,191,255);
                darkColor  = new Color(0,0,139);
                break;
            case "Desierto dorado":
                lightColor = new Color(255,239,184);
                darkColor  = new Color(210,180,140);
                break;
            case "Selva tropical":
                lightColor = new Color(144,238,144);
                darkColor  = new Color(0,100,0);
                break;
            case "Atardecer":
                lightColor = new Color(255,182,193);
                darkColor  = new Color(255,69,0);
                break;
            case "Hielo ártico":
                lightColor = new Color(224,255,255);
                darkColor  = new Color(0,139,139);
                break;
            case "Chocolate":
                lightColor = new Color(210,105,30);
                darkColor  = new Color(92,51,23);
                break;
            case "Noche estrellada":
                lightColor = new Color(72,61,139);
                darkColor  = new Color(25,25,112);
                break;
            case "Primavera":
                lightColor = new Color(255,250,205);
                darkColor  = new Color(60,179,113);
                break;
            case "Retro":
                lightColor = new Color(255,228,181);
                darkColor  = new Color(205,92,92);
                break;
            case "Metálico":
                lightColor = new Color(192,192,192);
                darkColor  = new Color(105,105,105);
                break;
            case "Pixel Retro":
                lightColor = new Color(255,179,71);
                darkColor  = new Color(255,127,80);
                break;
            case "Cyberpunk":
                lightColor = new Color(255,0,255);
                darkColor  = new Color(0,255,255);
                break;
            case "Mario Bros":
                lightColor = new Color(255,0,0);
                darkColor  = new Color(0,0,255);
                break;
            case "Matrix":
                lightColor = new Color(0,255,0);
                darkColor  = new Color(0,51,0);
                break;
            case "Neón":
                lightColor = new Color(57,255,20);
                darkColor  = new Color(13,13,13);
                break;
            case "Ciberpunk oscuro":
                lightColor = new Color(255,0,127);
                darkColor  = new Color(26,26,46);
                break;
            case "Galaxia":
                lightColor = new Color(138,43,226);
                darkColor  = new Color(75,0,130);
                break;
            case "Planeta rojo":
                lightColor = new Color(255,69,0);
                darkColor  = new Color(139,0,0);
                break;
            case "Nebulosa":
                lightColor = new Color(255,105,180);
                darkColor  = new Color(106,90,205);
                break;
            case "Chocolate y vainilla":
                lightColor = new Color(243,229,171);
                darkColor  = new Color(139,69,19);
                break;
            case "Fresa con crema":
                lightColor = new Color(255,192,203);
                darkColor  = new Color(199,21,133);
                break;
            case "Caramelo":
                lightColor = new Color(255,215,0);
                darkColor  = new Color(255,140,0);
                break;
            case "Furia":
                lightColor = new Color(255,99,71);
                darkColor  = new Color(139,0,0);
                break;
            case "Tranquilidad":
                lightColor = new Color(173,216,230);
                darkColor  = new Color(70,130,180);
                break;
            case "Misterio":
                lightColor = new Color(211,211,211);
                darkColor  = new Color(47,79,79);
                break;
            default:
                lightColor = new Color(210,165,125);
                darkColor  = new Color(175,115,70);
        }
    }

    public void draw(Graphics2D g2) {
        int c = 0;
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                g2.setColor(c == 0 ? lightColor : darkColor);
                g2.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                c = 1 - c;
            }
            c = 1 - c;
        }
    }
}