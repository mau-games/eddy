package generator.algorithm.MAPElites;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import finder.PatternFinder;
import game.MapContainer;
import game.Room;
import generator.algorithm.Algorithm;
import generator.algorithm.ZoneIndividual;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.Dimensions.NPatternGADimension;
import generator.algorithm.MAPElites.Dimensions.SimilarityGADimension;
import generator.algorithm.MAPElites.Dimensions.SymmetryGADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.Algorithm.AlgorithmTypes;
import generator.config.GeneratorConfig;
import util.eventrouting.EventRouter;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.AlgorithmStarted;
import util.eventrouting.events.MAPElitesDone;
import util.eventrouting.events.MapUpdate;

public class MAPEliteAlgorithm extends Algorithm {
	
	//Actually I do not create populations I create the cells and then the population is assigned to each cell! 
	
	protected ArrayList<GACell> cells;
	
	//This is actually calculated from the multiplication of the amount of granularity the different dimensions have.
	int cellAmounts = 1;
	private ArrayList<GADimension> MAPElitesDimensions;
	private Random rnd = new Random();

	public MAPEliteAlgorithm(GeneratorConfig config) {
		super(config);
	}
	
	public MAPEliteAlgorithm(Room room, GeneratorConfig config){ //This is called from the batch run and when asked for suggestions view
		
		super(room, config);
	}
	
	public MAPEliteAlgorithm(Room room, GeneratorConfig config, AlgorithmTypes algorithmTypes) //THIS IS THE ONE CALLED WHEN IS NOT PRESERVING
	{
		super(room, config, algorithmTypes);
		
	}
	
	/**
	 * Create an Algorithm run using mutations of a given map
	 * @param room
	 */
	public MAPEliteAlgorithm(Room room, AlgorithmTypes algorithmTypes) //THIS IS CALLED WHEN WE WANT TO PRESERVE THE ROOM 
	{
		super(room, algorithmTypes);
	}
	
	public void CreateCells(int dimension, int dimensionQuantity, float [] dimensionSizes, int[] indices)
	{
		if(dimension >= dimensionQuantity)
		{
			this.cells.add(new GACell(MAPElitesDimensions, indices));
			return;
		}
		
		for(int i = 1; i < dimensionSizes[dimension] +1 ; i++)
		{
			indices[dimension] = i;
			CreateCells(dimension+1, dimensionQuantity, dimensionSizes, indices);
		}
	}
	
	public void CreateCellsOpposite(int dimension, float [] dimensionSizes, int[] indices)
	{
		if(dimension < 0)
		{
			this.cells.add(new GACell(MAPElitesDimensions, indices));
			return;
		}
		
		for(int i = 1; i < dimensionSizes[dimension] +1 ; i++)
		{
			indices[dimension] = i;
			CreateCellsOpposite(dimension-1, dimensionSizes, indices);
		}
	}
	
	
	public void initPopulations(Room room, MAPEDimensionFXML[] dimensions){
		broadcastStatusUpdate("Initialising...");
		
		feasiblePool = new ArrayList<ZoneIndividual>();
		infeasiblePool = new ArrayList<ZoneIndividual>();
		feasiblePopulation = new ArrayList<ZoneIndividual>();
		infeasiblePopulation = new ArrayList<ZoneIndividual>();
		
		MAPElitesDimensions = new ArrayList<GADimension>();
		
//		float dimension = 5.0f;//This should be sent when calling the algorithm!
		
		float[] dimensionsGranularity = new float[dimensions.length];
		int counter = 0;
		
		for(MAPEDimensionFXML dimension : dimensions)
		{
			MAPElitesDimensions.add(GADimension.CreateDimension(dimension.getDimension(), dimension.getGranularity()));
			dimensionsGranularity[counter++] = dimension.getGranularity();
		}
		
		//Add manually two dimensions
		
		
//		MAPElitesDimensions.add(new NPatternGADimension(dimension));

		//Initialize all the cells!
		this.cells = new ArrayList<GACell>();
//		CreateCells(0, MAPElitesDimensions.size(), dimensionsGranularity, new int[dimensions.length]); //the two last values should be 
		CreateCellsOpposite(MAPElitesDimensions.size() - 1, dimensionsGranularity, new int[dimensions.length]); //the two last values should be 
		cellAmounts = this.cells.size();
		
		int i = 0;
		int j = 0;
			
		while((i + j) < populationSize){
			ZoneIndividual ind = new ZoneIndividual(room, mutationProbability);
			ind.mutateAll(0.1, roomWidth, roomHeight);
			
			if(checkZoneIndividual(ind)){
				if(i < feasibleAmount){
					feasiblePool.add(ind);
					evaluateFeasibleZoneIndividual(ind);
					ind.SetDimensionValues(MAPElitesDimensions, room);
					
					for(GACell cell : cells)
					{
						if(cell.BelongToCell(ind, true))
							break;
					}
					
					i++;
				}
			}
			else {
				if(j < populationSize - feasibleAmount){
					infeasiblePool.add(ind);
					evaluateInfeasibleZoneIndividual(ind);
					ind.SetDimensionValues(MAPElitesDimensions, room);
					
					for(GACell cell : cells)
					{
						if(cell.BelongToCell(ind, false))
							break;
					}
					
					j++;
				}
			}
		}
		
		
		broadcastStatusUpdate("Population generated.");
	}
	
