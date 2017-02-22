package finder.geometry;

/**
 * The Point class represents a point in a map.
 * 
 * @author Johan Holmberg
 */
public class Point extends Geometry {
	
	private int x;
	private int y;
	
	/**
	 * Creates a point at (0,0).
	 */
	public Point() {
		x = 0;
		y = 0;
	}
	
	/**
	 * Creates a point at (x, y).
	 * 
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns the x coordinate.
	 * 
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * Sets the x coordinate.
	 * 
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Returns the y coordinate.
	 * 
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * Sets the y coordinate.
	 * 
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public boolean equals(Object point) {
		if (point instanceof Point) {
			Point p = (Point) point;
			return x == p.getX() && y == p.getY();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 13 * x * 17 * y;
		
		return hash;
	}
}
