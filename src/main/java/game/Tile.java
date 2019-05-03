package game;

import java.util.ArrayList;

import finder.geometry.Point;
import gui.controls.Brush;
import gui.controls.Brush.BrushUsage;

public class Tile
{
	protected TileTypes m_type;
	private ArrayList<Point> positions;
	private Point center;
	private boolean m_immutable;
	
	//When using the tile as a brush
	protected BrushUsage usage = BrushUsage.DEFAULT;

	public TileTypes GetType()
	{
		return m_type;
	}
	
	public void SetType(TileTypes type)
	{
		m_type = type;
	}
	
	public Point GetCenterPosition()
	{
		return center;
	}
	
	public ArrayList<Point> GetPositions()
	{
		return positions;
	}
	
	public boolean GetImmutable()
	{
		return m_immutable;
	}
	
	public void SetImmutable(boolean value)
	{
		m_immutable = value;
	}
	
	public BrushUsage getBrushUsage()
	{
		return usage;
	}
	
	protected void setBrushUsage()
	{
		usage = BrushUsage.DEFAULT;
	}
	
	public Brush modification(Brush brush)
	{
		return brush;
	}
	
	public Tile()
	{
		
	}
	
	public Tile(Point p, TileTypes type)
	{
		this.m_type = type;
		this.center = p;
		this.positions = new ArrayList<Point>();
		this.positions.add(p);
		m_immutable = false;
	}
	
	public Tile(int x, int y, TileTypes type)
	{
		this.m_type = type;
		this.center = new Point(x,y);
		this.positions = new ArrayList<Point>();
		this.positions.add(center);
		m_immutable = false;
	}
	
	public Tile(Point p, int typeValue)
	{
		this.m_type = TileTypes.toTileType(typeValue);
		this.center = p;
		this.positions = new ArrayList<Point>();
		this.positions.add(center);
		m_immutable = false;
	}
	
	public Tile(int x, int y, int typeValue)
	{
		this.m_type = TileTypes.toTileType(typeValue);
		this.center = new Point(x,y);
		this.positions = new ArrayList<Point>();
		this.positions.add(center);
		m_immutable = false;
	}
	
	public Tile(Tile copyTile)
	{
		this.m_type = copyTile.GetType();
		this.center = copyTile.GetCenterPosition();
		this.positions = new ArrayList<Point>();
		this.positions.addAll(copyTile.GetPositions());
		this.m_immutable = copyTile.GetImmutable();
	}
	
	public void ToggleImmutable()
	{
		m_immutable = !m_immutable;
	}

}
