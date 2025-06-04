package manager;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import model.Character;
import model.Material;
import model.OwnedCharacter;
import model.Player;

public class DataManager {
    private static Map<String, Material> materials = new HashMap<>();
    private static Map<String, Character> characters = new HashMap<>();
    private static Map<String, Player> players = new HashMap<>();
    
    // File paths as constants
    private static final String[] MATERIAL_DATA_PATHS = {
        "src/gachamaterialdata.txt",
        "gachamaterialdata.txt",
        "./gachamaterialdata.txt",
        "./src/gachamaterialdata.txt",
        "../gachamaterialdata.txt"
    };
    
    private static final String[] PLAYER_STAT_DATA_PATHS = {
        "src/playerstatdata.txt",
        "playerstatdata.txt",
        "./playerstatdata.txt",
        "./src/playerstatdata.txt",
        "../playerstatdata.txt"
    };
    
    private static final String[] PLAYER_INVENTORY_DATA_PATHS = {
        "src/playerinventorydata.txt",
        "playerinventorydata.txt",
        "./playerinventorydata.txt",
        "./src/playerinventorydata.txt",
        "../playerinventorydata.txt"
    };
    
    private static String foundPlayerStatPath = null;
    private static String foundPlayerInventoryPath = null;
    
    static {
        ensureDataFilesExist(); // Make sure files exist before trying to load from them
        loadMaterials();
        loadCharacters();
        loadPlayerStats();
        loadPlayerInventories();
    }
    
