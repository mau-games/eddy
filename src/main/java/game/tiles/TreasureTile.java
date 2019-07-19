package game.tiles;

import finder.geometry.Point;
import game.Tile;
import game.TileTypes;
import gui.controls.BasicBrush;
import gui.controls.Brush;
import gui.controls.Brush.BrushUsage;

public class TreasureTile extends Tile {
	
	public TreasureTile()
	{
		m_type = TileTypes.TREASURE;
		setBrushUsage();

	}
	
	public TreasureTile(Point p, TileTypes type)
	{
		super(p, type);
		setBrushUsage();
	}
	
	public TreasureTile(int x, int y, TileTypes type)
	{
		super(x, y, type);
		setBrushUsage();
	}
	
	public TreasureTile(Point p, int typeValue)
	{
		super(p, typeValue);
		setBrushUsage();
	}
	
	public TreasureTile(int x, int y, int typeValue)
	{
		super(x, y, typeValue);
		setBrushUsage();
	}
	
	public TreasureTile(Tile copyTile)
	{
		super(copyTile);
		setBrushUsage();
	}
	
	public Brush modification(Brush brush)
	{
		return brush;
	}
	
}