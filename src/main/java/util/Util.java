package util;

import javafx.geometry.Point2D;

public class Util {

	public static int manhattanDistance(Point2D start, Point2D end){
		return (int) Math.round(Math.abs(start.getX() - end.getX()) + Math.abs(start.getY() - end.getY()));
	}
	
	public static double straightLineDistance(Point2D start, Point2D end){
		return start.distance(end);
	}
	
}
