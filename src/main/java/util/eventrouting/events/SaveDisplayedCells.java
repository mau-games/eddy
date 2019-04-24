package util.eventrouting.events;

import util.eventrouting.PCGEvent;

public class SaveDisplayedCells extends PCGEvent
{
	/*
	 * Once this event is invoked it will save the following:
	 * 1- A snapshot of the current displayed cells
	 * 2- the generation summary and cell info for such displayed cells
	 * 3- the current edit map
	 */
	public SaveDisplayedCells()
	{
		
	}
}
