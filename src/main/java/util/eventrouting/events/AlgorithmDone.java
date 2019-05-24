package util.eventrouting.events;

import game.MapContainer;
import generator.algorithm.Algorithm;

/**
 * This event is triggered when an algorithm run is completed.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class AlgorithmDone extends AlgorithmEvent {
	
	private Algorithm algorithm;
	public String configName;
	
	public AlgorithmDone(MapContainer map, Algorithm algorithm, String configurationName) {
		setPayload(map);
		this.algorithm = algorithm;
		this.configName = configurationName;
	}
	
	public Algorithm getAlgorithm()
	{
		return algorithm;
	}
}
