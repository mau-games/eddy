package game.tiles;

import finder.geometry.Point;
import game.Tile;
import game.TileTypes;
import gui.controls.BasicBrush;
import gui.controls.Brush;
import gui.controls.Brush.BrushUsage;

public class DoorTile extends Tile {
	
	public DoorTile()
	{
		m_type = TileTypes.DOOR;
		setBrushUsage();

	}
	
	public DoorTile(Point p, TileTypes type)
	{
		super(p, type);
		setBrushUsage();
	}
	
	public DoorTile(int x, int y, TileTypes type)
	{
		super(x, y, type);
		setBrushUsage();
	}
	
	public DoorTile(Point p, int typeValue)
	{
		super(p, typeValue);
		setBrushUsage();
	}
	
	public DoorTile(int x, int y, int typeValue)
	{
		super(x, y, typeValue);
		setBrushUsage();
	}
	
	public DoorTile(Tile copyTile)
	{
		super(copyTile);
		setBrushUsage();
	}
	
	public Brush modification(Brush brush)
	{
		return brush;
	}
	
}