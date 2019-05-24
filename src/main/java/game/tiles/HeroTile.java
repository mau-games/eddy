package game.tiles;

import finder.geometry.Point;
import game.Room;
import game.Tile;
import game.TileTypes;
import gui.controls.Drawer;
import gui.controls.InteractiveMap;

public class HeroTile extends Tile {

	public HeroTile()
	{
		m_type = TileTypes.FLOOR;
		setBrushUsage();
	}
	
	public HeroTile(Point p, TileTypes type)
	{
		super(p, type);
		setBrushUsage();
	}
	
	public HeroTile(int x, int y, TileTypes type)
	{
		super(x, y, type);
		setBrushUsage();
	}
	
	public HeroTile(Point p, int typeValue)
	{
		super(p, typeValue);
		setBrushUsage();
	}
	
	public HeroTile(int x, int y, int typeValue)
	{
		super(x, y, typeValue);
		setBrushUsage();
	}
	
	public HeroTile(Tile copyTile)
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
