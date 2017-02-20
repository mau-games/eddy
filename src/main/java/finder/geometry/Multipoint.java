package finder.geometry;

import java.util.ArrayList;

/**
 * The Multipoint class represents an abstract geometric object consisting of zero
 * or more points.
 * 
 * @author Johan Holmberg
 */
public abstract class Multipoint extends Geometry {
	protected ArrayList<Point> points = new ArrayList<Point>();
	
	/**
	 * Adds a point to the end of the list of points.
	 * 
	 * @param p The point.
	 */
	public void addPoint(Point p) {
		points.add(p);
	}
	
	/**
	 * Returns a point at the specified location.
	 * 
	 * @param index The position within the list of points.
	 * @return A point.
	 */
	public Point getPoint(int index) {
		return points.get(index);
	}
	
	/**
	 * Returns the number of points in the construct.
	 * 
	 * @return
	 */
	public int getNumberOfPoints() {
		return points.size();
	}
}
