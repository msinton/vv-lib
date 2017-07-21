package com.consideredgames.game.model.hex;

import java.util.EnumSet;

public enum Vertex {
	NE, E, SE, SW, W, NW;
	
	public static final int NUMBER_OF_VERTICES = 6;
	
	public final Vertex clockwise(){
		return adjacent( Direction.CLOCKWISE );
	}
	
	public final Vertex anticlockwise(){
		return adjacent( Direction.ANTICLOCKWISE );
	}
	
	private final Vertex adjacent( Direction d ){
		switch(this){
		case NE:
			return (d==Direction.ANTICLOCKWISE) ? NW : E;
		case E:
			return (d==Direction.ANTICLOCKWISE) ? NE : SE;
		case SE:
			return (d==Direction.ANTICLOCKWISE) ? E : SW;
		case SW:
			return (d==Direction.ANTICLOCKWISE) ? SE : W;
		case W:
			return (d==Direction.ANTICLOCKWISE) ? SW : NW;
		case NW:
			return (d==Direction.ANTICLOCKWISE) ? W : NE;
		default:
			return null;
		}
	}
	
	public final Side getClockwiseSide(){
		return adjacentSide(Direction.CLOCKWISE);
	}
	
	public final Side getAnticlockwiseSide(){
		return adjacentSide(Direction.ANTICLOCKWISE);
	}
	
	private final Side adjacentSide( Direction d ){
		switch(this){
		case NE:
			return (d==Direction.ANTICLOCKWISE) ? Side.north : Side.northEast;
		case E:
			return (d==Direction.ANTICLOCKWISE) ? Side.northEast : Side.southEast;
		case SE:
			return (d==Direction.ANTICLOCKWISE) ? Side.southEast : Side.south;
		case SW:
			return (d==Direction.ANTICLOCKWISE) ? Side.south : Side.southWest;
		case W:
			return (d==Direction.ANTICLOCKWISE) ? Side.southWest : Side.northWest;
		case NW:
			return (d==Direction.ANTICLOCKWISE) ? Side.northWest : Side.north;
		default:
			return null;
		}
	}

	/**
	 * The vertices East, SW and NW. This set of vertices correspond to the one of the two possible configurations 
	 * for the vertices to be found on three hexagons meeting around the same point.
	 */
	public static final EnumSet<Vertex> verticesSet_1 = EnumSet.of(E, SW, NW);
	
	/**
	 * The vertices West, SE and NE. This set of vertices correspond to one of the two possible configurations 
	 * for the vertices to be found on three hexagons meeting around the same point.
	 */
	public static final EnumSet<Vertex> verticesSet_2 = EnumSet.of(W, SE, NE);
}
