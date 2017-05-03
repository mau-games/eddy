package finder.patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * SpacialPatterns are patterns that represent some kind of spacial construct,
 * e.g. a corridor or a room/chamber. They can contain non-spacial patterns.
 * 
 * @author Johan Holmberg, Malmö University
 */
public abstract class SpacialPattern extends Pattern {
	private List<InventorialPattern> containedPatterns =
			new ArrayList<InventorialPattern>();
	
	/**
	 * Adds an inventorial pattern to the contained patterns list.
	 * 
	 * @param pattern An inventorial pattern.
	 */
	public void addPattern(InventorialPattern pattern) {
		if (!containedPatterns.contains(pattern)) {
			containedPatterns.add(pattern);
			pattern.setParent(this);
		}
	}
	
	/**
	 * Gets the list of inventorial patterns found in this pattern instance.
	 * 
	 * @return The list.
	 */
	public List<InventorialPattern> getContainedPatterns() {
		return containedPatterns;
	}
}
