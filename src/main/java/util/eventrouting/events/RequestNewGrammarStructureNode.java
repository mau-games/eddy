package util.eventrouting.events;

import game.Dungeon;
import game.narrative.TVTropeType;
import util.eventrouting.PCGEvent;

public class RequestNewGrammarStructureNode extends PCGEvent {
	/***
	 * For now only one type of grammar!
	 */
	private TVTropeType trope_type;
	public double x_pos;
	public double y_pos;

	public RequestNewGrammarStructureNode(TVTropeType tropeType, double x_pos, double y_pos) {
		this.trope_type = tropeType;
		this.x_pos = x_pos;
		this.y_pos = y_pos;
		setPayload(tropeType);
	}
}
