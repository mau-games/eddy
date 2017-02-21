package finder.patterns.micro;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import finder.geometry.Geometry;
import finder.geometry.Point;
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
				map.getRowCount() == 0 ||
				map.getColCount() == 0 ||
				p1.getX() >= map.getColCount() ||
				p2.getX() >= map.getColCount() ||
				p1.getY() >= map.getRowCount() ||
				p2.getY() >= map.getRowCount()) {
			return results;
		}
		System.out.println(p1.getX() + ", " + p1.getY() + "; " + p2.getX() + ", " + p2.getY());
		
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
						end = new Point(i, j);
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
				end = new Point(i, p2.getX());
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
//		results = new ArrayList<Pattern>(hset);

		return results;
	}
	
	private static Rectangle tryGrow(Map map, Geometry boundary, Rectangle candidate) {
		int y1 = candidate.getTopLeft().getY();
		int y2 = candidate.getBottomRight().getY();
		int left = candidate.getTopLeft().getX();
		int right = left;
		
		// First, try to grow upwards
		while (left >= ((Rectangle) boundary).getTopLeft().getY()
				&& isRectangle(map, left, y1, y2)) {
			left--;
		}
		
		// Then, try to grow downwards
		while (right <= ((Rectangle) boundary).getTopLeft().getY()
				&& isRectangle(map, right, y1, y2)) {
			right++;
		}
		
		if (right - left >= 3) { // TODO: Put this into the config file
			System.out.println("Found room!" + y1 + "," + left + "; " + y2 + "," + right);
			return new Rectangle(new Point(y1, left), new Point(y2, right));
		} else {
			return null;
		}
	}

	private static boolean isRectangle(Map map, int col, int y1, int y2) {
		for (int i = y1; i <= y2; i++) {
			if (map.getTile(col, i) == TileTypes.WALL) {
				return false;
			}
		}
		
		return true;
	}
}
