package gui.controls;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import finder.geometry.Bitmap;
import finder.geometry.Point;
import game.Room;
import game.Tile;
import game.TileTypes;
import game.tiles.BossEnemyTile;

public abstract class Brush 
{
	public enum BrushUsage
	{
		DEFAULT, //if tile has brush usage as default, the default use (one selected by the user is going to be used)
		BUCKET, 
		CUSTOM //if tile has brush usage as custom, the tile will provide size or even shape
	}
	
	public enum NeighborhoodStyle
	{
		NEUMANN,
		MOORE
	}
	

	protected TileTypes mainComponent; //Main "Color" of the brush
	protected Bitmap drawableTiles;
	protected int size;
	protected Point center;
	protected boolean drew;
	
	protected Tile brushTile;
	protected NeighborhoodStyle neighborStyle; //this info is set only if custom
	protected boolean immutable = false; //this info is set only if custom
	
	public Brush()
	{
		mainComponent = null;
		drawableTiles = new Bitmap();
		drew = false;
		size = 1;
		center = null;
	}
	
	public void SetMainComponent(TileTypes type)
	{
		mainComponent = type;
	}
	
	public void SetMainComponent(Tile type)
	{
		brushTile = type;
		mainComponent = type.GetType();
	}
	
	public void setNeighborhoodStyle(NeighborhoodStyle style) {
		neighborStyle = style;
	}
	
	public void setImmutable(boolean value) {
		immutable = value;
	}

	private void restore(Brush prev)
	{
		this.drawableTiles = prev.drawableTiles;
		this.size = prev.size;
		this.center = prev.center;
		this.drew = prev.drew;
		
	}
	
	public TileTypes GetMainComponent()
	{
		return mainComponent;
	}
	
	public int GetBrushSize()
	{
		return size;
	}
	
	public void SetBrushSize(int value)
	{
		size = value;
	}
	
	public Bitmap GetDrawableTiles()
	{
		return drawableTiles;
	}
	
	public void SetDrew()
	{
		drew = true;
	}
	
	public boolean canBrushDraw()
	{
		return brushTile.canDraw;
	}
	
	/**
	 * Updates the tiles that are drawable for this brush based
	 * based on the position of the tile and the brush size
	 * @param x X position of the hovered tile
	 * @param y Y position of the hovered tile
	 * @param room active map
	 */
	public abstract void UpdateDrawableTiles(int x, int y, Room room);
	
	/**
	 * Fill the Bitmap based on its neighbors and brush size
	 * @param p Point to be evaluated
	 * @param width Width of the map
	 * @param height Height of the map
	 * @param layer Current evaluated size (evaluated size - 1)
	 */
	abstract protected void FillDrawable(Point p, int width, int height, int layer);
	
	/**
	 * Fill the Bitmap based on its neighbors and brush size
	 * @param p Point to be evaluated
	 * @param width Width of the map
	 * @param height Height of the map
	 * @param layer Current evaluated size (evaluated size - 1)
	 */
	public void simulateDrawing(Point currentCenter, Room room, Drawer boss, InteractiveMap interactiveCanvas)
	{
		if(GetMainComponent() == null)
			return;
		
		Tile currentTile = null;
		this.immutable = boss.GetModifierValue("Lock");
		
		for(Point position : GetDrawableTiles().getPoints())
		{
			currentTile = room.getTile(position.getX(), position.getY());
			
			// Let's discard any attempts at erasing the doors
			if(currentTile.GetType() == TileTypes.DOOR
					|| currentTile.GetType() == TileTypes.HERO)
				continue;
			
			//Check if we are about to paint over a boss --> We can also just check if the tile is in the custom tiles
//			if(currentTile.GetType() == TileTypes.ENEMY_BOSS)
//			{
//				Tile prev = room.replaceCustomForNormal(currentTile);
//				
//				if(prev != null) //We actually erased something
//				{
//					//"ERASE" TILES
//					for(Point prevPosition :prev.GetPositions())
//					{
//						prev.PaintTile(prevPosition, room, boss, interactiveCanvas);
//					}
//				}
//			}
//			
			currentTile.SetImmutable(immutable);
			room.setTile(position.getX(), position.getY(), GetMainComponent());
		}
	}
	
