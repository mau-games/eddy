package finder.patterns.micro;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import finder.geometry.Bitmap;
import finder.geometry.Geometry;
import finder.geometry.Point;
import finder.geometry.Polygon;
import finder.geometry.Rectangle;
import finder.patterns.Pattern;
import game.Map;
import game.TileTypes;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;

/**
 * This class represents the dungeon game design pattern called Door.
 * 
 * @author Johan Holmberg
 */
public class Door extends Pattern {
	
	public Door(Geometry geometry, Map map) {
		boundaries = geometry;
		this.map = map;
	}
	
	@Override
	/**
	 * Returns a measure of the quality of this pattern.
	 * 
	 * <p>The quality for a room is decided by two factors:<br>
	 * * The ratio of the room's area versus it's bounding rectangle<br>
	 * * The deviation from a set area
	 *  
	 * @return A number between 0.0 and 1.0 representing the quality of the pattern (where 1 is best)
	 */
	public double getQuality() {
		//Probably just return 0 here, doors don't have any particular quality attributes (yet).
		
		return 0;
	}

	// TODO: Consider non-rectangular geometries in the future.
	/**
	 * Searches a map for doors. The searchable area can be limited by a set of
	 * boundaries. If these boundaries are invalid, no search will be
	 * performed.
	 * 
	 * @param map The map to search.
	 * @param boundary The boundary that limits the searchable area.
	 * @return A list of found room pattern instances.
	 */
	public static List<Pattern> matches(Map map, Geometry boundary) {

		ArrayList<Pattern> results = new ArrayList<Pattern>();
		
		if (map == null) {
			return results;
		}
		
		if (boundary == null) {
			boundary = new Rectangle(new Point(0, 0),
					new Point(map.getColCount() -1 , map.getRowCount() - 1));
		}

		// Check boundary sanity.
		Point p1 = ((Rectangle) boundary).getTopLeft();
		Point p2 = ((Rectangle) boundary).getBottomRight();
		if (p1.getX() >= map.getColCount() ||
				p2.getX() >= map.getColCount() ||
				p1.getY() >= map.getRowCount() ||
				p2.getY() >= map.getRowCount()) {
			return results;
		}
		
		int[][] matrix = map.toMatrix();
		
		// We only need to consider the rim of the geometry
		if (p1.equals(p2)) {
			if (isDoor(matrix, p1.getX(), p1.getY())) {
				results.add(new Door(new Point(p1.getX(), p1.getY()),map));
			}
			return results;
		}
		
		for (int i = p1.getX(); i <= p2.getX(); i++) {
			if (isDoor(matrix, i, p1.getY())) {
				results.add(new Door(new Point(i, p1.getY()),map));
			}
			if (isDoor(matrix, i, p2.getY())) {
				results.add(new Door(new Point(i, p2.getY()),map));
			}
		}
		
		for (int j = p1.getY() + 1; j < p2.getY(); j++) { // Don't count the corners twice
			if (isDoor(matrix, p1.getX(), j)) {
				results.add(new Door(new Point(p1.getX(), j),map));
			}
			if (isDoor(matrix, p2.getX(), j)) {
				results.add(new Door(new Point(p2.getX(), j),map));
			}
		}

		return results;
	}
	
	private static boolean isDoor(int[][] map, int x, int y) {
		return map[x][y] == 4;
	}
}
