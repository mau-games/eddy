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
    COIN(2),
    COIN2(3),
    ENEMY(4),
    ENEMY2(5),
    COFFER(6),
    COFFER2(7),
    WALLOUTTER(8),
    DOOR(9),
    DOORENTER(10),
    ALPHA_GREEN(11);
	
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
		return value == COIN.getValue() || value == COIN2.getValue() || value == COFFER.getValue() || value == COFFER2.getValue();
	}
	
	public boolean isEnemy(){
		return value == ENEMY.getValue() || value == ENEMY2.getValue();
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
			tile = TileTypes.COIN;
			break;
		case 3:
			tile = TileTypes.COIN2;
			break;
		case 4:
			tile = TileTypes.ENEMY;
			break;
		case 5:
			tile = TileTypes.ENEMY2;
			break;
		case 6:
			tile = TileTypes.COFFER;
			break;
		case 7:
			tile = TileTypes.COFFER2;
			break;
		case 8:
			tile = TileTypes.WALLOUTTER;
			break;
		case 9:
			tile = TileTypes.DOOR;
			break;
		case 10:
			tile = TileTypes.DOORENTER;
			break;
		case 11:
		default:
			tile = TileTypes.ALPHA_GREEN;
		}
		
		return tile;
	}
}
