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
		
		return (p2.getX() - p1.getX()) * (p2.getY() - p1.getY());
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
		// TODO Auto-generated method stub
		return super.getRoundness();
	}
	
	// TODO: Override *ness functions
	
}
