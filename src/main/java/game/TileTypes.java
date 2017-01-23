package game;

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
	
	public int getValue(){
		return value;
	}
}
