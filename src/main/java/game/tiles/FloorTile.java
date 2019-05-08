package game.tiles;

import finder.geometry.Point;
import game.Room;
import game.Tile;
import game.TileTypes;
import gui.controls.Drawer;
import gui.controls.InteractiveMap;

public class FloorTile extends Tile {

	public FloorTile()
	{
		m_type = TileTypes.FLOOR;
		setBrushUsage();
	}
	
	public FloorTile(Point p, TileTypes type)
	{
		super(p, type);
		setBrushUsage();
	}
	
	public FloorTile(int x, int y, TileTypes type)
	{
		super(x, y, type);
		setBrushUsage();
	}
	
	public FloorTile(Point p, int typeValue)
	{
		super(p, typeValue);
		setBrushUsage();
	}
	
	public FloorTile(int x, int y, int typeValue)
	{
		super(x, y, typeValue);
		setBrushUsage();
	}
	
	public FloorTile(Tile copyTile)
	{
		super(copyTile);
		m_type = TileTypes.FLOOR;
		setBrushUsage();
	}
	
	@Override
	public void PaintTile(Point currentCenter, Room room, Drawer drawer, InteractiveMap interactiveCanvas)
	{
		interactiveCanvas.getCell(currentCenter.getX(), currentCenter.getY()).
		setImage(interactiveCanvas.getImage(m_type, interactiveCanvas.scale));
	}
}
