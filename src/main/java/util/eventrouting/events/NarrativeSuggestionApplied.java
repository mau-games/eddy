package util.eventrouting.events;

import game.Room;
import game.narrative.GrammarGraph;
import util.eventrouting.IntraViewEvent;

public class NarrativeSuggestionApplied extends IntraViewEvent
{
	public NarrativeSuggestionApplied(GrammarGraph appliedSuggestion)
	{
		setPayload(appliedSuggestion);
	}
}
