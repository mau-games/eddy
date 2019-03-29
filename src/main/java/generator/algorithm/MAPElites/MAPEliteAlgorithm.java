package generator.algorithm.MAPElites;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import collectors.MAPECollector;
import finder.PatternFinder;
import game.MapContainer;
import game.Room;
import generator.algorithm.Algorithm;
import generator.algorithm.ZoneGenotype;
import generator.algorithm.ZoneIndividual;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.Dimensions.NPatternGADimension;
import generator.algorithm.MAPElites.Dimensions.SimilarityGADimension;
import generator.algorithm.MAPElites.Dimensions.SymmetryGADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.Algorithm.AlgorithmTypes;
import generator.config.GeneratorConfig;
import util.Util;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.AlgorithmStarted;
import util.eventrouting.events.MAPEGenerationDone;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.MAPElitesDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.SaveDisplayedCells;

public class MAPEliteAlgorithm extends Algorithm implements Listener {
	
	//Actually I do not create populations I create the cells and then the population is assigned to each cell! 
	
	protected ArrayList<GACell> cells;
	
	//This is actually calculated from the multiplication of the amount of granularity the different dimensions have.
	int cellAmounts = 1;
	private ArrayList<GADimension> MAPElitesDimensions;
	private Random rnd = new Random();
	private int iterationsToPublish = 50;
	private int breedingGenerations = 5; //this relates to how many generations will it breed 
	private int realCurrentGen = 0;
	private int currentGen = 0;
	MAPEDimensionFXML[] dimensions;
	private boolean dimensionsChanged = false;

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
	 * Create an Algorithm run using mutations of a given map -- actually no
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
		EventRouter.getInstance().registerListener(this, new MAPEGridUpdate(null));
		
		this.dimensions = dimensions;
		initCells(dimensions);
		room.SetDimensionValues(MAPElitesDimensions);
		
		int i = 0;
		int j = 0;
		
		populationSize = 1000;
		feasibleAmount = 750;

