package game;

import java.util.HashMap;

public class TileInformation
{
	HashMap<TileTypes, Float> tileValue = new HashMap<TileTypes, Float>();
	
	public TileInformation(float floorValue, float wallValue, float treasureValue, float enemyValue)
	{
		tileValue.put(TileTypes.FLOOR, floorValue);
		tileValue.put(TileTypes.DOOR, floorValue);
		tileValue.put(TileTypes.WALL, wallValue);
		tileValue.put(TileTypes.TREASURE, treasureValue);
		tileValue.put(TileTypes.ENEMY, enemyValue);
	}
	
	public float getStepToTileValue(TileTypes tileType)
	{
		return tileValue.containsKey(tileType) ? tileValue.get(tileType) : Float.MAX_VALUE;
	}
}