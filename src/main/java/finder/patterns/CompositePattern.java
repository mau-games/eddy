package finder.patterns;

import java.util.ArrayList;
import java.util.List;

import finder.geometry.Geometry;
import finder.graph.Graph;
import game.Room;

/**
 * This class is used to represent composite patterns, e.g. meso or macro patterns.
 * 
 * @author Johan Holmberg
 */
public abstract class CompositePattern extends Pattern {
	
	protected double quality;
	
	protected List<Pattern> patterns = new ArrayList<Pattern>();
	
	/**
	 * Returns a list of all pattern instances making up this pattern.
	 * 
	 * @return A list of patterns.
	 */
	public List<Pattern> getPatterns(){
		return patterns;
	}
	
	public static List<CompositePattern> matches(Room room, Graph<Pattern> patternGraph, List<CompositePattern> currentMeso) {
		return new ArrayList<CompositePattern>();
	}
	
	public double getQuality() {
		return quality;
	}
	
	public void setQuality(double value) {
		quality = value;
	}
}
