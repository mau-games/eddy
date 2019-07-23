package util.eventrouting;

/**
 * Event type that others should extend when they want to communicate with components of the same View
 * (E.g. SandBoxView and EvolutionMAPE) but not when the communication is InterView, then please refer and use
 * the other type of events when they are needed to.
 * @author Alberto Alvarez, Malmo University
 *
 */
public abstract class IntraViewEvent extends PCGEvent
{
	//TODO: Probably I can add more info!
}
