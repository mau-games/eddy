package generator.algorithm.MAPElites;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import collectors.DataSaverLoader;
import collectors.MAPECollector;
import finder.PatternFinder;
import game.MapContainer;
import game.Room;
import generator.algorithm.Algorithm;
import generator.algorithm.ZoneGenotype;
import generator.algorithm.ZoneIndividual;
import generator.algorithm.ZonePhenotype;
import generator.algorithm.MAPElites.Dimensions.CharacteristicSimilarityGADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.Dimensions.NPatternGADimension;
import generator.algorithm.MAPElites.Dimensions.SimilarityGADimension;
import generator.algorithm.MAPElites.Dimensions.SymmetryGADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.Algorithm.AlgorithmTypes;
import generator.config.GeneratorConfig;
import gui.InteractiveGUIController;
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
import util.eventrouting.events.NextStepSequenceExperiment;
import util.eventrouting.events.RoomEdited;
import util.eventrouting.events.SaveCurrentGeneration;
import util.eventrouting.events.SaveDisplayedCells;
import util.eventrouting.events.UpdatePreferenceModel;

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
	
	private int saveCounter = 0;
	
	//For the Expressive range test
//	ArrayList<Room> uniqueGeneratedRooms = new ArrayList<Room>();
	HashMap<Room, Double[]> uniqueGeneratedRooms = new HashMap<Room, Double[]>();
	HashMap<Room, Double[]> uniqueGeneratedRoomsFlush= new HashMap<Room, Double[]>();
	HashMap<Room, Double[]> uniqueGeneratedRoomsSince = new HashMap<Room, Double[]>();
	
	StringBuilder uniqueRoomsData = new StringBuilder();
	StringBuilder uniqueRoomsSinceData = new StringBuilder();
	
	private int saveIterations = 2;
	private int currentSaveStep = 0;
	
	//UGLY WAY OF DOING THIS!
	ArrayList<ZoneIndividual> currentRendered = new ArrayList<ZoneIndividual>(); //I think this didn't work

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
		EventRouter.getInstance().registerListener(this, new UpdatePreferenceModel(null));
		EventRouter.getInstance().registerListener(this, new SaveCurrentGeneration());
		EventRouter.getInstance().registerListener(this, new RoomEdited(null));
		
		this.dimensions = dimensions;
		initCells(dimensions);
		room.SetDimensionValues(MAPElitesDimensions);
		
		int i = 0;
		int j = 0;
		
		populationSize = 1000;
		feasibleAmount = 750;
		
		//initialize the data storage variables
		uniqueRoomsData = new StringBuilder();
		uniqueRoomsSinceData = new StringBuilder();
		uniqueRoomsData.append("Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;Score;DIM X;DIM Y;STEP;Gen;Type" + System.lineSeparator());
		uniqueRoomsSinceData.append("Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;Score;DIM X;DIM Y;STEP;Gen;Type" + System.lineSeparator());

		//TODO: THIS IS CREISI!mutate
