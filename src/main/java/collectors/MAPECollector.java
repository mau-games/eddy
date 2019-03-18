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
import util.eventrouting.events.SaveDisplayedCells;

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
	public String currentProjectPath = ""; //Project path
	public String currentRunPath = ""; //Evolutionary Run path
	public String currentGenerationPath = ""; //Current Generation in the evolutionary run path
	public int currentGeneration = 0;
	private File currentDirectory;
	
	StringBuilder[] currentGenerationSummary;
	ArrayList<StringBuilder[]> currentGenerationCell;
	
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
		EventRouter.getInstance().registerListener(this, new SaveDisplayedCells());
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
			SaveGeneration(generationInfo.getGenerationCounter(), generationInfo.getDimensions(), generationInfo.getCells(), false);
			
		}
		else if(e instanceof SaveDisplayedCells)
		{
			WriteGenToFile();
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
	
	public synchronized void SaveGeneration(int generationCounter, ArrayList<GADimension> dimensions, ArrayList<GACell> cells, String folderName, boolean save)
	{
		this.currentGeneration = generationCounter;
		currentGenerationPath = currentRunPath + "\\" + folderName + "-" + generationCounter;

		//Save the generation summary
		saveGenerationalSummary(dimensions, cells);
		
		currentGenerationCell = new ArrayList<StringBuilder[]>();
		
		for(int i = 0; i < cells.size(); i++)
		{
			saveGenerationPerCell(dimensions, cells.get(i),i);
		}
		
		if(save)
			WriteGenToFile();			
	}
	
	public synchronized void SaveGeneration(int generationCounter, ArrayList<GADimension> dimensions, ArrayList<GACell> cells, boolean save)
	{
		this.currentGeneration = generationCounter;
		currentGenerationPath = currentRunPath + "\\Generation-" + generationCounter;

		//Save the generation summary
		saveGenerationalSummary(dimensions, cells);
		
		currentGenerationCell = new ArrayList<StringBuilder[]>();
		
		for(int i = 0; i < cells.size(); i++)
		{
			saveGenerationPerCell(dimensions, cells.get(i),i);
		}
		
		if(save)
			WriteGenToFile();			
	}
	
	private synchronized void saveGenerationPerCell(ArrayList<GADimension> dimensions, GACell cell, int cellIndex)
	{
		
		/*
		 2- Each generation cell information 
		 *  2.1- Dimensions and their resolution
		 *  2.2- Specific cell information with its index, dimension index and dimension value
		 *  2.3- all the feasible individuals fitness
		 *  2.4- individual Dimension X-y Value
		 *  
		 */
		int counter = 0;
		
		currentGenerationCell.add(new StringBuilder[cell.GetFeasiblePopulation().size() + 1]);
		currentGenerationCell.get(cellIndex)[counter] = new StringBuilder();
		currentGenerationCell.get(cellIndex)[counter].append("DimensionX;DimensionX Resolution;DimensionY;DimensionY Resolution;Cell;Cell DimensionX; Cell DimensionY;"
				+ "IND Fit;IND DIMX;IND DIMY" + System.lineSeparator());
		
		counter++;
		for(int i = 0; i < cell.GetFeasiblePopulation().size(); i++, counter++)
		{
			
			currentGenerationCell.get(cellIndex)[counter] = new StringBuilder();
			//DimensionX, dimensionY
			currentGenerationCell.get(cellIndex)[counter].append(dimensions.get(0).GetType().toString() + ";");
			currentGenerationCell.get(cellIndex)[counter].append(dimensions.get(0).GetGranularity() + ";");
			currentGenerationCell.get(cellIndex)[counter].append(dimensions.get(1).GetType().toString()  + ";");
			currentGenerationCell.get(cellIndex)[counter].append(dimensions.get(1).GetGranularity() + ";");
			currentGenerationCell.get(cellIndex)[counter].append(cellIndex + ";");
			currentGenerationCell.get(cellIndex)[counter].append(cell.GetDimensionValue(dimensions.get(0).GetType()) + ";");
			currentGenerationCell.get(cellIndex)[counter].append(cell.GetDimensionValue(dimensions.get(1).GetType()) + ";");
			currentGenerationCell.get(cellIndex)[counter].append(cell.GetFeasiblePopulation().get(i).getFitness() + ";");
			currentGenerationCell.get(cellIndex)[counter].append(cell.GetFeasiblePopulation().get(i).getDimensionValue(dimensions.get(0).GetType()) + ";");
			currentGenerationCell.get(cellIndex)[counter].append(cell.GetFeasiblePopulation().get(i).getDimensionValue(dimensions.get(1).GetType()) + System.lineSeparator());
		}

	
		
	}
	
	private synchronized void saveGenerationalSummary(ArrayList<GADimension> dimensions, ArrayList<GACell> cells) 
	{
		
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

	}
	
	public synchronized void WriteGenToFile()
	{
		//Just checking if the directory exists!S
		File directory = new File(currentGenerationPath);
		if (!directory.exists()) 
		{
			directory.mkdir();
			currentDirectory = directory;
		}
		
		WriteGenSummaryToFile();
		WriteCellsToFile();
	}
	
	public File getDirectory()
	{
		return currentDirectory;
	}
	
	private synchronized void WriteGenSummaryToFile()
	{
		File file = new File(currentGenerationPath + "\\generational-summary.csv");
		
		try {
			for(StringBuilder tuple : currentGenerationSummary)
			{
				FileUtils.write(file, tuple, true);
			}
			
		} catch (IOException e1) {
			
		}
	}
	
	private synchronized void WriteCellsToFile()
	{
		for(int cellIndex = 0; cellIndex < currentGenerationCell.size(); cellIndex++)
		{
			//Rename the file!
			File file = new File(currentGenerationPath + "\\cell_" + cellIndex +".csv");
			
			try {
				for(StringBuilder tuple : currentGenerationCell.get(cellIndex))
				{
					FileUtils.write(file, tuple, true);
				}
			} catch (IOException e1) {
				
			}
		}
		
	}

}
