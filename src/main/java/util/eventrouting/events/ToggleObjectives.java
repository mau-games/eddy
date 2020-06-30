package util.eventrouting.events;

import util.eventrouting.PCGEvent;

public class ToggleObjectives extends PCGEvent {

	private boolean toggleState;
	
	public ToggleObjectives(boolean state)
	{
		toggleState = state;
	}
	
	public boolean getToggleState()
	{
		return toggleState;
	}
}
