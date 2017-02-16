package game;

/**
 * The TileTypes enum structure holds representations of the different types of
 * tiles that can be used in a dungeon.
 * 
 * @author Alexander Baldwin, Malmö University
 * @author Johan Holmberg, Malmö University
 */
public enum TileTypes {
	FLOOR(0),
    WALL(1),
    TREASURE(2),
    ENEMY(3),
    DOOR(4),
    DOORENTER(5),
    NONE(6);
	
	private final int value;
	private TileTypes(int value){
		this.value = value;
	}
	
	/**
	 * Converts a TileTypes value to an integer.
	 * 
	 * @return The integer representation of a tile.
	 */
	public int getValue(){
		return value;
	}
	
	public boolean isTreasure(){
		return value == TREASURE.getValue();
	}
	
	public boolean isEnemy(){
		return value == ENEMY.getValue();
	}
	
	/**
	 * Creates a TileType out of an integer value.
	 * 
	 * @param value The value to be converted.
	 * @return A TileType object.
	 */
	public static TileTypes toTileType(int value) {
		TileTypes tile = null;
		
		switch(value) {
		case 0:
			tile = TileTypes.FLOOR;
			break;
		case 1:
			tile = TileTypes.WALL;
			break;
		case 2:
			tile = TileTypes.TREASURE;
			break;
		case 3:
			tile = TileTypes.ENEMY;
			break;
		case 4:
			tile = TileTypes.DOOR;
			break;
		case 5:
			tile = TileTypes.DOORENTER;
			break;
		default:
			tile = TileTypes.NONE;
		}
		
		return tile;
	}
}
