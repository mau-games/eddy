package gui.controls;

import java.util.Arrays;
import java.util.List;

import finder.geometry.Bitmap;
import finder.geometry.Point;
import game.Room;
import game.TileTypes;

public abstract class Brush 
{
	protected TileTypes mainComponent; //Main "Color" of the brush
	protected Bitmap drawableTiles;
	protected int size;
	protected Point center;
	protected boolean drew;
	
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
}
