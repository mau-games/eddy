package game;

import java.util.ArrayList;
import java.util.List;

import finder.geometry.Point;
import game.CoCreativity.AICoCreator;
import gui.controls.Brush;
import gui.controls.Drawer;
import gui.controls.InteractiveMap;
import gui.controls.TileBrush;
import javafx.scene.canvas.GraphicsContext;
import gui.controls.Brush.BrushUsage;
import javafx.scene.image.Image;

public class Tile
{
	protected TileTypes m_type;
	private ArrayList<Point> positions;
	protected Point center;
	private boolean m_immutable;
	public boolean canDraw = true;
	private boolean editable = true; // for Co-Creation
	private boolean placedByAI = false; //for Co-Creation
	public int maxAmountPerRoom;
	public int width = 1; //tile size
	public int height = 1; //tile size
	
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
	
	public void setCenterPosition(Point center)
	{
		this.center = center;
	}
	
	public ArrayList<Point> GetPositions()
	{
		return positions;
	}
	
	//TODO: BUGG FOR THE Boss enemy TILE
	public void setPositions(List<Point> positions)
	{
		this.positions.clear();
		this.positions.addAll(positions);
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
		maxAmountPerRoom = -1;
	}
	
	public Brush modification(Brush brush)
	{
		return brush;
	}
	
	public void PaintTile(Point currentCenter, Room room, Drawer drawer, InteractiveMap interactiveCanvas)
	{
		//TODO: This needs to be done too
	}
	
	public void PaintCanvasTile(Image tileImage, GraphicsContext ctx, double tileSize)
	{
		
	}
	
	public Tile()
	{
		this.positions = new ArrayList<Point>();
//		this.center = 
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
		this.setPlacedByAI(copyTile.getPlacedByAI());
	}
	
	public Tile copy()
	{
		return new Tile(this);
	}
	
	public void ToggleImmutable()
	{
		m_immutable = !m_immutable;
	}

	/*** Tinea CC boolean ***/
	public boolean getEditable() { return editable; }

	//public void ToggleEditable() { editable = !editable; }

	public void setEditable(boolean b) { editable = b; }

	public Drawer GetTypeAsBrush()
	{
		Drawer brush = new Drawer();

		switch(m_type)
		{
			case FLOOR:
				brush.SetBrush(0); //single tile
				brush.SetMainComponent(TileTypes.toTileType(0));
				break;
			case WALL:
				brush.SetBrush(0); //single tile
				brush.SetMainComponent(TileTypes.toTileType(1));
				break;
			case TREASURE:
				brush.SetBrush(0); //single tile
				brush.SetMainComponent(TileTypes.toTileType(2));
				break;
			case ENEMY:
				brush.SetBrush(0); //single tile
				brush.SetMainComponent(TileTypes.toTileType(3));
				break;
			case DOOR:
				brush.SetBrush(0); //single tile
				brush.SetMainComponent(TileTypes.toTileType(4));
				break;
			case ENEMY_BOSS:
				brush.SetBrush(1); //
				brush.SetMainComponent(TileTypes.toTileType(5));
				break;
			case HERO:
				brush.SetBrush(0); //single tile
				brush.SetMainComponent(TileTypes.toTileType(6));
				break;
			case NONE:
				brush.SetBrush(0); //single tile
				brush.SetMainComponent(TileTypes.toTileType(7));
				break;

			default:
				brush.SetBrush(0); //single tile
				brush.SetMainComponent(TileTypes.toTileType(7));
		}

		return brush;
	}

	public void setEditable(AICoCreator aiCC)
	{
		if(aiCC.getControlLevel() == AICoCreator.ControlLevel.LOW || aiCC.getControlLevel() == AICoCreator.ControlLevel.MEDIUM)
			editable = true;
		else
			editable = false;

	}

	public void setPlacedByAI(boolean b)
	{
		placedByAI = b;
	}

	public boolean getPlacedByAI()
	{
		return placedByAI;
	}
}