package finder;

import java.util.List;

import game.Map;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;

/**
 * PatternFinder is used to find patterns within a map.
 * 
 * @author Johan Holmberg
 */
public class PatternFinder {
	
	private Map map;
	private List<Pattern> micropatterns = null;
	private List<CompositePattern> mesopatterns = null;
	private List<CompositePattern> macropatterns = null;
	
	/**
	 * Creates a pattern finder instance.
	 * 
	 * @param map The map to search in.
	 */
	public PatternFinder(Map map) {
		this.map = map;
	}
	
	// TODO: Implement this
	/**
	 * Finds micro patterns within a map. It searches for all patterns
	 * available in the finder.patterns.micro package.
	 * 
	 * @return A list of all found pattern instances.
	 */
	public List<Pattern> findMicroPatterns() {
		/*
		 * Do this:
		 * 1. Get all patterns in finder.patterns.micro
		 * 2. Do a pattern search for each pattern for the specified map
		 * 3. Save the found patterns
		 * 4. Return all found pattern instances
		 * 
		 * MAYBE: Specify which patterns to look for in the config?
		 */
		return null;
	}
	
	// TODO: Implement this
	/**
	 * Finds meso patterns within a map. It searches for all patterns available
	 * in the finder.patterns.meso package.
	 * 
	 * @return A list of all found pattern instances.
	 */
	public List<Pattern> findMesoPatterns() {
		/*
		 * Do this:
		 * 1. Get all patterns in finder.patterns.meso
		 * 2. Do a pattern search for each pattern for the specified map
		 * 	  using found micro patterns
		 * 3. Save the found patterns
		 * 4. Return all found pattern instances
		 * 
		 * MAYBE: Specify which patterns to look for in the config?
		 */
		if (micropatterns == null) {
			findMicroPatterns();
		}
		
		return null;
	}
	
	// TODO: Implement this
	/**
	 * Finds macro patterns witin a map. It searches for all patterns
	 * available in the finder.patterns.macro package.
	 * 
	 * @return A list of all found pattern instances.
	 */
	public List<Pattern> findMacroPatterns() {
		/*
		 * Do this:
		 * 1. Get all patterns in finder.patterns.meso
		 * 2. Do a pattern search for each pattern for the specified map
		 * 	  using found meso patterns
		 * 3. Save the found patterns
		 * 4. Return all found pattern instances
		 * 
		 * MAYBE: Specify which patterns to look for in the config?
		 */
		if (mesopatterns == null) {
			findMesoPatterns();
		}
		if (micropatterns == null) {
			findMicroPatterns();
		}
		
		return null;
	}
}
