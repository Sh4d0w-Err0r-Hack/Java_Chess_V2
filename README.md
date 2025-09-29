🏆 Ajedrez Maestro - Juego de Ajedrez Multijugador en Java

![Java17+](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![JavaSwing](https://img.shields.io/badge/Java%2520Swing-GUI-blue?style=for-the-badge)
![ONLINE](https://img.shields.io/badge/Multijugador-Online-green?style=for-the-badge)
![LICENCIA](https://img.shields.io/badge/Licencia-MIT-yellow?style=for-the-badge)

Un juego de ajedrez completo con inteligencia artificial, multijugador online y personalización avanzada

## Tabla de Contenidos
- [🎯 Características Principales](#-características-principales)
- [⚡ Características Avanzadas](#-características-avanzadas)
- [🛠️ Tecnologías y Arquitectura](#-tecnologías-y-arquitectura)
- [🧠 Lógica del Juego](#-lógica-del-juego)
- [🔧 Patrones de Diseño Implementados](#-patrones-de-diseño-implementados)
- [🚀 Instalación y Ejecución](#-instalación-y-ejecución)
- [🎮 Gameplay](#-gameplay)
- [📊 Especificaciones Técnicas Detalladas](#-especificaciones-técnicas-detalladas)
- [🎯 Roadmap Futuro](#-roadmap-futuro)
- [👥 Contribuciones](#-contribuciones)


## 🎯 Características Principales

### ♟️ Modos de Juego
```bash
-> 🆚 Vs IA: Juega contra una inteligencia artificial con diferentes niveles de dificultad

-> 👥 Multijugador Local: Dos jugadores en el mismo dispositivo

-> 🌐 Multijugador Online: Crea salas o únete a partidas con código de 4 dígitos

-> ⚡ Sistema de Turnos: Sincronización en tiempo real para partidas online
```


### 🎨 Personalización Visual
```bash
🎨 37 Temas de Tablero: Desde clásico hasta cyberpunk, bosque, galaxia y más

👑 Sets de Piezas Personalizables: Diferentes estilos visuales para las piezas

🔊 Sistema de Música: Playlist integrada con controles de reproducción

🎵 Efectos de Sonido: Experiencia auditiva inmersiva
```

### 🎮 Experiencia de Usuario
```bash
🖥️ Interfaz Intuitiva: Menús fáciles de navegar con botones claros

📱 Diseño Responsive: Se adapta a diferentes tamaños de pantalla

🎯 Sistema de Ayudas Visuales: Casillas destacadas, movimientos válidos marcados

📊 Información en Tiempo Real: Estado de la partida, turno actual, conexión online
```


## ⚡ Características Avanzadas
```bash
🤖 IA Inteligente: Movimientos estratégicos con priorización de capturas

📈 Sistema de XP: Rastrea el progreso del jugador

💾 Guardado de Partidas: Historial detallado de partidas jugadas

🔧 Configuraciones Persistente: Preferencias guardadas entre sesiones
```

## 🛠️ Tecnologías y Arquitectura
```bash
🏗️ Arquitectura del Proyecto
```bash
Java_Chess_V2
├── bin
│   ├── main
│   │   ├── AIMove.class
│   │   ├── AIPlayer.class
│   │   ├── Board.class
│   │   ├── GameConfig.class
│   │   ├── GameHistory.class
│   │   ├── GameMode.class
│   │   ├── GamePanel.class
│   │   ├── Main$1.class
│   │   ├── Main.class
│   │   ├── Mouse.class
│   │   ├── MusicPlayer.class
│   │   ├── OnlineGame.class
│   │   ├── OnlineManager.class
│   │   ├── PieceSetManager.class
│   │   ├── Type.class
│   │   ├── User.class
│   │   └── UserManager.class
│   ├── music
│   │   ├── base_music.wav
│   │   ├── music_2.wav
│   │   ├── music_3.wav
│   │   ├── music_4.wav
│   │   └── music_5.wav
│   └── piece
│       ├── b-bishop.png
│       ├── Bishop.class
│       ├── b-king.png
│       ├── b-knight.png
│       ├── b-pawn.png
│       ├── b-queen.png
│       ├── b-rook.png
│       ├── King.class
│       ├── Knight.class
│       ├── Pawn.class
│       ├── Piece.class
│       ├── Queen.class
│       ├── Rook.class
│       ├── w-bishop.png
│       ├── w-king.png
│       ├── w-knight.png
│       ├── w-pawn.png
│       ├── w-queen.png
│       └── w-rook.png
├── music
│   └── music
│       ├── base_music.wav
│       ├── music_2.wav
│       ├── music_3.wav
│       ├── music_4.wav
│       └── music_5.wav
├── README.md
├── res
│   └── piece
│       ├── b-bishop.png
│       ├── b-king.png
│       ├── b-knight.png
│       ├── b-pawn.png
│       ├── b-queen.png
│       ├── b-rook.png
│       ├── w-bishop.png
│       ├── w-king.png
│       ├── w-knight.png
│       ├── w-pawn.png
│       ├── w-queen.png
│       └── w-rook.png
└── src
    ├── main
    │   ├── AIMove.java
    │   ├── AIPlayer.java
    │   ├── Board.java
    │   ├── GameConfig.java
    │   ├── GameHistory.java
    │   ├── GameMode.java
    │   ├── GamePanel.java
    │   ├── Main.java
    │   ├── Mouse.java
    │   ├── MusicPlayer.java
    │   ├── OnlineGame.java
    │   ├── OnlineManager.java
    │   ├── PieceSetManager.java
    │   ├── Type.java
    │   ├── User.java
    │   └── UserManager.java
    └── piece
        ├── Bishop.java
        ├── King.java
        ├── Knight.java
        ├── Pawn.java
        ├── Piece.java
        ├── Queen.java
        └── Rook.java
```

## 🧠 Lógica del Juego
<h4>Sistema de Movimientos</h4>
Cada pieza implementa su propia lógica de movimiento mediante el método canMove(), que verifica las reglas específicas de movimiento, obstáculos en el camino y validez de las casillas destino.

### Inteligencia Artificial
```java
// La IA analiza el tablero completo y evalúa movimientos posibles
public AIMove findBestMove(ArrayList<Piece> pieces, int currentColor) {
    // 1. Genera todos los movimientos legales posibles
    // 2. Prioriza movimientos que capturen piezas enemigas
    // 3. Evalúa la seguridad de cada movimiento
    // 4. Selecciona el movimiento más estratégico
}
```
### Algoritmo de la IA:

Generación de Movimientos: Analiza todas las piezas propias y genera movimientos válidos para cada una

Filtrado por Legalidad: Descarta movimientos que dejen al rey en jaque usando simulación

### Sistema de Prioridades:

Máxima prioridad: Movimientos que capturan piezas enemigas

Prioridad media: Movimientos hacia casillas seguras (no atacadas)

Prioridad baja: Movimientos aleatorios válidos

Selección Final: Elige el mejor movimiento disponible basado en la estrategia

### Sistema Online
```java
public class OnlineManager {
    // Gestión de múltiples puertos para mayor compatibilidad
    // Sistema de códigos de sala únicos de 4 dígitos
    // Protocolo de comunicación para movimientos y promociones
    // Mecanismos de timeout y reconexión
}
```

### Protocolo de Comunicación:

Movimientos: Formato estructurado con coordenadas y turno

Promociones: Notificación de cambios de peón a otras piezas

Sincronización: Mantenimiento consistente del estado del tablero

### Detección de Estado del Juego
```java
private void checkGameState() {
    // Verificación fundamental de la existencia de ambos reyes
    // Análisis de jaque mate mediante evaluación de movimientos legales
    // Detección de tablas por ahogado (rey no en jaque sin movimientos)
    // Determinación automática del ganador en caso de captura de rey
}
```
### Estados del Juego:

Juego Activo: Ambos reyes presentes y movimientos disponibles

Jaque Mate: Rey en jaque sin movimientos legales de escape

Tablas: Situación de ahogado o imposibilidad de continuar

Fin por Captura: Cuando un rey es capturado (modo rápido)

### Sistema de Renderizado
```java
public void paintComponent(Graphics g) {
    // Renderizado eficiente del tablero con temas aplicados
    // Dibujado optimizado de piezas con imágenes escaladas
    // Interfaz de usuario contextual según el estado del juego
    // Efectos visuales para mejor experiencia de usuario
}
```
## 🔧 Patrones de Diseño Implementados
MVC (Model-View-Controller): Separación clara entre la lógica del juego, los datos y la interfaz de usuario

Strategy Pattern: Comportamientos diferentes para cada tipo de pieza mediante herencia

Observer Pattern: Actualizaciones automáticas cuando cambia el estado del juego

Factory Method: Creación dinámica de piezas durante la promoción de peones

## 🎵 Sistema de Audio
```java
public class MusicPlayer {
    // Gestión de playlist con transiciones suaves entre canciones
    // Control de volumen y estado de mute configurables
    // Carga eficiente de recursos de audio
    // Interfaz para selección manual de canciones
}
```
## 🚀 Instalación y Ejecución
### Prerrequisitos
Java 17 o superior

500 MB de espacio libre

Conexión a internet (para modo online)

### Ejecución
```bash
# Compilación (opcional)
javac -d bin src/main/*.java src/piece/*.java

# Ejecución directa
java -cp bin main.Main
Configuración de Red para Online
Puertos: 12345-12349 deben estar disponibles

Firewall: Permitir conexiones Java

NAT: Configuración para hosting desde redes locales
```
## 🎮 Gameplay
### Controles
🖱️ Click Izquierdo: Seleccionar y mover piezas

🖱️ Arrastrar: Movimiento con feedback visual inmediato

⎋ Escape/Cancelar: Deseleccionar pieza actual

### Flujo de Partida
Selección de Modo → Interfaz intuitiva con todas las opciones

Configuración → Personalización de tema, música y preferencias

Gameplay → Sistema de turnos con validación en tiempo real

Fin de Partida → Detección automática con resumen visual

### Características Especiales
Promoción de Peones: Interfaz interactiva para elegir nueva pieza

Enroque: Implementado según reglas oficiales del ajedrez

Jaque Visual: Indicación clara cuando el rey está en peligro

Movimientos Legales: Marcado visual de casillas válidas

## 📊 Especificaciones Técnicas Detalladas
### Rendimiento
FPS: 60 frames por segundo para fluidez visual

Memoria: Gestión optimizada de recursos

Red: Mínima latencia en modo online con comprobación de estado

### Compatibilidad
SO: Windows, macOS, Linux (multi-plataforma)

Java: Versión 17+ requerida para funcionalidades modernas

Resolución: Diseño adaptable desde 1100x800 hasta pantallas completas

### Seguridad
Validación: Doble verificación de movimientos (cliente-servidor)

Sincronización: Estado consistente entre todos los jugadores

Recuperación: Tolerante a fallos de conexión temporales

## 🎯 Roadmap Futuro
Dificultades de IA: Múltiples niveles de desafío ajustables

Base de Datos: Sistema de ranking y estadísticas persistentes

Replays: Grabación y reproducción de partidas completas

Torneos: Sistema competitivo con brackets y eliminatorias

Apps Móviles: Versiones optimizadas para iOS y Android

## 👥 Contribuciones
¡Las contribuciones son bienvenidas! Áreas prioritarias de mejora:

Optimización de algoritmos de IA para mayor desafío

Nuevos temas visuales y sets de piezas

Mejoras en la estabilidad de la red online

Documentación técnica adicional para desarrolladores

<div align="center">
¿Listo para convertirte en un maestro del ajedrez? 🏆

Desarrollado con ♟️ pasión por el ajedrez y 💻 excelencia técnica

</div>
