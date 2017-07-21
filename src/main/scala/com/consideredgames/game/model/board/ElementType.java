package com.consideredgames.game.model.board;

import java.util.EnumSet;

public enum ElementType {
    HEX, VOLCANO, WALL, RIVER, BOAT, BRIDGE, ANIMAL, PERSON;

    public static final EnumSet<ElementType> borders = EnumSet.of(RIVER, BOAT, BRIDGE);
}
