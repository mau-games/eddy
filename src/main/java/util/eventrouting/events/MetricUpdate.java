package util.eventrouting.events;

import game.Room;
import generator.algorithm.MetricIndividual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This event is used to post info on new maps.
 * 
 * @author Alexander Baldwin, Malmö University
 * @author Johan Holmberg, Malmö University
 */
public class MetricUpdate extends AlgorithmEvent {

	public List<MetricIndividual> top_individuals;

	/**
	 * Creates a new event.
	 */
	public MetricUpdate(MetricIndividual... individuals)
	{
		top_individuals = Arrays.asList(individuals);
	}
}
