package finder.patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represent composite patterns, e.g. meso or macro patterns.
 * 
 * @author Johan Holmberg
 */
public abstract class CompositePattern extends Pattern {
	
	/**
	 * Returns a list of all pattern instances making up this pattern.
	 * 
	 * @return A list of patterns.
	 */
	public List<Pattern> getPatterns(){
		return new ArrayList<Pattern>();
	}
}
