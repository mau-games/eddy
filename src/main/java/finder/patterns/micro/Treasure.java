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
import generator.config.Config;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;

/**
 * This class represents the dungeon game design pattern called Treasure.
 * 
 * @author Johan Holmberg
 */
public class Treasure extends Pattern {
	
	private double quality = 0.0;
	
	public Treasure(Geometry geometry, Map map) {
		boundaries = geometry;
		this.map = map;
	}
	
	@Override
	/**
	 * Returns a measure of the quality of this pattern.
	 *  
	 * @return A number between 0.0 and 1.0 representing the quality of the pattern (where 1 is best)
	 */
	public double getQuality() {
		return quality;
	}

	// TODO: Consider non-rectangular geometries in the future.
	/**
	 * Searches a map for treasures. The searchable area can be limited by a set of
	 * boundaries. If these boundaries are invalid, no search will be
	 * performed.
	 * 
	 * @param map The map to search.
	 * @param boundary The boundary that limits the searchable area.
	 * @return A list of found room pattern instances.
	 */
	public static List<Pattern> matches(Map map, Geometry boundary) {

		double quality = calculateTreasureQuality(map);
		
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

		if (p1.equals(p2)) {
			if (isTreasure(map.toMatrix(), p1.getX(), p1.getY())) {
				results.add(new Treasure(new Point(p1.getX(), p1.getY()),map));
			}
			return results;
		}
		
		int[][] matrix = new int[p2.getX() - p1.getX() + 1][p2.getY() - p1.getY() + 1];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = map.getTile(p1.getX() + i, p1.getY() + j).getValue();
			}
		}
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (isTreasure(matrix, i, j)) {
					results.add(new Treasure(new Point(i, j),map));
					((Treasure)results.get(results.size()-1)).quality = quality; 
				}
			}
		}

		return results;
	}
	
	private static boolean isTreasure(int[][] map, int x, int y) {
		return map[x][y] == 2;
	}
	
	private static double calculateTreasureQuality(Map map){
		double[] expectedTreasuresRange = null;
		try {
			expectedTreasuresRange = Config.getInstance().getTreasureQuantityRange();
		} catch (MissingConfigurationException e) {
			e.printStackTrace();
		}
        double quality = 0.0;
        double treasurePercent = map.getTreasurePercentage();
        if(treasurePercent < expectedTreasuresRange[0])
        {
        	quality = expectedTreasuresRange[0] - treasurePercent;
        }
        else if (treasurePercent > expectedTreasuresRange[1])
        {
        	quality = treasurePercent - expectedTreasuresRange[1];
        }
        //Scale fitness to be between 0 and 1:
        quality = quality/Math.max(expectedTreasuresRange[0], 1.0 - expectedTreasuresRange[1]);
        quality /= map.getTreasureCount();
        
        return quality;
	}
}
