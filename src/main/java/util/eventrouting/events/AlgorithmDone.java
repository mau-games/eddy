package util.eventrouting.events;

import java.util.List;

import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import util.eventrouting.PCGEvent;

/**
 * This event is triggered when an algorithm run is completed.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class AlgorithmDone extends PCGEvent {
	public List<Pattern> micropatterns = null;
	public List<CompositePattern> mesopatterns = null;
	public List<CompositePattern> macropatterns = null;
	
	public AlgorithmDone(List<Pattern> micropatterns,
			List<CompositePattern> mesopatterns,
			List<CompositePattern> macropatterns) {
		this.micropatterns = micropatterns;
		this.mesopatterns = mesopatterns;
		this.macropatterns = macropatterns;
	}
}
