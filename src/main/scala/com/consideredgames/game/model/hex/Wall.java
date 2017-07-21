package com.consideredgames.game.model.hex;

/**
 * @author matt
 *
 */
public class Wall {

    public enum WallType {WOOD, STONE}
    
    private WallType type;

    public Wall(WallType type) {
        this.type = type;
    }
    
    public WallType getType() {
        return type;
    }

}
