package main;

import java.io.*;
import java.util.HashMap;

public class UserManager {
    private static final String USERS_FILE = "users.dat";
    private HashMap<String, User> users;
    private User currentUser;
    
    public UserManager() {
        users = new HashMap<>();
        loadUsers();
    }
    
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try {
            File file = new File(USERS_FILE);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                users = (HashMap<String, User>) ois.readObject();
                ois.close();
                fis.close();
            }
        } catch (Exception e) {
            System.out.println("Error cargando usuarios: " + e.getMessage());
            users = new HashMap<>();
        }
    }
    
    private void saveUsers() {
        try {
            FileOutputStream fos = new FileOutputStream(USERS_FILE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(users);
            oos.close();
            fos.close();
        } catch (Exception e) {
            System.out.println("Error guardando usuarios: " + e.getMessage());
        }
    }
    
    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false; // Usuario ya existe
        }
        
        User newUser = new User(username, password);
        users.put(username, newUser);
        saveUsers();
        return true;
    }
    
    public boolean login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }
    
    public void logout() {
        currentUser = null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}