//		System.out.println(mutationProbability);
//		mutationProbability = 0.3f;
		
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
					ind.SetDimensionValues(MAPElitesDimensions, null);
					evaluateFeasibleZoneIndividual(ind);
					
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
					ind.SetDimensionValues(MAPElitesDimensions, null);
					evaluateInfeasibleZoneIndividual(ind);
					
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
		
		//New addition
		currentRendered.clear();
		
		for(int i = 0; i < cellAmounts; i++)
		{
			currentRendered.add(null);
		}
		
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
	
	
	public void ping(PCGEvent e) //TODO: I SHOULD ALSO ADD THE INFO WHEN A MAP IS UPDATED --> For all the extra calculations
	{
		// TODO Auto-generated method stub
		if(e instanceof MAPEGridUpdate)
		{
			this.dimensions = ((MAPEGridUpdate) e).getDimensions(); 
			dimensionsChanged = true;
		}
		else if(e instanceof UpdatePreferenceModel) 
		{
			this.userPreferences = ((UpdatePreferenceModel) e).getCurrentUserModel(); 
			//TODO: PLEASE CHANGE THIS
//			RecreateCells();
			dimensionsChanged = true;
		}
		else if(e instanceof SaveCurrentGeneration)
		{
			storeMAPELITESXml();
		}
		else if(e instanceof RoomEdited)
		{
			originalRoom = (Room) e.getPayload();
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
		
		for(ZoneIndividual rend : currentRendered)
		{
			if(rend != null)
			{
				rend.ResetPhenotype(this.config);
				evaluateFeasibleZoneIndividual(rend);
			}
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
    	//Comment or uncomment to store unique rooms every generation (based on what is generated before)
    	storeUniqueRooms();
    	
    	//If we have receive the event that the dimensions changed, please modify the dimensions and recalculate the cells!
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
    		//TODO: For next evaluation
    		saveIterations--;
    		
    		if(saveIterations == 0)
    		{
//    			System.out.println("NEXT");
    			saveIterations=2;
    			saveUniqueRoomsToFileAndFlush();
    			currentSaveStep++;
    			EventRouter.getInstance().postEvent(new NextStepSequenceExperiment());
    			System.out.println(realCurrentGen);
    		}
    		
//    		System.out.println(realCurrentGen);
    		publishGeneration();
    	}
    	else {
    		currentGen++;
    	}
    	
    	//Uncomment to save unique rooms
    	
//    	if(realCurrentGen == 5000)
//    	{
//    		System.out.println(uniqueGeneratedRooms.size());
//    		saveUniqueRoomsToFile();
////    		publishGeneration();
//    	}
//    	
//    	if(realCurrentGen % 1000 == 0)
//    	{
//    		System.out.println(uniqueGeneratedRooms.size());
//        	System.out.println("Current Generation: " + realCurrentGen);
//    	}


    	
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
        	runNoInterbreedingApplElites(); //This is actually the good one!! 2019-04-23
//        	runInterbreedingApplElites();
        }
        
       ////////////////////////// POST END MAP-ELITES /////////////////////////////
        
        for(GACell cell : cells)
		{
			cell.SortPopulations(false);
			cell.ApplyElitism();
		}
		
//        broadcastResultedRooms();
        
        //We save the last generation
//        MAPECollector.getInstance().SaveGeneration(realCurrentGen, MAPElitesDimensions, cells, "STOP", false);
//        EventRouter.getInstance().postEvent(new SaveDisplayedCells());
        
        for(GACell cell : cells)
		{
        	if(!cell.GetFeasiblePopulation().isEmpty())
        	{
        		room = cell.GetFeasiblePopulation().get(0).getPhenotype().getMap(-1, -1, null, null, null);
        		break;
        	}
		}
        
        
        PatternFinder finder = room.getPatternFinder();
		MapContainer result = new MapContainer();
		result.setMap(room);
		result.setMicroPatterns(finder.findMicroPatterns());
		result.setMesoPatterns(finder.findMesoPatterns());
		result.setMacroPatterns(finder.findMacroPatterns());
        AlgorithmDone ev = new AlgorithmDone(result, this, config.fileName);
        ev.setID(id);
        EventRouter.getInstance().postEvent(ev);
        destructor();
	}
	
	//TODO: this still needs to be checked!
	public void destructor()
	{
		EventRouter.getInstance().unregisterListener(this, new MAPEGridUpdate(null));
		EventRouter.getInstance().unregisterListener(this, new UpdatePreferenceModel(null));
		EventRouter.getInstance().unregisterListener(this, new SaveCurrentGeneration());
		EventRouter.getInstance().unregisterListener(this, new RoomEdited(null));
		
		HashMap<Room, Double[]> uniqueGeneratedRooms = new HashMap<Room, Double[]>();
		HashMap<Room, Double[]> uniqueGeneratedRoomsFlush= new HashMap<Room, Double[]>();
		HashMap<Room, Double[]> uniqueGeneratedRoomsSince = new HashMap<Room, Double[]>();
		
		StringBuilder uniqueRoomsData = new StringBuilder();
		StringBuilder uniqueRoomsSinceData = new StringBuilder();
		
		uniqueGeneratedRooms.clear();
		uniqueGeneratedRoomsFlush.clear();
		uniqueGeneratedRoomsSince.clear();
		uniqueRoomsData = null;
		uniqueRoomsSinceData = null;
		cells.clear();
		MAPElitesDimensions.clear();
	}
	
	//TODO: There are problems on how the cells are rendered!
	public void broadcastResultedRooms()
	{
		//TODO: CHECK FOR THE OTHER THAT ARE ALREADY RENDERED
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
        		if(currentRendered.get(cellIndex) != null)
        		{
        			double increasedAmount = currentRendered.get(cellIndex).getFitness() * 0.01; //1% different
        			
        			if(cell.GetFeasiblePopulation().get(0).getFitness() >= currentRendered.get(cellIndex).getFitness() + increasedAmount)
        			{
        				currentRendered.set(cellIndex, cell.GetFeasiblePopulation().get(0));
        			}
        		}
        		else
        		{
        			currentRendered.set(cellIndex, cell.GetFeasiblePopulation().get(0));
        		}
        		
//        		ev.addRoom(currentRendered.get(cellIndex).getPhenotype().getMap(-1, -1, null, null, null));
        		ev.addRoom(cell.GetFeasiblePopulation().get(0).getPhenotype().getMap(-1, -1, null, null, null)); 
        		//Uncomment top to get previous result!
        		
//        		cell.GetFeasiblePopulation().get(0).BroadcastIndividualDimensions();
        		evaluateFeasibleZoneIndividual(cell.GetFeasiblePopulation().get(0));
//        		System.out.println("FIT ROOM Fitness: " + cell.GetFeasiblePopulation().get(0).getFitness() + 
//        							", symmetry: " + cell.GetFeasiblePopulation().get(0).getDimensionValue(DimensionTypes.SIMILARITY) +
//        							", pat: " + cell.GetFeasiblePopulation().get(0).getDimensionValue(DimensionTypes.SYMMETRY));
        	}	
        	
