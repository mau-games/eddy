package game;

import java.util.ArrayList;
import java.util.List;

import finder.geometry.Bitmap;
import finder.geometry.Point;

//TODO: I THINK THERE IS A VERY BIG PROBLEM RELATED
// TO THE MAP ITSELF, I THINK THE X AND Y POSITIONS ARE EXCHANGED
//I WOULD SAY THAT THERE IS A CORE PROBLEM
//BUT I STILL NEED MORE TIME TO FIGURE OUT
//IF IT IS IN MY END OR THE OPPOSITE

/**
 * This class represent a section of the map
 * It may be invalid or valid 
 * @author Alberto Alvarez, Malmö University
 *
 */
public class ZoneNode 
{
	private ZoneNode parent;
	private Bitmap section; 
	private ArrayList<ZoneNode> children;
	private Map refMap;
	private int width;
	private int height;
	
	private boolean valid;

	public ZoneNode(ZoneNode parent, Map map, int w, int h)
	{
		this.parent = parent;
		this.refMap = map;
		this.width = w;
		this.height = h;
		this.children = new ArrayList<ZoneNode>();
		this.section = FillSection();
		
		Validation();
		
		//Maybe divide or expand explicit calls instead
		if(this.parent == null ) //It is the root of a tree
		{
			RectangleDivision(3);
		}
		else if(!valid)
		{
			Expand();
		}
	}
	
	public ZoneNode(int n, Bitmap section, ZoneNode parent, Map map, int w, int h)
	{
		this.parent = parent;
		this.refMap = map;
		this.section = section;
		this.width = w;
		this.height = h;
		this.children = new ArrayList<ZoneNode>();

		Validation();
		
		//Maybe divide or expand explicit calls instead
		if(this.parent == null ) //It is the root of a tree
		{
			RectangleDivision(n);
		}
		else if(!valid)
		{
			Expand();
		}
	}
	
	/**
	 * Only to be called for the root
	 * @param init_x Initial X Coordinate
	 * @param init_y Initial Y Coordinate
	 * @param h Height of the section
	 * @param w Width of the section
	 */
	private Bitmap FillSection(int init_x, int init_y, int w, int h)
	{
		Bitmap sec = new Bitmap();
		List<Point> sectionPoints = section.getPoints();
		
		for(int y = init_y; y < h; ++y)
		{
			for(int x = init_x; x < w; ++x)
			{
				sec.addPoint(sectionPoints.get(y * width + x));
			}
		}
		
		return sec;
	}
	
	private Bitmap FillSection()
	{
		Bitmap sec = new Bitmap();
		
		for(int y = 0; y < height; ++y)
		{
			for(int x = 0; x < width; ++x)
			{
				sec.addPoint(new Point(x,y));
			}
		}
		
		return sec;
	}

	//FOR TESTING WE ARE GOING TO TRY ONLY WITH FLOOR OR WALL :D 
	/**
	 * Expand this node to have children? apply floodfill algorithm
	 * TODO: Maybe is better to expand to have children based on the invalid positioning
	 */
	private void Expand()
	{
		List<Point> c = section.getPoints();
		Tile[] tileMap = refMap.getTileBasedMap();
		
		//iterative - send next point
		int index = 0;
		while(CheckPointsValidity(c, tileMap))
		{
			ArrayList<Point> secPoints = new ArrayList<Point>();
			secPoints = BucketFill(c.get(index), secPoints, tileMap, c);
			
			if(secPoints.size() < 1)
			{
				index++;
				continue;
			}
			
			index = 0;
			c.removeAll(secPoints);
			Bitmap sec = new Bitmap();
			sec.AddAllPoints(secPoints);
			children.add(new ZoneNode(-1, sec, this, refMap, width, height)); //Check these values
		}
	}
	
	private boolean CheckPointsValidity(List<Point> currentPoints, Tile[] tileMap)
	{
		for(Point p : currentPoints)
		{
			if(!tileMap[p.getY() * refMap.getColCount() + p.getX()].GetImmutable())
			{
				return true;
			}
		}
		
		return false;
	}
	
	private ArrayList<Point> BucketFill(Point p, ArrayList<Point> currentPoints, Tile[] tileMap, List<Point> missingPoints)
	{	
		if(p.getX() < 0 || p.getX() > refMap.getColCount() -1 || p.getY() < 0 || p.getY() > refMap.getRowCount() -1)
			return currentPoints;

		if(tileMap[p.getY() * refMap.getColCount() + p.getX()].GetImmutable())
		{
			return currentPoints;
		}
		
		//MAGIC
		currentPoints.add(p);

		//EAST
		Point next = new Point(p.getX() + 1, p.getY());
		if(missingPoints.contains(next) && !currentPoints.contains(next))
		{
			BucketFill(next, currentPoints, tileMap, missingPoints);
		}
		
		//SOUTH
		next = new Point(p.getX(), p.getY() + 1);
		if(missingPoints.contains(next) && !currentPoints.contains(next))
		{
			BucketFill(next, currentPoints, tileMap, missingPoints);
		}
		
		//WEST
		next = new Point(p.getX() - 1, p.getY());
		if(missingPoints.contains(next) && !currentPoints.contains(next))
		{
			BucketFill(next, currentPoints, tileMap, missingPoints);
		}
		
		//NORTH
		next = new Point(p.getX(), p.getY() - 1);
		if(missingPoints.contains(next) && !currentPoints.contains(next))
		{
			BucketFill(next, currentPoints, tileMap, missingPoints);
		}
		
		return currentPoints;
	}
	
	/**
	 * Divide the node in 4 different sections
	 * @param n Layer
	 */
	private void RectangleDivision(int n)
	{
		int new_width = (width)/2;
		int new_height = (height)/2;
		
		children.add(new ZoneNode(n - 1, FillSection(0, 0, new_width, new_height), this, refMap, new_width, new_height));
		children.add(new ZoneNode(n - 1, FillSection(new_width, 0, width, new_height), this, refMap, width - new_width, new_height));
		children.add(new ZoneNode(n - 1, FillSection(0, new_height, new_width, height), this, refMap, new_width, height - new_height));
		children.add(new ZoneNode(n - 1, FillSection(new_width, new_height, width, height), this, refMap, width - new_width, height - new_height));
	}
	
	/***
	 * Checks if the owned section is valid (does not contain any inmutable cell)
	 * @return if the section is valid
	 */
	private boolean Validation()
	{
		Tile[] tileMap = refMap.getTileBasedMap();
		List<Point> points = section.getPoints();
		
		for(Point p : points)
		{	
			if(tileMap[p.getY() * refMap.getColCount() + p.getX()].GetImmutable())
			{
				valid = false;
				return false;
			}
		}
		
		valid = true;
		return true;
	}
	
	public boolean isLeaf()
	{
		return children.size() < 1;
	}
	
	public Bitmap GetSection()
	{
		return section;
	}
	
	public ArrayList<ZoneNode> getChildren()
	{
		return children;
	}
	
	public ArrayList<ZoneNode> traverseToLayer(int layer)
	{
		ArrayList<ZoneNode> returnNodes = new ArrayList<ZoneNode>();
		
		if(--layer < 0 || isLeaf())
		{
			returnNodes.add(this);
			return returnNodes;
		}
		
		for(ZoneNode child : children)
		{
			returnNodes.addAll(child.traverseToLayer(layer));
		}
		
		return returnNodes;
	}
}
