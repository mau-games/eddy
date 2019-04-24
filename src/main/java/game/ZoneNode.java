package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import finder.geometry.Bitmap;
import finder.geometry.Point;

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
	private List<Integer> sortedKeys;
	private HashMap<Integer, Point> s;
	private ArrayList<ZoneNode> children;
	private Room refMap;
	private int width;
	private int height;
	
	private boolean valid;

	public ZoneNode(ZoneNode parent, Room room, int w, int h)
	{
		s = new HashMap<Integer, Point>();
		this.parent = parent;
		this.refMap = room;
		this.setWidth(w);
		this.setHeight(h);
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
	
	public ZoneNode(int n, Bitmap section, ZoneNode parent, Room room, int w, int h)
	{
		s = new HashMap<Integer, Point>();
		this.parent = parent;
		this.refMap = room;
		this.section = section;
		
		for(Point p : section.getPoints())
		{
			s.put(p.getY() * refMap.getColCount() + p.getX(), p);
		}
		
		this.setWidth(w);
		this.setHeight(h);
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
	
	public ZoneNode(ZoneNode copy)
	{
		this.s = new HashMap<Integer, Point>();
		this.parent = copy.parent;
		this.refMap = new Room(copy.refMap, this);
		this.setWidth(copy.getWidth());
		this.setHeight(copy.getHeight());
		this.section = FillSection();
		this.children = new ArrayList<ZoneNode>();
		this.valid = copy.valid;
		
		RectangleDivision(-1);
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
				sec.addPoint(sectionPoints.get(y * getWidth() + x));
				s.put(y * refMap.getColCount() + x, new Point(x,y));
			}
		}
		
		return sec;
	}
	
	private Bitmap FillSection()
	{
		Bitmap sec = new Bitmap();
		
		for(int y = 0; y < getHeight(); ++y)
		{
			for(int x = 0; x < getWidth(); ++x)
			{
				sec.addPoint(new Point(x,y));
				s.put(y * refMap.getColCount() + x, new Point(x,y));
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
			children.add(new ZoneNode(-1, sec, this, refMap, getWidth(), getHeight())); //Check these values
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
		int new_width = (getWidth())/2;
		int new_height = (getHeight())/2;
		
		children.add(new ZoneNode(n - 1, FillSection(0, 0, new_width, new_height), this, refMap, new_width, new_height));
		children.add(new ZoneNode(n - 1, FillSection(new_width, 0, getWidth(), new_height), this, refMap, getWidth() - new_width, new_height));
		children.add(new ZoneNode(n - 1, FillSection(0, new_height, new_width, getHeight()), this, refMap, new_width, getHeight() - new_height));
		children.add(new ZoneNode(n - 1, FillSection(new_width, new_height, getWidth(), getHeight()), this, refMap, getWidth() - new_width, getHeight() - new_height));
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
	
	public Room GetMap()
	{
		return refMap;
	}
	
	public Bitmap GetSection()
	{
		return section;
	}
	
	public HashMap<Integer, Point> GetS()
	{
		return s;
	}
	
	public List<Integer> GetOrderedKeys()
	{	
		if(sortedKeys == null)
		{
			sortedKeys=new ArrayList(s.keySet());
			Collections.sort(sortedKeys);
		}
		
		return sortedKeys;
	}
	
	public ArrayList<ZoneNode> getChildren()
	{
		return children;
	}
	
	public void SetRefMap(Room room)
	{
		this.refMap = room;
		
		for(ZoneNode child : children)
		{
			child.SetRefMap(room);
		}	
	}
	
	public void UpdateRefMap(int[] updatedMatrix)
	{
		this.refMap.Update(updatedMatrix);
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
	
	public ArrayList<ZoneNode> GetAllValidZones()
	{
		ArrayList<ZoneNode> returnNodes = new ArrayList<ZoneNode>();
		
		if(isLeaf() || valid)
		{
			returnNodes.add(this);
			return returnNodes;
		}
		
		for(ZoneNode child : children)
		{
			returnNodes.addAll(child.GetAllValidZones());
		}
		
		return returnNodes;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
