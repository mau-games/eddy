package finder.patterns;

import java.util.ArrayList;
import java.util.List;

import finder.geometry.Geometry;
import game.Map;

/**
 * The Pattern class is used to describe dungeon game design patterns.
 * 
 * @author Johan Holmberg
 */
public abstract class Pattern {
	
	protected Geometry boundaries = null;
	
	/**
	 * Searches a map for instances of this pattern and returns a list of found
	 * instances.
	 * 
	 * @param map The map to search for patterns in.
	 * @param boundary A boundary in which the pattern is searched for.
	 * @return A list of found instances.
	 */
	public static List<Pattern> matches(Map map, Geometry boundary) {
		return new ArrayList<Pattern>();
	}

	/**
	 * Returns the geometry of this pattern.
	 * 
	 * @return The correspoinding geometry.
	 */
	public Geometry getGeometry() {
		return boundaries;
	}
}
