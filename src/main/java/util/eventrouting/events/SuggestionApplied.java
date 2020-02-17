package util.eventrouting.events;

import game.Room;
import util.eventrouting.IntraViewEvent;

public class SuggestionApplied extends IntraViewEvent 
{
	public SuggestionApplied(Room appliedSuggestion)
	{
		setPayload(appliedSuggestion);
	}
}
