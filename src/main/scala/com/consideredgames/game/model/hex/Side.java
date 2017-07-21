package com.consideredgames.game.model.hex;


public enum Side{
	north,    northEast,    southEast,     south,     southWest,    northWest ;

	public static final int NUMBER_OF_SIDES = 6;

	public final Side opposite(){
		switch(this){
		case north:
			return south;

		case northEast:
			return southWest;

		case southEast:
			return northWest;

		case south:
			return north;

		case southWest:
			return northEast;

		case northWest:
			return southEast;
		default:
			return null;
		}
	}
	
	public final Side clockwise(){
		return adjacent(Direction.CLOCKWISE);
	}

	public final Side anticlockwise(){
		return adjacent(Direction.ANTICLOCKWISE);
	}
		
	private final Side adjacent( Direction d ){
		switch(this){
		case north:
			return (d==Direction.ANTICLOCKWISE) ? northWest : northEast;
		case northEast:
			return (d==Direction.ANTICLOCKWISE) ? north : southEast;
		case southEast:
			return (d==Direction.ANTICLOCKWISE) ? northEast : south;
		case south:
			return (d==Direction.ANTICLOCKWISE) ? southEast : southWest;
		case southWest:
			return (d==Direction.ANTICLOCKWISE) ? south : northWest;
		case northWest:
			return (d==Direction.ANTICLOCKWISE) ? southWest : north;
		default:
			return null;
		}
	}
	
	public final Vertex clockwiseVertex(){
		return adjacentVertex(Direction.CLOCKWISE);
	}

	public final Vertex anticlockwiseVertex(){
		return adjacentVertex(Direction.ANTICLOCKWISE);
	}
	
	private final Vertex adjacentVertex( Direction d ){
		switch(this){
		case north:
			return (d==Direction.ANTICLOCKWISE) ? Vertex.NW : Vertex.NE;
		case northEast:
			return (d==Direction.ANTICLOCKWISE) ? Vertex.NE : Vertex.E;
		case southEast:
			return (d==Direction.ANTICLOCKWISE) ? Vertex.E : Vertex.SE;
		case south:
			return (d==Direction.ANTICLOCKWISE) ? Vertex.SE : Vertex.SW;
		case southWest:
			return (d==Direction.ANTICLOCKWISE) ? Vertex.SW : Vertex.W;
		case northWest:
			return (d==Direction.ANTICLOCKWISE) ? Vertex.W : Vertex.NW;
		default:
			return null;
		}
	}

}// End of enum