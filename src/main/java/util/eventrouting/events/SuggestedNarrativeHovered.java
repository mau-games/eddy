package util.eventrouting.events;

import gui.controls.SuggestionNarrativeStructure;
import util.eventrouting.PCGEvent;

public class SuggestedNarrativeHovered extends PCGEvent
{
	public SuggestedNarrativeHovered(SuggestionNarrativeStructure sugNarrative)
	{
		setPayload(sugNarrative);
	}
}
