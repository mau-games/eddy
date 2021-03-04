package util.eventrouting.events;

import game.Room;
import game.narrative.GrammarGraph;
import generator.algorithm.MAPElites.GrammarGACell;

import java.util.ArrayList;
import java.util.List;

public class NarrativeStructMAPElitesDone extends AlgorithmEvent
{
	//We shuold add information relevant to the algorithm, like time, fitness, different cells, how many reach maximum fitness, etc.
	//TODO: Might be interesting to add that info to have easier access!

	// for starter
	private List<GrammarGACell> evaluatedCells;

	public NarrativeStructMAPElitesDone()
	{
		evaluatedCells = new ArrayList<GrammarGACell>();
	}
	
	public void addCell(GrammarGACell grammarCell)
	{
		evaluatedCells.add(grammarCell);
	}
	
	public List<GrammarGACell> getCells()
	{
		return evaluatedCells;
	}
}
