package util.eventrouting.events;

import generator.algorithm.MetricExampleRooms;
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
public class MetricContinue extends AlgorithmEvent {

	public List<MetricExampleRooms> new_examples;

	/**
	 * Creates a new event.
	 */
	public MetricContinue(MetricExampleRooms... individuals)
	{
		new_examples = Arrays.asList(individuals);
	}

	public MetricContinue(ArrayList<MetricExampleRooms> examples)
	{
		new_examples = examples;
	}
}
