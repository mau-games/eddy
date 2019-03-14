package util.eventrouting.events;

import java.util.ArrayList;
import java.util.List;

import game.Room;
import generator.algorithm.MAPElites.GACell;
import generator.algorithm.MAPElites.Dimensions.GADimension;

public class MAPEGenerationDone extends AlgorithmEvent
{
	//We shuold add information relevant to the algorithm, like time, fitness, different cells, how many reach maximum fitness, etc.
	
	// for starter
	private int generationCounter;
	private ArrayList<GADimension> dimensions;
	private ArrayList<GACell> cells;
	
	public MAPEGenerationDone(int generationCounter, ArrayList<GADimension> dimensions, ArrayList<GACell> cells)
	{
		this.setGenerationCounter(generationCounter);
		this.setDimensions(dimensions);
		this.setCells(cells);
	}

	public int getGenerationCounter() {
		return generationCounter;
	}

	public void setGenerationCounter(int generationCounter) {
		this.generationCounter = generationCounter;
	}

	public ArrayList<GADimension> getDimensions() {
		return dimensions;
	}

	public void setDimensions(ArrayList<GADimension> dimensions) {
		this.dimensions = dimensions;
	}

	public ArrayList<GACell> getCells() {
		return cells;
	}

	public void setCells(ArrayList<GACell> cells) {
		this.cells = cells;
	}

}
