package game;

import finder.geometry.Point;

public class Tile
{
	private TileTypes m_type;
	private Point position;
	private boolean m_immutable;

	public TileTypes GetType()
	{
		return m_type;
	}
	
	public void SetType(TileTypes type)
	{
		m_type = type;
	}
	
	public Point GetPosition()
	{
		return position;
	}
	
	public boolean GetImmutable()
	{
		return m_immutable;
	}
	
	public void SetImmutable(boolean value)
	{
		m_immutable = value;
	}
	
	public Tile(Point p, TileTypes type)
	{
		this.m_type = type;
		this.position = p;
		m_immutable = false;
	}
	
	public Tile(int x, int y, TileTypes type)
	{
		this.m_type = type;
		this.position = new Point(x,y);
		m_immutable = false;
	}
	
	public Tile(Point p, int typeValue)
	{
		this.m_type = TileTypes.toTileType(typeValue);
		this.position = p;
		m_immutable = false;
	}
	
	public Tile(int x, int y, int typeValue)
	{
		this.m_type = TileTypes.toTileType(typeValue);
		this.position = new Point(x,y);
		m_immutable = false;
	}
	
	public Tile(Tile copyTile)
	{
		this.m_type = copyTile.GetType();
		this.position = copyTile.GetPosition();
		this.m_immutable = copyTile.GetImmutable();
	}
	
	public void ToggleImmutable()
	{
		m_immutable = !m_immutable;
	}

}
