package game.tiles;

import finder.geometry.Point;
import game.Tile;
import game.TileTypes;

public class WallTile extends Tile {
	
	public WallTile()
	{
		m_type = TileTypes.WALL;
		setBrushUsage();
	}

	public WallTile(Point p, TileTypes type)
	{
		super(p, type);
		setBrushUsage();
	}
	
	public WallTile(int x, int y, TileTypes type)
	{
		super(x, y, type);
		setBrushUsage();
	}
	
	public WallTile(Point p, int typeValue)
	{
		super(p, typeValue);
		setBrushUsage();
	}
	
	public WallTile(int x, int y, int typeValue)
	{
		super(x, y, typeValue);
		setBrushUsage();
	}
	
	public WallTile(Tile copyTile)
	{
		super(copyTile);
	}
}