	/**
	 * Creates lists for the valid and invalid populations and populates them with ZoneIndividuals.
	 */
	public void initPopulations(){
		broadcastStatusUpdate("Initialising...");
		
		feasiblePool = new ArrayList<ZoneIndividual>();
		infeasiblePool = new ArrayList<ZoneIndividual>();
		feasiblePopulation = new ArrayList<ZoneIndividual>();
		infeasiblePopulation = new ArrayList<ZoneIndividual>();
		
		MAPElitesDimensions = new ArrayList<GADimension>();
		
		float dimension = 5.0f;//This should be sent when calling the algorithm!
		
		//Add manually two dimensions
		MAPElitesDimensions.add(new SymmetryGADimension(dimension));
		MAPElitesDimensions.add(new SimilarityGADimension(dimension));

		//Initialize all the cells!
		this.cells = new ArrayList<GACell>();
		CreateCells(0, MAPElitesDimensions.size(), new float[] {dimension, dimension}, new int[] {0, 0}); //the two last values should be
		
		cellAmounts = this.cells.size();
		
		int i = 0;
		int j = 0;
		
			
		while((i + j) < populationSize){
			ZoneIndividual ind = new ZoneIndividual(config, roomWidth * roomHeight, mutationProbability);
			ind.initialize();
			
			if(checkZoneIndividual(ind)){
				if(i < feasibleAmount){
					feasiblePool.add(ind);
					evaluateFeasibleZoneIndividual(ind);
					ind.SetDimensionValues(MAPElitesDimensions, null);
					
					for(GACell cell : cells)
					{
						if(cell.BelongToCell(ind, true))
							break;
					}
					
					i++;
				}
			}
			else {
				if(j < populationSize - feasibleAmount){
					infeasiblePool.add(ind);
					evaluateInfeasibleZoneIndividual(ind);
					ind.SetDimensionValues(MAPElitesDimensions, null);
					
					for(GACell cell : cells)
					{
						if(cell.BelongToCell(ind, false))
							break;
					}
					
					j++;
				}
			}
		}
		
		
		broadcastStatusUpdate("Population generated.");
	
	}
	
	/**
	 * Starts the algorithm. Called when the thread starts.
	 */
	public void run()
	{
		AlgorithmStarted as = new AlgorithmStarted();
		as.setID(id);
		EventRouter.getInstance().postEvent(as);
		
		broadcastStatusUpdate("Evolving...");
        int generations = config.getGenerations();
//        generations = 5;
        
        Room room = null;
        
        //Simple!
        
        // 1- We have already done that but ... Create N-Dimensional  MAP of Elites
        // 2- Place populations in their right cell (actually this probably means to check in which cell they belong?)
        // 3- Selection! Randomly select a cell, then do selection (tournament)
        // 4- mutate and/or crossover (while retaining parents)
        // 5- Simulate the individuals (in this case calculate the fitness) evaluate and place in the right cell
        // 6- after generations 
        // 	6.1 - Replace: Eliminate low performing individual from cells that are above or at capacity
        
        //200
        for(int generationCount = 1; generationCount <= generations; generationCount++) {
        	if(stop)
        		return;
        	
        	for(int iteration = 0; iteration < 1; iteration++)
        	{
        		ArrayList<ZoneIndividual> parents = new ArrayList<ZoneIndividual>();
        		List<ZoneIndividual> children = new ArrayList<ZoneIndividual>();
        		GACell current = null;
        		for(int count = 0; count < 5; count++)
            	{
        			children = new ArrayList<ZoneIndividual>();
	        		//This could actually be looped to select parents from different cells (according to TALAKAT)
	        		current = SelectCell(true);
	        		
	        		if(current != null)
	        		{
	        			parents.addAll(tournamentSelection(current.GetFeasiblePopulation()));
	            		
	            		//Breed!
	            		children.addAll(crossOverBetweenProgenitors(parents));
	            		
	            		//Evaluate and assign to correct Cell
	            		CheckAndAssignToCell(children, false);
	        		}
            	}
        		
        		
        		
        		////////////////////// NOW WE DO IT FOR THE INFEASIBLES! ///////////////////////
        		
        		parents = new ArrayList<ZoneIndividual>();
         		children = new ArrayList<ZoneIndividual>();
        		
        		//This could actually be looped to select parents from different cells (according to TALAKAT)
        		current = SelectCell(false);
        		
        		if(current != null)
        		{
        			parents.addAll(tournamentSelection(current.GetInfeasiblePopulation()));
            		
            		//Breed!
            		children.addAll(crossOverBetweenProgenitors(parents));
            		
            		//Evaluate and assign to correct Cell
            		CheckAndAssignToCell(children, true);
        		}
        		
        	}
        	
        	//Now we sort both populations in a given cell and cut through capacity!!!
        	for(GACell cell : cells)
			{
				cell.SortPopulations(false);
				cell.ApplyElitism();
			}
        }

        broadcastResultedRooms();
        for(GACell cell : cells)
		{
        	if(!cell.GetFeasiblePopulation().isEmpty())
        	{
        		room = cell.GetFeasiblePopulation().get(0).getPhenotype().getMap(-1, -1, null, null);
        		break;
        	}
		}
        
        
        PatternFinder finder = room.getPatternFinder();
		MapContainer result = new MapContainer();
		result.setMap(room);
		result.setMicroPatterns(finder.findMicroPatterns());
		result.setMesoPatterns(finder.findMesoPatterns());
		result.setMacroPatterns(finder.findMacroPatterns());
        AlgorithmDone ev = new AlgorithmDone(result, this);
        ev.setID(id);
        EventRouter.getInstance().postEvent(ev);
	}
	
