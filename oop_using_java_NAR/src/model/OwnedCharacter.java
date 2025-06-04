package model;

import model.Character;

public class OwnedCharacter {
    private Character character;
    private int level;

    public OwnedCharacter(Character character, int level) {
        this.character = character;
        this.level = level;
    }

    public Character getCharacter() { return character; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
