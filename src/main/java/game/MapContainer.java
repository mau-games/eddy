package game;

import java.util.List;

import finder.patterns.CompositePattern;
import finder.patterns.Pattern;

/**
 * MapContainer contains a map and potentially its associated patterns.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class MapContainer {
	private Map map;
	private List<Pattern> microPatterns;
	private List<CompositePattern> mesoPatterns;
	private List<CompositePattern> macroPatterns;
	private String message;

	/**
	 * Creates an instance of this container.
	 */
	public MapContainer() {
		
	}
	
	/**
	 * Gets the map associated with this container.
	 * 
	 * @return A map.
	 */
	public Map getMap() {
		return map;
	}

	/**
	 * Associates a map with this container.
	 * 
	 * @param map A map.
	 */
	public void setMap(Map map) {
		this.map = map;
	}

	/**
	 * Gets a list of micro patterns associated with this container.
	 * 
	 * @return A list of micro patterns.
	 */
	public List<Pattern> getMicroPatterns() {
		return microPatterns;
	}

	/**
	 * Associates a list of patterns with this container.
	 * 
	 * @param microPatterns A list of micro patterns.
	 */
	public void setMicroPatterns(List<Pattern> microPatterns) {
		this.microPatterns = microPatterns;
	}

	/**
	 * Gets a list of meso patterns associated with this container.
	 * 
	 * @return A list of meso patterns.
	 */
	public List<CompositePattern> getMesoPatterns() {
		return mesoPatterns;
	}

	/**
	 * Associates a list of meso patterns with this container.
	 * 
	 * @param mesoPatterns A list of meso patterns.
	 */
	public void setMesoPatterns(List<CompositePattern> mesoPatterns) {
		this.mesoPatterns = mesoPatterns;
	}

	/**
	 * Gets a list of macro patterns associated with this container.
	 * 
	 * @return A list of macro patterns.
	 */
	public List<CompositePattern> getMacroPatterns() {
		return macroPatterns;
	}

	/**
	 * Associates a list of macro patterns with this container.
	 * 
	 * @param macroPatterns A list of macro patterns
	 */
	public void setMacroPatterns(List<CompositePattern> macroPatterns) {
		this.macroPatterns = macroPatterns;
	}

	/**
	 * Gets the message that comes with this container.
	 * 
	 * @return A message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message that goes with this container.
	 * 
	 * @param message A message.
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
