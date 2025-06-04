package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {
    private String playerId;
    private String username;
    private String email;
    private String password;
    private int level;
    private int gems;
    private int currentExp;
    private int currentEnergy;
    private String equippedCharacterId;
    private int trophies;
    private Map<String, Integer> materials;
    private List<OwnedCharacter> characters;
    private Map<String, Property> properties;
    
    public Player(String playerId, String username, String email, String password) {
        this.playerId = playerId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.level = 1;
        this.gems = 0;
        this.currentExp = 0;
        this.currentEnergy = 0;
        this.equippedCharacterId = "0";
        this.trophies = 0;
        this.materials = new HashMap<>();
        this.characters = new ArrayList<>();
        this.properties = new HashMap<>();
    }
    
    public Player(String playerId, String username, String email, String password, 
                 int level, int gems, int currentExp, int currentEnergy, 
                 String equippedCharacterId, int trophies) {
        this.playerId = playerId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.level = level;
        this.gems = gems;
        this.currentExp = currentExp;
        this.currentEnergy = currentEnergy;
        this.equippedCharacterId = equippedCharacterId;
        this.trophies = trophies;
        this.materials = new HashMap<>();
        this.characters = new ArrayList<>();
        this.properties = new HashMap<>();
    }
    
    public int getMaxEnergy() {
        return (int) (50 * (1 + (level * 0.1)));
    }
    
    public void addEnergy(int amount) {
    	this.currentEnergy = Math.min(this.currentEnergy + amount, getMaxEnergy());
    }
    
    public void addGems(int amount) {
        this.gems += amount;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getGems() {
        return gems;
    }
    
    public int getCurrentExp() {
        return currentExp;
    }
    
    public int getCurrentEnergy() {
        return currentEnergy;
    }
    
    public void setCurrentEnergy(int currentEnergy) {
        this.currentEnergy = currentEnergy;
    }
    
    public String getEquippedCharacterId() {
        return equippedCharacterId;
    }
    
    public int getTrophies() {
        return trophies;
    }
    
    public void addMaterial(String materialId, int amount) {
        materials.put(materialId, materials.getOrDefault(materialId, 0) + amount);
    }
    
    public Map<String, Integer> getMaterials() {
        return materials;
    }
    
    public List<OwnedCharacter> getCharacters() {
        return characters;
    }
    
    public void addCharacter(OwnedCharacter oc) {
        this.characters.add(oc);
    }
    
    public void equipCharacter(String characterId) {
        this.equippedCharacterId = characterId;
    }
    
    public Character getEquippedCharacter() {
        for (OwnedCharacter ownedChar : characters) {
            if (ownedChar.getCharacter().getCharacterId().equals(equippedCharacterId)) {
                return ownedChar.getCharacter();
            }
        }
        return null;
    }
    
    public Map<String, Property> getProperties() {
        return properties;
    }
    
    public void addProperty(Property property) {
        this.properties.put(property.getPropertyId(), property);
    }
}