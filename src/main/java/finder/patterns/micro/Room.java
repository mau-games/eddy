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
import finder.patterns.SpacialPattern;
import game.Map;
import game.TileTypes;
import generator.config.GeneratorConfig;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;

/**
 * This class represents the dungeon game design pattern called Room.
 * 
 * @author Johan Holmberg
 */
public class Room extends SpacialPattern {
	
	private ConfigurationUtility config;
	
	double desiredRectangleArea = 36;
	double floorRatioWeight = 0.8;
	double areaDeviationWeight = 0.2;
	
	public Room(GeneratorConfig config, Geometry geometry) {
		boundaries = geometry;

		desiredRectangleArea = config.getChamberTargetArea();
		floorRatioWeight = config.getChamberTargetSquareness();
		areaDeviationWeight = config.getChamberAreaCorrectness();
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
		Polygon polygon = (Polygon) boundaries;
		Rectangle rectangle = constructRectangle();
		double polyArea = polygon.getArea();
		double rectArea = rectangle.getArea();
		double floorRatio = polyArea / rectArea;
		double areaDeviation = 1;
		
		areaDeviation = 1 - Math.abs(desiredRectangleArea - polyArea) / desiredRectangleArea;		
		if (areaDeviation < 0) {
			areaDeviation = 0;
		}

		return floorRatio * floorRatioWeight + areaDeviation * areaDeviationWeight;
	}
	
	public double getSquareness(){
		Polygon polygon = (Polygon) boundaries;
		Rectangle rectangle = constructRectangle();
		double polyArea = polygon.getArea();
		double rectArea = rectangle.getArea();
		return polyArea / rectArea;
	}
	
	private Rectangle constructRectangle() {
		Polygon polygon = (Polygon) boundaries;
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		
		Iterator<Point> iter = polygon.getPoints().iterator();
		Point p = null;
		while (iter.hasNext()) {
			p = iter.next();
			if (minX > p.getX()) {
				minX = p.getX();
			}
			if (maxX < p.getX()) {
				maxX = p.getX();
			}
			if (minY > p.getY()) {
				minY = p.getY();
			}
			if (maxY < p.getY()) {
				maxY = p.getY();
			}
		}
		
		return new Rectangle(new Point(minX, minY), new Point(maxX, maxY));
	}

	// TODO: Consider non-rectangular geometries in the future.
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
		if (map.getRowCount() < 3 ||
				map.getColCount() < 3 ||
				p2.getX() - p1.getX() < 2 ||
				p2.getY() - p1.getY() < 2 ||
				p1.getX() >= map.getColCount() ||
				p2.getX() >= map.getColCount() ||
				p1.getY() >= map.getRowCount() ||
				p2.getY() >= map.getRowCount()) {
			return results;
		}
		
		int[][] matrix = new int[p2.getX() - p1.getX() + 1][p2.getY() - p1.getY() + 1];
		boolean[][] allocated = map.getAllocationMatrix();
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (map.getTile(p1.getX() + i, p1.getY() + j) == TileTypes.WALL) {
					matrix[i][j] = -1;
				} else {
					matrix[i][j] = 0;
				}
			}
		}
		// Uncomment this for fancy debugging
//		for (int i = 0; i < matrix.length; i++) {
//			for (int j = 0; j < matrix[0].length; j++) {
//				System.out.print(matrix[i][j] + 1);
//			}
//			System.out.println();
//		}
//		System.out.println();

		
		int roomCounter = 0;
		for (int i = 1; i < matrix.length - 1; i++) {
			for (int j = 1; j < matrix[0].length - 1; j++) {
				if (isRoom(matrix, allocated, i, j)) {
					results.add(new Room(map.getConfig(),growRoom(matrix, allocated, i, j, ++roomCounter)));
				}
			}
		}
		// Uncomment this for fancy debugging
