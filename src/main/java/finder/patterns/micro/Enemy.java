package finder.patterns.micro;

import java.util.ArrayList;
import java.util.List;

import finder.geometry.Geometry;
import finder.geometry.Point;
import finder.geometry.Rectangle;
import finder.patterns.InventorialPattern;
import finder.patterns.Pattern;
import game.Room;

/**
 * This class represents the dungeon game design pattern called Enemy.
 * 
 * @author Johan Holmberg
 */
public class Enemy extends InventorialPattern {
	
	private double quality = 0.0;
	
	public Enemy(Geometry geometry, Room room) {
		boundaries = geometry;
		this.room = room;
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
	 * @param room The map to search.
	 * @param boundary The boundary that limits the searchable area.
	 * @return A list of found room pattern instances.
	 */
	public static List<Pattern> matches(Room room, Geometry boundary) {
		
		double quality = calculateEnemyQuality(room);

		ArrayList<Pattern> results = new ArrayList<Pattern>();
		
		if (room == null) {
			return results;
		}
		
		if (boundary == null) {
			boundary = new Rectangle(new Point(0, 0),
					new Point(room.getColCount() -1 , room.getRowCount() - 1));
		}

		// Check boundary sanity.
		Point p1 = ((Rectangle) boundary).getTopLeft();
		Point p2 = ((Rectangle) boundary).getBottomRight();
		if (p1.getX() >= room.getColCount() ||
				p2.getX() >= room.getColCount() ||
				p1.getY() >= room.getRowCount() ||
				p2.getY() >= room.getRowCount()) {
			return results;
		}
		
		for(util.Point p : room.getEnemies()){
			Point p_ = new Point(p.getX(),p.getY());
			if(((Rectangle)boundary).contains(p_)){
				Enemy e = new Enemy(p_,room);
				e.quality = quality;
				results.add(e);
			}
		}
		
		return results;
	}
	
	private static boolean isEnemy(int[][] map, int x, int y) {
		return map[y][x] == 3;
	}
	
	private static double calculateEnemyQuality(Room room){
		double[] expectedEnemiesRange = null;
		expectedEnemiesRange = room.getConfig().getEnemyQuantityRange();
        double quality = 0.0;
        double enemyPercent = room.getEnemyPercentage();
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
        quality /= room.getEnemyCount();
        
        return quality;
		
	}
}
