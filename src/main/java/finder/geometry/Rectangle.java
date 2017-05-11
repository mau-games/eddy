package finder.geometry;

/**
 * The Multipoint class represents an abstract geometric object consisting of zero
 * or more points.
 * 
 * @author Johan Holmberg
 */
public class Rectangle extends Polygon {
	
	/**
	 * Creates an instance of Rectangle with four corners. As a rectangle is
	 * always regular, only two opposite coordinates is needed to construct the
	 * full rectangle.
	 * 
	 * @param c1 The top left corner of a rectangle.
	 * @param c2 The bottom right corner of a rectangle.
	 */
	public Rectangle(Point c1, Point c2) {
		points.add(c1);
		points.add(new Point(c1.getX(), c2.getY()));
		points.add(c2);
		points.add(new Point(c2.getX(), c1.getY()));
	}
	
	public Point getTopLeft() {
		return points.get(0);
	}
	
	public Point getBottomRight() {
		return points.get(2);
	}

	/* (non-Javadoc)
	 * @see finder.geometry.Polygon#getArea()
	 */
	@Override
	public double getArea() {
		Point p1 = points.get(0);
		Point p2 = points.get(2);
		
		return (p2.getX() - p1.getX() + 1) * (p2.getY() - p1.getY() + 1);
	}

	/* (non-Javadoc)
	 * @see finder.geometry.Polygon#getSquareness()
	 */
	@Override
	public double getSquareness() {
		return 1.0;
	}

	/* (non-Javadoc)
	 * @see finder.geometry.Polygon#getSmoothness()
	 */
	@Override
	public double getSmoothness() {
		return 1.0;
	}

	/* (non-Javadoc)
	 * @see finder.geometry.Polygon#getRoundness()
	 */
	@Override
	public double getRoundness() {
		return super.getRoundness();
	}
	
	// TODO: Override *ness functions
	
	@Override
	public boolean overlaps(Polygon p) {
		if (p instanceof Rectangle) {
			return points.get(0).getX() < p.getPoint(2).getX() &&
					points.get(2).getX() > p.getPoint(0).getX() &&
					points.get(0).getY() < p.getPoint(2).getY() &&
					points.get(2).getY() > p.getPoint(0).getY();
		} else {
			return super.overlaps(p);
		}
	}
	
	@Override
	public boolean equals(Object rect) {
		if (rect instanceof Rectangle) {
			Rectangle r = (Rectangle) rect;
			return points.get(0).equals(r.getTopLeft()) && points.get(2).equals(r.getBottomRight());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 7 * points.get(0).getX()
				* 13 * points.get(0).getY()
				* 17 * points.get(2).getX()
				* 19 * points.get(2).getY();
		
		return hash;
	}
	
	@Override
	public boolean contains(Point p){
		return p.getX() >= points.get(0).getX() && p.getX() <= points.get(2).getX() && p.getY() >= points.get(0).getY() && p.getY() <= points.get(2).getY();
	}
}
