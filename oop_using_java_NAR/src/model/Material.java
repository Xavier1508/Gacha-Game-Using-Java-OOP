package model;

public class Material {
    private String id;
    private String rarity;
    private String name;
    private String forCharacter;

    public Material(String id, String rarity, String name, String forCharacter) {
        this.id = id;
        this.rarity = rarity;
        this.name = name;
        this.forCharacter = forCharacter;
    }

    public String getId() { return id; }
    public String getRarity() { return rarity; }
    public String getName() { return name; }
    public String getForCharacter() { return forCharacter; }
}
