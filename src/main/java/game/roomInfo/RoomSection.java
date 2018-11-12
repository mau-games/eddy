package game.roomInfo;

import java.util.ArrayList;
import util.Point;

public class RoomSection
{
	ArrayList<Point> sectionPosition;
	boolean door = false;
	
	public RoomSection()
	{
		sectionPosition = new ArrayList<Point>();
		door = false;
	}
	
	public boolean hasDoor() { return door; }
	
	public ArrayList<Point> getPositions() { return sectionPosition; }
	
	public void doorFound()
	{
		door = true;
	}
	
	public void addPoint(Point p)
	{
		sectionPosition.add(p);
	}
}