		while((i + j) < populationSize){
			ZoneIndividual ind = new ZoneIndividual(room, mutationProbability);
			ind.mutateAll(0.7, roomWidth, roomHeight);
			
			if(checkZoneIndividual(ind)){
				if(i < feasibleAmount){
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
	
	
	private void initCells(MAPEDimensionFXML[] dimensions)
	{
		//Initialize cells
		MAPElitesDimensions = new ArrayList<GADimension>();
		
		//Helper variables to create the cells
		float[] dimensionsGranularity = new float[dimensions.length];
		int counter = 0;
		
		for(MAPEDimensionFXML dimension : dimensions)
		{
			MAPElitesDimensions.add(GADimension.CreateDimension(dimension.getDimension(), dimension.getGranularity()));
			dimensionsGranularity[counter++] = dimension.getGranularity();
		}

		//Initialize all the cells!
		this.cells = new ArrayList<GACell>();
		CreateCellsOpposite(MAPElitesDimensions.size() - 1, dimensionsGranularity, new int[dimensions.length]);
		cellAmounts = this.cells.size();
	}
	
	public void RecreateCells()
	{
		List<ZoneIndividual> children = new ArrayList<ZoneIndividual>();
		List<ZoneIndividual> nonFeasibleChildren = new ArrayList<ZoneIndividual>();
		for(GACell cell : cells)
		{
			children.addAll(cell.GetFeasiblePopulation());
			nonFeasibleChildren.addAll(cell.GetInfeasiblePopulation());
			cell.GetFeasiblePopulation().clear();
			cell.GetInfeasiblePopulation().clear();
		}
		
		//now init cells again!
		initCells(this.dimensions);
		
		//Assign everything
		CheckAndAssignToCell(children, false);
		CheckAndAssignToCell(nonFeasibleChildren, true);
	}
	
	
	@Override
	public void ping(PCGEvent e) //TODO: I SHOULD ALSO ADD THE INFO WHEN A MAP IS UPDATED --> For all the extra calculations
	{
		// TODO Auto-generated method stub
		if(e instanceof MAPEGridUpdate)
		{
			this.dimensions = ((MAPEGridUpdate) e).getDimensions(); 
			dimensionsChanged = true;
		}
	}
	
	
	
	
	//FIXME: this is called in the loop when X amount of generations have pass. It should be called by event!
	//FIXME: The problem is that because the original map is by reference, it gets "updated" as the reference have changed
	public void UpdateConfigFile()
	{
		this.config = originalRoom.getCalculatedConfig();
		populationSize = config.getPopulationSize();
		mutationProbability = (float)config.getMutationProbability();
		offspringSize = (float)config.getOffspringSize();
		feasibleAmount = (int)((double)populationSize * config.getFeasibleProportion());
		roomTarget = config.getRoomProportion();
		corridorTarget = config.getCorridorProportion();
		originalRoom.SetDimensionValues(MAPElitesDimensions);
		
		//Extra
		for(GACell cell : cells)
		{
			cell.ResetPopulation(this.config);
		}
	}
	
	/**
     * Selects parents from a population using (deterministic) tournament selection - i.e. the winner is always the ZoneIndividual with the "best" fitness.
     * See: https://en.wikipedia.org/wiki/Tournament_selection
     * 
     * @param population A whole population of ZoneIndividuals
     * @return A list of chosen progenitors
     */
    protected List<ZoneIndividual> tournamentSelection(List<ZoneIndividual> population, int parentNumber)
    { 
        List<ZoneIndividual> parents = new ArrayList<ZoneIndividual>();
        List<ZoneIndividual> candidates = new ArrayList<ZoneIndividual>(population);
//        int numberOfParents = (int)(offspringSize * population.size()) / 2;

        if(candidates.size() == 1)
        	return candidates;
        
        while(parents.size() <= parentNumber && candidates.size() > 1)
        {
        	//Select at least one ZoneIndividual to "fight" in the tournament
            int tournamentSize = Util.getNextInt(1, candidates.size());

            ZoneIndividual winner = null;
            for(int i = 0; i < tournamentSize; i++)
            {
                int progenitorIndex = Util.getNextInt(0, candidates.size());
                ZoneIndividual ZoneIndividual = candidates.remove(progenitorIndex);

                //select the ZoneIndividual with the highest fitness
                if(winner == null || (winner.getFitness() < ZoneIndividual.getFitness()))
                {
                	winner = ZoneIndividual;
                }
            }

            parents.add(winner);
        }

        return parents;
    }
    
    private void runInterbreedingApplElites()
    {
    	//If we have receive the even that the dimensions changed, please modify the dimensions and recalculate the cells!
    	if(dimensionsChanged)
    	{
    		RecreateCells();
    		dimensionsChanged = false;
    	}


    	ArrayList<ZoneIndividual> parents = new ArrayList<ZoneIndividual>();
		List<ZoneIndividual> children = new ArrayList<ZoneIndividual>();
		GACell current = null;
		for(int count = 0; count < 10; count++) //Actual gens
    	{
//			children = new ArrayList<ZoneIndividual>();
    		//This could actually be looped to select parents from different cells (according to TALAKAT)
    		current = SelectCell(true);
    		
    		if(current != null)
    		{
    			current.exploreCell();
    			parents.addAll(tournamentSelection(current.GetFeasiblePopulation(), 5));
        		
    		}
    		
    		//This could actually be looped to select parents from different cells (according to TALAKAT)
			current = SelectCell(false);
    		
    		if(current != null)
    		{
    			parents.addAll(tournamentSelection(current.GetInfeasiblePopulation(), 5));
    		}
    	}

		
		//Breed!
		children.addAll(crossOverBetweenProgenitors(parents));
		
		//Evaluate and assign to correct Cell
		CheckAndAssignToCell(children, false);
	
		//Now we sort both populations in a given cell and cut through capacity!!!
		for(GACell cell : cells)
		{
			cell.SortPopulations(false);
			cell.ApplyElitism();
		}
		

		
    	//This is only when we want to update the current Generation
    	if(currentGen >= iterationsToPublish)
    	{

    		publishGeneration();
    	}
    	else {
    		currentGen++;
    	}
    	
    	realCurrentGen++;
    }
    
    private void runNoInterbreedingApplElites()
    {
    	//If we have receive the even that the dimensions changed, please modify the dimensions and recalculate the cells!
    	if(dimensionsChanged)
    	{
    		RecreateCells();
    		dimensionsChanged = false;
    	}
		ArrayList<ZoneIndividual> feasibleParents = new ArrayList<ZoneIndividual>();
		ArrayList<ZoneIndividual> infeasibleParents = new ArrayList<ZoneIndividual>();
		
    	List<ZoneIndividual> feasibleChildren = new ArrayList<ZoneIndividual>();
    	List<ZoneIndividual> infeasibleChildren = new ArrayList<ZoneIndividual>();
		GACell current = null;

		for(int count = 0; count < breedingGenerations; count++) //Actual gens
    	{
//    			children = new ArrayList<ZoneIndividual>();
    		//This could actually be looped to select parents from different cells (according to TALAKAT)
    		current = SelectCell(true);
    		
    		if(current != null)
    		{
    			current.exploreCell();
    			feasibleParents.addAll(tournamentSelection(current.GetFeasiblePopulation(), 5));   		
    		}
    		
    		//This could actually be looped to select parents from different cells (according to TALAKAT)
			current = SelectCell(false);
    		
    		if(current != null)
    		{
    			infeasibleParents.addAll(tournamentSelection(current.GetInfeasiblePopulation(), 5));
    		}
    	}
    	
		//Breed!
		feasibleChildren.addAll(crossOverBetweenProgenitors(feasibleParents));
		infeasibleChildren.addAll(crossOverBetweenProgenitors(infeasibleParents));
		
		CheckAndAssignToCell(feasibleChildren, false);
		CheckAndAssignToCell(infeasibleChildren, true);
		
    	//Now we sort both populations in a given cell and cut through capacity!!!
    	for(GACell cell : cells)
		{
			cell.SortPopulations(false);
			cell.ApplyElitism();
		}
    	
		
    	//This is only when we want to update the current Generation
    	if(currentGen >= iterationsToPublish)
    	{

    		publishGeneration();
    	}
    	else {
    		currentGen++;
    	}
    	
    	realCurrentGen++;
    }
    
    private void runNoInterbreedingExperiment()
    {
    	//If we have receive the even that the dimensions changed, please modify the dimensions and recalculate the cells!
    	if(dimensionsChanged)
    	{
    		RecreateCells();
    		dimensionsChanged = false;
    	}
		ArrayList<ZoneIndividual> feasibleParents = new ArrayList<ZoneIndividual>();
		ArrayList<ZoneIndividual> infeasibleParents = new ArrayList<ZoneIndividual>();
		
    	List<ZoneIndividual> feasibleChildren = new ArrayList<ZoneIndividual>();
    	List<ZoneIndividual> infeasibleChildren = new ArrayList<ZoneIndividual>();
		GACell current = null;
		
		for(int count = 0; count < breedingGenerations; count++) //Actual gens
    	{
//    			children = new ArrayList<ZoneIndividual>();
    		//This could actually be looped to select parents from different cells (according to TALAKAT)
    		current = SelectCell(true);
    		
    		if(current != null)
    		{
    			current.exploreCell();
    			feasibleParents.addAll(tournamentSelection(current.GetFeasiblePopulation(), 5));   		
    		}
    		
    		//This could actually be looped to select parents from different cells (according to TALAKAT)
			current = SelectCell(false);
    		
    		if(current != null)
    		{
    			infeasibleParents.addAll(tournamentSelection(current.GetInfeasiblePopulation(), 5));
    		}
    	}
    	
		//Breed!
		feasibleChildren.addAll(crossOverBetweenProgenitors(feasibleParents));
		infeasibleChildren.addAll(crossOverBetweenProgenitors(infeasibleParents));
		
		CheckAndAssignToCell(feasibleChildren, false);
		CheckAndAssignToCell(infeasibleChildren, true);
		
    	//This is only when we want to update the current Generation
    	if(currentGen >= iterationsToPublish)
    	{
        	//Now we sort both populations in a given cell and cut through capacity!!!
        	for(GACell cell : cells)
    		{
    			cell.SortPopulations(false);
    			cell.ApplyElitism();
    		}
        	
    		publishGeneration();
    	}
    	else {
    		currentGen++;
    	}
    	
    	realCurrentGen++;
    }
    
    private void runInterbreedingExperiment()
    {
    	//If we have receive the even that the dimensions changed, please modify the dimensions and recalculate the cells!
    	if(dimensionsChanged)
    	{
    		RecreateCells();
    		dimensionsChanged = false;
    	}

		ArrayList<ZoneIndividual> parents = new ArrayList<ZoneIndividual>();
		List<ZoneIndividual> children = new ArrayList<ZoneIndividual>();
		
		GACell current = null;
		
		for(int count = 0; count < breedingGenerations; count++) //Actual gen
		{
			
//    		children = new ArrayList<ZoneIndividual>();
    		//This could actually be looped to select parents from different cells (according to TALAKAT)
    		current = SelectCell(true);
    		
    		if(current != null)
    		{
    			current.exploreCell();
    			parents.addAll(tournamentSelection(current.GetFeasiblePopulation(), 5));   		
    		}
    		
    		//This could actually be looped to select parents from different cells (according to TALAKAT)
			current = SelectCell(false);
    		
    		if(current != null)
    		{
    			parents.addAll(tournamentSelection(current.GetInfeasiblePopulation(), 5));
    		}

    	}
		
		//Breed!
		children.addAll(crossOverBetweenProgenitors(parents));
		CheckAndAssignToCell(children, false);
    	
    	
    	//This is only when we want to update the current Generation
    	if(currentGen >= iterationsToPublish)
    	{
        	//Now we sort both populations in a given cell and cut through capacity!!!
        	for(GACell cell : cells)
    		{
    			cell.SortPopulations(false);
    			cell.ApplyElitism();
    		}
        	
    		publishGeneration();
    	}
    	else {
    		currentGen++;
    	}
    	
    	realCurrentGen++;
    }
    
    private void publishGeneration()
    {
		broadcastResultedRooms();
		MAPECollector.getInstance().SaveGeneration(realCurrentGen, MAPElitesDimensions, cells, false); //store the cells in memory
		
		//This should be in a call when the ping happens! --> FIXME!!
		UpdateConfigFile();

		List<ZoneIndividual> feasibleChildren = new ArrayList<ZoneIndividual>();
		List<ZoneIndividual> nonFeasibleChildren = new ArrayList<ZoneIndividual>();
		
		//To impulse diversity, every 100 gens, we create a mutation of all the populations
		//and we evaluate that new population and the previous population and assign the right to the right cell
		for(GACell cell : cells)
		{
			feasibleChildren.addAll(createMutatedChildren(cell.GetFeasiblePopulation(), 0.6f));
			feasibleChildren.addAll(cell.GetFeasiblePopulation());
			nonFeasibleChildren.addAll(cell.GetInfeasiblePopulation());
			cell.GetFeasiblePopulation().clear();
			cell.GetInfeasiblePopulation().clear();
		}
		
		//We add a untouched copy of the currently edited room into the population (with the hope that it will be incorporated as an elite)
		ZoneIndividual ind = new ZoneIndividual(originalRoom, mutationProbability);
		ind.SetDimensionValues(MAPElitesDimensions, this.originalRoom);
		 if(checkZoneIndividual(ind))
		 {
			 feasibleChildren.add(ind);
		 }
		 else
		 {
			 nonFeasibleChildren.add(ind);
		 }

		 //Check and assign the cells!
		CheckAndAssignToCell(feasibleChildren, false);
		CheckAndAssignToCell(nonFeasibleChildren, true);
		
		//Sort the populations in the cell and Eliminate low performing cells individuals
    	for(GACell cell : cells)
		{
			cell.SortPopulations(false);
			cell.ApplyElitism();
		}

    	//"restart" current gen!
		currentGen = 0;
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
        MAPECollector.getInstance().MapElitesStarted(id);
        
        //Simple!
        
        // 1- We have already done that but ... Create N-Dimensional  MAP of Elites
        // 2- Place populations in their right cell (actually this probably means to check in which cell they belong?)
        // 3- Selection! Randomly select a cell, then do selection (tournament)
        // 4- mutate and/or crossover (while retaining parents)
        // 5- Simulate the individuals (in this case calculate the fitness) evaluate and place in the right cell
        // 6- after generations 
        // 	6.1 - Replace: Eliminate low performing individual from cells that are above or at capacity
        
        currentGen = 0;
        realCurrentGen = 0;
        
        while(!stop) //continuous evolution
        {
//        	runNoInterbreedingExperiment();
//        	runInterbreedingExperiment(); // This one and run experiment previous are the best!
        	runNoInterbreedingApplElites();
//        	runInterbreedingApplElites();
        	/*
        	//If we have receive the even that the dimensions changed, please modify the dimensions and recalculate the cells!
        	if(dimensionsChanged)
        	{
        		RecreateCells();
        		dimensionsChanged = false;
        	}
    		ArrayList<ZoneIndividual> feasibleParents = new ArrayList<ZoneIndividual>();
    		ArrayList<ZoneIndividual> infeasibleParents = new ArrayList<ZoneIndividual>();
    		
    		List<ZoneIndividual> children = new ArrayList<ZoneIndividual>();
    		
//        		List<ZoneIndividual> feasibleChildren = new ArrayList<ZoneIndividual>();
//        		List<ZoneIndividual> infeasibleChildren = new ArrayList<ZoneIndividual>();
    		GACell current = null;
    		
    		for(int count = 0; count < breedingGenerations; count++) //Actual gens
        	{
    			List<ZoneIndividual> feasibleChildren = new ArrayList<ZoneIndividual>();
        		List<ZoneIndividual> infeasibleChildren = new ArrayList<ZoneIndividual>();
//        			children = new ArrayList<ZoneIndividual>();
        		//This could actually be looped to select parents from different cells (according to TALAKAT)
        		current = SelectCell(true);
        		
        		if(current != null)
        		{
        			current.exploreCell();
        			feasibleParents.addAll(tournamentSelection(current.GetFeasiblePopulation()));   		
        		}
        		
        		//This could actually be looped to select parents from different cells (according to TALAKAT)
    			current = SelectCell(false);
        		
        		if(current != null)
        		{
        			infeasibleParents.addAll(tournamentSelection(current.GetInfeasiblePopulation()));
        		}
        		
        		//Breed!
        		feasibleChildren.addAll(crossOverBetweenProgenitors(feasibleParents));
        		infeasibleChildren.addAll(crossOverBetweenProgenitors(infeasibleParents));
        		
        		CheckAndAssignToCell(feasibleChildren, false);
        		CheckAndAssignToCell(infeasibleChildren, true);

        		
        		//Breed!
//        		children.addAll(crossOverBetweenProgenitors(parents));
        		
        		//Evaluate and assign to correct Cell
//        		CheckAndAssignToCell(feasibleChildren, false);
//        		CheckAndAssignToCell(infeasibleChildren, true);
        		
        	}
        	
        	//Now we sort both populations in a given cell and cut through capacity!!!
        	for(GACell cell : cells)
			{
				cell.SortPopulations(false);
				cell.ApplyElitism();
			}
        	
//        	EventRouter.getInstance().postEvent(new MAPEGenerationDone(realCurrentGen, MAPElitesDimensions, cells));
//        	MAPECollector.getInstance().SaveGeneration(realCurrentGen, MAPElitesDimensions, cells, true);
        	
        	
        	//This is only when we want to update the current Generation
        	if(currentGen >= iterationsToPublish)
        	{
        		publishGeneration();
        	}
        	else {
        		currentGen++;
        	}
        	
        	realCurrentGen++;
        	*/
        }
        
       ////////////////////////// POST END MAP-ELITES /////////////////////////////
        
        for(GACell cell : cells)
		{
			cell.SortPopulations(false);
			cell.ApplyElitism();
		}
		
        broadcastResultedRooms();
        
        //We save the last generation
        MAPECollector.getInstance().SaveGeneration(realCurrentGen, MAPElitesDimensions, cells, "STOP", false);
        EventRouter.getInstance().postEvent(new SaveDisplayedCells());
        
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
//        	System.out.println("CELL = " + cellIndex++);
//        	System.out.println("SYMMETRY: " + cell.GetDimensionValue(DimensionTypes.SIMILARITY) + ", PAT: " + cell.GetDimensionValue(DimensionTypes.SYMMETRY));
//        	cell.BroadcastCellInfo();
        	if(cell.GetFeasiblePopulation().isEmpty())
        	{
        		ev.addRoom(null);
//        		System.out.println("NO FIT ROOM!");
        	}
        	else //This is more tricky!!
        	{
        		ev.addRoom(cell.GetFeasiblePopulation().get(0).getPhenotype().getMap(-1, -1, null, null));
//        		cell.GetFeasiblePopulation().get(0).BroadcastIndividualDimensions();
        		evaluateFeasibleZoneIndividual(cell.GetFeasiblePopulation().get(0));
//        		System.out.println("FIT ROOM Fitness: " + cell.GetFeasiblePopulation().get(0).getFitness() + 
//        							", symmetry: " + cell.GetFeasiblePopulation().get(0).getDimensionValue(DimensionTypes.SIMILARITY) +
//        							", pat: " + cell.GetFeasiblePopulation().get(0).getDimensionValue(DimensionTypes.SYMMETRY));
        	}	
        	
//        	System.out.println("------------------");
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
	
	protected ArrayList<ZoneIndividual> createMutatedChildren(List<ZoneIndividual> population, float mProbability)
	{
		ArrayList<ZoneIndividual> mutatedChildren = new ArrayList<ZoneIndividual>();
		ZoneIndividual child = null;
		for(ZoneIndividual ind : population)
		{
			if(rnd.nextFloat() < mProbability)
			{
				child = new ZoneIndividual(config, new ZoneGenotype(config, ind.getGenotype().getChromosome().clone(), 
						ind.getGenotype().GetRootChromosome()), this.mutationProbability);
				
				child.mutateAll(mProbability, this.roomWidth, this.roomHeight);
				mutatedChildren.add(child);
			}
		}
		
		
		return mutatedChildren;
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
