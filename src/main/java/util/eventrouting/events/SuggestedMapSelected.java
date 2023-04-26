package util.eventrouting.events;

import gui.controls.ScaledRoom;
import gui.controls.SuggestionRoom;
import util.eventrouting.PCGEvent;

public class SuggestedMapSelected extends PCGEvent
{
	public SuggestedMapSelected(SuggestionRoom sugRoom)
	{
		setPayload(sugRoom);
	}

}