    private static void ensureDataFilesExist() {
        // Create playerstatdata.txt if it doesn't exist
        if (foundPlayerStatPath == null) {
            for (String path : PLAYER_STAT_DATA_PATHS) {
                File file = new File(path);
                try {
                    if (!file.exists()) {
                        // Try to create parent directory if needed
                        if (file.getParentFile() != null && !file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        
                        if (file.createNewFile()) {
                            System.out.println("Created empty player stat file: " + path);
                            foundPlayerStatPath = path;
                            break;
                        }
                    } else {
                        foundPlayerStatPath = path;
                        System.out.println("Found existing player stat file: " + path);
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Failed to create player stat file at: " + path);
                }
            }
        }
        
        // Create playerinventorydata.txt if it doesn't exist
        if (foundPlayerInventoryPath == null) {
            for (String path : PLAYER_INVENTORY_DATA_PATHS) {
                File file = new File(path);
                try {
                    if (!file.exists()) {
                        // Try to create parent directory if needed
                        if (file.getParentFile() != null && !file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        
                        if (file.createNewFile()) {
                            System.out.println("Created empty player inventory file: " + path);
                            foundPlayerInventoryPath = path;
                            break;
                        }
                    } else {
                        foundPlayerInventoryPath = path;
                        System.out.println("Found existing player inventory file: " + path);
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Failed to create player inventory file at: " + path);
                }
            }
        }
        
        // If still null, use defaults and make one final attempt to create them
        if (foundPlayerStatPath == null) {
            try {
                File file = new File(PLAYER_STAT_DATA_PATHS[0]);
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
                foundPlayerStatPath = PLAYER_STAT_DATA_PATHS[0];
                System.out.println("Forced creation of player stat file: " + foundPlayerStatPath);
            } catch (IOException e) {
                foundPlayerStatPath = PLAYER_STAT_DATA_PATHS[1]; // Fallback to current directory
                System.out.println("Using default player stat path: " + foundPlayerStatPath);
            }
        }
        
        if (foundPlayerInventoryPath == null) {
            try {
                File file = new File(PLAYER_INVENTORY_DATA_PATHS[0]);
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
                foundPlayerInventoryPath = PLAYER_INVENTORY_DATA_PATHS[0];
                System.out.println("Forced creation of player inventory file: " + foundPlayerInventoryPath);
            } catch (IOException e) {
                foundPlayerInventoryPath = PLAYER_INVENTORY_DATA_PATHS[1]; // Fallback to current directory
                System.out.println("Using default player inventory path: " + foundPlayerInventoryPath);
            }
        }
        
        // Check if files are writable
        checkFileWritable(foundPlayerStatPath, "Player stats");
        checkFileWritable(foundPlayerInventoryPath, "Player inventory");
    }
    
    /**
     * Check if a file is writable and log the result
     */
    private static void checkFileWritable(String path, String fileType) {
        File file = new File(path);
        if (file.exists()) {
            if (file.canWrite()) {
                System.out.println(fileType + " file is writable: " + path);
            } else {
                System.err.println("WARNING: " + fileType + " file exists but is NOT writable: " + path);
            }
        } else {
            System.err.println("WARNING: " + fileType + " file does not exist: " + path);
        }
    }
    
    private static void loadMaterials() {
        for (String path : MATERIAL_DATA_PATHS) {
            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                System.out.println("Successfully loaded materials from: " + path);
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("#");
                    if (parts.length >= 4) {
                        String id = parts[0];
                        String rarity = parts[1];
                        String name = parts[2];
                        String forCharacter = parts[3];
                        materials.put(id, new Material(id, rarity, name, forCharacter));
                    }
                }
                // If we got here, we successfully loaded the file
                return;
            } catch (IOException e) {
                // Continue trying other paths
                System.err.println("Failed loading materials from path: " + path);
            }
        }
        
        // If we get here, all paths failed
        System.err.println("Failed to load material data from all possible paths.");
    }

    private static void loadCharacters() {
        // Hardcoded characters (as per the original code)
        characters.put("CH001", new Character("CH001", "Ainz Ooal Gown", "The Sorcerer King", 
            "Create Fortress", "When buying/overtaking a property, acquire and fully upgrade it to a Landmark for free.", 55));
        characters.put("CH002", new Character("CH002", "Albedo", "Guardian Overseer", 
            "Armor of Malice", "Ignore all toll payments.", 35));
        characters.put("CH003", new Character("CH003", "Shalltear Bloodfallen", "The Crimson Valkyrie", 
            "Blood Tribute", "Steal 50% of an opponent's money when passing them.", 65));
        characters.put("CH004", new Character("CH004", "Demiurge", "Tactical Genius", 
            "Infernal Strategy", "Instantly move to the Start after purchasing/upgrading a property.", 55));
        characters.put("CH005", new Character("CH005", "Cocytus", "Glacial General", 
            "Glacial Imprisonment", "Send an opponent to Jail when passing them.", 35));
        characters.put("CH006", new Character("CH006", "Pandora's Actor", "Perfect Impersonator", 
            "False Fortune", "3 toll payments are free. Chance to increase free toll counter.", 10));
    }
    
    /**
     * Load player stats from playerstatdata.txt
     * Format: PlayerId#Level#Gems#CurrentExp#CurrentEnergy#EquippedCharacterId#Trophies
     */
    private static void loadPlayerStats() {
        if (foundPlayerStatPath == null) {
            System.err.println("No player stat file path found!");
            return;
        }
        
        File statFile = new File(foundPlayerStatPath);
        if (!statFile.exists() || statFile.length() == 0) {
            System.out.println("Player stat file is empty or doesn't exist. No players to load.");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(foundPlayerStatPath))) {
            System.out.println("Loading player stats from: " + foundPlayerStatPath);
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("#");
                if (parts.length >= 7) {
                    String playerId = parts[0];
                    int level = Integer.parseInt(parts[1]);
                    int gems = Integer.parseInt(parts[2]);
                    int currentExp = Integer.parseInt(parts[3]);
                    int currentEnergy = Integer.parseInt(parts[4]);
                    String equippedCharacterId = parts[5];
                    int trophies = Integer.parseInt(parts[6]);
                    
                    // First check if we have real user data for this player
                    Player existingPlayer = loadUserInfoFromUserdataFile(playerId);
                    
                    Player player;
                    if (existingPlayer != null) {
                        // Use the real user information but update with game stats
                        player = new Player(
                            playerId, 
                            existingPlayer.getUsername(), 
                            existingPlayer.getEmail(), 
                            existingPlayer.getPassword(),
                            level, 
                            gems, 
                            currentExp, 
                            currentEnergy, 
                            equippedCharacterId, 
                            trophies
                        );
                    } else {
                        // Create player with placeholder data for username, email, password
                        player = new Player(
                            playerId, 
                            "player_" + playerId, 
                            "player_" + playerId + "@example.com", 
                            "password",
                            level, 
                            gems, 
                            currentExp, 
                            currentEnergy, 
                            equippedCharacterId, 
                            trophies
                        );
                    }
                    
                    players.put(playerId, player);
                    count++;
                }
            }
            System.out.println("Loaded " + count + " players from stat data");
        } catch (IOException e) {
            System.err.println("Failed loading player stats from: " + foundPlayerStatPath);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing player stats: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Player loadUserInfoFromUserdataFile(String playerId) {
        final String USER_DATA_FILE = "userdata.txt";
        
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("#");
                if (parts.length >= 4 && parts[0].equals(playerId)) {
                    String username = parts[1];
                    String email = parts[2];
                    String password = parts[3];
                    
                    // Create player with real account data
                    return new Player(playerId, username, email, password);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading user data: " + e.getMessage());
        }
        
        return null;
    }
    
    private static void loadPlayerInventories() {
        if (foundPlayerInventoryPath == null) {
            System.err.println("No player inventory file path found!");
            return;
        }
        
        File inventoryFile = new File(foundPlayerInventoryPath);
        if (!inventoryFile.exists() || inventoryFile.length() == 0) {
            System.out.println("Player inventory file is empty or doesn't exist. No inventories to load.");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(foundPlayerInventoryPath))) {
            System.out.println("Loading player inventories from: " + foundPlayerInventoryPath);
            String line;
            int materialCount = 0;
            int characterCount = 0;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("#");
                if (parts.length >= 3) {
                    String playerId = parts[0];
                    String itemId = parts[1];
                    int quantity = Integer.parseInt(parts[2]);
                    
                    Player player = players.get(playerId);
                    if (player == null) {
                        // Create player if not found - this is crucial for when inventory is loaded
                        // before player stats or if a player has items but no stats entry
                        player = new Player(playerId, 
                                          "player_" + playerId, 
                                          "player_" + playerId + "@example.com", 
                                          "password",
                                          1, // Default level 
                                          0, // Default gems
                                          0, // Default exp
                                          0, // Default energy
                                          "", // No equipped character
                                          0); // Default trophies
                        players.put(playerId, player);
                        
                        // Since we created a player from inventory but without stats,
                        // make sure to save the player stats too
                        savePlayerStats();
                    }
                    
                    // Check if this is a character or material
                    if (itemId.startsWith("CH")) {
                        Character character = characters.get(itemId);
                        if (character != null) {
                            // Add character with specified level
                            player.addCharacter(new OwnedCharacter(character, quantity));
                            characterCount++;
                        }
                    } else if (itemId.startsWith("MT")) {
                        // Add material with specified quantity
                        player.addMaterial(itemId, quantity);
                        materialCount++;
                    }
                }
            }
            System.out.println("Loaded " + materialCount + " materials and " + characterCount + 
                               " characters across all players");
        } catch (IOException e) {
            System.err.println("Failed loading player inventories from: " + foundPlayerInventoryPath);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing player inventory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Map<String, Material> getMaterials() {
        return materials;
    }

    public static Map<String, Character> getCharacters() {
        return characters;
    }
    
    public static Map<String, Player> getPlayers() {
        return players;
    }

    public static boolean playerExists(String playerId) {
        if (players.containsKey(playerId)) {
            return true;
        }

        if (foundPlayerStatPath != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(foundPlayerStatPath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("#");
                    if (parts.length > 0 && parts[0].equals(playerId)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error checking player existence in stats file: " + e.getMessage());
            }
        }
        
        // Check player inventory file
        if (foundPlayerInventoryPath != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(foundPlayerInventoryPath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("#");
                    if (parts.length > 0 && parts[0].equals(playerId)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error checking player existence in inventory file: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Get player by ID, creating a new one if not found
     * This ensures we always have a player object to work with
     * 
     * @param playerId ID of the player to get
     * @return the player object, either existing or newly created
     */
    public static Player getPlayer(String playerId) {
        // Create player if not found - essential for game functionality
        Player player = players.get(playerId);
        if (player == null) {
            System.out.println("Creating new player with ID: " + playerId);
            
            // Try to get real user info first
            Player realUserData = loadUserInfoFromUserdataFile(playerId);
            
            if (realUserData != null) {
                // Use real user data but initialize game stats
                player = new Player(
                    playerId, 
                    realUserData.getUsername(), 
                    realUserData.getEmail(), 
                    realUserData.getPassword(),
                    1, // Default level 
                    0, // Default gems
                    0, // Default exp
                    0, // Default energy
                    "", // No equipped character
                    0  // Default trophies
                );
                System.out.println("Created player with real username: " + player.getUsername());
            } else {
                // Use placeholder data
                player = new Player(
                    playerId, 
                    "player_" + playerId, 
                    "player_" + playerId + "@example.com", 
                    "password",
                    1, // Default level 
                    0, // Default gems
                    0, // Default exp
                    0, // Default energy
                    "", // No equipped character
                    0  // Default trophies
                );
                System.out.println("Created player with placeholder username: " + player.getUsername());
            }
            
            players.put(playerId, player);
            
            // Save the new player immediately so it's not lost
            savePlayerStats();
        }
        return player;
    }
    
    /**
     * Login a player to the system, create if not exists
     * This ensures a player is created when they log in if needed
     * 
     * @param playerId ID of the player logging in
     * @return the player object
     */
    public static Player loginPlayer(String playerId) {
        // Ensure data files exist
        ensureDataFilesExist();
        
        // Check if player exists before login
        boolean exists = playerExists(playerId);
        System.out.println("Player " + playerId + " exists in data? " + exists);
        
        // Get or create player
        Player player = getPlayer(playerId);
        
        // If this player was loaded from game data but doesn't have real user info,
        // try to load it now
        if (player.getUsername().equals("player_" + playerId)) {
            Player realUserData = loadUserInfoFromUserdataFile(playerId);
            if (realUserData != null) {
                // Update with real user information
                player.setUsername(realUserData.getUsername());
                player.setEmail(realUserData.getEmail());
                player.setPassword(realUserData.getPassword());
                System.out.println("Updated player " + playerId + " with real username: " + player.getUsername());
            }
        }
        
        // Force save player data to ensure it's persisted
        savePlayerStats();
        savePlayerInventories();
        
        if (exists) {
            System.out.println("Player " + playerId + " logged in successfully with existing data");
            
            // Log details about the player's inventory for debugging
            System.out.println("  - Username: " + player.getUsername());
            System.out.println("  - Materials: " + player.getMaterials().size());
            System.out.println("  - Characters: " + player.getCharacters().size());
            System.out.println("  - Gems: " + player.getGems());
            System.out.println("  - Energy: " + player.getCurrentEnergy());
        } else {
            System.out.println("New player " + playerId + " created and logged in");
        }
        
        return player;
    }
    
    public static boolean updatePlayerMaterial(String playerId, String materialId, int quantity) {
        Player player = getPlayer(playerId);
        
        Map<String, Integer> materials = player.getMaterials();
        int currentAmount = materials.getOrDefault(materialId, 0);
        
        if (quantity < 0 && (currentAmount + quantity) < 0) {
            System.out.println("Cannot remove " + Math.abs(quantity) + " of material " + 
                             materialId + " from player " + playerId + 
                             " (current amount: " + currentAmount + ")");
            return false;
        }
        
        player.addMaterial(materialId, quantity);
        
        System.out.println("Updated player " + playerId + "'s material " + materialId + 
                         " by " + quantity + " (new total: " + 
                         player.getMaterials().getOrDefault(materialId, 0) + ")");
        
        System.out.println("Player inventory before saving: " + player.getMaterials().size() + " materials");
        
        savePlayerInventories();
        return true;
    }
    
    public static boolean addPlayerCharacter(String playerId, String characterId, int level) {
        Player player = getPlayer(playerId);
        Character character = characters.get(characterId);
        
        if (character == null) {
            System.out.println("Character " + characterId + " not found");
            return false;
        }
        
        player.addCharacter(new OwnedCharacter(character, level));
        System.out.println("Added character " + characterId + " (level " + level + ") to player " + playerId);
        
        // Save changes to file
        savePlayerInventories();
        return true;
    }
    
    /**
     * Updates player stats (e.g., after level up, spending gems, etc.)
     * Automatically saves stats to data file
     * 
     * @param playerId ID of the player to update
     * @param gems Change in gems (can be positive or negative)
     * @param energy Change in energy (can be positive or negative)
     * @param exp Change in experience points
     * @return true if successful, false if player not found or invalid update
     */
    public static boolean updatePlayerStats(String playerId, int gems, int energy, int exp) {
        Player player = getPlayer(playerId);
        
        // Update gems
        if (gems != 0) {
            player.addGems(gems);
            System.out.println("Updated player " + playerId + "'s gems by " + gems + 
                             " (new total: " + player.getGems() + ")");
        }
        
        // Update energy
        if (energy != 0) {
            player.addEnergy(energy);
            System.out.println("Updated player " + playerId + "'s energy by " + energy + 
                             " (new total: " + player.getCurrentEnergy() + ")");
        }
        
        // Update exp (would need to add method to Player class)
        // Here you would add experience and handle potential level ups
        
        // Save changes to file
        savePlayerStats();
        return true;
    }
    
    /**
     * Updates player's equipped character
     * Automatically saves stats to data file
     * 
     * @param playerId ID of the player
     * @param characterId ID of the character to equip
     * @return true if successful, false if player not found or player doesn't own character
     */
    public static boolean equipPlayerCharacter(String playerId, String characterId) {
        Player player = getPlayer(playerId);
        
        // Check if player owns this character
        boolean ownsCharacter = player.getCharacters().stream()
            .anyMatch(oc -> oc.getCharacter().getId().equals(characterId));
            
        if (!ownsCharacter) {
            System.out.println("Player " + playerId + " does not own character " + characterId);
            return false;
        }
        
        player.equipCharacter(characterId);
        System.out.println("Player " + playerId + " equipped character " + characterId);
        
        // Save changes to file
        savePlayerStats();
        return true;
    }

    public static List<Material> getMaterialsByRarity(String rarity) {
        return materials.values().stream()
            .filter(m -> m.getRarity().equalsIgnoreCase(rarity))
            .collect(Collectors.toList());
    }
    
    /**
     * Save player stats to file
     * This should be called whenever player stats change (level up, gems spent, energy change, etc.)
     */
    public static void savePlayerStats() {
        ensureDataFilesExist();
        
        // Verify the file exists and is writable
        File statFile = new File(foundPlayerStatPath);
        if (!statFile.exists()) {
            try {
                statFile.createNewFile();
                System.out.println("Created player stat file: " + foundPlayerStatPath);
            } catch (IOException e) {
                System.err.println("Failed to create player stat file: " + e.getMessage());
                return;
            }
        }
        
        if (!statFile.canWrite()) {
            System.err.println("Cannot write to player stat file: " + foundPlayerStatPath);
            return;
        }
        
        try {
            // First write to a temporary file to avoid data corruption
            File tempFile = new File(foundPlayerStatPath + ".tmp");
            try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
                for (Player player : players.values()) {
                    writer.println(String.format("%s#%d#%d#%d#%d#%s#%d",
                        player.getPlayerId(),
                        player.getLevel(),
                        player.getGems(),
                        player.getCurrentExp(),
                        player.getCurrentEnergy(),
                        player.getEquippedCharacterId(),
                        player.getTrophies()
                    ));
                }
                writer.flush();
            }
            
            // Then rename the temp file to the actual file
            if (tempFile.exists()) {
                if (statFile.exists() && !statFile.delete()) {
                    System.err.println("Could not delete old stat file before replacing it");
                    return;
                }
                
                if (tempFile.renameTo(statFile)) {
                    System.out.println("Successfully saved player stats to " + foundPlayerStatPath);
                } else {
                    // If rename fails, try copying content directly
                    try (BufferedReader reader = new BufferedReader(new FileReader(tempFile));
                         PrintWriter writer = new PrintWriter(new FileWriter(statFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.println(line);
                        }
                        writer.flush();
                        System.out.println("Successfully copied player stats data to " + foundPlayerStatPath);
                    } catch (IOException e) {
                        System.err.println("Error copying player stat data: " + e.getMessage());
                    }
                    tempFile.delete();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving player stats: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void savePlayerInventories() {
        ensureDataFilesExist();
        
        // Log players data for debugging
        System.out.println("Saving inventories for " + players.size() + " players");
        for (Player p : players.values()) {
            System.out.println("Player " + p.getPlayerId() + ": " + 
                              p.getMaterials().size() + " materials, " + 
                              p.getCharacters().size() + " characters");
        }
        
        // Verify the file exists and is writable
        File inventoryFile = new File(foundPlayerInventoryPath);
        if (!inventoryFile.exists()) {
            try {
                inventoryFile.createNewFile();
                System.out.println("Created player inventory file: " + foundPlayerInventoryPath);
            } catch (IOException e) {
                System.err.println("Failed to create player inventory file: " + e.getMessage());
                e.printStackTrace();
                createAlternativeInventoryFile();
                return;
            }
        }
        
        if (!inventoryFile.canWrite()) {
            System.err.println("Cannot write to player inventory file: " + foundPlayerInventoryPath);
            createAlternativeInventoryFile();
            return;
        }
        
        try {
            // First write to a temporary file to avoid data corruption
            File tempFile = new File(foundPlayerInventoryPath + ".tmp");
            boolean dataWritten = false;
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
                for (Player player : players.values()) {
                    String playerId = player.getPlayerId();
                    
                    // Save materials first
                    for (Map.Entry<String, Integer> entry : player.getMaterials().entrySet()) {
                        if (entry.getValue() > 0) {
                            writer.println(String.format("%s#%s#%d", 
                                playerId, entry.getKey(), entry.getValue()));
                            dataWritten = true;
                        }
                    }
                    
                    // Then save characters
                    for (OwnedCharacter ownedChar : player.getCharacters()) {
                        Character character = ownedChar.getCharacter();
                        if (character != null && character.getId() != null) {
                            writer.println(String.format("%s#%s#%d",
                                playerId, character.getId(), ownedChar.getLevel()));
                            dataWritten = true;
                        }
                    }
                }
                writer.flush();
            }
            
            // Check if temp file has data
            if (tempFile.length() == 0) {
                System.out.println("Warning: Temp file is empty after writing. No inventory data written.");
                if (!dataWritten) {
                    System.out.println("No inventory data available to write.");
                }
                tempFile.delete();
                return;
            } else {
                System.out.println("Temp file size: " + tempFile.length() + " bytes");
            }
            
            // Then copy the temp file to the actual file (more reliable than rename)
            try (BufferedReader reader = new BufferedReader(new FileReader(tempFile));
                 PrintWriter writer = new PrintWriter(new FileWriter(inventoryFile, false))) { // false to overwrite
                String line;
                int lineCount = 0;
                while ((line = reader.readLine()) != null) {
                    writer.println(line);
                    lineCount++;
                }
                writer.flush();
                System.out.println("Successfully copied " + lineCount + " inventory records to " + foundPlayerInventoryPath);
            } catch (IOException e) {
                System.err.println("Error copying player inventory data: " + e.getMessage());
                e.printStackTrace();
                createAlternativeInventoryFile();
            }
            
            // Clean up temp file
            if (tempFile.exists()) {
                tempFile.delete();
            }
        } catch (IOException e) {
            System.err.println("Error saving player inventories: " + e.getMessage());
            e.printStackTrace();
            createAlternativeInventoryFile();
        }
    }
    
    private static void createAlternativeInventoryFile() {
        System.out.println("Attempting to create alternative inventory file in current directory...");
        
        try {
            File altFile = new File("playerinventorydata_backup.txt");
            if (altFile.exists() && !altFile.canWrite()) {
                System.err.println("Cannot write to alternative inventory file either.");
                return;
            }
            
            boolean dataWritten = false;
            try (PrintWriter writer = new PrintWriter(new FileWriter(altFile))) {
                for (Player player : players.values()) {
                    String playerId = player.getPlayerId();
                    
                    // Save materials
                    for (Map.Entry<String, Integer> entry : player.getMaterials().entrySet()) {
                        if (entry.getValue() > 0) {
                            writer.println(String.format("%s#%s#%d", 
                                playerId, entry.getKey(), entry.getValue()));
                            dataWritten = true;
                        }
                    }
                    
                    // Save characters
                    for (OwnedCharacter ownedChar : player.getCharacters()) {
                        Character character = ownedChar.getCharacter();
                        if (character != null && character.getId() != null) {
                            writer.println(String.format("%s#%s#%d",
                                playerId, character.getId(), ownedChar.getLevel()));
                            dataWritten = true;
                        }
                    }
                }
                writer.flush();
            }
            
            if (!dataWritten) {
                System.out.println("Warning: No inventory data was written to alternative file.");
            }
            
            // Check if file has data
            if (altFile.length() > 0) {
                System.out.println("Successfully created alternative inventory file: " + altFile.getAbsolutePath());
                System.out.println("Alternative file size: " + altFile.length() + " bytes");
                System.out.println("Please copy this file to your desired location and rename it to playerinventorydata.txt");
                
                // Update the path to use this new file from now on
                foundPlayerInventoryPath = altFile.getAbsolutePath();
            } else {
                System.out.println("Warning: Alternative inventory file is empty. Check if players have inventory data.");
            }
        } catch (IOException e) {
            System.err.println("Failed to create alternative inventory file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static boolean createNewInventoryFile(String filename) {
        try {
            File newFile = new File(filename);
            if (newFile.exists()) {
                System.out.println("File already exists, will overwrite: " + filename);
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(newFile))) {
                for (Player player : players.values()) {
                    String playerId = player.getPlayerId();
                    
                    // Save materials
                    for (Map.Entry<String, Integer> entry : player.getMaterials().entrySet()) {
                        if (entry.getValue() > 0) {
                            writer.println(String.format("%s#%s#%d", 
                                playerId, entry.getKey(), entry.getValue()));
                        }
                    }
                    
                    // Save characters
                    for (OwnedCharacter ownedChar : player.getCharacters()) {
                        Character character = ownedChar.getCharacter();
                        if (character != null && character.getId() != null) {
                            writer.println(String.format("%s#%s#%d",
                                playerId, character.getId(), ownedChar.getLevel()));
                        }
                    }
                }
                writer.flush();
            }
            
            System.out.println("Successfully created new inventory file: " + newFile.getAbsolutePath());
            // Update the path to use this new file from now on
            foundPlayerInventoryPath = newFile.getAbsolutePath();
            return true;
        } catch (IOException e) {
            System.err.println("Failed to create new inventory file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}   