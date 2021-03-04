package util.eventrouting.events;

import game.narrative.GrammarGraph;
import gui.controls.SuggestionNarrativeStructure;
import util.eventrouting.PCGEvent;

public class NarrativeStructEdited extends PCGEvent
{
	public NarrativeStructEdited(GrammarGraph edited_narrative_graph)
	{
		setPayload(edited_narrative_graph);
	}
}
