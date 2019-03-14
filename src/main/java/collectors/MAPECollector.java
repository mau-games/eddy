package collectors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import generator.algorithm.MAPElites.GACell;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.AlgorithmStarted;
import util.eventrouting.events.GenerationDone;
import util.eventrouting.events.MAPEGenerationDone;

/**
 * This class has the functionality to store 3 different files corresponding to the MAP-Elites generation
 * 1- Summary of the generational data
 *  1.1- Dimensions and their resolution
 *  1.2- Cells with their index, dimension index and dimension value
 *  1.3- how many in the current feasible population
 *  1.4- how many individuals in the current infeasible population
 *  1.5- best fitness in each population.
 * 2- Each generation cell information 
 *  2.1- Dimensions and their resolution
 *  2.2- Specific cell information with its index, dimension index and dimension value
 *  2.3- all the feasible individuals fitness
 *  2.4- all the infeasible individuals fitness
 * 3- Algorithm information --> Every time the target room is changed
 *  3.1- Generation number (initially 0)
 *  3.2- Evolutionary targets (ratios, corridor and room weights, etc.)
 *  3.3- 
 * @author Alberto Alvarez, Malmö University
 *
 */
public class MAPECollector implements Listener
{
	private static MAPECollector instance = null;
	String currentProjectPath = ""; //Project path
	String currentRunPath = ""; //Evolutionary Run path
	String currentGenerationPath = ""; //Current Generation in the evolutionary run path
	
	
	StringBuilder[] currentGenerationSummary;
	StringBuilder[] currentGenerationCell;
	