	//TODO: There are problems on how the cells are rendered!
	public void broadcastResultedRooms()
	{
		MAPElitesDone ev = new MAPElitesDone();
        ev.setID(id);
        int cellIndex = 0;
        for(GACell cell : cells)
		{
        	System.out.println("CELL = " + cellIndex++);
//        	System.out.println("SYMMETRY: " + cell.GetDimensionValue(DimensionTypes.SIMILARITY) + ", PAT: " + cell.GetDimensionValue(DimensionTypes.SYMMETRY));
        	cell.BroadcastCellInfo();
        	if(cell.GetFeasiblePopulation().isEmpty())
        	{
        		ev.addRoom(null);
        		System.out.println("NO FIT ROOM!");
        	}
        	else //This is more tricky!!
        	{
        		ev.addRoom(cell.GetFeasiblePopulation().get(0).getPhenotype().getMap(-1, -1, null, null));
        		cell.GetFeasiblePopulation().get(0).BroadcastIndividualDimensions();
//        		System.out.println("FIT ROOM Fitness: " + cell.GetFeasiblePopulation().get(0).getFitness() + 
//        							", symmetry: " + cell.GetFeasiblePopulation().get(0).getDimensionValue(DimensionTypes.SIMILARITY) +
//        							", pat: " + cell.GetFeasiblePopulation().get(0).getDimensionValue(DimensionTypes.SYMMETRY));
        	}	
        	
        	System.out.println("------------------");
		}
        
		EventRouter.getInstance().postEvent(ev);
	}
	
	protected void CheckAndAssignToCell(List<ZoneIndividual> individuals, boolean infeasible)
	{
		 for (ZoneIndividual individual : individuals)
	        {
			 
	        	if(infeasible)
	        		individual.setChildOfInfeasibles(true);
	        	
	            if(checkZoneIndividual(individual))
	            {
	            	if(infeasible)
	            		infeasiblesMoved++;
	            	
	                evaluateFeasibleZoneIndividual(individual);
	                individual.SetDimensionValues(MAPElitesDimensions, this.originalRoom);
					
					for(GACell cell : cells)
					{
						if(cell.BelongToCell(individual, true))
							break;
					}
	            }
	            else
	            {
					evaluateInfeasibleZoneIndividual(individual);
					individual.SetDimensionValues(MAPElitesDimensions, this.originalRoom);
					
					for(GACell cell : cells)
					{
						if(cell.BelongToCell(individual, false))
							break;
					}
	            }
	        }
	}
	
	//Selects which cell to pick parents (Rnd)
	protected GACell SelectCell(boolean feasible)
	{
		GACell selected = null;
		
		List<GACell> cellsWithPop = new ArrayList<GACell>();
		
		for(GACell cell : cells)
		{
			if(feasible && !cell.GetFeasiblePopulation().isEmpty())
			{
				cellsWithPop.add(cell);
			}
			else if(!feasible && !cell.GetInfeasiblePopulation().isEmpty())
			{
				cellsWithPop.add(cell);
			}
		}
		
		if(cellsWithPop.isEmpty()) return null;
		
		selected = cellsWithPop.get(rnd.nextInt(cellsWithPop.size()));
		
//		while(selected == null)
//		{
//			selected = cells.get(rnd.nextInt(cells.size()));
//			
//			if(feasible && selected.GetFeasiblePopulation().isEmpty())
//			{
//				selected = null;
//			}
//			else if(!feasible && selected.GetInfeasiblePopulation().isEmpty())
//			{
//				selected =null;
//			}
//		}
		
		return selected;
	}
	
	//Select the parents from the popuation
	protected void SelectParents()
	{
		//This method
	}
	
	protected void BreedFeasibleIndividuals()
	{
		
	}
	
	protected void BreedInfeasibleIndividuals()
	{
		
	}
	
}
