package finder.patterns.micro;

import java.util.ArrayList;
import java.util.List;

import finder.geometry.Geometry;
import finder.geometry.Point;
import finder.geometry.Rectangle;
import finder.patterns.InventorialPattern;
import finder.patterns.Pattern;
import game.Room;
import game.Tile;
import game.tiles.BossEnemyTile;

public class Boss extends InventorialPattern {

private double quality = 0.0;
	
	public Boss(Geometry geometry, Room room) {
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
		
		double quality = calculateBossQuality(room);

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
		
		//Combine the info
		for(Tile custom : room.customTiles)
		{
			if(custom instanceof BossEnemyTile )
			{
				Point p_ = new Point(custom.GetCenterPosition().getX(),custom.GetCenterPosition().getY());
				if(((Rectangle)boundary).contains(p_)){
					Boss e = new Boss(p_,room);
					e.quality = quality;
					results.add(e);
				}
			}
		}

		return results;
	}
	
	private static boolean isBoss(int[][] map, int x, int y) {
		return map[y][x] == 5;
	}
	
	//Quality is give by the space the boss have? maybe that is more the the "boss chamber meso-pattern"
	//
	private static double calculateBossQuality(Room room)
	{
		double quality = 0.0;
		double bossesInDungeon = room.owner.getBosses().size();
		double bossesInRoom = 0;
		
		if(bossesInDungeon == 0) return 0.0;
		
		for(Tile custom : room.customTiles)
		{
			if(custom instanceof BossEnemyTile)
			{
				bossesInRoom++;
			}
		}
		
		quality = bossesInRoom/bossesInDungeon;
		quality /= bossesInRoom;
		
		return quality;
	}
	
}
