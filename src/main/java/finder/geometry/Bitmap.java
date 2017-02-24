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
}