//        	System.out.println("------------------");
        	
        	cellIndex++;
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
	            	               
	                individual.SetDimensionValues(MAPElitesDimensions, this.originalRoom);
	                evaluateFeasibleZoneIndividual(individual);
	                
					for(GACell cell : cells)
					{
						if(cell.BelongToCell(individual, true))
							break;
					}
	            }
	            else
	            {
					individual.SetDimensionValues(MAPElitesDimensions, this.originalRoom);
					evaluateInfeasibleZoneIndividual(individual);
					
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
	
	
	protected void saveUniqueRoomsToFile()
	{
		String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\expressive-range\\";
		StringBuilder data = new StringBuilder();
		
		data.append("Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;GEN;Score" + System.lineSeparator());
		
		//Create the data:
		for (Entry<Room, Double[]> entry : uniqueGeneratedRooms.entrySet()) 
		{
		    Room currentRoom = entry.getKey();
		    data.append(currentRoom.getDimensionValue(DimensionTypes.LENIENCY) + ";");
		    data.append(currentRoom.getDimensionValue(DimensionTypes.LINEARITY) + ";");
		    data.append(currentRoom.getDimensionValue(DimensionTypes.SIMILARITY) + ";");
		    data.append(currentRoom.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN) + ";");
		    data.append(currentRoom.getDimensionValue(DimensionTypes.NUMBER_PATTERNS) + ";");
		    data.append(currentRoom.getDimensionValue(DimensionTypes.SYMMETRY) + ";");
		    data.append(currentRoom.getDimensionValue(DimensionTypes.INNER_SIMILARITY) + ";");
		    data.append(entry.getValue()[0] + ";");
		    data.append(entry.getValue()[1] + ";");
		    data.append("1.0" + System.lineSeparator());
		}
		

		File file = new File(DIRECTORY + "expressive_range-" + dimensions[0].getDimension() + "_" + dimensions[1].getDimension() + ".csv");
		try {
			FileUtils.write(file, data, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void saveUniqueRoomsToFileAndFlush()
	{
//		String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\expressive-range\\";
		String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\custom-save\\";
		//Create the data:
		for (Entry<Room, Double[]> entry : uniqueGeneratedRoomsFlush.entrySet()) 
		{
		    Room currentRoom = entry.getKey();
		    uniqueRoomsData.append(currentRoom.getDimensionValue(DimensionTypes.LENIENCY) + ";");
		    uniqueRoomsData.append(currentRoom.getDimensionValue(DimensionTypes.LINEARITY) + ";");
		    uniqueRoomsData.append(currentRoom.getDimensionValue(DimensionTypes.SIMILARITY) + ";");
		    uniqueRoomsData.append(currentRoom.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN) + ";");
		    uniqueRoomsData.append(currentRoom.getDimensionValue(DimensionTypes.NUMBER_PATTERNS) + ";");
		    uniqueRoomsData.append(currentRoom.getDimensionValue(DimensionTypes.SYMMETRY) + ";");
		    uniqueRoomsData.append(currentRoom.getDimensionValue(DimensionTypes.INNER_SIMILARITY) + ";");
		    uniqueRoomsData.append(entry.getValue()[0] + ";");
		    uniqueRoomsData.append("1.0;");
		    uniqueRoomsData.append(dimensions[0].getDimension() + ";");
		    uniqueRoomsData.append(dimensions[1].getDimension() + ";");
		    uniqueRoomsData.append(currentSaveStep + ";");
		    uniqueRoomsData.append(entry.getValue()[1] + ";");
		    uniqueRoomsData.append("GR" + System.lineSeparator()); //TYPE	    
		}
		

//		File file = new File(DIRECTORY + "expressive_range-" + dimensions[0].getDimension() + "_" + dimensions[1].getDimension() + ".csv");
		File file = new File(DIRECTORY + "custom-unique-overtime_" + id + ".csv");
		try {
			FileUtils.write(file, uniqueRoomsData, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		uniqueGeneratedRoomsFlush.clear();
		uniqueRoomsData = new StringBuilder();
//		IO.saveFile(FileName, data.getSaveString(), true);
	}

	
	protected void storeUniqueRooms() //Only feasible
	{
		for(GACell cell : cells)
		{
			for(ZoneIndividual ind : cell.GetFeasiblePopulation())
			{
				boolean unique = true;
				Room individualRoom = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
				for (Room key : uniqueGeneratedRooms.keySet()) 
				{
				    if(SimilarityGADimension.sameRooms(key, individualRoom))
				    {
				    	unique = false;
				    	break;
				    }
				}
				
				if(unique)
				{
					Room copy = new Room(individualRoom);
					copy.calculateAllDimensionalValues();
					copy.setSpeficidDimensionValue(DimensionTypes.SIMILARITY, 
							SimilarityGADimension.calculateValueIndependently(copy, originalRoom));
					copy.setSpeficidDimensionValue(DimensionTypes.INNER_SIMILARITY, 
							CharacteristicSimilarityGADimension.calculateValueIndependently(copy, originalRoom));
					uniqueGeneratedRooms.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
					uniqueGeneratedRoomsFlush.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
//					uniqueGeneratedRoomsFlush.put(copy, ind.getFitness());
					
				}
				
			}
		}
	}
	
	protected void storeRoom(ZoneIndividual ind ) //Only feasible
	{
		boolean unique = true;
		Room individualRoom = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
		for (Room key : uniqueGeneratedRooms.keySet()) 
		{
		    if(SimilarityGADimension.sameRooms(key, individualRoom))
		    {
		    	unique = false;
		    	break;
		    }
		}
		
		if(unique)
		{
			Room copy = new Room(individualRoom);
			copy.calculateAllDimensionalValues();
			copy.setSpeficidDimensionValue(DimensionTypes.SIMILARITY, 
					SimilarityGADimension.calculateValueIndependently(copy, originalRoom));
			copy.setSpeficidDimensionValue(DimensionTypes.INNER_SIMILARITY, 
					CharacteristicSimilarityGADimension.calculateValueIndependently(copy, originalRoom));
//			uniqueGeneratedRooms.put(copy, ind.getFitness());
//			uniqueGeneratedRoomsFlush.put(copy, ind.getFitness());
			uniqueGeneratedRooms.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
			uniqueGeneratedRoomsFlush.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
		}

	}
	
	//ok
	public void storeMAPELITESXml()
	{
		Document dom;
	    Element e = null;
	    Element next = null;
	    
	    File file = new File(DataSaverLoader.projectPath + "\\summer-school\\" + InteractiveGUIController.runID + "\\algorithm\\" + id);
		if (!file.exists()) {
			file.mkdirs();
		}
	    
	    String xml = System.getProperty("user.dir") + "\\my-data\\summer-school\\"+ InteractiveGUIController.runID + "\\algorithm\\" + id + "\\algorithm-" + id + "_" + saveCounter + ".xml";

	    // instance of a DocumentBuilderFactory
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    try {
	        // use factory to get an instance of document builder
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        // create instance of DOM
	        dom = db.newDocument();

	        // create the root element
	        Element rootEle = dom.createElement("Run");
	        rootEle.setAttribute("ID", id.toString());
//	        rootEle.setAttribute("TIME", TIMESTAMP);
	        
	        // create data elements and place them under root
	        e = dom.createElement("Dimensions");
	        rootEle.appendChild(e);
	        
	        //DIMENSIONS
	        for(GADimension dimension : MAPElitesDimensions)
	        {
	        	 next = dom.createElement("Dimension");
	 	        next.setAttribute("name", dimension.GetType().toString());
	 	        next.setAttribute("granularity", Double.toString(dimension.GetGranularity()));
	 	        e.appendChild(next);
	        }
	       
	        //ROOMS
	        e = dom.createElement("Cells");
	        rootEle.appendChild(e);
	        
	        for(GACell cell : cells)
			{
		        next = dom.createElement("Cell");
		        if(cell.GetFeasiblePopulation().isEmpty())
		        {
		        	next.setAttribute("ROOM_ID", "NULL");
		        }
		        else
		        {
		        	cell.GetFeasiblePopulation().get(0).getPhenotype().getMap(-1, -1, null, null, null).saveRoomXMLMapElites("algorithm\\" + id + "\\algorithm-" + saveCounter + "_");
		        	next.setAttribute("ROOM_ID", cell.GetFeasiblePopulation().get(0).getPhenotype().getMap(-1, -1, null, null, null).toString());
			        next.setAttribute("fitness", Double.toString(cell.GetFeasiblePopulation().get(0).getFitness()));
			       
		        }
		        e.appendChild(next);
			}

	        dom.appendChild(rootEle);

	        try {
	            Transformer tr = TransformerFactory.newInstance().newTransformer();
	            tr.setOutputProperty(OutputKeys.INDENT, "yes");
	            tr.setOutputProperty(OutputKeys.METHOD, "xml");
	            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "algorithm.dtd");
	            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	            // send DOM to file
	            tr.transform(new DOMSource(dom), 
	                                 new StreamResult(new FileOutputStream(xml)));

	        } catch (TransformerException te) {
	            System.out.println(te.getMessage());
	        } catch (IOException ioe) {
	            System.out.println(ioe.getMessage());
	        }
	    } catch (ParserConfigurationException pce) {
	        System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
	    }
	    
	    saveCounter++;
	}

	
}
