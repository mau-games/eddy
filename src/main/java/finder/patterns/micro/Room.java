package finder.patterns.micro;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import finder.geometry.Geometry;
import finder.geometry.Point;
import finder.geometry.Polygon;
import finder.geometry.Rectangle;
import finder.patterns.Pattern;
import game.Map;
import game.TileTypes;

/**
 * This class represents the dungeon game design pattern called Room.
 * 
 * @author Johan Holmberg
 */
public class Room extends Pattern {
	
	public Room(Geometry geometry) {
		boundaries = geometry;
	}

	// TODO: This might be slightly more efficient: https://gist.github.com/pelya/4babc0bab224bd22e6f30ce17d784c07
	// TODO: Consider non-rectangular geometries in the future.
	// TODO: Consider non-rectangular rooms in the future.
	/**
	 * Searches a map for rooms. The searchable area can be limited by a set of
	 * boundaries. If these boundaries are invalid, no search will be
	 * performed.
	 * 
	 * @param map The map to search.
	 * @param boundary The boundary that limits the searchable area.
	 * @return A list of found room pattern instances.
	 */
	public static List<Pattern> matches(Map map, Geometry boundary) {

		ArrayList<Pattern> results = new ArrayList<Pattern>();

		// Check boundary sanity.
		Point p1 = ((Rectangle) boundary).getTopLeft();
		Point p2 = ((Rectangle) boundary).getBottomRight();
		if (map == null || boundary == null ||
				map.getRowCount() < 3 ||
				map.getColCount() < 3 ||
				p2.getX() - p1.getX() < 2 ||
				p2.getY() - p1.getY() < 2 ||
				p1.getX() >= map.getColCount() ||
				p2.getX() >= map.getColCount() ||
				p1.getY() >= map.getRowCount() ||
				p2.getY() >= map.getRowCount()) {
			return results;
		}
		System.out.println(p1.getX() + ", " + p1.getY() + "; " + p2.getX() + ", " + p2.getY());
		
//		HashSet<Point> candidates = new HashSet<Point>();
//		for (int i = p1.getX() + 1; i < p2.getX() - 1; i++) {
//			for (int j = p1.getY() + 1; j < p2.getY() - 1; j++) {
//				if (map.getTile(i, j) != TileTypes.WALL) {
//					candidates.add(new Point(i, j));
//				}
//			}
//		}
//		for (Point p : candidates) {
//			if (isRoom(map, p)) {
//				growRoom(map, candidates, p);
//			}
//		}
		
		// -------------------8<-------------------------------------
		
		// While searching for rooms, we treat anything not being a wall as a
		// part of a potential room.
		ArrayDeque<Rectangle> candidates = new ArrayDeque<Rectangle>();
		HashSet<Rectangle> rects = new HashSet<Rectangle>();
		int width = 0;
		Point start = null;
		Point end = null;
		for (int i = p1.getX(); i <= p2.getX(); i++) {
			for (int j = p1.getY(); j <= p2.getY(); j++) {
				if (map.getTile(i, j) == TileTypes.WALL) {
					if (width >= 3) { // TODO: Put this into the config file
						end = new Point(i, j - 1);
						candidates.push(new Rectangle(start, end));
						System.out.println("Added rectangle: "
								+ start.getX() + "," + start.getY() + "; "
								+ end.getX() + ", " + end.getY());
					}
					start = null;
					width = 0;
				} else {
					if (start == null) {
						start = new Point(i, j);
					}
					width++;
				}
			}
			if (width >= 3) { // TODO: Put this into the config file
				end = new Point(i, p2.getY());
				candidates.push(new Rectangle(start, end));
				System.out.println("Added rectangle: "
						+ start.getX() + "," + start.getY() + "; "
						+ end.getX() + "," + end.getY());
			}
			start = null;
			width = 0;
		}
		System.out.println("Number of candidates: " + candidates.size());
		
		// For each candidate: Try to grow. If big enough, push to rectangles.
		for (Rectangle candidate : candidates) {
			candidate = tryGrow(map, boundary, candidate);
			if (candidate != null) {
				rects.add(candidate);
			}
		}
		System.out.println("Number of rooms: " + rects.size());
		for (Rectangle rect : rects) {
			results.add(new Room(rect));
		}

		return results;
	}
	
	private static boolean isRoom(Map map, Point p) {
		int x = p.getX();
		int y = p.getY();
		
		if (map.getTile(x, y) == TileTypes.FLOOR ||
				map.getTile(x - 1, y) == TileTypes.FLOOR ||
				map.getTile(x + 1, y) == TileTypes.FLOOR) {
			return false;
		}
		
		for (int i = x - 1; i <= x + 1; i++) {
			if (map.getTile(i, y - 1) == TileTypes.FLOOR) {
				return false;
			}
		}
		
		for (int i = x - 1; i <= y + 1; i++) {
			if (map.getTile(i, y + 1) == TileTypes.FLOOR) {
				return false;
			}
		}
		
		return true;
	}
	
	private static Polygon growRoom(Map map, HashSet candidates, Point point) {
		HashSet<Point> ps = new HashSet<Point>();
		int x = point.getX();
		int y = point.getY();
		
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				Point p = new Point(x, y);
				ps.add(p);
				candidates.remove(p);
			}
		}
		
		
		
		return null;
	}
	
	private static boolean hasThreeNeighbours(Map map, Point p) {
		return false;
	}
	
	private static Rectangle tryGrow(Map map, Geometry boundary, Rectangle candidate) {
		int y1 = candidate.getTopLeft().getY();
		int y2 = candidate.getBottomRight().getY();
		int left = candidate.getTopLeft().getX();
		int right = left;

		System.out.println("Hello with " + left + " and " + y1 + "," + y2);
		
		// First, try to grow leftwards
		while (left - 1 >= ((Rectangle) boundary).getTopLeft().getY()
				&& isRectangle(map, left - 1, y1, y2)) {
			left--;
			System.out.println("Checked " + left);
		}
		
		// Then, try to grow rightwards
		while (right + 1 <= ((Rectangle) boundary).getBottomRight().getY()
				&& isRectangle(map, right + 1, y1, y2)) {
			right++;
			System.out.println("Checked " + right);
		}
		
		if (right - left >= 2) { // TODO: Put this into the config file (i.e. 3 - 1)
			System.out.println("Found room!" + y1 + "," + left + "; " + y2 + "," + right);
			return new Rectangle(new Point(y1, left), new Point(y2, right));
		} else {
			return null;
		}
	}

	private static boolean isRectangle(Map map, int row, int y1, int y2) {
		for (int i = y1; i <= y2; i++) {
			System.out.println(map.getTile(row, i));
			if (map.getTile(row, i) == TileTypes.WALL) {
				return false;
			}
		}
		
		return true;
	}
}
