package util.algorithms;

/**
 * This class represents a basic point in a map.
 * 
 * @author Alexander Baldwin, Malmö University
 */
public class Point2D {

	private int x;
	private int y;
	
	public Point2D(){
		x = 0;
		y = 0;
	}
	
	public Point2D(int x, int y)
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
	
	public boolean equals(Point2D p){
		return x == p.x && y == p.y;
	}
	
	
}
