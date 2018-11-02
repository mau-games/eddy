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
 * This class represents the dungeon game design pattern called Door.
 * 
 * @author Johan Holmberg
 */
public class Door extends InventorialPattern {
	
	public Door(Geometry geometry, Room room) {
		boundaries = geometry;
		this.room = room;
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

		for(util.Point p : room.getDoors()){
			Point p_ = new Point(p.getX(),p.getY());
			if(((Rectangle)boundary).contains(p_)){
				Door d = new Door(p_,room);
				results.add(d);
			}
		}

		return results;
	}
	
	private static boolean isDoor(int[][] map, int x, int y) {
		return map[y][x] == 4;
	}
}
