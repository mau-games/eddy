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
	
	public Point(double x, double y) {
		// TODO Auto-generated constructor stub
		this.x = (int)x;
		this.y = (int)y;
	}

	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return equals((Point)obj);
	}
	
	public boolean equals(Point p){
		return x == p.x && y == p.y;
	}
	
	public void setX(int value) { x = value; }
	public void setY(int value) { y = value; }
	
	@Override
	public String toString()
	{
		return "(" + x + "," + y + ")";
	}
	
	public static final finder.geometry.Point castToGeometry(final Point from)
	{
		return new finder.geometry.Point(from.x, from.y);
	}
}
