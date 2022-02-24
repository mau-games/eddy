package game;

/**
 * The TileTypes enum structure holds representations of the different types of
 * tiles that can be used in a dungeon.
 * 
 * @author Alexander Baldwin, Malmö University
 * @author Johan Holmberg, Malmö University
 * @modified Alberto Alvarez, Malmö University
 */
public enum TileTypes {
	FLOOR(0),
    WALL(1),
    TREASURE(2),
    ENEMY(3),
    DOOR(4),
    ENEMY_BOSS(5),
    HERO(6),
	NPC(7),
	ITEM(8),
	NONE(9),
	SOLDIER(10),
	MAGE(11),
	BOUNTYHUNTER(12),
	CIVILIAN(13);

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
	
	public boolean isFloor() {
		return value == FLOOR.getValue();
	}
	
	public boolean isEnemy(){
		return value == ENEMY.getValue();
	}

	public boolean isEnemyBoss(){
		return value == ENEMY_BOSS.getValue();
	}

	public boolean isDoor(){
		return value == DOOR.getValue();
	}	
	
	public boolean isWall() {
		return value == WALL.getValue();
	}

	public boolean isItem(){
		return value == ITEM.getValue();
	}

	public boolean isNPC(){
		return value == NPC.getValue();
	}
	
	public boolean isSoldier() {
		return value == SOLDIER.getValue();
	}
	
	public boolean isMage() {
		return value == MAGE.getValue();
	}
	
	public boolean isBountyhunter() {
		return value == BOUNTYHUNTER.getValue();
	}
	
	public boolean isCivilian() {
		return value == CIVILIAN.getValue();
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
			tile = TileTypes.ENEMY_BOSS;
			break;
		case 6:
			tile = TileTypes.HERO;
			break;
		case 7:
			tile = TileTypes.NPC;
			break;
		case 8:
			tile = TileTypes.ITEM;
			break;
		case 10:
			tile = TileTypes.SOLDIER;
			break;
		case 11:
			tile = TileTypes.MAGE;
			break;
		case 12:
			tile = TileTypes.BOUNTYHUNTER;
			break;
		case 13:
			tile = TileTypes.CIVILIAN;
			break;
		default:
			tile = TileTypes.NONE;
		}
		
		return tile;
	}
}