//		for (int i = 0; i < matrix.length; i++) {
//			for (int j = 0; j < matrix[0].length; j++) {
//				System.out.print(matrix[i][j] + 1);
//			}
//			System.out.println();
//		}
//		System.out.println();

		return results;
	}
	
	private static boolean isRoom(int[][] map, boolean[][] allocated, int x, int y) {
		if (map[x][y] != 0) {
			return false;
		}
		
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				if (map[i][j] != 0 || allocated[i][j]) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private static Polygon growRoom(int[][] map, boolean[][] allocated, int x, int y, int room) {
		Polygon polygon = new Bitmap();
		LinkedList<Point> pq = new LinkedList<Point>();
//		LinkedList<Point> cloud = new LinkedList<Point>(); // poly
		Point p;
		
		// The core will never be allocated, no need to check for that
		for (int i = x - 1; i < x + 2; i++) {
			for (int j = y - 1; j < y + 2; j++) {
				map[i][j] = room;
				allocated[i][j] = true;
			}
		}
		if (x + 2 < map.length) {
			if (!allocated[x + 2][y]) {
				pq.addLast(new Point(x + 2, y));
			}
			if (!allocated[x + 2][y + 1]) {
				pq.addLast(new Point(x + 2, y + 1));
			}
			if (!allocated[x + 2][y - 1]) {
				pq.addLast(new Point(x + 2, y - 1));
			}
		}
		if (y - 2 >= 0) {
			if (!allocated[x][y - 2]) {
				pq.addLast(new Point(x, y - 2));
			}
			if (!allocated[x + 1][y - 2]) {
				pq.addLast(new Point(x + 1, y - 2));
			}
			if (!allocated[x - 1][y - 2]) {
				pq.addLast(new Point(x - 1, y - 2));
			}
		}
		if (x - 2 >= 0) {
			if (!allocated[x - 2][y]) {
				pq.addLast(new Point(x - 2, y));
			}
			if (!allocated[x - 2][y - 1]) {
				pq.addLast(new Point(x - 2, y - 1));
			}
			if (!allocated[x - 2][y + 1]) {
				pq.addLast(new Point(x - 2, y + 1));
			}
		}
		if (y + 2 < map[0].length) {
			if (!allocated[x][y + 2]) {
				pq.addLast(new Point(x, y + 2));
			}
			if (!allocated[x - 1][y + 2]) {
				pq.addLast(new Point(x - 1, y + 2));
			}
			if (!allocated[x + 1][y + 2]) {
				pq.addLast(new Point(x + 1, y + 2));
			}
		}
//		cloud.addAll(pq); // poly
		
		while (!pq.isEmpty()) {
			p = pq.removeFirst();
			x = p.getX();
			y = p.getY();
			
			if (map[x][y] == 0 && hasThreeNeighbours(map, p, room)) {
				map[x][y] = room;
//				cloud.addLast(p); // poly
				if (x + 1 < map.length && !allocated[x + 1][y]) {
					pq.addLast(new Point(x + 1, y));
				}
				if (y - 1 >= 0 && !allocated[x][y - 1]) {
					pq.addLast(new Point(x, y - 1));
				}
				if (x - 1 >= 0 && !allocated[x - 1][y]) {
					pq.addLast(new Point(x - 1, y));
				}
				if (y + 1 < map[0].length && !allocated[x][y + 1]) {
					pq.addLast(new Point(x, y + 1));
				}
			}
		}
		
		for (x = 0; x < map.length; x++) {
			for (y = 0; y < map[0].length; y++) {
				if (map[x][y] == room) {
					polygon.addPoint(new Point(x, y));
					allocated[x][y] = true;
				}
			}
		}
		
		polygon = tryAgain(map, allocated, polygon, room);
		
		// ˇˇˇ poly ˇ̌ˇˇ
//		Iterator<Point> iter = cloud.iterator();
//		p = null;
//		while (iter.hasNext()) {
//			p = iter.next();
//			polygon.addPoint(p);
//			if (hasEightNeighbours(map, p, room)) {
//				iter.remove();
//			}
//		}
		
		return polygon;
	}
	
	private static Polygon tryAgain(int[][] map, boolean[][] allocated, Polygon polygon, int room) {
		LinkedList<Point> pq = new LinkedList<Point>();
		Point p = null;
		int x = 0, y = 0;
		int size = 0;
		
		for (x = 0; x < map.length; x++) {
			for (y = 0; y < map[0].length; y++) {
				if (map[x][y] == room) {
					p = new Point(x, y);
					allocated[x][y] = true;
					++size;
					if (!hasEightNeighbours(map, p, room)) {
						if (x + 1 < map.length && !allocated[x + 1][y]) {
							pq.addLast(new Point(x + 1, y));
						}
						if (y - 1 >= 0 && !allocated[x][y - 1]) {
							pq.addLast(new Point(x, y - 1));
						}
						if (x - 1 >= 0 && !allocated[x - 1][y]) {
							pq.addLast(new Point(x - 1, y));
						}
						if (y + 1 < map[0].length && !allocated[x][y + 1]) {
							pq.addLast(new Point(x, y + 1));
						}
					}
				}
			}
		}
		
		while (!pq.isEmpty()) {
			p = pq.removeFirst();
			x = p.getX();
			y = p.getY();
			
			if (map[x][y] == 0 && hasThreeNeighbours(map, p, room)) {
				map[x][y] = room;
				polygon.addPoint(p);
				if (x + 1 < map.length && !allocated[x + 1][y]) {
					pq.addLast(new Point(x + 1, y));
				}
				if (y - 1 >= 0 && !allocated[x][y - 1]) {
					pq.addLast(new Point(x, y - 1));
				}
				if (x - 1 >= 0 && !allocated[x - 1][y]) {
					pq.addLast(new Point(x - 1, y));
				}
				if (y + 1 < map[0].length && !allocated[x][y + 1]) {
					pq.addLast(new Point(x, y + 1));
				}
			}
		}
		
		if (polygon.getArea() > size) {
			polygon = tryAgain(map, allocated, polygon, room);
		}
		
		return polygon;
	}
	
	private static boolean hasThreeNeighbours(int[][] map, Point p, int room) {
		return countNeighbours(map, p, room) >= 3;
	}
	
	private static boolean hasEightNeighbours(int[][] map, Point p, int room) {
		return countNeighbours(map, p, room) == 8;
	}
	
	private static int countNeighbours(int[][] map, Point p, int room) {
		int neighbours = 0;
		int x = p.getX();
		int y = p.getY();

		for (int i = x - 1; i <= x + 1; i++) {
			if (i >= 0 && i < map.length) {
				for (int j = y - 1; j <= y + 1; j++) {
					if (j >= 0 && j < map[0].length && map[i][j] == room) {
						neighbours++;
					}
				}
			}

		}
		
		return neighbours;
	}
}
