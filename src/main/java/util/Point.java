package util;

/**
 * This class represents a basic point in a map.
 * 
 * @author Alexander Baldwin, Malmö University
 */
public class Point {

	private int x;
	private int y;
	
	public Point(){
		x = 0;
		y = 0;
	}
	
	public Point(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public boolean equals(Point p){
		return x == p.x && y == p.y;
	}
	
	public void setX(int value) { x = value; }
	public void setY(int value) { y = value; }
}
