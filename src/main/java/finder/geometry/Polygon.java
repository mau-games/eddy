package finder.geometry;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * The Polygon class represents a geometric object consisting of zero
 * or more points.
 * 
 * @author Johan Holmberg
 */
public class Polygon extends Multipoint {
	
	/**
	 * Creates an instance of Polygon.
	 */
	public Polygon() {
		super();
	}
	
	/**
	 * Calculates the area of the current polygon object.
	 * 
	 * @return The area.
	 */
	public double getArea() {
		double sum = 0;
		
		for (int i = 0; i < getNumberOfPoints() - 1; i++) {
			sum += points.get(i).getX() * points.get(i + 1).getY()
					- points.get(i).getY() * points.get(i + 1).getX();
		}
		// Add the last ones
		sum += points.get(getNumberOfPoints() - 1).getX() * points.get(0).getY()
				- points.get(getNumberOfPoints() - 1).getY() * points.get(0).getX();
		
		return Math.abs(sum / 2);
	}
	
	/**
	 * Calculates the squareness of the current polygon object. A completely
	 * smooth rectangle will yield a value of 1.0, whereas a completely
	 * irregular shape will yield a value of 0.
	 * 
	 * @return The squareness value.
	 */
	public double getSquareness() {
		// TODO: Implement this.
		return -1;
	}
	
	/**
	 * Calculates the smoothness of the current polygon object. A completely
	 * smooth object will yield a value of 1.0, whereas a completely irregular
	 * shape will yield a value of 0.
	 * 
	 * @return The smoothness value.
	 */
	public double getSmoothness() {
		// TODO: Implement this.
		return -1;
	}
	
	/**
	 * Calculates the roundness of the current polygon object. A circle will
	 * yield a value of 1.0, whereas a completely irregular shape will yield a
	 * value of 0.
	 * 
	 * <p>See https://en.wikipedia.org/wiki/Roundness_(object) for more info.
	 * 
	 * @return The roundness value.
	 */
	public double getRoundness() {
		// TODO: Implement this.
		return -1;
	}
	
	/**
	 * Takes two polygons and combines them into a single object.
	 * 
	 * @param p The polygon to combine with.
	 * @return A new polygon object.
	 */
	public Polygon union(Polygon p) {
		// TODO: Implement this. Maybe from http://www.cs.ucr.edu/~vbz/cs230papers/martinez_boolean.pdf?
		
//		if (!this.overlaps(p)) {
//			return null;
//		}
//		
//		ArrayList<Line> edges = new ArrayList<Line>();
//		for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
//			if (points.get(i).getX() > points.get(j).getX()) {
//				edges.add(new Line(points.get(j), points.get(i)));
//			} else {
//				edges.add(new Line(points.get(i), points.get(j)));
//			}
//			
//		}
//		for (int i = 0, j = p.getNumberOfPoints() - 1; i < p.getNumberOfPoints(); j = i++) {
//			if (p.getPoint(i).getX() > p.getPoint(j).getX()) {
//				edges.add(new Line(p.getPoint(j), p.getPoint(i)));
//			} else {
//				edges.add(new Line(p.getPoint(i), p.getPoint(j)));
//			}
//		}
//		edges.sort((Line l1, Line l2) -> l1.getPoint(0).getX() - l2.getPoint(0).getX());
//		
//		PriorityQueue<Line> pq = new PriorityQueue<Line>(edges);
//		PriorityQueue<Line> s = new PriorityQueue<Line>();
//		Line e = null;
//		int sValue = Integer.MIN_VALUE;
//		
//		while (!pq.isEmpty()) {
//			e = pq.poll();
//			sValue = e.getPoint(0).getX();
//			
//			
//		}
		
//		ArrayList<Point> ps = new ArrayList<Point>();
//		ps.addAll(points);
//		ps.addAll(p.getPoints());
//		ps.sort((Point p1, Point p2) -> p1.getX() - p2.getX());
//		PriorityQueue<Point> pq = new PriorityQueue<Point>(ps);
		
//		while (!pq.isEmpty()) {
//			// something something event
//			pq.poll();
//			
//		}
		
		return null;
	}
	
	/**
	 * Checks whether two polygons overlap or not.
	 * 
	 * @param p The polygon to check against.
	 * @return True if the two polygons overlap, otherwise false.
	 */
	public boolean overlaps(Polygon p) {
		for (int i = 0; i < points.size(); i++) {
			if (p.contains(points.get(i))) {
				return true;
			}
		}
		for (int i = 0; i < p.getNumberOfPoints(); i++) {
			if (this.contains(p.getPoint(i))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks whether this instance of Polygon contains a point p.
	 * 
	 * @param p The point to look for.
	 * @return True if the point is contained, otherwise false.
	 */
	public boolean contains(Point p) {
		// No need to check if the polygon is a line, then check if not
		// within boundary box
		Polygon bounds = getBounds();
		if (points.size() < 2 ||
				p.getX() < bounds.getPoint(0).getX() ||
				p.getX() > bounds.getPoint(2).getX() ||
				p.getY() < bounds.getPoint(0).getY() ||
				p.getY() > bounds.getPoint(2).getY()) {
			return false;
		}
        
        boolean inside = false;
        int denominator = 0;
        for ( int i = 0, j = points.size() - 1 ; i < points.size() ; j = i++ ) {
        	denominator = points.get(j).getY() - points.get(i).getY() + points.get(i).getX();
        	if (denominator < 1) {
        		denominator = 1;
        	}
        	
            if ( ( points.get(i).getY() > p.getY() ) != ( points.get(j).getY() > p.getY() ) &&
                 p.getX() < ( points.get(j).getX() - points.get(i).getX() )
                 * (p.getY() - points.get(i).getY())
                 / denominator) {
                inside = !inside;
            }
        }

        return inside;
	}
	
	private Polygon getBounds() {
		Polygon bounds = new Polygon();
		Point p = null;
		
		int xMin = Integer.MAX_VALUE;
        int yMin = Integer.MAX_VALUE;
        int xMax = Integer.MIN_VALUE;
        int yMax = Integer.MIN_VALUE;

        for (int i = 0; i < points.size(); i++) {
        	p = points.get(i);
            xMin = Math.min(xMin, p.getX());
            xMax = Math.max(xMax, p.getX());
            yMin = Math.min(yMin, p.getY());
            yMax = Math.max(yMax, p.getY());
        }
        
        bounds.addPoint(new Point(xMin, yMin));
        bounds.addPoint(new Point(xMax, yMin));
        bounds.addPoint(new Point(xMax, yMax));
        bounds.addPoint(new Point(xMin, yMax));
        
        return bounds;
	}
}
