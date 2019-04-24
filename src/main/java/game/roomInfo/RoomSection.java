package game.roomInfo;

import java.util.ArrayList;
import util.Point;

public class RoomSection
{
	ArrayList<Point> sectionPosition;
	boolean door = false;
	int enemies = 0;
	int treasures = 0;
	
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
	
	public void addEnemy() {enemies++;}
	public void addTreasure() {treasures++;}
	
	public int getEnemies() {return enemies;}
	public int getTreasures() {return treasures;}
	
	public void addPoint(Point p)
	{
		sectionPosition.add(p);
	}
}
