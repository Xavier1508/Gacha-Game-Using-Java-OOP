package manager;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import model.Character;
import model.Material;
import model.OwnedCharacter;
import model.Player;
import model.Property;
import manager.AccountManager;
import main.UserInterface;

public class GameManager {
    private Map<String, char[][]> maps;
    private int playerX, playerY;
     
    public GameManager() {
        this.maps = new HashMap<>();
        loadMaps();
    }
    
    private void fixMapExits() {
        // Pastikan setiap peta memiliki pintu keluar yang tepat
        for (String mapKey : maps.keySet()) {
            char[][] mapData = maps.get(mapKey);
            if (mapData == null) continue;
            
            int width = mapData[0].length;
            int height = mapData.length;
            
            // Periksa apakah peta memiliki pintu keluar yang sesuai
            boolean hasLeftExit = false;
            boolean hasRightExit = false;
            boolean hasTopExit = false;
            boolean hasBottomExit = false;
            
            // Cek pintu keluar di tepi kiri
            for (int y = 0; y < height; y++) {
                if (mapData[y][0] != '#') {
                    hasLeftExit = true;
                    break;
                }
            }
            
            // Cek pintu keluar di tepi kanan
            for (int y = 0; y < height; y++) {
                if (mapData[y][width-1] != '#') {
                    hasRightExit = true;
                    break;
                }
            }
            
            // Cek pintu keluar di tepi atas
            for (int x = 0; x < width; x++) {
                if (mapData[0][x] != '#') {
                    hasTopExit = true;
                    break;
                }
            }
            
            // Cek pintu keluar di tepi bawah
            for (int x = 0; x < width; x++) {
                if (mapData[height-1][x] != '#') {
                    hasBottomExit = true;
                    break;
                }
            }
            
            // Buat pintu keluar jika tidak ada
            if (mapKey.equals("spawn")) {
                // Spawn harus memiliki 4 pintu keluar
                if (!hasLeftExit) mapData[height/2][0] = ' ';  // Keluar ke gacha
                if (!hasRightExit) mapData[height/2][width-1] = ' ';  // Keluar ke trade
                if (!hasTopExit) mapData[0][width/2] = ' ';  // Keluar ke home
                if (!hasBottomExit) mapData[height-1][width/2] = ' ';  // Keluar ke shop
            } else if (mapKey.equals("gacha")) {
                // Gacha harus memiliki pintu keluar ke kanan
                if (!hasRightExit) mapData[height/2][width-1] = ' ';
            } else if (mapKey.equals("shop")) {
                // Shop harus memiliki pintu keluar ke atas
                if (!hasTopExit) mapData[0][width/2] = ' ';
            } else if (mapKey.equals("trade")) {
                // Trade harus memiliki pintu keluar ke kiri
                if (!hasLeftExit) mapData[height/2][0] = ' ';
            } else if (mapKey.equals("home")) {
                // Home harus memiliki pintu keluar ke bawah
                if (!hasBottomExit) mapData[height-1][width/2] = ' ';
            }
            
            // Simpan kembali peta yang telah diperbaiki
            maps.put(mapKey, mapData);
        }
    }

    /**
     * Metode untuk mencari posisi yang aman (bukan tembok) di peta
     * @param map Peta yang diperiksa
     * @return int[] berisi {x, y} posisi yang aman
     */
    private int[] findSafePosition(char[][] map) {
        // Cari posisi yang aman di tengah peta
        for (int y = 1; y < map.length-1; y++) {
            for (int x = 1; x < map[0].length-1; x++) {
                if (map[y][x] == ' ') {
                    return new int[]{x, y};
                }
            }
        }
        
        // Jika tidak ada posisi aman di tengah, coba cari di seluruh peta
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                if (map[y][x] == ' ') {
                    return new int[]{x, y};
                }
            }
        }
        
        // Default jika tidak ada posisi kosong
        int midX = map[0].length / 2;
        int midY = map.length / 2;
        map[midY][midX] = ' ';  // Paksa area tengah jadi kosong
        return new int[]{midX, midY};
    }
    
    private void loadMaps() {
        // Load all required maps dengan nama file yang tepat
        boolean spawnLoaded = loadMap("spawnMap.txt", "spawn");
        loadMap("shopMap.txt", "shop");
        loadMap("gachaMap.txt", "gacha");
        loadMap("tradeMap.txt", "trade");
        loadMap("homeMap.txt", "home");
        
        // If spawn map failed to load, use a default map to prevent null
        if (!spawnLoaded) {
            char[][] defaultMap = createDefaultMap();
            maps.put("spawn", defaultMap);
            playerX = 8; // Sesuai dengan posisi 'P' pada defaultMap
            playerY = 4;
        }
        
        // Pastikan semua peta memiliki pintu keluar yang sesuai
        fixMapExits();
        
        // Hapus karakter P duplikat jika ada
        for (String mapKey : maps.keySet()) {
            if (!mapKey.equals("spawn")) { // Skip spawn map karena player mulai di sana
                char[][] mapData = maps.get(mapKey);
                if (mapData != null) {
                    // Hapus semua karakter P dari peta yang bukan spawn
                    for (int y = 0; y < mapData.length; y++) {
                        for (int x = 0; x < mapData[y].length; x++) {
                            if (mapData[y][x] == 'P') {
                                mapData[y][x] = ' ';
                            }
                        }
                    }
                    maps.put(mapKey, mapData);
                }
            }
        }
    }
    
    private boolean loadMap(String filename, String mapKey) {
        try {
            // Coba beberapa lokasi file yang mungkin
            String[] possiblePaths = {
                filename,           // Root direktori
                "src/" + filename,  // src folder
                "../" + filename    // Parent direktori
            };
            
            BufferedReader reader = null;
            String filePath = "";
            
            // Coba semua kemungkinan lokasi file
            for (String path : possiblePaths) {
                try {
                    reader = new BufferedReader(new FileReader(path));
                    filePath = path;
                    break;
                } catch (IOException e) {
                    // Coba lokasi lain
                }
            }
            
            // Jika file tidak ditemukan di semua lokasi
            if (reader == null) {
                System.err.println("Error: Map file " + filename + " tidak ditemukan di semua lokasi yang dicoba.");
                return false;
            }
            
            List<String> lines = new ArrayList<>();
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) { // Skip empty lines
                    lines.add(line);
                }
            }
            reader.close();
            
            if (lines.isEmpty()) {
                System.err.println("Error: Map file " + filePath + " is empty.");
                return false;
            }
            
            int rows = lines.size();
            int cols = lines.stream().mapToInt(String::length).max().orElse(0);
            
            if (cols == 0) {
                System.err.println("Error: Map file " + filePath + " has no valid columns.");
                return false;
            }
            
            char[][] map = new char[rows][cols];
            
            for (int i = 0; i < rows; i++) {
                char[] rowChars = lines.get(i).toCharArray();
                for (int j = 0; j < rowChars.length; j++) {
                    map[i][j] = rowChars[j];
                    
                    if (mapKey.equals("spawn") && rowChars[j] == 'P') {
                        playerX = j;
                        playerY = i;
                    }
                }
                for (int j = rowChars.length; j < cols; j++) {
                    map[i][j] = ' ';
                }
            }
            
            maps.put(mapKey, map);
            return true;
        } catch (IOException e) {
            System.err.println("Error loading map " + filename + ": " + e.getMessage());
            return false;
        }
    }
    
    private char[][] createDefaultMap() {
        // Create a simple 10x10 default map with a player position
        char[][] defaultMap = new char[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (i == 0 || i == 9 || j == 0 || j == 9) {
                    defaultMap[i][j] = '#';
                } else {
                    defaultMap[i][j] = ' ';
                }
            }
        }
        defaultMap[4][8] = 'P'; // Place player near right edge
        defaultMap[2][2] = 'L'; // Add leaderboard for testing
        defaultMap[7][7] = '+'; // Add gameplay entry point
        return defaultMap;
    }
    
    public void startGame(Player player, UserInterface ui) {
        String currentMap = "spawn";
        char[][] map = maps.get(currentMap);
        if (map == null) {
            System.err.println("Fatal error: Spawn map is null. Exiting game.");
            return;
        }
        boolean exit = false;
        
        while (!exit) {
            ui.clearScreen();
            ui.displayPlayerInfo(player);
            ui.displayMap(map);
            System.out.println("\n============================");
            System.out.println("|          Controls        |");
            System.out.println("|  W/A/S/D -> Move         |");
            System.out.println("|  I       -> Inventory    |");
            System.out.println("|  Q       -> Quit         |");
            System.out.println("============================");
            
            String input = ui.getInput().toUpperCase();
            
            if (input.equals("Q")) {
                exit = true;
            } else if (input.equals("I")) {
                handleInventory(player, ui);
            } else if (input.equals("W") || input.equals("A") || input.equals("S") || input.equals("D")) {
                int newX = playerX;
                int newY = playerY;
                
                if (input.equals("W")) newY--;
                else if (input.equals("A")) newX--;
                else if (input.equals("S")) newY++;
                else if (input.equals("D")) newX++;
                
                // Cek apakah pemain mencoba pindah ke luar batas peta
                boolean outOfBounds = newX < 0 || newY < 0 || 
                                      newY >= map.length || 
                                      (newX >= map[0].length);
                
                if (outOfBounds) {
                    // Jika di luar batas, cek transisi ke peta lain
                    String newMap = checkMapTransition(currentMap, playerX, playerY, newX, newY);
                    
                    if (!newMap.equals(currentMap)) {
                        // Hapus pemain dari peta lama
                        map[playerY][playerX] = ' ';
                        
                        // Ganti peta saat ini
                        currentMap = newMap;
                        map = maps.get(currentMap);
                        
                        if (map == null) {
                            currentMap = "spawn";
                            map = maps.get(currentMap);
                        }
                        
                        // Cari jalur masuk yang tepat (area yang tidak ada tembok '#')
                        if (currentMap.equals("spawn")) {
                            if (input.equals("A")) {  // Datang dari gacha (masuk dari kanan spawn)
                                // Cari jalur masuk di sisi kanan spawn map
                                for (int y = 0; y < map.length; y++) {
                                    if (y < map.length && map[y][map[0].length-1] != '#') {
                                        playerX = map[0].length - 2; // Posisi tepat sebelum tepi kanan
                                        playerY = y;
                                        break;
                                    }
                                }
                            } else if (input.equals("D")) {  // Datang dari trade (masuk dari kiri spawn)
                                // Cari jalur masuk di sisi kiri spawn map
                                for (int y = 0; y < map.length; y++) {
                                    if (y < map.length && map[y][0] != '#') {
                                        playerX = 1; // Posisi tepat setelah tepi kiri
                                        playerY = y;
                                        break;
                                    }
                                }
                            } else if (input.equals("W")) {  // Datang dari home (masuk dari bawah spawn)
                                // Cari jalur masuk di sisi bawah spawn map
                                for (int x = 0; x < map[0].length; x++) {
                                    if (x < map[0].length && map[map.length-1][x] != '#') {
                                        playerX = x;
                                        playerY = map.length - 2; // Posisi tepat sebelum tepi bawah
                                        break;
                                    }
                                }
                            } else if (input.equals("S")) {  // Datang dari shop (masuk dari atas spawn)
                                // Cari jalur masuk di sisi atas spawn map
                                for (int x = 0; x < map[0].length; x++) {
                                    if (x < map[0].length && map[0][x] != '#') {
                                        playerX = x;
                                        playerY = 1; // Posisi tepat setelah tepi atas
                                        break;
                                    }
                                }
                            }
                        } else if (currentMap.equals("gacha")) {
                            // Cari jalur masuk di sisi kanan gacha map
                            for (int y = 0; y < map.length; y++) {
                                if (y < map.length && map[y][map[0].length-1] != '#') {
                                    playerX = map[0].length - 2;
                                    playerY = y;
                                    break;
                                }
                            }
                        } else if (currentMap.equals("shop")) {
                            // Cari jalur masuk di sisi atas shop map
                            for (int x = 0; x < map[0].length; x++) {
                                if (x < map[0].length && map[0][x] != '#') {
                                    playerX = x;
                                    playerY = 1;
                                    break;
                                }
                            }
                        } else if (currentMap.equals("trade")) {
                            // Cari jalur masuk di sisi kiri trade map
                            for (int y = 0; y < map.length; y++) {
                                if (y < map.length && map[y][0] != '#') {
                                    playerX = 1;
                                    playerY = y;
                                    break;
                                }
                            }
                        } else if (currentMap.equals("home")) {
                            // Cari jalur masuk di sisi bawah home map
                            for (int x = 0; x < map[0].length; x++) {
                                if (x < map[0].length && map[map.length-1][x] != '#') {
                                    playerX = x;
                                    playerY = map.length - 2;
                                    break;
                                }
                            }
                        }
                        
                        // Jika tidak menemukan jalur masuk yang tepat, gunakan posisi default yang aman
                        if (playerX <= 0 || playerY <= 0 || playerY >= map.length-1 || playerX >= map[0].length-1) {
                            // Cari posisi aman di tengah peta
                            for (int y = 1; y < map.length-1; y++) {
                                for (int x = 1; x < map[0].length-1; x++) {
                                    if (map[y][x] == ' ') {
                                        playerX = x;
                                        playerY = y;
                                        y = map.length; // Keluar dari loop
                                        break;
                                    }
                                }
                            }
                        }
                        
                        // Pastikan tidak ada karakter 'P' lain di peta
                        for (int y = 0; y < map.length; y++) {
                            for (int x = 0; x < map[0].length; x++) {
                                if (map[y][x] == 'P') {
                                    map[y][x] = ' ';
                                }
                            }
                        }
                        
                        // Tempatkan pemain di peta baru
                        map[playerY][playerX] = 'P';
                    }
                } else {
                    // Pemain bergerak dalam batas peta
                    char targetCell = map[newY][newX];
                    
                    if (targetCell == ' ' || targetCell == 'P') {
                        // Gerakkan pemain jika tujuan kosong
                        map[playerY][playerX] = ' ';
                        playerX = newX;
                        playerY = newY;
                        map[playerY][playerX] = 'P';
                    } else if (targetCell == 'L') {
                        handleLeaderboard(player, ui);
                    } else if (targetCell == 'G') {
                        handleGacha(player, ui);
                    } else if (targetCell == 'S') {
                        handleShop(player, ui);
                    } else if (targetCell == 'T') {
                        handleTrade(player, ui);
                    } else if (targetCell == '+') {
                        handleGameplay(player, ui);
                    }
                }
            }
        }
    }
    

