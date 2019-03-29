package finder.geometry;

import java.util.ArrayList;

import game.PathInformation;

public class Bitmap extends Polygon {
	
	public Point medoid;
	
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
	
	public void AddAllPoints(ArrayList<Point> otherPoints)
	{
		this.points.addAll(otherPoints);
	}
	
	public void removePoint(Point p)
	{
		if(contains(p))
			points.remove(p);
	}
	
	public void clearAllPoints()
	{
		points.clear();
	}
	
	public void CalculateMedoid()
	{
		int currentMin = Integer.MAX_VALUE;
		for(Point current : points)
		{
			int currentAmount = 0;
			for(Point other : points)
			{
				currentAmount += distManhattan(current, other);
			}
			
			if(currentAmount < currentMin)
			{
				currentMin = currentAmount;
				medoid = current;
			}
		}
	}
	
	public int distManhattan(Point a, Point b)
	{
		return (Math.abs(b.getX() - a.getX()) + Math.abs(b.getY() - a.getY()));
	}
	
}
