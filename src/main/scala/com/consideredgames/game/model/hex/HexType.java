package com.consideredgames.game.model.hex;

import java.util.EnumMap;
import java.util.EnumSet;

/**
 * Each hexType is given a weighting which describes the proportion of this hex type
 * in comparison to the others. The name is a string which can be used in the UI.
 */
public enum HexType { //TODO define all types and play with values
	CLAY	 ("clay"  , 10)
	,ORE     ("ore"   , 12)
	,PLAINS  ("plains", 25)
	,STONE   ("stone" , 15)
	,WOODS   ("woods" , 20)
	,WATER   ("water" , 7)
    ,FLOODED ("flooded", 0);

    private String name;
    private int weighting;

    HexType(String name, int weighting) {
        this.name = name;
        this.weighting = weighting;
    }

    public static int getTotalWeighting() {
        int sum = 0;
        for (HexType hexType : HexType.values()) {
            sum += hexType.weighting;
        }
        return sum;
    }

    public String getName() {
        return this.name;
    }

    public Integer getWeighting() {
        return this.weighting;
    }

    public static EnumSet<HexType> earthTypes = EnumSet.of(CLAY, ORE, STONE);

    public static EnumMap<HexType, Integer> upperEarthBoundaries = initialiseEarthUpperBoundaries();

    public static EnumMap<HexType, Integer> initialiseEarthUpperBoundaries() {

        int cumulativeSum = 0;
        EnumMap<HexType, Integer> tempHexTypeUpperBoundaries = new EnumMap<>(HexType.class);
        for (HexType hexType : earthTypes) {
            cumulativeSum += hexType.weighting;
            tempHexTypeUpperBoundaries.put(hexType, cumulativeSum);
        }
        return tempHexTypeUpperBoundaries;
    }
}