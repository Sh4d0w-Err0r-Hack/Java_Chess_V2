package main;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class MusicPlayer {
    private Clip currentClip;
    private boolean isMuted = false;
    private ArrayList<String> playlist;
    private int currentSongIndex = 0;
    private boolean loopPlaylist = true;
    
    public MusicPlayer() {
        playlist = new ArrayList<>();
        loadPlaylist();
        loadCurrentSong();
    }
    
    private void loadPlaylist() {
        // Lista de canciones disponibles (debes tener estos archivos en la carpeta resources/music/)
        String[] songs = {
            "/music/music_3.wav",
            "/music/music_2.wav", 
            "/music/base_music.wav",
            "/music/music_4.wav",
            "/music/music_5.wav"
        };
        
        for (String song : songs) {
            if (getClass().getResourceAsStream(song) != null) {
                playlist.add(song);
            }
        }
        
        // Si no hay canciones, mostrar mensaje
        if (playlist.isEmpty()) {
            System.out.println("No se encontraron canciones en la playlist");
        }
    }
    
    public void loadCurrentSong() {
        if (playlist.isEmpty()) return;
        
        try {
            if (currentClip != null) {
                currentClip.stop();
                currentClip.close();
            }
            
            InputStream audioSrc = getClass().getResourceAsStream(playlist.get(currentSongIndex));
            if (audioSrc == null) {
                System.out.println("No se encontró el archivo: " + playlist.get(currentSongIndex));
                playNextSong(); // Intentar con la siguiente canción
                return;
            }
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(audioSrc));
            currentClip = AudioSystem.getClip();
            currentClip.open(audioInputStream);
            
            // Listener para cuando termine la canción
            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && !isMuted) {
                    playNextSong();
                }
            });
            
        } catch (Exception e) {
            System.out.println("Error cargando música: " + e.getMessage());
            playNextSong(); // Intentar con la siguiente canción
        }
    }
    
    public void play() {
        if (currentClip != null && !isMuted) {
            currentClip.start();
        }
    }
    
    public void stop() {
        if (currentClip != null) {
            currentClip.stop();
        }
    }
    
    public void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            stop();
        } else {
            play();
        }
    }
    
    public void playNextSong() {
        if (playlist.isEmpty()) return;
        
        currentSongIndex = (currentSongIndex + 1) % playlist.size();
        loadCurrentSong();
        play();
    }
    
    public void playPreviousSong() {
        if (playlist.isEmpty()) return;
        
        currentSongIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
        loadCurrentSong();
        play();
    }
    
    public void setSong(int index) {
        if (index >= 0 && index < playlist.size()) {
            currentSongIndex = index;
            loadCurrentSong();
            if (!isMuted) {
                play();
            }
        }
    }
    
    public String getCurrentSongName() {
        if (playlist.isEmpty()) return "No hay canciones";
        
        String fullPath = playlist.get(currentSongIndex);
        String fileName = fullPath.substring(fullPath.lastIndexOf("/") + 1);
        return fileName.replace(".wav", "").replace("_", " ");
    }
    
    public ArrayList<String> getPlaylistNames() {
        ArrayList<String> names = new ArrayList<>();
        for (String path : playlist) {
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            names.add(fileName.replace(".wav", "").replace("_", " "));
        }
        return names;
    }
    
    public int getCurrentSongIndex() {
        return currentSongIndex;
    }
    
    public int getPlaylistSize() {
        return playlist.size();
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    public boolean hasSongs() {
        return !playlist.isEmpty();
    }
}