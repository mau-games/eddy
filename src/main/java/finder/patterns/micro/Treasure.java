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
 * This class represents the dungeon game design pattern called Treasure.
 * 
 * @author Johan Holmberg
 */
public class Treasure extends InventorialPattern {
	
	private double quality = 0.0;
	
	public Treasure(Geometry geometry, Room room) {
		boundaries = geometry;
		this.room = room;
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
	 * @param room The map to search.
	 * @param boundary The boundary that limits the searchable area.
	 * @return A list of found room pattern instances.
	 */
	public static List<Pattern> matches(Room room, Geometry boundary) {
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

		double quality = calculateTreasureQuality(room);
		
		for(util.Point p : room.getTreasures()){
			Point p_ = new Point(p.getX(),p.getY());
			if(((Rectangle)boundary).contains(p_)){
				Treasure t = new Treasure(p_,room);
				t.quality = quality;
				results.add(t);
			}
		}

		return results;
	}
	
	private static boolean isTreasure(int[][] map, int x, int y) {
		return map[y][x] == 2;
	}
	
	private static double calculateTreasureQuality(Room room){
		double[] expectedTreasuresRange = expectedTreasuresRange = room.getConfig().getTreasureQuantityRange();
        double quality = 0.0;
        double treasurePercent = room.getTreasurePercentage();
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
        quality /= room.getTreasureCount();
        
        return quality;
	}
}
