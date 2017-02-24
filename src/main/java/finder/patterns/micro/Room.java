package finder.patterns.micro;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

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
		
		int[][] matrix = new int[p2.getX() - p1.getX() + 1][p2.getY() - p1.getY() + 1];
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (map.getTile(p1.getX() + i, p1.getY() + j) == TileTypes.WALL) {
					matrix[i][j] = -1;
				} else {
					matrix[i][j] = 0;
				}
			}
		}
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.print(matrix[i][j] + 1);
			}
			System.out.println();
		}
		System.out.println();
		
		int roomCounter = 0;
		for (int i = 1; i < matrix.length - 1; i++) {
			for (int j = 1; j < matrix[0].length - 1; j++) {
				if (isRoom(matrix, i, j)) {
					growRoom(matrix, i, j, ++roomCounter);
					results.add(new Room(new Point(i, j)));
				}
			}
		}
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.print(matrix[i][j] + 1);
			}
			System.out.println();
		}

		return results;
	}
	
	private static boolean isRoom(int[][] map, int x, int y) {
		if (map[x][y] != 0) {
			return false;
		}
		
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				if (map[i][j] != 0) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private static void growRoom(int[][] map, int x, int y, int room) {
		LinkedList<Point> pq = new LinkedList<Point>();
		Point p;
		
		for (int i = x - 1; i < x + 2; i++) {
			for (int j = y - 1; j < y + 2; j++) {
				map[i][j] = room;
			}
		}
		if (x + 2 < map.length) {
			pq.addLast(new Point(x + 2, y));
			pq.addLast(new Point(x + 2, y + 1));
			pq.addLast(new Point(x + 2, y - 1));
		}
		if (y - 2 >= 0) {
			pq.addLast(new Point(x, y - 2));
			pq.addLast(new Point(x + 1, y - 2));
			pq.addLast(new Point(x - 1, y - 2));
		}
		if (x - 2 >= 0) {
			pq.addLast(new Point(x - 2, y));
			pq.addLast(new Point(x - 2, y - 1));
			pq.addLast(new Point(x - 2, y + 1));
		}
		if (y + 2 < map[0].length) {
			pq.addLast(new Point(x, y + 2));
			pq.addLast(new Point(x - 1, y + 2));
			pq.addLast(new Point(x + 1, y + 2));
		}
		
		while (!pq.isEmpty()) {
			p = pq.removeFirst();
			x = p.getX();
			y = p.getY();
			
			if (map[x][y] == 0 && hasThreeNeighbours(map, p, room)) {
				map[x][y] = room;
				if (x + 1 < map.length) {
					pq.addLast(new Point(x + 1, y));
				}
				if (y - 1 >= 0) {
					pq.addLast(new Point(x, y - 1));
				}
				if (x - 1 >= 0) {
					pq.addLast(new Point(x - 1, y));
				}
				if (y + 1 < map[0].length) {
					pq.addLast(new Point(x, y + 1));
				}
			}
		}
	}
	
	private static boolean hasThreeNeighbours(int[][] map, Point p, int room) {
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

		return neighbours >= 3;
	}
}