	/**
	 * This constructor is only to be called by the getInstance() method.
	 */
	protected MAPECollector() {
		currentProjectPath = System.getProperty("user.dir") + "\\my-data";
		
		//Just checking if the directory exists!S
		File directory = new File(currentProjectPath);
		if (!directory.exists()) {
			directory.mkdir();
		}
		
		EventRouter.getInstance().registerListener(this, new GenerationDone(null));
	}
	
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return An instance of the MAP-Elites GA Collector
	 */
	public static MAPECollector getInstance() {
		if (instance == null) {
			instance = new MAPECollector();
		}
		return instance;
	}
	
	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub
		if (e instanceof MAPEGenerationDone) {
			MAPEGenerationDone generationInfo = (MAPEGenerationDone)e;
			SaveGeneration(generationInfo.getGenerationCounter(), generationInfo.getDimensions(), generationInfo.getCells());
			
		}
//		} else if (e instanceof AlgorithmDone) {
//			if (active) {
//				saveRun(((AlgorithmDone) e).getID());
//			}
//		} else if (e instanceof AlgorithmStarted) {
//			data.put(((AlgorithmStarted) e).getID(), new StringBuffer());
//		}
		
	}
	
	public synchronized void MapElitesStarted(UUID runID)
	{
		currentRunPath = currentProjectPath + "\\RUN_" + runID;
	}
	
	public synchronized void SaveGeneration(int generationCounter, ArrayList<GADimension> dimensions, ArrayList<GACell> cells)
	{
		currentGenerationPath = currentRunPath + "\\Generation-" + generationCounter;
		new File(currentGenerationPath).mkdirs();
		
		saveGenerationalSummary(dimensions, cells);
		
		for(int i = 0; i < cells.size(); i++)
			saveGenerationPerCell(dimensions, cells.get(i),i);
	}
	
	private synchronized void saveGenerationPerCell(ArrayList<GADimension> dimensions, GACell cell, int cellIndex)
	{
		File file = new File(currentGenerationPath + "\\cell_" + cellIndex +".csv");
		/*
		 2- Each generation cell information 
		 *  2.1- Dimensions and their resolution
		 *  2.2- Specific cell information with its index, dimension index and dimension value
		 *  2.3- all the feasible individuals fitness
		 *  2.4- individual Dimension X-y Value
		 *  
		 */
		int counter = 0;
		
		currentGenerationCell = new StringBuilder[cell.GetFeasiblePopulation().size() + 1];
		currentGenerationCell[counter] = new StringBuilder();
		currentGenerationCell[counter].append("DimensionX;DimensionX Resolution;DimensionY;DimensionY Resolution;Cell;Cell DimensionX; Cell DimensionY;"
				+ "IND Fit;IND DIMX;IND DIMY" + System.lineSeparator());
		
		counter++;
		for(int i = 0; i < cell.GetFeasiblePopulation().size(); i++, counter++)
		{
			
			currentGenerationCell[counter] = new StringBuilder();
			//DimensionX, dimensionY
			currentGenerationCell[counter].append(dimensions.get(0).GetType().toString() + ";");
			currentGenerationCell[counter].append(dimensions.get(0).GetGranularity() + ";");
			currentGenerationCell[counter].append(dimensions.get(1).GetType().toString()  + ";");
			currentGenerationCell[counter].append(dimensions.get(1).GetGranularity() + ";");
			currentGenerationCell[counter].append(cellIndex + ";");
			currentGenerationCell[counter].append(cell.GetDimensionValue(dimensions.get(0).GetType()) + ";");
			currentGenerationCell[counter].append(cell.GetDimensionValue(dimensions.get(1).GetType()) + ";");
			currentGenerationCell[counter].append(cell.GetFeasiblePopulation().get(i).getFitness() + ";");
			currentGenerationCell[counter].append(cell.GetFeasiblePopulation().get(i).getDimensionValue(dimensions.get(0).GetType()) + ";");
			currentGenerationCell[counter].append(cell.GetFeasiblePopulation().get(i).getDimensionValue(dimensions.get(1).GetType()) + System.lineSeparator());
		}

		try {
			for(StringBuilder tuple : currentGenerationCell)
			{
				FileUtils.write(file, tuple, true);
			}
		} catch (IOException e1) {
			
		}
		
	}
	
	private synchronized void saveGenerationalSummary(ArrayList<GADimension> dimensions, ArrayList<GACell> cells) 
	{
		File file = new File(currentGenerationPath + "\\generational-summary.csv");
		/*
		 *	1.1- Dimensions and their resolution
		 *  1.2- Cells with their index, dimension index and dimension value
		 *  1.3- how many in the current feasible population
		 *  1.4- how many individuals in the current infeasible population
		 *  1.5- best fitness in each population.
		 *  1.6- Cell Dimension X-y Value
		 *  
		 */
		int counter = 0;
		currentGenerationSummary = new StringBuilder[cells.size() + 1];
		
		currentGenerationSummary[counter] = new StringBuilder();
		currentGenerationSummary[counter].append("DimensionX;DimensionX Resolution;DimensionY;DimensionY Resolution;Cell;Cell DimensionX; Cell DimensionY;"
				+ "Cell F-POP;Cell IF-POP;Best Ind Fit;Best Ind DIMX;Best Ind DIMY" + System.lineSeparator());
		
		counter++;
		for(int i = 0; i < cells.size(); i++, counter++)
		{
			currentGenerationSummary[counter] = new StringBuilder();
			//DimensionX, dimensionY
			currentGenerationSummary[counter].append(dimensions.get(0).GetType().toString() + ";");
			currentGenerationSummary[counter].append(dimensions.get(0).GetGranularity() + ";");
			currentGenerationSummary[counter].append(dimensions.get(1).GetType().toString()  + ";");
			currentGenerationSummary[counter].append(dimensions.get(1).GetGranularity() + ";");
			currentGenerationSummary[counter].append(i + ";");
			currentGenerationSummary[counter].append(cells.get(i).GetDimensionValue(dimensions.get(0).GetType()) + ";");
			currentGenerationSummary[counter].append(cells.get(i).GetDimensionValue(dimensions.get(1).GetType()) + ";");
			currentGenerationSummary[counter].append(cells.get(i).GetFeasiblePopulation().size() + ";");
			currentGenerationSummary[counter].append(cells.get(i).GetInfeasiblePopulation().size() + ";");
			
			if(cells.get(i).GetFeasiblePopulation().size() > 0)
			{
				currentGenerationSummary[counter].append(cells.get(i).GetFeasiblePopulation().get(0).getFitness() + ";");
				currentGenerationSummary[counter].append(cells.get(i).GetFeasiblePopulation().get(0).getDimensionValue(dimensions.get(0).GetType()) + ";");
				currentGenerationSummary[counter].append(cells.get(i).GetFeasiblePopulation().get(0).getDimensionValue(dimensions.get(1).GetType()) + System.lineSeparator());
			}
			else
			{
				currentGenerationSummary[counter].append(";;" + System.lineSeparator());
			}

		}
		
		
		
		try {
			for(StringBuilder tuple : currentGenerationSummary)
			{
				FileUtils.write(file, tuple, true);
			}
			
		} catch (IOException e1) {
			
		}
		
	}

}
