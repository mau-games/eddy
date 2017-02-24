package finder.geometry;

public class Bitmap extends Polygon {
	@Override
	public double getArea() {
		return points.size();
	}
}