private String checkMapTransition(String currentMap, int x, int y, int newX, int newY) {
    // Buat variabel untuk menampung dimensi peta saat ini
    char[][] currentMapData = maps.get(currentMap);
    int currentMapWidth = currentMapData[0].length;
    int currentMapHeight = currentMapData.length;
    
    // Cek apakah posisi saat ini ada di dekat "pintu" (area tanpa tembok di tepi)
    boolean nearLeftExit = (x == 0 || x == 1) && currentMapData[y][0] != '#';
    boolean nearRightExit = (x == currentMapWidth-1 || x == currentMapWidth-2) && 
                           currentMapData[y][currentMapWidth-1] != '#';
    boolean nearTopExit = (y == 0 || y == 1) && currentMapData[0][x] != '#';
    boolean nearBottomExit = (y == currentMapHeight-1 || y == currentMapHeight-2) && 
                            currentMapData[currentMapHeight-1][x] != '#';
    
    // Check map transition berdasarkan batas-batas peta dan keberadaan pintu
    if (currentMap.equals("spawn")) {
        if (newX < 0 && nearLeftExit) { // Keluar dari sisi kiri melalui pintu
            return "gacha";
        }
        if (newX >= currentMapWidth && nearRightExit) { // Keluar dari sisi kanan melalui pintu
            return "trade";
        }
        if (newY >= currentMapHeight && nearBottomExit) { // Keluar dari sisi bawah melalui pintu
            return "shop";
        }
        if (newY < 0 && nearTopExit) { // Keluar dari sisi atas melalui pintu
            return "home";
        }
    } else if (currentMap.equals("gacha")) {
        if (newX >= currentMapWidth && nearRightExit) { // Keluar dari sisi kanan melalui pintu
            return "spawn";
        }
    } else if (currentMap.equals("shop")) {
        if (newY < 0 && nearTopExit) { // Keluar dari sisi atas melalui pintu
            return "spawn";
        }
    } else if (currentMap.equals("trade")) {
        if (newX < 0 && nearLeftExit) { // Keluar dari sisi kiri melalui pintu
            return "spawn";
        }
    } else if (currentMap.equals("home")) {
        if (newY >= currentMapHeight && nearBottomExit) { // Keluar dari sisi bawah melalui pintu
            return "spawn";
        }
    }
    
    // Jika tidak ada transisi yang terjadi, kembalikan map saat ini
    return currentMap;
}
    
	private void handleInventory(Player player, UserInterface ui) {
	    boolean back = false;
	    
	    while (!back) {
	        ui.clearScreen();
	        System.out.println("===== INVENTORY =====");
	        System.out.println("What do you want to see?");
	        System.out.println("1. Owned Materials");
	        System.out.println("2. Owned Characters");
	        System.out.println("3. Back");
	        
	        System.out.print(">> ");
	        String choice = ui.getInput();
	        
	        switch (choice) {
	            case "1":
	                handleOwnedMaterials(player, ui);
	                break;
	            case "2":
	                handleOwnedCharacters(player, ui);
	                break;
	            case "3":
	                back = true;
	                break;
	            default:
	                System.out.println("Invalid choice. Please choose between 1 and 3.");
	                System.out.println("Press Enter to continue...");
	                ui.getInput();
	        }
	    }
	}
	
	private void handleOwnedMaterials(Player player, UserInterface ui) {
	    Map<String, Integer> materials = player.getMaterials();
	    List<Map.Entry<String, Integer>> materialList = new ArrayList<>(materials.entrySet());
	    
	    int page = 1;
	    int itemsPerPage = 10;
	    int totalPages = (int) Math.ceil((double) materialList.size() / itemsPerPage);
	    
	    if (totalPages == 0) totalPages = 1;
	    
	    boolean back = false;
	    
	    while (!back) {
	        ui.clearScreen();
	        System.out.println("===== Material List =====");
	        System.out.printf("Your Materials (Page %d of %d):\n", page, totalPages);
	        System.out.println("-----------------------------------------------------------------------");
	        System.out.printf("| %-5s | %-20s | %-10s | %-10s | %-15s |\n", "No", "Name", "Rarity", "Amount", "For Character");
	        System.out.println("-----------------------------------------------------------------------");
	        
	        int start = (page - 1) * itemsPerPage;
	        int end = Math.min(start + itemsPerPage, materialList.size());
	        
	        if (materialList.isEmpty()) {
	            System.out.println("| You don't have any materials yet.                                   |");
	        } else {
	            Map<String, Material> materialsData = DataManager.getMaterials();
	            for (int i = start; i < end; i++) {
	                Map.Entry<String, Integer> entry = materialList.get(i);
	                Material mat = materialsData.get(entry.getKey());
	                if (mat != null) {
	                    System.out.printf("| %-5d | %-20s | %-10s | %-10d | %-15s |\n", 
	                        i + 1, mat.getName(), mat.getRarity(), entry.getValue(), mat.getForCharacter());
	                } else {
	                    System.out.printf("| %-5d | %-20s | %-10s | %-10d | %-15s |\n", 
	                        i + 1, entry.getKey(), "Unknown", entry.getValue(), "Unknown");
	                }
	            }
	        }
	        System.out.println("-----------------------------------------------------------------------");
	        
	        System.out.println("\n1. Previous Page");
	        System.out.println("2. Next Page");
	        System.out.println("3. Sort");
	        System.out.println("4. Back to Inventory");
	        
	        System.out.print(">> ");
	        String choice = ui.getInput();
	        
	        switch (choice) {
	            case "1":
	                if (page > 1) {
	                    page--;
	                } else {
	                    System.out.println("You are already on the first page.");
	                    System.out.println("Press Enter to continue...");
	                    ui.getInput();
	                }
	                break;
	            case "2":
	                if (page < totalPages) {
	                    page++;
	                } else {
	                    System.out.println("You are already on the last page.");
	                    System.out.println("Press Enter to continue...");
	                    ui.getInput();
	                }
	                break;
	            case "3":
	                handleSortMaterials(materialList, ui);
	                break;
	            case "4":
	                back = true;
	                break;
	            default:
	                System.out.println("Invalid choice. Please choose between 1 and 4.");
	                System.out.println("Press Enter to continue...");
	                ui.getInput();
	        }
	    }
	}
	
	private void handleSortMaterials(List<Map.Entry<String, Integer>> materialList, UserInterface ui) {
	    ui.clearScreen();
	    System.out.println("===== Pick a sorting option =====");
	    System.out.println("1. Name");
	    System.out.println("2. Rarity");
	    System.out.println("3. Amount");
	    System.out.println("4. For Character");
	    System.out.println("5. Back");
	    
	    System.out.print(">> ");
	    String choice = ui.getInput();
	    
	    if (choice.equals("5")) {
	        return;
	    }
	    
	    if (choice.equals("1") || choice.equals("2") || choice.equals("3") || choice.equals("4")) {
	        ui.clearScreen();
	        System.out.println("===== Pick a sorting option =====");
	        System.out.println("1. Ascending");
	        System.out.println("2. Descending");
	        System.out.println("3. Back");
	        
	        System.out.print(">> ");
	        String order = ui.getInput();
	        
	        if (order.equals("3")) {
	            return;
	        }
	        
	        Comparator<Map.Entry<String, Integer>> comparator = null;
	        Map<String, Material> materialsData = DataManager.getMaterials();
	        
	        switch (choice) {
	            case "1":
	                comparator = Comparator.comparing(e -> {
	                    Material mat = materialsData.get(e.getKey());
	                    return mat != null ? mat.getName() : e.getKey();
	                });
	                break;
	            case "2":
	                comparator = Comparator.comparing(e -> {
	                    Material mat = materialsData.get(e.getKey());
	                    return mat != null ? mat.getRarity() : "Unknown";
	                });
	                break;
	            case "3":
	                comparator = Comparator.comparing(Map.Entry::getValue);
	                break;
	            case "4":
	                comparator = Comparator.comparing(e -> {
	                    Material mat = materialsData.get(e.getKey());
	                    return mat != null ? mat.getForCharacter() : "Unknown";
	                });
	                break;
	        }
	        
	        if (order.equals("2")) {
	            comparator = comparator.reversed();
	        }
	        
	        materialList.sort(comparator);
	    } else {
	        System.out.println("Invalid choice.");
	        System.out.println("Press Enter to continue...");
	        ui.getInput();
	    }
	}
	
	private void handleOwnedCharacters(Player player, UserInterface ui) {
	    List<OwnedCharacter> chars = player.getCharacters();
	    boolean back = false;

	    while (!back) {
	        ui.clearScreen();
	        System.out.println("===== Owned Characters =====");
	        if (chars.isEmpty()) {
	            System.out.println("You don't have any characters yet.");
	        } else {
	            System.out.println("-----------------------------------------------------------------------");
	            System.out.printf("| %-5s | %-20s | %-6s | %-10s | %-15s |\n", "No", "Name", "Level", "Status", "Skill");
	            System.out.println("-----------------------------------------------------------------------");
	            
	            for (int i = 0; i < chars.size(); i++) {
	                OwnedCharacter oc = chars.get(i);
	                Character c = oc.getCharacter();
	                String equipped = player.getEquippedCharacterId().equals(c.getId()) ? "Equipped" : "-";
	                System.out.printf("| %-5d | %-20s | %-6d | %-10s | %-15s |\n", 
	                    i + 1, c.getName(), oc.getLevel(), equipped, c.getSkillDesc());
	            }
	            System.out.println("-----------------------------------------------------------------------");
	        }

	        System.out.println("\n1. Equip Character");
	        System.out.println("2. Back to Inventory");
	        System.out.print(">> ");
	        String choice = ui.getInput();

	        switch (choice) {
	            case "1":
	                if (chars.isEmpty()) {
	                    System.out.println("No characters to equip.");
	                } else {
	                    System.out.print("Enter character number to equip (0 to cancel): ");
	                    String input = ui.getInput();
	                    if (input.equals("0")) break;
	                    try {
	                        int index = Integer.parseInt(input) - 1;
	                        if (index >= 0 && index < chars.size()) {
	                            OwnedCharacter selected = chars.get(index);
	                            player.equipCharacter(selected.getCharacter().getId());
	                            System.out.println("Equipped " + selected.getCharacter().getName());
	                        } else {
	                            System.out.println("Invalid character number.");
	                        }
	                    } catch (NumberFormatException e) {
	                        System.out.println("Invalid input.");
	                    }
	                }
	                System.out.println("Press Enter to continue...");
	                ui.getInput();
	                break;
	            case "2":
	                back = true;
	                break;
	            default:
	                System.out.println("Invalid choice.");
	                System.out.println("Press Enter to continue...");
	                ui.getInput();
	        }
	    }
	}

	private void handleLeaderboard(Player player, UserInterface ui) {
	    List<Player> sortedPlayers = AccountManager.getPlayersSortedByTrophies();
	    int page = 1;
	    int itemsPerPage = 10;
	    int totalPages = (int) Math.ceil((double) sortedPlayers.size() / itemsPerPage);
	    if (totalPages == 0) totalPages = 1;

	    boolean back = false;
	    while (!back) {
	        ui.clearScreen();
	        System.out.println("===========================");
	        System.out.println("||      Leaderboard      ||");
	        System.out.println("===========================");
	        System.out.printf("Leaderboard (Page %d of %d):\n", page, totalPages);
	        System.out.println("=======================================================");
	        System.out.printf("| %-5s | %-20s | %-10s |\n", "Rank", "Name", "Trophies");
	        System.out.println("|-------|----------------------|------------|");

	        int start = (page - 1) * itemsPerPage;
	        int end = Math.min(start + itemsPerPage, sortedPlayers.size());
	        for (int i = start; i < end; i++) {
	            Player p = sortedPlayers.get(i);
	            System.out.printf("| %-5d | %-20s | %-10d |\n", i + 1, p.getUsername(), p.getTrophies());
	        }
	        System.out.println("=======================================================");

	        System.out.println("1. Previous Page");
	        System.out.println("2. Next Page");
	        System.out.println("3. Back");
	        System.out.print(">> ");
	        String choice = ui.getInput();

	        switch (choice) {
	            case "1":
	                if (page > 1) page--;
	                else {
	                    System.out.println("You are already on the first page.");
	                    System.out.println("Press Enter to continue...");
	                    ui.getInput();
	                }
	                break;
	            case "2":
	                if (page < totalPages) page++;
	                else {
	                    System.out.println("You are already on the last page.");
	                    System.out.println("Press Enter to continue...");
	                    ui.getInput();
	                }
	                break;
	            case "3":
	                back = true;
	                break;
	            default:
	                System.out.println("Invalid choice.");
	                System.out.println("Press Enter to continue...");
	                ui.getInput();
	        }
	    }
	}

	private void handleGacha(Player player, UserInterface ui) {
	    boolean back = false;
	    Random random = new Random();

	    while (!back) {
	        ui.clearScreen();
	        System.out.println("===========================");
	        System.out.println("||   Character Gacha    ||");
	        System.out.println("===========================");
	        System.out.printf("%s lvl. %d | Gems: %d\n\n", player.getUsername(), player.getLevel(), player.getGems());
	        System.out.println("1. Roll (150 gems)");
	        System.out.println("2. Go Back");
	        System.out.print(">> ");
	        String choice = ui.getInput();

	        switch (choice) {
	            case "1":
	                if (player.getGems() < 150) {
	                    System.out.println("Not enough gems!");
	                    System.out.println("Press Enter to continue...");
	                    ui.getInput();
	                } else {
	                    player.addGems(-150);
	                    rollGacha(player, ui, random);
	                }
	                break;
	            case "2":
	                back = true;
	                break;
	            default:
	                System.out.println("Invalid choice.");
	                System.out.println("Press Enter to continue...");
	                ui.getInput();
	        }
	    }
	}

	private void rollGacha(Player player, UserInterface ui, Random random) {
	    // Define rarities and their probabilities
	    String[] rarities = {"COMMON", "RARE", "EPIC", "LEGENDARY"};
	    double[] probabilities = {0.79, 0.15, 0.05, 0.01};

	    // Generate 4 random materials based on probability
	    List<Material> results = new ArrayList<>();

	    for (int i = 0; i < 4; i++) {
	        double rand = random.nextDouble();
	        double cumulative = 0;
	        String selectedRarity = rarities[0]; // Default to common

	        for (int j = 0; j < rarities.length; j++) {
	            cumulative += probabilities[j];
	            if (rand <= cumulative) {
	                selectedRarity = rarities[j];
	                break;
	            }
	        }

	        // Get all materials of the selected rarity
	        List<Material> materialsOfRarity = DataManager.getMaterialsByRarity(selectedRarity);

	        if (!materialsOfRarity.isEmpty()) {
	            // Select a random material from the list
	            Material selected = materialsOfRarity.get(random.nextInt(materialsOfRarity.size()));
	            results.add(selected);
	        }
	    }

	    // Show revealing animation
	    ui.clearScreen();
	    System.out.println("Revealing card...");
	    System.out.println();

	    // Print hidden cards
	    printHiddenCards(results);

	    System.out.println("\nPress ENTER to reveal...");
	    ui.getInput();

	    // Reveal all cards
	    System.out.println("You got:");

	    // Display obtained materials
	    for (Material m : results) {
	        String colorCode = getRarityColorCode(m.getRarity());
	        System.out.printf("- %s%s\u001B[0m (%s) - for %s\n",
	                      colorCode, m.getName(), m.getRarity(), m.getForCharacter());

	        // Add to player's inventory
	        player.addMaterial(m.getId(), 1);
	    }

	    System.out.println("All materials added to inventory!");
	    
	    // Simpan inventory setelah gacha
	    DataManager.savePlayerInventories();
	    
	    System.out.println();
	    System.out.print("Press ENTER to continue...");
	    ui.getInput();
	}

	private void printHiddenCards(List<Material> cards) {
	    final int CARD_WIDTH = 15;
	    
	    // Top border
	    for (int i = 0; i < cards.size(); i++) {
	        String colorCode = getRarityColorCode(cards.get(i).getRarity());
	        System.out.print(colorCode + "+" + "-".repeat(CARD_WIDTH) + "+ \u001B[0m");
	        if (i < cards.size() - 1) System.out.print(" ");
	    }
	    System.out.println();
	    
	    // Empty space above letter
	    for (int i = 0; i < cards.size(); i++) {
	        String colorCode = getRarityColorCode(cards.get(i).getRarity());
	        System.out.print(colorCode + "|" + " ".repeat(CARD_WIDTH) + "| \u001B[0m");
	        if (i < cards.size() - 1) System.out.print(" ");
	    }
	    System.out.println();
	    
	    // Middle section with rarity letter
	    for (int i = 0; i < cards.size(); i++) {
	        String rarityLetter = getRarityLetter(cards.get(i).getRarity());
	        String colorCode = getRarityColorCode(cards.get(i).getRarity());
	        
	        // Center the letter in the card
	        int spacesBeforeLetter = (CARD_WIDTH - 1) / 2;
	        int spacesAfterLetter = CARD_WIDTH - 1 - spacesBeforeLetter;
	        
	        System.out.print(colorCode + "|" + " ".repeat(spacesBeforeLetter) + 
	                        rarityLetter + " ".repeat(spacesAfterLetter) + "| \u001B[0m");
	        if (i < cards.size() - 1) System.out.print(" ");
	    }
	    System.out.println();
	    
	    // Empty space below letter
	    for (int i = 0; i < cards.size(); i++) {
	        String colorCode = getRarityColorCode(cards.get(i).getRarity());
	        System.out.print(colorCode + "|" + " ".repeat(CARD_WIDTH) + "| \u001B[0m");
	        if (i < cards.size() - 1) System.out.print(" ");
	    }
	    System.out.println();
	    
	    // Bottom border
	    for (int i = 0; i < cards.size(); i++) {
	        String colorCode = getRarityColorCode(cards.get(i).getRarity());
	        System.out.print(colorCode + "+" + "-".repeat(CARD_WIDTH) + "+ \u001B[0m");
	        if (i < cards.size() - 1) System.out.print(" ");
	    }
	    System.out.println();
	}

	private String getRarityLetter(String rarity) {
	    switch (rarity.toUpperCase()) {
	        case "COMMON": return "C";
	        case "RARE": return "R";
	        case "EPIC": return "E";
	        case "LEGENDARY": return "L";
	        default: return "?";
	    }
	}

	private String getRarityColorCode(String rarity) {
	    switch (rarity.toUpperCase()) {
	        case "COMMON": return "\u001B[32m"; // Green
	        case "RARE": return "\u001B[36m";   // Cyan/Blue
	        case "EPIC": return "\u001B[35m";   // Purple
	        case "LEGENDARY": return "\u001B[33m"; // Yellow/Gold
	        default: return "\u001B[0m";        // Reset
	    }
	}


	private void handleShop(Player player, UserInterface ui) {
	    boolean back = false;
	
	    while (!back) {
	        ui.clearScreen();
	        // Display bordered shop title
	        System.out.println("┌───────────────────────────────────────┐");
	        System.out.println("│              Shop                     │");
	        System.out.println("└───────────────────────────────────────┘");
	        
	        // Display player info with colored username and separator line
	        System.out.printf("\033[32m%s\033[0m lvl. %d  |  Gems: \033[32m%d\033[0m\n", 
	            player.getUsername(), player.getLevel(), player.getGems());
	        System.out.println("═══════════════════════════════════════════════");
	        
	        // Shop menu options
	        System.out.println("1. Level Up Character");
	        System.out.println("2. Back");
	        System.out.print(">> ");
	        String choice = ui.getInput();
	
	        switch (choice) {
	            case "1":
	                handleLevelUpCharacter(player, ui);
	                break;
	            case "2":
	                back = true;
	                break;
	            default:
	                System.out.println("Invalid choice.");
	                System.out.println("Press ENTER to continue...");
	                ui.getInput();
	        }
	    }
	}

	private void handleLevelUpCharacter(Player player, UserInterface ui) {
	    ui.clearScreen();
	    System.out.println("Level Up Character");
	    System.out.println("───────────────────────────────────────────────────");
	    
	    // Display player info header
	    System.out.printf("\033[32m%s\033[0m lvl. %d  |  Gems: \033[32m%d\033[0m\n\n", 
	        player.getUsername(), player.getLevel(), player.getGems());
	    
	    List<OwnedCharacter> chars = player.getCharacters();
	    if (chars.isEmpty()) {
	        System.out.println("You don't have any characters to level up.");
	        System.out.println("Press ENTER to continue...");
	        ui.getInput();
	        return;
	    }
	    
	    // Display character table with headers
	    System.out.printf("%-4s %-25s %-7s %-15s %-15s %-35s %-20s\n",
	        "No.", "Name", "Level", "Title", "Skill Name", "Skill Desc", "Skill Chance");
	    System.out.println("───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
	    
	    // Display each character with their details
	    for (int i = 0; i < chars.size(); i++) {
	        OwnedCharacter oc = chars.get(i);
	        Character character = oc.getCharacter();
	        String equipped = "";
	        
	        // Check if this character is equipped
	        if (player.getEquippedCharacterId() != null && 
	            player.getEquippedCharacterId().equals(character.getId())) {
	            equipped = "(Equipped)";
	        }
	        
	        // Calculate skill chance based on character level
	        int skillChance = character.getSkillBaseChance();
	        
	        System.out.printf("%-4d %-25s %-7d %-15s %-15s %-35s %-3d%% + %d%%\n",
	            i + 1, 
	            character.getName() + " " + equipped, 
	            oc.getLevel(),
	            character.getTitle(),
	            character.getSkillName(),
	            character.getSkillDesc(),
	            skillChance, 
	            skillChance / 5);
	    }
	    
	    System.out.print("\nEnter the number of the character to level up (0 to go back): ");
	    String input = ui.getInput();
	    
	    if (input.equals("0")) return;
	    
	    try {
	        int index = Integer.parseInt(input) - 1;
	        if (index >= 0 && index < chars.size()) {
	            OwnedCharacter oc = chars.get(index);
	            int currentLevel = oc.getLevel();
	            int cost = 100 * currentLevel;
	            int newLevel = currentLevel + 1;
	            
	            // Display level up confirmation
	            ui.clearScreen();
	            System.out.println("Level Up Character");
	            System.out.println("───────────────────────────────────────────────────");
	            
	            System.out.printf("\033[32m%s\033[0m lvl. %d  |  Gems: \033[32m%d\033[0m\n\n", 
	                player.getUsername(), player.getLevel(), player.getGems());
	                
	            System.out.printf("Level up %s to level %d?\n", oc.getCharacter().getName(), newLevel);
	            System.out.printf("Cost: %d gems\n", cost);
	            System.out.print("Confirm (y/n): ");
	            
	            if (ui.getInput().equalsIgnoreCase("Y")) {
	                if (player.getGems() >= cost) {
	                    // Update player gems and character level
	                    player.addGems(-cost);
	                    oc.setLevel(newLevel);
	                    
	                    // Save changes to data files
	                    DataManager.savePlayerStats();
	                    DataManager.savePlayerInventories();
	                    
	                    System.out.println("Level up success!");
	                    System.out.printf("%s is now level %d\n", oc.getCharacter().getName(), newLevel);
	                } else {
	                    System.out.println("Not enough gems!");
	                }
	            } else {
	                System.out.println("Level up cancelled.");
	            }
	        } else {
	            System.out.println("Invalid character number.");
	        }
	    } catch (NumberFormatException e) {
	        System.out.println("Invalid input.");
	    }
	    
	    System.out.println("Press ENTER to continue...");
	    ui.getInput();
	}

    private void handleTrade(Player player, UserInterface ui) {
        boolean back = false;

        while (!back) {
            ui.clearScreen();
            System.out.println("===========================");
            System.out.println("||    Material Trade     ||");
            System.out.println("===========================");
            System.out.println("1. Trade Materials for Character");
            System.out.println("2. Trade Materials for Higher Rarity");
            System.out.println("3. Back");
            System.out.print(">> ");
            String choice = ui.getInput();

            switch (choice) {
                case "1":
                    handleTradeForCharacter(player, ui);
                    break;
                case "2":
                    handleTradeForHigherRarity(player, ui);
                    break;
                case "3":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    System.out.println("Press Enter to continue...");
                    ui.getInput();
            }
        }
    }

    private void handleTradeForCharacter(Player player, UserInterface ui) {
        List<Character> unowned = new ArrayList<>(DataManager.getCharacters().values());
        unowned.removeIf(c -> player.getCharacters().stream().anyMatch(oc -> oc.getCharacter().getId().equals(c.getId())));
        int page = 1;
        int itemsPerPage = 3;
        int totalPages = (int) Math.ceil((double) unowned.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;

        boolean back = false;
        while (!back) {
            ui.clearScreen();
            System.out.println("Trade Materials for Character (Page " + page + " of " + totalPages + "):");
            System.out.println("=======================================================");
            
            int start = (page - 1) * itemsPerPage;
            int end = Math.min(start + itemsPerPage, unowned.size());

            Map<String, Integer> playerMats = player.getMaterials();
            Map<String, Material> allMats = DataManager.getMaterials();
            
            for (int i = start; i < end; i++) {
                Character c = unowned.get(i);
                // Color the character name based on first material rarity
                System.out.printf("%d. \u001B[35m%s\u001B[0m\n", i - start + 1, c.getName());
                System.out.println("   Required Materials:");
                
                List<Material> reqMats = allMats.values().stream()
                    .filter(m -> m.getForCharacter().equals(c.getName()))
                    .sorted(Comparator.comparing(m -> getRarityOrdinal(m.getRarity())))
                    .collect(Collectors.toList());
                    
                for (Material m : reqMats) {
                    int owned = playerMats.getOrDefault(m.getId(), 0);
                    String status = owned >= 1 ? "" : " \u001B[31m(Not enough!)\u001B[0m";
                    String color = getRarityColor(m.getRarity());
                    System.out.printf("   - %s%s\u001B[0m (%d/1)%s\n", color, m.getName(), owned, status);
                }
                System.out.println("=======================================================");
            }

            System.out.println("1. Previous Page");
            System.out.println("2. Next Page");
            System.out.println("3. Back");
            System.out.print("Enter Character Name to Trade: ");
            String choice = ui.getInput();

            switch (choice) {
                case "1":
                    if (page > 1) page--;
                    break;
                case "2":
                    if (page < totalPages) page++;
                    break;
                case "3":
                    back = true;
                    break;
                default:
                    // Try to find character by name
                    Optional<Character> selectedChar = unowned.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(choice))
                        .findFirst();
                        
                    if (selectedChar.isPresent()) {
                        Character c = selectedChar.get();
                        List<Material> reqMats = allMats.values().stream()
                            .filter(m -> m.getForCharacter().equals(c.getName()))
                            .collect(Collectors.toList());
                            
                        boolean canTrade = reqMats.stream()
                            .allMatch(m -> playerMats.getOrDefault(m.getId(), 0) >= 1);
                            
                        if (canTrade) {
                            for (Material m : reqMats) {
                                playerMats.put(m.getId(), playerMats.get(m.getId()) - 1);
                            }
                            player.addCharacter(new OwnedCharacter(c, 1));
                            System.out.println("Successfully traded for " + c.getName() + "!");
                            
                            // Simpan inventory setelah trade
                            DataManager.savePlayerInventories();
                        } else {
                            System.out.println("Not enough materials!");
                        }
                        System.out.println("Press Enter to continue...");
                        ui.getInput();
                    } else {
                        System.out.println("Invalid character name.");
                        System.out.println("Press Enter to continue...");
                        ui.getInput();
                    }
            }
        }
    }


	private void handleTradeForHigherRarity(Player player, UserInterface ui) {
	    List<Character> unowned = new ArrayList<>(DataManager.getCharacters().values());
	    unowned.removeIf(c -> player.getCharacters().stream().anyMatch(oc -> oc.getCharacter().getId().equals(c.getId())));
	
	    ui.clearScreen();
	    System.out.println("Trade Materials for Higher Rarity:");
	    System.out.println();
	    
	    for (int i = 0; i < unowned.size(); i++) {
	        String color = getColorForIndex(i);
	        System.out.printf("%d. %s%s\u001B[0m\n", i + 1, color, unowned.get(i).getName());
	    }
	    System.out.printf("%d. Back\n", unowned.size() + 1);
	    System.out.print(">> ");
	    String choice = ui.getInput();
	
	    try {
	        int sel = Integer.parseInt(choice) - 1;
	        if (sel == unowned.size()) return;
	        if (sel >= 0 && sel < unowned.size()) {
	            Character c = unowned.get(sel);
	            tradeForHigherRarityForCharacter(player, ui, c);
	            
	            DataManager.savePlayerInventories();
	        }
	    } catch (NumberFormatException e) {
	        System.out.println("Invalid choice.");
	        System.out.println("Press Enter to continue...");
	        ui.getInput();
	    }
	}

    private void tradeForHigherRarityForCharacter(Player player, UserInterface ui, Character c) {
        boolean back = false;
        while (!back) {
            ui.clearScreen();
            String color = "\u001B[35m"; // Purple for character name
            System.out.println("Trade Materials for Higher Rarity for " + color + c.getName() + "\u001B[0m:");
            System.out.println();
            System.out.println("Required Materials:");
            
            Map<String, Integer> playerMats = player.getMaterials();
            List<Material> mats = DataManager.getMaterials().values().stream()
                .filter(m -> m.getForCharacter().equals(c.getName()))
                .sorted(Comparator.comparing(m -> getRarityOrdinal(m.getRarity())))
                .collect(Collectors.toList());

            for (Material m : mats) {
                int owned = playerMats.getOrDefault(m.getId(), 0);
                String matColor = getRarityColor(m.getRarity());
                String status = "";
                if (m.getRarity().equalsIgnoreCase("Legendary") && owned == 0) {
                    status = " \u001B[31m(Not enough!)\u001B[0m";
                }
                System.out.printf("- %s%s\u001B[0m (%d/1)%s\n", matColor, m.getName(), owned, status);
            }
            
            System.out.println("=======================================================");
            System.out.println();
            System.out.println("What do you want to trade?");
            System.out.println("1. 20 " + getRarityColor("Common") + mats.get(0).getName() + "\u001B[0m -> 1 " + 
                              getRarityColor("Rare") + mats.get(1).getName() + "\u001B[0m");
            System.out.println("2. 20 " + getRarityColor("Rare") + mats.get(1).getName() + "\u001B[0m -> 1 " + 
                              getRarityColor("Epic") + mats.get(2).getName() + "\u001B[0m");
            System.out.println("3. 20 " + getRarityColor("Epic") + mats.get(2).getName() + "\u001B[0m -> 1 " + 
                              getRarityColor("Legendary") + mats.get(3).getName() + "\u001B[0m");
            System.out.println("4. Back");
            System.out.print(">> ");
            String tradeChoice = ui.getInput();

            switch (tradeChoice) {
                case "1":
                case "2":
                case "3":
                    int index = Integer.parseInt(tradeChoice) - 1;
                    if (index < mats.size() - 1) {
                        Material from = mats.get(index);
                        Material to = mats.get(index + 1);
                        int owned = playerMats.getOrDefault(from.getId(), 0);
                        if (owned >= 20) {
                            playerMats.put(from.getId(), owned - 20);
                            player.addMaterial(to.getId(), 1);
                            System.out.println("Trade successful!");
                            
                            DataManager.savePlayerInventories();
                        } else {
                            System.out.println("Not enough materials!");
                        }
                    }
                    System.out.println("Press Enter to continue...");
                    ui.getInput();
                    break;
                case "4":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    System.out.println("Press Enter to continue...");
                    ui.getInput();
            }
        }
    }

    private String getRarityColor(String rarity) {
        switch (rarity.toLowerCase()) {
            case "common": return "\u001B[32m"; // Green
            case "rare": return "\u001B[36m";   // Cyan
            case "epic": return "\u001B[35m";   // Purple
            case "legendary": return "\u001B[33m"; // Yellow
            default: return "\u001B[0m";        // Reset
        }
    }

    private String getColorForIndex(int index) {
        switch (index % 6) {
            case 0: return "\u001B[35m"; // Purple
            case 1: return "\u001B[36m"; // Cyan
            case 2: return "\u001B[31m"; // Red
            case 3: return "\u001B[33m"; // Yellow
            case 4: return "\u001B[32m"; // Green
            case 5: return "\u001B[34m"; // Blue
            default: return "\u001B[0m"; // Reset
        }
    }

    private int getRarityOrdinal(String rarity) {
        switch (rarity.toLowerCase()) {
            case "common": return 0;
            case "rare": return 1;
            case "epic": return 2;
            case "legendary": return 3;
            default: return 4;
        }
    }
 
    private boolean loadGameBoardMap() {
        try {
            // Try to load the gameboard map
            String filename = "gameboardMap.txt";
            String[] possiblePaths = {
                filename,           // Root directory
                "src/" + filename,  // src folder
                "../" + filename    // Parent directory
            };
            
            BufferedReader reader = null;
            String filePath = "";
            
            // Try all possible file locations
            for (String path : possiblePaths) {
                try {
                    reader = new BufferedReader(new FileReader(path));
                    filePath = path;
                    break;
                } catch (IOException e) {
                    // Try next location
                }
            }
            
            // If file not found in all locations
            if (reader == null) {
                System.err.println("Error: Game board map file " + filename + " not found in any of the tried locations.");
                return false;
            }
            
            List<String> lines = new ArrayList<>();
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) { // Skip empty lines
                    lines.add(line);
                }
            }
            reader.close();
            
            if (lines.isEmpty()) {
                System.err.println("Error: Game board map file " + filePath + " is empty.");
                return false;
            }
            
            int rows = lines.size();
            int cols = lines.stream().mapToInt(String::length).max().orElse(0);
            
            if (cols == 0) {
                System.err.println("Error: Game board map file " + filePath + " has no valid columns.");
                return false;
            }
            
            char[][] gameBoard = new char[rows][cols];
            
            for (int i = 0; i < rows; i++) {
                char[] rowChars = lines.get(i).toCharArray();
                for (int j = 0; j < rowChars.length; j++) {
                    gameBoard[i][j] = rowChars[j];
                }
                for (int j = rowChars.length; j < cols; j++) {
                    gameBoard[i][j] = ' ';
                }
            }
            
            maps.put("gameboard", gameBoard);
            return true;
        } catch (IOException e) {
            System.err.println("Error loading game board map: " + e.getMessage());
            return false;
        }
    }

    private void handleGameplay(Player player, UserInterface ui) {
        // Check if player has enough energy
        if (player.getCurrentEnergy() < 10) {
            ui.clearScreen();
            System.out.println("You don't have enough energy to play MonOVoly. (Required: 10)");
            System.out.println("Press Enter to return...");
            ui.getInput();
            return;
        }
        
        // Load game board if not loaded yet
        if (!maps.containsKey("gameboard")) {
            loadGameBoardMap();
        }
        
        ui.clearScreen();
        System.out.println("Monovoly requires 10 energy to play.");
        System.out.println();
        System.out.println("Would you like to enter Monovoly?");
        System.out.println("1. Enter");
        System.out.println("2. Cancel");
        System.out.print(">> ");
        
        String choice = ui.getInput();
        if (!choice.equals("1")) {
            return;
        }
        
        // Consume 10 energy
        player.setCurrentEnergy(player.getCurrentEnergy() - 10);
        
        // Initialize game state
        Map<String, Property> boardProperties = initializeProperties();
        
        // Display game board
        ui.clearScreen();
        displayGameBoard(maps.get("gameboard"));
        System.out.println("Press ENTER to start...");
        ui.getInput();
        
        // Pick cards to decide who goes first
        ui.clearScreen();
        System.out.println("Pick a card to decide who moves first:");
        System.out.println();
        System.out.println("-------------------------   -------------------------");
        System.out.println("|                       |   |                       |");
        System.out.println("|           ?           |   |           ?           |");
        System.out.println("|                       |   |                       |");
        System.out.println("-------------------------   -------------------------");
        System.out.println();
        System.out.print("Enter 1 (left card) or 2 (right card): ");
        
        String cardChoice;
        do {
            cardChoice = ui.getInput();
        } while (!cardChoice.equals("1") && !cardChoice.equals("2"));
        
        boolean playerFirst = determineFirstPlayer(Integer.parseInt(cardChoice), player.getUsername(), ui);
        
        startGameLoop(player, ui, playerFirst, boardProperties);
    }

    private void displayGameBoard(char[][] gameBoard) {
        for (int i = 0; i < gameBoard.length; i++) {
            for (int j = 0; j < gameBoard[i].length; j++) {
                System.out.print(gameBoard[i][j]);
            }
            System.out.println();
        }
    }

    private boolean determineFirstPlayer(int playerCardChoice, String playerName, UserInterface ui) {
        // Randomly assign values to the cards
        Random random = new Random();
        int card1Value = random.nextInt(2) + 1; // Either 1 or 2
        int card2Value = (card1Value == 1) ? 2 : 1; // Ensure one card is 1, the other is 2
        
        int playerCardValue = (playerCardChoice == 1) ? card1Value : card2Value;
        int enemyCardValue = (playerCardChoice == 1) ? card2Value : card1Value;
        
        ui.clearScreen();
        System.out.println("Revealing your card...");
        System.out.println();
        System.out.println("-------------------------   -------------------------");
        System.out.println("|                       |   |                       |");
        System.out.println("|           " + playerCardValue + "           |   |           " + enemyCardValue + "           |");
        System.out.println("|                       |   |                       |");
        System.out.println("-------------------------   -------------------------");
        System.out.println();
        
        boolean playerFirst = (playerCardValue == 1);
        
        if (playerFirst) {
            System.out.println(playerName + " moves first!");
        } else {
            System.out.println("Enemy moves first!");
        }
        
        System.out.println("Press ENTER to continue...");
        ui.getInput();
        
        return playerFirst;
    }

    private Map<String, Property> initializeProperties() {
        Map<String, Property> properties = new HashMap<>();
        
        // Initialize all property positions from the game board
        char[][] gameBoard = maps.get("gameboard");
        for (int y = 0; y < gameBoard.length; y++) {
            for (int x = 0; x < gameBoard[y].length; x++) {
                if (gameBoard[y][x] == 'P') {
                    String position = x + "," + y;
                    properties.put(position, createProperty(position));
                }
            }
        }
        
        return properties;
    }

    private void startGameLoop(Player player, UserInterface ui, boolean playerFirst, Map<String, Property> boardProperties) {
        char[][] gameBoard = maps.get("gameboard");
        
        String enemyName = generateEnemyName(player);
        
        // Periksa apakah karakter sudah di-equip, jika tidak redirect ke inventory
        Character playerChar = player.getEquippedCharacter();
        if (playerChar == null) {
            ui.clearScreen();
            System.out.println("No character equipped! Please equip a character to proceed.");
            System.out.println("Redirecting to inventory...");
            handleInventory(player, ui); // Panggil method inventory untuk equip karakter
            playerChar = player.getEquippedCharacter(); // Cek ulang setelah dari inventory
            
            if (playerChar == null) {
                ui.clearScreen();
                System.out.println("No character equipped. Using default character.");
                playerChar = new Character("default", "Default Character", "Beginner", "Basic Skill", "A basic skill", 10, 1);
                player.equipCharacter("default"); // Equip karakter default
            }
        }
        
        int enemyLevel = Math.max(1, playerChar.getLevel() + (new Random().nextInt(5) - 2)); // Baris 1590
        
        int playerMoney = 20000;
        int playerAssets = playerMoney;
        List<Property> playerProperties = new ArrayList<>();
        
        int enemyMoney = 20000;
        int enemyAssets = enemyMoney;
        List<Property> enemyProperties = new ArrayList<>();
        
        int[] playerPosition = findPosition(gameBoard, 'S');
        int[] enemyPosition = Arrays.copyOf(playerPosition, 2);
        
        int oddDiceRollsLeft = 3;
        int evenDiceRollsLeft = 3;
        
        boolean isGameOver = false;
        boolean isPlayerTurn = playerFirst;
        
        while (!isGameOver) {
            if (isPlayerTurn) {
                handlePlayerTurn(player, ui, gameBoard, playerPosition, playerMoney, playerAssets, 
                                playerProperties, oddDiceRollsLeft, evenDiceRollsLeft, boardProperties);
                isPlayerTurn = false;
            } else {
                handleEnemyTurn(ui, gameBoard, enemyName, enemyPosition, enemyMoney, enemyAssets, 
                               enemyProperties, boardProperties);
                isPlayerTurn = true;
            }
            isGameOver = true;
        }
    }

    private int[] findPosition(char[][] gameBoard, char target) {
        for (int y = 0; y < gameBoard.length; y++) {
            for (int x = 0; x < gameBoard[y].length; x++) {
                if (gameBoard[y][x] == target) {
                    return new int[]{x, y};
                }
            }
        }
        return new int[]{1, 1};
    }

    private void handlePlayerTurn(Player player, UserInterface ui, char[][] gameBoard, int[] playerPosition, 
            int playerMoney, int playerAssets, List<Property> playerProperties,
            int oddDiceRollsLeft, int evenDiceRollsLeft, Map<String, Property> boardProperties) {
		ui.clearScreen();
		displayGameBoard(gameBoard);
		
		System.out.println("(" + player.getUsername() + ")");
		System.out.println("Character    : " + player.getEquippedCharacter().getName() + " (Lvl. " + player.getEquippedCharacter().getLevel() + ")");
		System.out.println("Money        : $" + playerMoney + " Total: $" + playerAssets);
		System.out.println();
		System.out.println("1. Roll dice (ODD) [" + oddDiceRollsLeft + "/3]");
		System.out.println("2. Roll dice (ANY)");
		System.out.println("3. Roll dice (EVEN) [" + evenDiceRollsLeft + "/3]");
		System.out.println("4. View My Properties");
		System.out.println("5. Exit Game");
		System.out.print(">> ");
		
		String choice = ui.getInput();
		int roll;
		
		switch (choice) {
		case "1":
		if (oddDiceRollsLeft > 0) {
		roll = rollDice(true, false);
		oddDiceRollsLeft--;
		handleDiceRoll(player, ui, gameBoard, playerPosition, roll, playerMoney, playerAssets, 
		         playerProperties, boardProperties, true);
		} else {
		System.out.println("No ODD dice rolls left!");
		ui.getInput();
		}
		break;
		case "2": // Roll dice (ANY)
		roll = rollDice(false, false);
		handleDiceRoll(player, ui, gameBoard, playerPosition, roll, playerMoney, playerAssets, 
		     playerProperties, boardProperties, false);
		break;
		case "3": // Roll dice (EVEN)
		if (evenDiceRollsLeft > 0) {
		roll = rollDice(false, true);
		evenDiceRollsLeft--;
		handleDiceRoll(player, ui, gameBoard, playerPosition, roll, playerMoney, playerAssets, 
		         playerProperties, boardProperties, true);
		} else {
		System.out.println("No EVEN dice rolls left!");
		ui.getInput();
		}
		break;
		case "4": // View My Properties
		viewPlayerProperties(player, ui, playerProperties);
		break;
		case "5": // Exit Game
		// Exit game
		return;
		default:
		System.out.println("Invalid choice. Press Enter to try again.");
		ui.getInput();
		break;
		}
    	}	

    private int rollDice(boolean forceOdd, boolean forceEven) {
        Random random = new Random();
        int dice1, dice2;
        int total;
        
        do {
            dice1 = random.nextInt(6) + 1;
            dice2 = random.nextInt(6) + 1;
            total = dice1 + dice2;
        } while ((forceOdd && total % 2 == 0) || (forceEven && total % 2 != 0));
        
        return total;
    }

    private void handleDiceRoll(Player player, UserInterface ui, char[][] gameBoard, int[] playerPosition, int roll,
                               int playerMoney, int playerAssets, List<Property> playerProperties, 
                               Map<String, Property> boardProperties, boolean specialDiceUsed) {
        ui.clearScreen();
        System.out.println("-------------------------   -------------------------");
        System.out.println("|                       |   |                       |");
        System.out.println("|         • • •         |   |         • • •         |");
        System.out.println("|                       |   |                       |");
        System.out.println("-------------------------   -------------------------");
        System.out.println();
        System.out.println(player.getUsername() + " rolled: " + roll);
        System.out.println("Press ENTER to continue...");
        ui.getInput();
        
        movePlayer(player, ui, gameBoard, playerPosition, roll, playerMoney, playerAssets, 
                   playerProperties, boardProperties);
    }

    private void viewPlayerProperties(Player player, UserInterface ui, List<Property> properties) {
        ui.clearScreen();
        
        if (properties.isEmpty()) {
            System.out.println("You don't own any properties yet.");
        } else {
            int totalPages = (int) Math.ceil(properties.size() / 3.0);
            int currentPage = 1;
            
            boolean viewingProperties = true;
            while (viewingProperties) {
                ui.clearScreen();
                System.out.println("===== Your Properties (Page " + currentPage + "/" + totalPages + ") =====");
                
                int startIndex = (currentPage - 1) * 3;
                int endIndex = Math.min(startIndex + 3, properties.size());
                
                for (int i = startIndex; i < endIndex; i++) {
                    Property prop = properties.get(i);
                    System.out.println("Property " + (i + 1));
                    System.out.println("Total Value  : $" + prop.getTotalValue());
                    System.out.println("Toll Fee     : $" + prop.calculateToll());
                    System.out.println("Building Lvl : " + prop.getBuildingLevel());
                    System.out.println("Landmark     : " + (prop.hasLandmark() ? "Yes" : "No"));
                    System.out.println("Festival     : " + (prop.isInFestivalMode() ? "Yes (" + prop.getFestivalTurnsLeft() + " turns left)" : "No"));
                    System.out.println();
                }
                
                System.out.println("1. Previous Page");
                System.out.println("2. Next Page");
                System.out.println("3. Back to Game");
                System.out.print(">> ");
                
                String choice = ui.getInput();
                switch (choice) {
                    case "1":
                        if (currentPage > 1) currentPage--;
                        break;
                    case "2":
                        if (currentPage < totalPages) currentPage++;
                        break;
                    case "3":
                        viewingProperties = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        ui.getInput();
                }
            }
        }
    }

    private String generateEnemyName(Player player) {
        // Placeholder for actual leaderboard implementation
        List<String> leaderboardNames = new ArrayList<>(Arrays.asList("Alice", "Bob", "Charlie", "Diana", "Edward"));
        
        // Remove the player's name if it's in the list
        leaderboardNames.remove(player.getUsername());
        
        // If leaderboard is empty after removing player's name, use default "Enemy"
        if (leaderboardNames.isEmpty()) {
            return "Enemy";
        }
        
        // Return a random name from the leaderboard
        Random random = new Random();
        return leaderboardNames.get(random.nextInt(leaderboardNames.size()));
    }

    /**
     * Handles the enemy's turn
     * @param boardProperties 
     */
    private void handleEnemyTurn(UserInterface ui, char[][] gameBoard, String enemyName, int[] enemyPosition, 
                                int enemyMoney, int enemyAssets, List<Property> enemyProperties, Map<String, Property> boardProperties) {
        // Randomly select dice roll mode
        Random random = new Random();
        int diceMode = random.nextInt(3); // 0: ODD, 1: ANY, 2: EVEN
        
        int roll;
        switch (diceMode) {
            case 0:
                roll = rollDice(true, false); // ODD
                break;
            case 2:
                roll = rollDice(false, true); // EVEN
                break;
            default:
                roll = rollDice(false, false); // ANY
        }
        
        ui.clearScreen();
        System.out.println(enemyName + " is rolling the dice...");
        System.out.println(enemyName + " rolled: " + roll);
        System.out.println("Press ENTER to continue...");
        ui.getInput();
    }

    private Property createProperty(String position) {
        int basePrice = 1250; // Base price as mentioned in the document
        return new Property(position, "Property " + position, basePrice);
    }
    
    private void movePlayer(Player player, UserInterface ui, char[][] gameBoard, int[] playerPosition, int roll,
            int playerMoney, int playerAssets, List<Property> playerProperties, 
            Map<String, Property> boardProperties) {
	ui.clearScreen();
	System.out.println("Moving " + player.getUsername() + " " + roll + " steps...");
	
	// Store original position and find path
	int[] originalPosition = Arrays.copyOf(playerPosition, 2);
	List<int[]> path = findPath(gameBoard, playerPosition, roll);
	
	// Check if player passes or lands on Start
	boolean passesStart = false;
	for (int[] pos : path) {
	if (gameBoard[pos[1]][pos[0]] == 'S') {
	 passesStart = true;
	 break;
	}
	}
	
	// Move to final position
	int[] finalPosition = path.get(path.size() - 1);
	playerPosition[0] = finalPosition[0];
	playerPosition[1] = finalPosition[1];
	
	// Get the block type at the landing position
	char landedBlock = gameBoard[playerPosition[1]][playerPosition[0]];
	
	// Show movement animation or text
	System.out.println(player.getUsername() + " moves from position (" + originalPosition[0] + "," + 
	           originalPosition[1] + ") to (" + playerPosition[0] + "," + playerPosition[1] + ")");
	System.out.println("Landed on: " + getBlockName(landedBlock));
	System.out.println("Press ENTER to continue...");
	ui.getInput();
	
	// Handle the effect of passing Start
	if (passesStart) {
	handlePassingStart(player, ui, playerMoney, playerProperties);
	}
	
	// Handle block effects based on where the player landed
	switch (landedBlock) {
	case 'S': // Start Block
	 handleStartBlock(player, ui, playerMoney, playerProperties);
	 break;
	case 'T': // Tax Block
	 handleTaxBlock(player, ui, playerMoney, playerAssets);
	 break;
	case 'P': // Property Block
	 String position = playerPosition[0] + "," + playerPosition[1];
	 handlePropertyBlock(player, ui, playerMoney, playerAssets, playerProperties, 
	                    boardProperties.get(position), position, gameBoard);
	 break;
	case 'G': // Go To Jail
	 handleGoToJailBlock(player, ui, gameBoard, playerPosition);
	 break;
	case 'J': // Jail Block
	 handleJailBlock(player, ui, gameBoard, playerPosition, playerMoney, playerAssets);
	 break;
	case 'C': // Card Block
	 handleCardBlock(player, ui, gameBoard, playerPosition, playerMoney, playerAssets, 
	                playerProperties, boardProperties);
	 break;
	case 'W': // World Travel
	 handleWorldTravelBlock(player, ui, gameBoard, playerPosition);
	 break;
	default:
	 System.out.println("Unknown block type. No effect.");
	 ui.getInput();
	 break;
	}
	}

	private List<int[]> findPath(char[][] gameBoard, int[] startPosition, int steps) {
	List<int[]> path = new ArrayList<>();
	path.add(Arrays.copyOf(startPosition, 2)); // Add starting position
	
	// Define movement direction: clockwise around the board
	int[][] directions = {
	{0, -1}, // Up
	{-1, 0}, // Left
	{0, 1},  // Down
	{1, 0}   // Right
	};
	
	int directionIndex = 0; // Start moving up
	int currentX = startPosition[0];
	int currentY = startPosition[1];
	
	for (int i = 0; i < steps; i++) {
	// Try to move in the current direction
	int nextX = currentX + directions[directionIndex][0];
	int nextY = currentY + directions[directionIndex][1];
	
	// Check if we're still on the board and there's a valid block
	if (nextX >= 0 && nextX < gameBoard[0].length && 
	 nextY >= 0 && nextY < gameBoard.length &&
	 gameBoard[nextY][nextX] != ' ') {
	 // Move successful
	 currentX = nextX;
	 currentY = nextY;
	} else {
	 // Try the next direction
	 directionIndex = (directionIndex + 1) % 4;
	 i--; // Retry this step
	 continue;
	}
	
	// Add this position to the path
	path.add(new int[]{currentX, currentY});
	}
	
	return path;
	}
	
	/**
	* Get the block name based on the block character
	* @param blockChar The character representing the block
	* @return The name of the block
	*/
	private String getBlockName(char blockChar) {
	switch (blockChar) {
	case 'S': return "Start";
	case 'T': return "Tax";
	case 'P': return "Property";
	case 'G': return "Go To Jail";
	case 'J': return "Jail";
	case 'C': return "Card";
	case 'W': return "World Travel";
	default: return "Unknown";
	}
	}
	
	/**
	* Handle the effect of passing the Start block
	* @param player The current player
	* @param ui The user interface
	* @param playerMoney The player's current money
	* @param playerProperties List of properties owned by the player
	*/
	private void handlePassingStart(Player player, UserInterface ui, int playerMoney, List<Property> playerProperties) {
	ui.clearScreen();
	System.out.println("You passed the Start Block!");
	playerMoney += 3000;
	System.out.println("You received $3,000!");
	System.out.println("Your money: $" + playerMoney);
	
	// Offer property upgrades
	if (!playerProperties.isEmpty()) {
	System.out.println("Would you like to upgrade your properties?");
	System.out.println("1. Yes");
	System.out.println("2. No");
	System.out.print(">> ");
	
	String choice = ui.getInput();
	if (choice.equals("1")) {
	 upgradeProperties(player, ui, playerMoney, playerProperties);
	}
	} else {
	System.out.println("You don't have any properties to upgrade.");
	System.out.println("Press ENTER to continue...");
	ui.getInput();
	}
	}
	
	/**
	* Handle the effect of landing on the Start block
	* @param player The current player
	* @param ui The user interface
	* @param playerMoney The player's current money
	* @param playerProperties List of properties owned by the player
	*/
	private void handleStartBlock(Player player, UserInterface ui, int playerMoney, List<Property> playerProperties) {
	ui.clearScreen();
	System.out.println("You landed on the Start Block!");
	playerMoney += 3000;
	System.out.println("You received $3,000!");
	System.out.println("Your money: $" + playerMoney);
	
	// Offer property upgrades
	if (!playerProperties.isEmpty()) {
	System.out.println("Would you like to upgrade your properties?");
	System.out.println("1. Yes");
	System.out.println("2. No");
	System.out.print(">> ");
	
	String choice = ui.getInput();
	if (choice.equals("1")) {
	 upgradeProperties(player, ui, playerMoney, playerProperties);
	}
	} else {
	System.out.println("You don't have any properties to upgrade.");
	System.out.println("Press ENTER to continue...");
	ui.getInput();
	}
	}
	
	/**
	* Handle the property upgrade process
	* @param player The current player
	* @param ui The user interface
	* @param playerMoney The player's current money
	* @param playerProperties List of properties owned by the player
	*/
	private void upgradeProperties(Player player, UserInterface ui, int playerMoney, List<Property> playerProperties) {
	boolean upgrading = true;
	
	while (upgrading && !playerProperties.isEmpty()) {
	ui.clearScreen();
	System.out.println("=== Upgrade Properties ===");
	System.out.println("Your money: $" + playerMoney);
	System.out.println();
	
	// Display properties
	for (int i = 0; i < playerProperties.size(); i++) {
	 Property prop = playerProperties.get(i);
	 System.out.println((i + 1) + ". " + prop.getName());
	 System.out.println("   Building Level: " + prop.getBuildingLevel() + "/3");
	 System.out.println("   Landmark: " + (prop.hasLandmark() ? "Yes" : "No"));
	 
	 // Show upgrade cost
	 if (prop.getBuildingLevel() < 3) {
	     System.out.println("   Upgrade cost: $" + prop.calculateConstructionCost());
	 } else if (!prop.hasLandmark()) {
	     System.out.println("   Landmark cost: $10,000");
	 } else {
	     System.out.println("   Fully upgraded");
	 }
	 System.out.println();
	}
	
	System.out.println((playerProperties.size() + 1) + ". Done upgrading");
	System.out.print(">> ");
	
	try {
	 int choice = Integer.parseInt(ui.getInput());
	 
	 if (choice >= 1 && choice <= playerProperties.size()) {
	     Property selectedProperty = playerProperties.get(choice - 1);
	     
	     if (selectedProperty.getBuildingLevel() < 3) {
	         // Upgrade building
	         int cost = selectedProperty.calculateConstructionCost();
	         if (playerMoney >= cost) {
	             playerMoney -= cost;
	             selectedProperty.constructBuilding();
	             System.out.println("Building upgraded to level " + selectedProperty.getBuildingLevel() + "!");
	         } else {
	             System.out.println("Not enough money to upgrade this building.");
	         }
	     } else if (!selectedProperty.hasLandmark()) {
	         // Build landmark
	         if (playerMoney >= 10000) {
	             playerMoney -= 10000;
	             selectedProperty.buildLandmark();
	             System.out.println("Landmark built successfully!");
	         } else {
	             System.out.println("Not enough money to build a landmark.");
	         }
	     } else {
	         System.out.println("This property is fully upgraded.");
	     }
	     
	     System.out.println("Press ENTER to continue...");
	     ui.getInput();
	     
	 } else if (choice == playerProperties.size() + 1) {
	     upgrading = false;
	 }
	} catch (NumberFormatException e) {
	 System.out.println("Invalid input. Please enter a number.");
	 System.out.println("Press ENTER to continue...");
	 ui.getInput();
	}
	}
	}
	
	/**
	* Handle the effect of landing on the Tax block
	* @param player The current player
	* @param ui The user interface
	* @param playerMoney The player's current money
	* @param playerAssets The player's total assets
	*/
	private void handleTaxBlock(Player player, UserInterface ui, int playerMoney, int playerAssets) {
	ui.clearScreen();
	System.out.println("You landed on the Tax Block!");
	
	// Calculate tax (10% of total assets)
	int taxAmount = (int)(playerAssets * 0.1);
	
	System.out.println("You must pay 10% of your total assets: $" + taxAmount);
	playerMoney -= taxAmount;
	
	System.out.println("Your remaining money: $" + playerMoney);
	System.out.println("Press ENTER to continue...");
	ui.getInput();
	}
	
	private void handlePropertyBlock(Player player, UserInterface ui, int playerMoney, int playerAssets, 
	                    List<Property> playerProperties, Property property, String position, char[][] gameBoard) {
	ui.clearScreen();
	System.out.println("You landed on a Property Block: " + property.getName());
	
	if (property.getOwnerId() == null) {
	// Unowned property - offer to buy
	int purchasePrice = property.calculatePurchasePrice();
	System.out.println("This property is unowned.");
	System.out.println("Purchase price: $" + purchasePrice);
	System.out.println("Your money: $" + playerMoney);
	System.out.println();
	System.out.println("Would you like to buy this property?");
	System.out.println("1. Yes");
	System.out.println("2. No");
	System.out.print(">> ");
	
	String choice = ui.getInput();
	if (choice.equals("1")) {
	 if (playerMoney >= purchasePrice) {
	     playerMoney -= purchasePrice;
	     property.setOwnerId(player.getPlayerId());
	     playerProperties.add(property);
	     
	     // Update the game board to show ownership
	     String[] coordinates = position.split(",");
	     int x = Integer.parseInt(coordinates[0]);
	     int y = Integer.parseInt(coordinates[1]);
	     gameBoard[y][x] = 'O'; // Player ownership marker
	     
	     System.out.println("Congratulations! You now own " + property.getName() + "!");
	     System.out.println("Your remaining money: $" + playerMoney);
	 } else {
	     System.out.println("You don't have enough money to buy this property.");
	 }
	}
	} else if (property.getOwnerId().equals(player.getPlayerId())) {
	// Player's own property - offer to upgrade
	System.out.println("You own this property!");
	System.out.println("Current Building Level: " + property.getBuildingLevel() + "/3");
	System.out.println("Has Landmark: " + (property.hasLandmark() ? "Yes" : "No"));
	System.out.println();
	
	if (property.getBuildingLevel() < 3 || !property.hasLandmark()) {
	 System.out.println("Would you like to upgrade this property?");
	 System.out.println("1. Yes");
	 System.out.println("2. No");
	 System.out.print(">> ");
	 
	 String choice = ui.getInput();
	 if (choice.equals("1")) {
	     if (property.getBuildingLevel() < 3) {
	         // Upgrade building
	         int cost = property.calculateConstructionCost();
	         System.out.println("Construction cost: $" + cost);
	         
	         if (playerMoney >= cost) {
	             playerMoney -= cost;
	             property.constructBuilding();
	             System.out.println("Building upgraded to level " + property.getBuildingLevel() + "!");
	             System.out.println("Your remaining money: $" + playerMoney);
	         } else {
	             System.out.println("You don't have enough money for this upgrade.");
	         }
	     } else if (!property.hasLandmark()) {
	         // Build landmark
	         System.out.println("Landmark cost: $10,000");
	         
	         if (playerMoney >= 10000) {
	             playerMoney -= 10000;
	             property.buildLandmark();
	             System.out.println("Landmark built successfully!");
	             System.out.println("Your remaining money: $" + playerMoney);
	         } else {
	             System.out.println("You don't have enough money to build a landmark.");
	         }
	     }
	 }
	} else {
	 System.out.println("This property is fully upgraded.");
	}
	} else {
	// Enemy property - pay toll or try to overtake
	System.out.println("This property is owned by an opponent!");
	int toll = property.calculateToll();
	int overtakeCost = property.calculateOvertakeCost();
	
	System.out.println("Toll: $" + toll);
	System.out.println("Overtake Cost: $" + overtakeCost);
	System.out.println("Your money: $" + playerMoney);
	
	if (playerMoney >= toll) {
	 System.out.println();
	 System.out.println("Options:");
	 System.out.println("1. Pay toll ($" + toll + ")");
	 if (playerMoney >= overtakeCost) {
	     System.out.println("2. Overtake property ($" + overtakeCost + ")");
	 }
	 System.out.print(">> ");
	 
	 String choice = ui.getInput();
	 if (choice.equals("1")) {
	     // Pay toll
	     playerMoney -= toll;
	     System.out.println("You paid the toll. Your remaining money: $" + playerMoney);
	 } else if (choice.equals("2") && playerMoney >= overtakeCost) {
	     // Overtake property
	     playerMoney -= overtakeCost;
	     property.setOwnerId(player.getPlayerId());
	     playerProperties.add(property);
	     
	     // Update the game board to show ownership
	     String[] coordinates = position.split(",");
	     int x = Integer.parseInt(coordinates[0]);
	     int y = Integer.parseInt(coordinates[1]);
	     gameBoard[y][x] = 'O'; // Player ownership marker
	     
	     System.out.println("You overtook the property! It's now yours!");
	     System.out.println("Your remaining money: $" + playerMoney);
	 }
	} else {
	 // Need to sell properties to pay toll
	 System.out.println("You don't have enough money to pay the toll!");
	 System.out.println("You need to sell some properties.");
	 
	 // Implement property selling logic here
	 // This is a simplified version - in actual implementation, you'd need to 
	 // provide options for the player to select which properties to sell
	 boolean canPay = sellPropertiesToPayDebt(player, ui, playerProperties, playerMoney, toll);
	 
	 if (canPay) {
	     System.out.println("You paid the toll after selling properties.");
	 } else {
	     System.out.println("You couldn't raise enough money even after selling all properties.");
	     System.out.println("Game over - you lose!");
	     // Handle game over logic
	 }
	}
	}
	
	System.out.println("Press ENTER to continue...");
	ui.getInput();
	}
	
	/**
	* Sell properties to pay a debt
	* @param player The current player
	* @param ui The user interface
	* @param playerProperties List of properties owned by the player
	* @param playerMoney The player's current money
	* @param debtAmount The amount that needs to be paid
	* @return Whether the player was able to raise enough money
	*/
	private boolean sellPropertiesToPayDebt(Player player, UserInterface ui, 
	                           List<Property> playerProperties, int playerMoney, int debtAmount) {
	if (playerProperties.isEmpty()) {
	return playerMoney >= debtAmount;
	}
	
	while (playerMoney < debtAmount && !playerProperties.isEmpty()) {
	ui.clearScreen();
	System.out.println("You need to sell properties to pay $" + debtAmount);
	System.out.println("Your current money: $" + playerMoney);
	System.out.println("You need: $" + (debtAmount - playerMoney) + " more");
	System.out.println();
	
	// Display properties
	for (int i = 0; i < playerProperties.size(); i++) {
	 Property prop = playerProperties.get(i);
	 System.out.println((i + 1) + ". " + prop.getName());
	 System.out.println("   Value: $" + prop.getTotalValue());
	 System.out.println("   Sell price: $" + (prop.getTotalValue() / 2)); // 50% sell value
	 System.out.println();
	}
	
	System.out.print("Select a property to sell (1-" + playerProperties.size() + "): ");
	
	try {
	 int choice = Integer.parseInt(ui.getInput());
	 
	 if (choice >= 1 && choice <= playerProperties.size()) {
	     Property selectedProperty = playerProperties.get(choice - 1);
	     int sellPrice = selectedProperty.getTotalValue() / 2; // 50% sell value
	     
	     playerMoney += sellPrice;
	     playerProperties.remove(choice - 1);
	     
	     System.out.println("Sold " + selectedProperty.getName() + " for $" + sellPrice);
	     System.out.println("Your money: $" + playerMoney);
	     System.out.println("Press ENTER to continue...");
	     ui.getInput();
	 } else {
	     System.out.println("Invalid choice.");
	     System.out.println("Press ENTER to continue...");
	     ui.getInput();
	 }
	} catch (NumberFormatException e) {
	 System.out.println("Invalid input. Please enter a number.");
	 System.out.println("Press ENTER to continue...");
	 ui.getInput();
	}
	}
	
	return playerMoney >= debtAmount;
	}
	
	/**
	* Handle the effect of landing on the Go To Jail block
	* @param player The current player
	* @param ui The user interface
	* @param gameBoard The game board
	* @param playerPosition The player's position
	*/
	private void handleGoToJailBlock(Player player, UserInterface ui, char[][] gameBoard, int[] playerPosition) {
	ui.clearScreen();
	System.out.println("You landed on the Go To Jail Block!");
	System.out.println("You are being sent to Jail!");
	
	// Find the Jail position
	int[] jailPosition = findPosition(gameBoard, 'J');
	playerPosition[0] = jailPosition[0];
	playerPosition[1] = jailPosition[1];
	
	// Set jail status (would be implemented in a full game)
	System.out.println("You are now in Jail!");
	System.out.println("Press ENTER to continue...");
	ui.getInput();
	}
	
	/**
	* Handle the effect of landing on the Jail block
	* @param player The current player
	* @param ui The user interface
	* @param gameBoard The game board
	* @param playerPosition The player's position
	* @param playerMoney The player's current money
	* @param playerAssets The player's total assets
	*/
	private void handleJailBlock(Player player, UserInterface ui, char[][] gameBoard, int[] playerPosition, 
	                int playerMoney, int playerAssets) {
	ui.clearScreen();
	System.out.println("You landed on the Jail Block!");
	
	// For this implementation, we'll just say the player is visiting
	System.out.println("You're just visiting the jail.");
	System.out.println("Press ENTER to continue...");
	ui.getInput();
	
	// In a full implementation, you would check if the player is imprisoned
	// and handle the logic for escaping (rolling doubles or paying a fee)
	}
	
	/**
	* Handle the effect of landing on the Card block
	* @param player The current player
	* @param ui The user interface
	* @param gameBoard The game board
	* @param playerPosition The player's position
	* @param playerMoney The player's current money
	* @param playerAssets The player's total assets
	* @param playerProperties List of properties owned by the player
	* @param boardProperties Map of all properties on the board
	*/
	private void handleCardBlock(Player player, UserInterface ui, char[][] gameBoard, int[] playerPosition, 
	                int playerMoney, int playerAssets, List<Property> playerProperties, 
	                Map<String, Property> boardProperties) {
	ui.clearScreen();
	System.out.println("You landed on a Card Block!");
	System.out.println("Drawing a random card...");
	
	// Generate a random card (0-4)
	Random random = new Random();
	int cardType = random.nextInt(5);
	
	System.out.println("Press ENTER to reveal your card...");
	ui.getInput();
	
	ui.clearScreen();
	switch (cardType) {
	case 0: // Send to Jail
	 System.out.println("Card: Send to Jail");
	 System.out.println("You can send an enemy directly to Jail!");
	 // In a full implementation, you would implement this logic
	 System.out.println("(Enemy would be sent to jail)");
	 break;
	 
	case 1: // Send to Property
	 System.out.println("Card: Send to Property");
	 System.out.println("You can send an enemy to one of your properties!");
	 
	 if (playerProperties.isEmpty()) {
	     System.out.println("You don't own any properties to send an enemy to.");
	 } else {
	     // In a full implementation, you would show a list of properties
	     // and let the player choose which one to send the enemy to
	     System.out.println("(Enemy would be sent to your property)");
	 }
	 break;
	 
	case 2: // Festival Time
	 System.out.println("Card: Festival Time");
	 
	 if (playerProperties.isEmpty()) {
	     System.out.println("You don't have any properties for a festival.");
	 } else {
	     // Randomly select a property for festival
	     int propertyIndex = random.nextInt(playerProperties.size());
	     Property festivalProperty = playerProperties.get(propertyIndex);
	     
	     festivalProperty.activateFestivalMode();
	     System.out.println("Festival activated on " + festivalProperty.getName() + "!");
	     System.out.println("Toll fee is increased to 1.5x for 3 turns!");
	 }
	 break;
	 
	case 3: // Tornado Disaster
	 System.out.println("Card: Tornado Disaster");
	 
	 if (playerProperties.isEmpty()) {
	     System.out.println("You don't have any properties that could be affected.");
	 } else {
	     // Randomly select a property for disaster
	     int propertyIndex = random.nextInt(playerProperties.size());
	     Property disasterProperty = playerProperties.get(propertyIndex);
	     
	     System.out.println("A tornado hit " + disasterProperty.getName() + "!");
	     
	     // Downgrade the property
	     if (disasterProperty.hasLandmark() || disasterProperty.getBuildingLevel() > 0) {
	         disasterProperty.downgradeBuilding();
	         System.out.println("The property has been downgraded!");
	         
	         if (disasterProperty.hasLandmark()) {
	             System.out.println("Landmark has been destroyed!");
	         } else if (disasterProperty.getBuildingLevel() > 0) {
	             System.out.println("Building level reduced to " + disasterProperty.getBuildingLevel());
	         } else {
	             System.out.println("All buildings have been destroyed!");
	         }
	     } else {
	         System.out.println("You lose ownership of this property!");
	         playerProperties.remove(propertyIndex);
	     }
	 }
	 break;
	 
	case 4: // Free Upgrade
	 System.out.println("Card: Free Upgrade");
	 
	 if (playerProperties.isEmpty()) {
	     System.out.println("You don't have any properties to upgrade.");
	 } else {
	     System.out.println("You can upgrade one of your properties for free!");
	     
	     // Display properties for selection
	     for (int i = 0; i < playerProperties.size(); i++) {
	         Property prop = playerProperties.get(i);
	         System.out.println((i + 1) + ". " + prop.getName());
	         System.out.println("   Building Level: " + prop.getBuildingLevel() + "/3");
	         System.out.println("   Landmark: " + (prop.hasLandmark() ? "Yes" : "No"));
	         System.out.println();
	     }
	     
	     System.out.print("Select a property to upgrade (1-" + playerProperties.size() + "): ");
	     
	     try {
	         int choice = Integer.parseInt(ui.getInput());
	         
	         if (choice >= 1 && choice <= playerProperties.size()) {
	             Property selectedProperty = playerProperties.get(choice - 1);
	             
	             if (selectedProperty.getBuildingLevel() < 3) {
	                 selectedProperty.constructBuilding();
	                 System.out.println("Building upgraded to level " + selectedProperty.getBuildingLevel() + "!");
	             } else if (!selectedProperty.hasLandmark()) {
	                 selectedProperty.buildLandmark();
	                 System.out.println("Landmark built successfully!");
	             } else {
	                 System.out.println("This property is already fully upgraded.");
	             }
	         } else {
	             System.out.println("Invalid choice.");
	         }
	     } catch (NumberFormatException e) {
	         System.out.println("Invalid input. Please enter a number.");
	     }
	 }
	 break;
	}
	
	System.out.println("Press ENTER to continue...");
	ui.getInput();
	}
	
	/**
	* Handle the effect of landing on the World Travel block
	* @param player The current player
	* @param ui The user interface
	* @param gameBoard The game board
	* @param playerPosition The player's position
	*/
	private void handleWorldTravelBlock(Player player, UserInterface ui, char[][] gameBoard, int[] playerPosition) {
	ui.clearScreen();
	System.out.println("You landed on the World Travel Block!");
	System.out.println("You can travel to any block on the board!");
	
	// In a full implementation, you would display all possible destinations
	// and let the player choose where to go
	System.out.println("(This feature would allow you to select any block to travel to)");
	System.out.println("Press ENTER to continue...");
	ui.getInput();
	}
}

