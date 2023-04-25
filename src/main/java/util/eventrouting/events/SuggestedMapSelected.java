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

	public SuggestedMapSelected(ScaledRoom scaledRoom, int index){
		setPayload(scaledRoom);

	}
}
