package finder.patterns.meso;

import java.util.ArrayList;
import java.util.List;

import finder.geometry.Geometry;
import finder.graph.Graph;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import game.Map;

/**
 * The ChokePoint class represents the choke point pattern.
 * 
 * <p>This is not yet properly implemented and is mostly added as a meso
 * pattern placeholder.
 * 
 * @author Johan Holmberg
 */
public class ChokePoint extends CompositePattern {
	
	/**
	 * Searches a map for instances of this pattern and returns a list of found
	 * instances.
	 * 
	 * @param map The map to search for patterns in.
	 * @param boundary A boundary in which the pattern is searched for.
	 * @return A list of found instances.
	 */
	public static List<CompositePattern> matches(Map map, Graph<Pattern> patternGraph) {
		
		// How to find a choke point:
		// Look at boundaries between patterns where: one or both of the patterns are rooms.
		// If the width of the edge is 1, we have a potential choke point.
		// The potential choke point is an actual choke point if all paths from pattern A to pattern B must pass through that edge (BFS?)
		
		return new ArrayList<CompositePattern>();
	}
	
}
