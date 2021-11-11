package util.eventrouting.events;

import game.narrative.TVTropeType;
import util.eventrouting.PCGEvent;
import game.narrative.GrammarNode;

public class RequestReplacementGrammarStructureNode extends PCGEvent {
	/***
	 * For now only one type of grammar!
	 */
	public TVTropeType trope_type;
	public GrammarNode to_replace;

	public RequestReplacementGrammarStructureNode(TVTropeType tropeType, GrammarNode to_replace) {
		this.trope_type = tropeType;
		this.to_replace = to_replace;
		setPayload(tropeType);
	}
}
