package finder.patterns;

/**
 * InventorialPatterns are patterns that don't represent some kind of spacial
 * construct, e.g. a corridor or a room/chamber. They usually know their
 * parental, spacial pattern instances.
 * 
 * @author Johan Holmberg, Malmö University
 */
public abstract class InventorialPattern extends Pattern {
	private SpacialPattern parent;
	
	/**
	 * Sets the parental pattern.
	 * 
	 * @param pattern The patrental pattern.
	 */
	public void setParent(SpacialPattern pattern) {
		parent = pattern;
	}
	
	/**
	 * Gets the parental pattern.
	 * 
	 * @return The parental pattern.
	 */
	public SpacialPattern getParent() {
		return parent;
	}
}
