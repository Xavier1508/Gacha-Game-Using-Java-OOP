package model;

public class Character {
    private String id;
    private String name;
    private String title;
    private String skillName;
    private String skillDesc;
    private int skillBaseChance;
    private int level; // Menambahkan field level yang diperlukan

    public Character(String id, String name, String title, String skillName, String skillDesc, int skillBaseChance) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.skillName = skillName;
        this.skillDesc = skillDesc;
        this.skillBaseChance = skillBaseChance;
        this.level = 1; // Default level
    }

    // Constructor dengan parameter level
    public Character(String id, String name, String title, String skillName, String skillDesc, int skillBaseChance, int level) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.skillName = skillName;
        this.skillDesc = skillDesc;
        this.skillBaseChance = skillBaseChance;
        this.level = level;
    }

    public Object getCharacterId() {
        return id;  // Mengembalikan id sebagai Object, bukan null
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getSkillName() { return skillName; }
    public String getSkillDesc() { return skillDesc; }
    public int getSkillBaseChance() { return skillBaseChance; }
    
    // Menambahkan method getLevel() yang diperlukan
    public int getLevel() { return level; }
    
    // Menambahkan setter untuk level jika diperlukan
    public void setLevel(int level) { 
    	this.level = level; 
    }
    }