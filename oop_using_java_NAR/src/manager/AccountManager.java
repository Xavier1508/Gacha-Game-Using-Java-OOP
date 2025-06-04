package manager;

import java.io.*;
import java.util.*;
import model.Player;

public class AccountManager {
    private static final String USER_DATA_FILE = "userdata.txt";
    private static Map<String, Player> accountUsers;
    private int nextPlayerId;
    
    public AccountManager() {
        accountUsers = new HashMap<>();
        loadUserData();
        determineNextPlayerId();
    }
    
    private void loadUserData() {
        File userFile = new File(USER_DATA_FILE);
        if (!userFile.exists()) {
            System.out.println("User data file does not exist. Will be created when users register.");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_DATA_FILE))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("#");
                if (parts.length >= 4) {
                    String playerId = parts[0];
                    String username = parts[1];
                    String email = parts[2];
                    String password = parts[3];
                    
                    // Create user with basic account info only
                    Player player = new Player(playerId, username, email, password);
                    accountUsers.put(playerId, player);
                    count++;
                }
            }
            System.out.println("Loaded " + count + " user accounts from " + USER_DATA_FILE);
        } catch (IOException e) {
            System.err.println("Error loading user data: " + e.getMessage());
        }
    }
    
    private void determineNextPlayerId() {
        int maxId = 0;
        for (String playerId : accountUsers.keySet()) {
            if (playerId.startsWith("OV")) {
                try {
                    int id = Integer.parseInt(playerId.substring(2));
                    maxId = Math.max(maxId, id);
                } catch (NumberFormatException e) {
                    // Ignore non-numeric IDs
                }
            }
        }
        nextPlayerId = maxId + 1;
        System.out.println("Next player ID will be: OV" + String.format("%03d", nextPlayerId));
    }
    
    public Player login(String email, String password) {
        String encryptedPassword = encryptPassword(password);
        
        for (Player accountUser : accountUsers.values()) {
            if (accountUser.getEmail().equals(email) && accountUser.getPassword().equals(encryptedPassword)) {
                // Credentials valid, now load full player data from DataManager
                String playerId = accountUser.getPlayerId();
                Player fullPlayer = DataManager.loginPlayer(playerId);
                
                // Copy account information to ensure it's complete
                fullPlayer.setUsername(accountUser.getUsername());
                fullPlayer.setEmail(accountUser.getEmail());
                fullPlayer.setPassword(accountUser.getPassword());
                
                System.out.println("User " + email + " (ID: " + playerId + ") logged in successfully");
                return fullPlayer;
            }
        }
        
        System.out.println("Login failed for email: " + email);
        return null;
    }
    
    public Player register(String username, String email, String password) {
        // Generate new player ID
        String playerId = generatePlayerId();
        String encryptedPassword = encryptPassword(password);
        
        // Create basic account info
        Player newPlayer = new Player(playerId, username, email, encryptedPassword);
        accountUsers.put(playerId, newPlayer);
        
        // Save account info to userdata.txt
        saveUserData(newPlayer);
        
        // Initialize player in DataManager (which handles game data)
        Player fullPlayer = DataManager.loginPlayer(playerId);
        
        // Copy account information to the full player object
        fullPlayer.setUsername(username);
        fullPlayer.setEmail(email);
        fullPlayer.setPassword(encryptedPassword);
        
        System.out.println("New user registered: " + username + " (ID: " + playerId + ")");
        return fullPlayer;
    }
    

    private String generatePlayerId() {
        String id = String.format("OV%03d", nextPlayerId);
        nextPlayerId++;
        return id;
    }
    

    private String encryptPassword(String password) {
        // 1. Reverse the password
        StringBuilder reversed = new StringBuilder(password).reverse();
        
        // 2. Swap adjacent characters
        char[] chars = reversed.toString().toCharArray();
        for (int i = 0; i < chars.length - 1; i += 2) {
            char temp = chars[i];
            chars[i] = chars[i + 1];
            chars[i + 1] = temp;
        }
        
        // 3. Shift characters to the right by 8 positions
        char[] shifted = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            shifted[(i + 8) % chars.length] = chars[i];
        }
        
        return new String(shifted);
    }
    
    private void saveUserData(Player player) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_DATA_FILE, true))) {
            writer.write(String.format("%s#%s#%s#%s\n", 
                player.getPlayerId(), 
                player.getUsername(), 
                player.getEmail(), 
                player.getPassword()));
            System.out.println("Saved user data for: " + player.getUsername());
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }
    
    public static List<Player> getPlayersSortedByTrophies() {
        Map<String, Player> gameDataPlayers = DataManager.getPlayers();
        
        for (Player player : gameDataPlayers.values()) {
            if (player.getUsername().startsWith("player_")) {
                for (Player accountUser : accountUsers.values()) {
                    if (accountUser.getPlayerId().equals(player.getPlayerId())) {
                        player.setUsername(accountUser.getUsername());
                        player.setEmail(accountUser.getEmail());
                        player.setPassword(accountUser.getPassword());
                        break;
                    }
                }
            }
        }
        
        List<Player> playerList = new ArrayList<>(gameDataPlayers.values());
        playerList.sort(Comparator.comparingInt(Player::getTrophies).reversed());
        return playerList;
    }
    
    public boolean isUsernameExist(String username) {
        for (Player player : accountUsers.values()) {
            if (player.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isEmailExist(String email) {
        for (Player player : accountUsers.values()) {
            if (player.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".com");
    }
}