package util.eventrouting.events;

import gui.controls.SuggestionNarrativeStructure;
import util.eventrouting.PCGEvent;

public class SuggestedNarrativeSelected extends PCGEvent
{
	public SuggestedNarrativeSelected(SuggestionNarrativeStructure sugNarrative)
	{
		setPayload(sugNarrative);
	}
}
