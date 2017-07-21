package com.consideredgames.game.model.hex;

public class PointFactory {

    private int count = 0;
    
    public Point build() {
        return new Point(count++);
    }
}
