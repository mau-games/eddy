package util.eventrouting.events;

import game.narrative.GrammarGraph;
import gui.controls.SuggestionNarrativeStructure;
import util.eventrouting.PCGEvent;

public class NarrativeStructEdited extends PCGEvent
{
	public boolean replaced_graph = false;

	public NarrativeStructEdited(GrammarGraph edited_narrative_graph, boolean replaced_graph)
	{
		setPayload(edited_narrative_graph);
		this.replaced_graph = replaced_graph;
	}
}
