package game;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//import finder.geometry.Point;
import util.Point;

@XmlRootElement(name="Tile")
public class Tile
{
	private TileTypes m_type;
	private Point position;
	private boolean m_immutable;
	
	@XmlElement(name="Type")
	public TileTypes getM_type() {
		return m_type;
	}


	public void setM_type(TileTypes m_type) {
		this.m_type = m_type;
	}

	@XmlElement(name="Position")
	public Point getPosition() {
		return position;
	}


	public void setPosition(Point position) {
		this.position = position;
	}

	@XmlElement(name="Immutable")
	public boolean isM_immutable() {
		return m_immutable;
	}

	public void setM_immutable(boolean m_immutable) {
		this.m_immutable = m_immutable;
	}

	


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
	
	public Tile() {}
	
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
