package finder.patterns.micro;

import java.util.ArrayList;
import java.util.List;

import finder.geometry.Geometry;
import finder.geometry.Point;
import finder.geometry.Rectangle;
import finder.patterns.InventorialPattern;
import finder.patterns.Pattern;
import game.Map;
import generator.config.Config;
import util.config.MissingConfigurationException;

/**
 * This class represents the dungeon game design pattern called Treasure.
 * 
 * @author Johan Holmberg
 */
public class Treasure extends InventorialPattern {
	
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
		
		for(util.Point p : map.getTreasures()){
			Point p_ = new Point(p.getX(),p.getY());
			if(((Rectangle)boundary).contains(p_)){
				Treasure t = new Treasure(p_,map);
				t.quality = quality;
				results.add(t);
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