	/**
	 * Fill the Bitmap based on its neighbors and brush size
	 * @param p Point to be evaluated
	 * @param width Width of the map
	 * @param height Height of the map
	 * @param layer Current evaluated size (evaluated size - 1)
	 */
	public void Draw(Point currentCenter, Room room, Drawer boss, InteractiveMap interactiveCanvas)
	{
		if(GetMainComponent() == null)
			return;
		
		Tile currentTile = null;
		this.immutable = boss.GetModifierValue("Lock");
		boolean noRules = boss.GetModifierValue("No-Rules");
		
		for(Point position : GetDrawableTiles().getPoints())
		{
			currentTile = room.getTile(position.getX(), position.getY());
			
			ActionLogger.getInstance().storeAction(ActionType.CHANGE_TILE, 
													View.ROOM, 
													TargetPane.MAP_PANE,
													true,
													room, //ROOM A
													position, //Pos A
													currentTile.GetType(), //TILE A
													GetMainComponent()); //TILE B
			
			// Let's discard any attempts at erasing the doors
			if(!noRules && (currentTile.GetType() == TileTypes.DOOR
					|| currentTile.GetType() == TileTypes.HERO))
				continue;
			
			//Check if we are about to paint over a boss --> We can also just check if the tile is in the custom tiles
			if(currentTile.GetType() == TileTypes.ENEMY_BOSS)
			{
				Tile prev = room.replaceCustomForNormal(currentTile);
				
				if(prev != null) //We actually erased something
				{
					//"ERASE" TILES
					for(Point prevPosition :prev.GetPositions())
					{
						prev.PaintTile(prevPosition, room, boss, interactiveCanvas);
					}
				}
			}
			
			currentTile.SetImmutable(immutable);
			
			if(GetMainComponent() == TileTypes.DOOR)
			{
				room.createDoor(new util.Point(position.getX(), position.getY()));
			}
			else
			{
				if(currentTile.GetType() == TileTypes.DOOR)
					room.removeDoor(new util.Point(position.getX(), position.getY()));
				
				room.setTile(position.getX(), position.getY(), GetMainComponent());
			}
			
			interactiveCanvas.getCell(position.getX(), position.getY()).
				setImage(interactiveCanvas.getImage(GetMainComponent(), interactiveCanvas.scale));
		}
	}
	
	/***
	 * Calculates the neighborhood given a certain point
	 * @param p Center point of the neighborhood
	 * @return The North, East, South and West neighbor of the point
	 */
	protected List<Point> GetNeumannNeighborhood(Point p)
	{
		return Arrays.asList(	new Point(p.getX(), p.getY() + 1),
								new Point(p.getX() + 1, p.getY()),
								new Point(p.getX(), p.getY() - 1),
								new Point(p.getX() - 1, p.getY()));						
	}
	
	/**
	 * Calculates the neighborhood given a certain point
	 * @param p Center point of the neighborhood
	 * @return N, NE, E, SE, S, SW, W, NW neighbor of the point
	 */
	protected List<Point> GetMooreNeighborhood(Point p)
	{
		return Arrays.asList(	new Point(p.getX(), p.getY() + 1),
								new Point(p.getX() + 1, p.getY() + 1),				
								new Point(p.getX() + 1, p.getY()),
								new Point(p.getX() + 1, p.getY() - 1),	
								new Point(p.getX(), p.getY() - 1),
								new Point(p.getX() - 1, p.getY() - 1),
								new Point(p.getX() - 1, p.getY()),
								new Point(p.getX() - 1, p.getY() + 1));		
	}
	
	Function<Point, List<Point>> f = new Function<Point, List<Point>>() {
		public List<Point> GetMooreNeighborhood(Point p)
		{
			return Arrays.asList(	new Point(p.getX(), p.getY() + 1),
									new Point(p.getX() + 1, p.getY() + 1),				
									new Point(p.getX() + 1, p.getY()),
									new Point(p.getX() + 1, p.getY() - 1),	
									new Point(p.getX(), p.getY() - 1),
									new Point(p.getX() - 1, p.getY() - 1),
									new Point(p.getX() - 1, p.getY()),
									new Point(p.getX() - 1, p.getY() + 1));		
		}

		@Override
		public List<Point> apply(Point t) {
			return GetMooreNeighborhood(t);
		}
	};
}
