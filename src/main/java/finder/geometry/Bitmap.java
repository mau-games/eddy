package finder.geometry;

public class Bitmap extends Polygon {
	@Override
	public double getArea() {
		return points.size();
	}
	
	@Override
	public void addPoint(Point p) {
		if (!points.contains(p)) {
			points.add(p);
		}
	}
	
	/**
	 * Checks whether this instance of Bitmap contains a point p.
	 * 
	 * @param p The point to look for.
	 * @return True if the point is contained, otherwise false.
	 */
	public boolean contains(Point p) {
		return points.contains(p);
	}
	
}
