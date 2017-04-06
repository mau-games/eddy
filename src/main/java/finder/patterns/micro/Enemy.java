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
 * This class represents the dungeon game design pattern called Enemy.
 * 
 * @author Johan Holmberg
 */
public class Enemy extends InventorialPattern {
	
	private double quality = 0.0;
	
	public Enemy(Geometry geometry, Map map) {
		boundaries = geometry;
		this.map = map;
	}
	
	@Override
	/**
	 * Returns a measure of the quality of this pattern.
	 * 
	 * The quality for an enemy is decided by the number of enemies in the room
	 *  
	 * @return A number between 0.0 and 1.0 representing the quality of the pattern (where 1 is best)
	 */
	public double getQuality() {
		return quality;
	}
	
	// TODO: Consider non-rectangular geometries in the future.
	/**
	 * Searches a map for enemies. The searchable area can be limited by a set of
	 * boundaries. If these boundaries are invalid, no search will be
	 * performed.
	 * 
	 * @param map The map to search.
	 * @param boundary The boundary that limits the searchable area.
	 * @return A list of found room pattern instances.
	 */
	public static List<Pattern> matches(Map map, Geometry boundary) {
		
		double quality = calculateEnemyQuality(map);

		ArrayList<Pattern> results = new ArrayList<Pattern>();
		
		if (map == null) {
			return results;
		}
		
		if (boundary == null) {
			boundary = new Rectangle(new Point(0, 0),
					new Point(map.getColCount() -1 , map.getRowCount() - 1));
		}
		
		for(util.Point p : map.getEnemies()){
			Point p_ = new Point(p.getX(),p.getY());
			if(((Rectangle)boundary).contains(p_)){
				Enemy e = new Enemy(p_,map);
				e.quality = quality;
				results.add(e);
			}
		}
		
		return results;
	}
	
	private static boolean isEnemy(int[][] map, int x, int y) {
		return map[x][y] == 3;
	}
	
	private static double calculateEnemyQuality(Map map){
		double[] expectedEnemiesRange = null;
		try {
			expectedEnemiesRange = Config.getInstance().getEnemyQuantityRange();
		} catch (MissingConfigurationException e) {
			e.printStackTrace();
		}
        double quality = 0.0;
        double enemyPercent = map.getEnemyPercentage();
        if(enemyPercent < expectedEnemiesRange[0])
        {
        	quality = expectedEnemiesRange[0] - enemyPercent;
        }
        else if(enemyPercent > expectedEnemiesRange[1])
        { 
        	quality = enemyPercent - expectedEnemiesRange[1];
        }
        //Scale fitness to be between 0 and 1:
        quality = quality/Math.max(expectedEnemiesRange[0], 1.0 - expectedEnemiesRange[1]);
        quality /= map.getEnemyCount();
        
        return quality;
		
	}
}
