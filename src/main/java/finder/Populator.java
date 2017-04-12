package finder;

import java.util.ArrayList;
import java.util.List;

import finder.geometry.Point;
import finder.geometry.Polygon;
import finder.patterns.InventorialPattern;
import finder.patterns.Pattern;
import finder.patterns.SpacialPattern;

/**
 * Populator is used to populate spacial pattern instances with inventorial
 * pattern instances.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class Populator {
	
	/**
	 * Populates spacial patterns with inventorial pattern instances.
	 * 
	 * @param micropatterns All micropatterns.
	 */
	public static void populate(List<Pattern> micropatterns) {
		List<SpacialPattern> spacials = new ArrayList<SpacialPattern>();
		List<InventorialPattern> inventorials = new ArrayList<InventorialPattern>();
		
		// First, separate the micropatterns into two different ones
		for (Pattern p : micropatterns) {
			if (p instanceof SpacialPattern) {
				spacials.add((SpacialPattern) p);
			} else if (p instanceof InventorialPattern) {
				inventorials.add((InventorialPattern) p);
			}
		}
		
		// Now, try to fit all inventorials into the spacial patterns.
		for (InventorialPattern ip : inventorials) {
			for (SpacialPattern sp : spacials) {
				Polygon polygon = (Polygon) sp.getGeometry();
				if (polygon.contains((Point) ip.getGeometry())) {
					sp.addPattern(ip);
					break;
				}
			}
		}
	}
}
