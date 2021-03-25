package generator.algorithm.MAPElites;

import collectors.DataSaverLoader;
import collectors.MAPECollector;
import finder.PatternFinder;
import finder.geometry.Polygon;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import finder.patterns.meso.*;
import finder.patterns.micro.*;
import game.AlgorithmSetup;
import game.MapContainer;
import game.Room;
import game.narrative.GrammarGraph;
import game.narrative.NarrativeFinder.CompoundConflictPattern;
import game.narrative.NarrativeFinder.NarrativePattern;
import game.narrative.TVTropeType;
import generator.algorithm.Algorithm;
import generator.algorithm.MAPElites.Dimensions.*;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.MAPElites.grammarDimensions.GADimensionGrammar;
import generator.algorithm.MAPElites.grammarDimensions.MAPEDimensionGrammarFXML;
import generator.algorithm.ZoneGenotype;
import generator.algorithm.GrammarIndividual;
import generator.algorithm.ZoneIndividual;
import generator.config.GeneratorConfig;
import gui.InteractiveGUIController;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import util.Util;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class GrammarMAPEliteAlgorithm extends Algorithm implements Listener {

	//Actually I do not create populations I create the cells and then the population is assigned to each cell!

	protected ArrayList<GrammarGACell> cells;

	//This is actually calculated from the multiplication of the amount of granularity the different dimensions have.
	int cellAmounts = 1;
	private ArrayList<GADimensionGrammar> MAPElitesDimensions;
	private Random rnd = new Random();
	private int iterationsToPublish = 100;
	private int breedingGenerations = 5; //this relates to how many generations will it breed
	private int realCurrentGen = 0;
	private int currentGen = 0;
	MAPEDimensionGrammarFXML[] dimensions;
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
	ArrayList<GrammarIndividual> currentRendered = new ArrayList<GrammarIndividual>(); //I think this didn't work


	GrammarGraph axiom;
	GrammarGraph target;
	private int recipe_iterations = 10;

	public GrammarMAPEliteAlgorithm(GeneratorConfig config) {
		super(config);
	}

	public GrammarMAPEliteAlgorithm(Room room, GeneratorConfig config){ //This is called from the batch run and when asked for suggestions view

		super(room, config);
	}

	public GrammarMAPEliteAlgorithm(Room room, GeneratorConfig config, AlgorithmTypes algorithmTypes) //THIS IS THE ONE CALLED WHEN IS NOT PRESERVING
	{
		super(room, config, algorithmTypes);

	}

	/**
	 * Create an Algorithm run using mutations of a given map -- actually no
	 * @param room
	 */
	public GrammarMAPEliteAlgorithm(Room room, AlgorithmTypes algorithmTypes) //THIS IS CALLED WHEN WE WANT TO PRESERVE THE ROOM
	{
		super(room, algorithmTypes);
	}

	public GrammarMAPEliteAlgorithm(GrammarGraph axiom)
	{
		this.axiom = axiom;
		id = UUID.randomUUID();
//		populationSize = config.getPopulationSize();
		populationSize = 1250; //Setting same as experiments
		mutationProbability = 0.5f;
		offspringSize = 50;
//		feasibleAmount = (int)((double)populationSize * config.getFeasibleProportion());
		feasibleAmount = 625; //Setting same as experiments

		this.save_data = AlgorithmSetup.getInstance().getSaveData();
		this.iter_generations = AlgorithmSetup.getInstance().getITER_GENERATIONS();
	}
	
	public void CreateCells(int dimension, int dimensionQuantity, float [] dimensionSizes, int[] indices)
	{
		if(dimension >= dimensionQuantity)
		{
			this.cells.add(new GrammarGACell(MAPElitesDimensions, indices));
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
			this.cells.add(new GrammarGACell(MAPElitesDimensions, indices));
			return;
		}
		
		for(int i = 1; i < dimensionSizes[dimension] +1 ; i++)
		{
			indices[dimension] = i;
			CreateCellsOpposite(dimension-1, dimensionSizes, indices);
		}
	}
	
	
	public void initPopulations(Room room, MAPEDimensionGrammarFXML[] dimensions){
		broadcastStatusUpdate("Initialising...");
		EventRouter.getInstance().registerListener(this, new MAPEGridUpdate(null));
		EventRouter.getInstance().registerListener(this, new UpdatePreferenceModel(null));
		EventRouter.getInstance().registerListener(this, new SaveCurrentGeneration());
		EventRouter.getInstance().registerListener(this, new RoomEdited(null));
		EventRouter.getInstance().registerListener(this, new NarrativeStructEdited(null));


		this.dimensions = dimensions;
		initCells(dimensions);

		//FIXme:
//		room.SetDimensionValues(MAPElitesDimensions);
		
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
			GrammarIndividual ind = new GrammarIndividual(mutationProbability);
//			ind.mutateAll(0.7, roomWidth, roomHeight);
			
			if(evaluateGrammarIndividual(ind)){
				if(i < feasibleAmount){
					evaluateFeasibleGrammarIndividual(ind);
					ind.SetDimensionValues(MAPElitesDimensions, axiom);
					
					for(GrammarGACell cell : cells)
					{
						if(cell.BelongToCell(ind, true))
							break;
					}
					
					i++;
				}
			}
			else {
				if(j < populationSize - feasibleAmount){
					evaluateInfeasibleGrammarIndividual(ind);
					ind.SetDimensionValues(MAPElitesDimensions, axiom);
					
					for(GrammarGACell cell : cells)
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
	 * Creates lists for the valid and invalid populations and populates them with GrammarIndividuals.
	 */
	public void initPopulations(MAPEDimensionGrammarFXML[] dimensions){

		broadcastStatusUpdate("Initialising...");
		EventRouter.getInstance().registerListener(this, new MAPEGridUpdate(null));
		EventRouter.getInstance().registerListener(this, new UpdatePreferenceModel(null));
		EventRouter.getInstance().registerListener(this, new SaveCurrentGeneration());
		EventRouter.getInstance().registerListener(this, new RoomEdited(null));
		EventRouter.getInstance().registerListener(this, new NarrativeStructEdited(null));

		this.dimensions = dimensions;
		initCells(dimensions);

		//FIXme:
//		room.SetDimensionValues(MAPElitesDimensions);

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
			GrammarIndividual ind = new GrammarIndividual(mutationProbability);
//			ind.mutateAll(0.7, roomWidth, roomHeight);

			if(evaluateGrammarIndividual(ind)){
				if(i < feasibleAmount){
					evaluateFeasibleGrammarIndividual(ind);
					ind.SetDimensionValues(MAPElitesDimensions, this.axiom);

					for(GrammarGACell cell : cells)
					{
						if(cell.BelongToCell(ind, true))
							break;
					}

					i++;
				}
			}
			else {
				if(j < populationSize - feasibleAmount){
					evaluateInfeasibleGrammarIndividual(ind);
					ind.SetDimensionValues(MAPElitesDimensions, this.axiom);

					for(GrammarGACell cell : cells)
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
	
	
	private void initCells(MAPEDimensionGrammarFXML[] dimensions)
	{
		//Initialize cells
		MAPElitesDimensions = new ArrayList<GADimensionGrammar>();
		
		//Helper variables to create the cells
		float[] dimensionsGranularity = new float[dimensions.length];
		int counter = 0;
		
		for(MAPEDimensionGrammarFXML dimension : dimensions)
		{
			MAPElitesDimensions.add(GADimensionGrammar.CreateDimension(dimension.getDimension(), dimension.getGranularity()));
			dimensionsGranularity[counter++] = dimension.getGranularity();
		}

		//Initialize all the cells!
		this.cells = new ArrayList<GrammarGACell>();
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
		List<GrammarIndividual> children = new ArrayList<GrammarIndividual>();
		List<GrammarIndividual> nonFeasibleChildren = new ArrayList<GrammarIndividual>();
		for(GrammarGACell cell : cells)
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

		//Sort the populations in the cell and Eliminate low performing cells individuals
		for(GrammarGACell cell : cells)
		{
			cell.SortPopulations(false);
			cell.ApplyElitism();
		}
	}
	
	
	public void ping(PCGEvent e) //TODO: I SHOULD ALSO ADD THE INFO WHEN A MAP IS UPDATED --> For all the extra calculations
	{
		// TODO Auto-generated method stub
		if(e instanceof MAPEGridUpdate)
		{
//			this.dimensions = ((MAPEGridUpdate) e).getDimensions();
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
		else if(e instanceof NarrativeStructEdited)
		{
			axiom = (GrammarGraph) e.getPayload();
			dimensionsChanged = true;
		}
	}

	/**
	 * Checks if an ZoneIndividual is valid (feasible), that is:
	 * 1. There exist paths between the entrance and all other doors
	 * 2. There exist paths between the entrance and all enemies
	 * 3. There exist paths between the entrance and all treasures
	 * 4. There is at least one enemy
	 * 5. There is at least one treasure
	 *
	 * @param ind The ZoneIndividual to check
	 * @return Return true if ZoneIndividual is valid, otherwise return false
	 */
	protected boolean checkGrammarIndividual(GrammarIndividual ind){

		//FIXME: IMPLEMENT!
		return true;

//		GrammarGraph nStructure = ind.getPhenotype().getGrammarGraphOutput(axiom, 1);
//		int unconnectedNodes = nStructure.checkUnconnectedNodes();
//
//		short dist = axiom.distanceBetweenGraphs(nStructure);
//
//		//With this, the graph will be fully connected and always a step more than axiom!
////		return nStructure.fullyConnectedGraph() && dist == 1;
//
//		return nStructure.fullyConnectedGraph();

//		return unconnectedNodes <= 0;
	}

	/**
	 *
	 * @param ind The ZoneIndividual to check
	 * @return Return true if ZoneIndividual is valid, otherwise return false
	 */
	protected boolean checkGrammarIndividual(GrammarGraph nStructure){

		//FIXME: IMPLEMENT!

//		GrammarGraph nStructure = ind.getPhenotype().getGrammarGraphOutput(axiom, 1);
		int unconnectedNodes = nStructure.checkUnconnectedNodes();
		short dist = axiom.distanceBetweenGraphs(nStructure);

		//With this, the graph will be fully connected and always a step more than axiom!
//		return nStructure.fullyConnectedGraph() && dist == 1;

		return nStructure.fullyConnectedGraph();

//		return unconnectedNodes <= 0;
	}

	/**
	 * First, lets check if the random recipe is infeasible
	 * if it is, we discard and move to the next iteration
	 * if all the random_recipes generate infeasible individuals, we set the individual as infeasible.
	 * if at least one recipe is feasible, we keep the individual as feasible. (this might change to a min_value).
	 * We store all the recipes that are feasible and test for fitness.
	 * At the same time we store all the infeasible recipes as well, to evaluate then. well actually... no it is fine! keep them.
	 * @param ind
	 */
	public boolean evaluateGrammarIndividual(GrammarIndividual ind)
	{
		boolean feasible = false;

		for(int i = 0; i < recipe_iterations; i++)
		{
			GrammarGraph nStructure = ind.getPhenotype().getGrammarGraphOutputRndRecipe(axiom, 1);
			if(checkGrammarIndividual(nStructure))
			{
				ind.getPhenotype().addFeasibleRecipe();
			}
			else
			{
				ind.getPhenotype().addInfeasibleRecipe();
			}
		}

		int actual_recipes = ind.getPhenotype().feasible_grammar_recipes.size() + ind.getPhenotype().infeasible_grammar_recipes.size();

		// at least half of the recipes have to be feasible! (at least the ones we actually created!)
		if(ind.getPhenotype().feasible_grammar_recipes.size() >= actual_recipes/2)
			feasible = true; //This could change if we want to make something more based on how many!

		ind.setFeasible(feasible);
		return feasible;
	}

	/**
	 * Evaluates the fitness of a valid ZoneIndividual using the following factors:
	 *  1. Entrance safety (how close are enemies to the entrance)
	 *  2. Proportion of tiles that are enemies
	 *  3. Average treasure safety (Are treasures closer to the door or enemies?)
	 *  4. Proportion of tiles that are treasure
	 *  5. Treasure safety variance (whatever this is!)
	 *
	 * @param ind The valid ZoneIndividual to evaluate
	 */
	public void evaluateFeasibleGrammarIndividual(GrammarIndividual ind)
	{
		List<LinkedHashMap<Integer, Integer>> feasible_grammar_recipes = ind.getPhenotype().feasible_grammar_recipes;
		LinkedHashMap<Integer, Integer> best_recipe = null;
		double best_fitness = Double.NEGATIVE_INFINITY;
		double final_fitness = 0.0;

		for(LinkedHashMap<Integer, Integer> feasible_recipe : feasible_grammar_recipes)
		{
			GrammarGraph nStructure = ind.getPhenotype().getGrammarGraphOutput(axiom, feasible_recipe);
			double fitness = 0.0;
			double w_any = 0.2; //Weight for the amount of "ANY" in the grammar (ANY is a wildcard)
			double w_node_repetition = 0.3; //Weight for the node repetition count
			int min_freq_nodes = 1; //Min freq for the node repetition
			TVTropeType[] excluded_repeated_nodes = {TVTropeType.CONFLICT}; //Nodes to exclude from the count.
			//TODO: Size is going to be done by the elites+
			double w_tSize = 0.5; //Weight for the size of the resulting grammar
			float expected_size = 4.0f; //Expected size (anything more or less than this decreases fitness)


			//AND THEN WHAT?
			//TESTING
//		if(nStructure.nodes.get(0).getGrammarNodeType() == TVTropeType.ANY)
//		{
//			ind.setFitness(0.0);
////		ind.setFitness(1.0);
//			ind.setEvaluate(true);
//			return;
//		}

			short dist = axiom.distanceBetweenGraphs(nStructure);

			//A bit hardcore, perhaps we should scale based on how different
			//then we could use as target one step more.
			if(axiom.testGraphMatchPattern(nStructure))
				fitness = 0.0;
			else
			{
				//Get first how many ANY exist
				float cumulative_any = 1.0f - nStructure.checkAmountNodes(TVTropeType.ANY, true);

				//get the right size!! -- probably for elites
				float targetSize = expected_size - nStructure.checkGraphSize();
				targetSize *= 0.1f;
				targetSize = 1.0f - Math.abs(targetSize);


//			fitness += targetSize;

				//Penalize repeting nodes
				float node_repetition = 1.0f - nStructure.SameNodes(min_freq_nodes, excluded_repeated_nodes);

				fitness = (w_any * (cumulative_any)) + (w_tSize * targetSize) + (w_node_repetition * node_repetition);

			}

			nStructure.pattern_finder.findNarrativePatterns();
			float structure_count = 0.0f;
			for(NarrativePattern np : nStructure.pattern_finder.all_narrative_patterns)
			{
				if(np instanceof CompoundConflictPattern)
					structure_count++;
			}

			float targetSize = expected_size - structure_count;
			targetSize *= 0.1f;
			fitness = 1.0f - Math.abs(targetSize);

			if(fitness > best_fitness)
			{
				best_fitness = fitness;
				best_recipe = feasible_recipe;
			}

			final_fitness += fitness;

		}
		// We set not only the best fitness to the individual, but also the avg. of all the feasible recipes!
		final_fitness = final_fitness/(double)feasible_grammar_recipes.size();
		ind.setAvgFitness(final_fitness);
		ind.setFitness(best_fitness);
		ind.getPhenotype().setBestRecipe(best_recipe);
//		ind.setFitness(1.0);
		ind.setEvaluate(true);
	}

	/**
	 * Evaluates the fitness of a valid ZoneIndividual using the following factors:
	 *  1. Entrance safety (how close are enemies to the entrance)
	 *  2. Proportion of tiles that are enemies
	 *  3. Average treasure safety (Are treasures closer to the door or enemies?)
	 *  4. Proportion of tiles that are treasure
	 *  5. Treasure safety variance (whatever this is!)
	 *
	 * @param ind The valid ZoneIndividual to evaluate
	 */
	public void evaluateInfeasibleGrammarIndividual(GrammarIndividual ind)
	{
		List<LinkedHashMap<Integer, Integer>> infeasible_grammar_recipes = ind.getPhenotype().infeasible_grammar_recipes;
		LinkedHashMap<Integer, Integer> best_recipe = null;
		double best_fitness = Double.NEGATIVE_INFINITY;

		for(LinkedHashMap<Integer, Integer> infeasible_recipe : infeasible_grammar_recipes)
		{
			GrammarGraph nStructure = ind.getPhenotype().getGrammarGraphOutput(axiom, infeasible_recipe);
			double fitness = 0.0;
			double w_any = 0.2; //Weight for the amount of "ANY" in the grammar (ANY is a wildcard)
			double w_node_repetition = 0.3; //Weight for the node repetition count
			int min_freq_nodes = 1; //Min freq for the node repetition
			TVTropeType[] excluded_repeated_nodes = {TVTropeType.CONFLICT}; //Nodes to exclude from the count.
			//TODO: Size is going to be done by the elites+
			double w_tSize = 0.5; //Weight for the size of the resulting grammar
			float expected_size = 4.0f; //Expected size (anything more or less than this decreases fitness)


			//AND THEN WHAT?
			//TESTING
//		if(nStructure.nodes.get(0).getGrammarNodeType() == TVTropeType.ANY)
//		{
//			ind.setFitness(0.0);
////		ind.setFitness(1.0);
//			ind.setEvaluate(true);
//			return;
//		}

			short dist = axiom.distanceBetweenGraphs(nStructure);

			//A bit hardcore, perhaps we should scale based on how different
			//then we could use as target one step more.
			if(axiom.testGraphMatchPattern(nStructure))
				fitness = 0.0;
			else
			{
				//Get first how many ANY exist
				float cumulative_any = 1.0f - nStructure.checkAmountNodes(TVTropeType.ANY, true);

				//get the right size!! -- probably for elites
				float targetSize = expected_size - nStructure.checkGraphSize();
				targetSize *= 0.1f;
				targetSize = 1.0f - Math.abs(targetSize);


//			fitness += targetSize;

				//Penalize repeting nodes
				float node_repetition = 1.0f - nStructure.SameNodes(min_freq_nodes, excluded_repeated_nodes);

				fitness = (w_any * (cumulative_any)) + (w_tSize * targetSize) + (w_node_repetition * node_repetition);

			}

			nStructure.pattern_finder.findNarrativePatterns();
			float structure_count = 0.0f;
			for(NarrativePattern np : nStructure.pattern_finder.all_narrative_patterns)
			{
				if(np instanceof CompoundConflictPattern)
					structure_count++;
			}

			float targetSize = expected_size - structure_count;
			targetSize *= 0.1f;
			fitness = 1.0f - Math.abs(targetSize);

			if(fitness > best_fitness)
			{
				best_fitness = fitness;
				best_recipe = infeasible_recipe;
			}

		}

		ind.setFitness(best_fitness);
		ind.getPhenotype().setBestRecipe(best_recipe);
//		ind.setFitness(1.0);
		ind.setEvaluate(true);
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
//		originalRoom.SetDimensionValues(MAPElitesDimensions);
		
		//Extra
		for(GrammarGACell cell : cells)
		{
			cell.ResetPopulation(this.config);
		}
		
		for(GrammarIndividual rend : currentRendered)
		{
			if(rend != null)
			{
				rend.ResetPhenotype(this.config);
				evaluateFeasibleGrammarIndividual(rend);
			}
		}
	}
	
	/**
     * Selects parents from a population using (deterministic) tournament selection - i.e. the winner is always the GrammarIndividual with the "best" fitness.
     * See: https://en.wikipedia.org/wiki/Tournament_selection
     * 
     * @param population A whole population of GrammarIndividuals
     * @return A list of chosen progenitors
     */
    protected List<GrammarIndividual> tournamentSelection(List<GrammarIndividual> population, int parentNumber)
    { 
        List<GrammarIndividual> parents = new ArrayList<GrammarIndividual>();
        List<GrammarIndividual> candidates = new ArrayList<GrammarIndividual>(population);
//        int numberOfParents = (int)(offspringSize * population.size()) / 2;

        if(candidates.size() == 1)
        	return candidates;
        
        while(parents.size() <= parentNumber && candidates.size() > 1)
        {
        	//Select at least one GrammarIndividual to "fight" in the tournament
            int tournamentSize = Util.getNextInt(1, candidates.size());

            GrammarIndividual winner = null;
            for(int i = 0; i < tournamentSize; i++)
            {
                int progenitorIndex = Util.getNextInt(0, candidates.size());
                GrammarIndividual GrammarIndividual = candidates.remove(progenitorIndex);

                //select the GrammarIndividual with the highest fitness
                if(winner == null || (winner.getFitness() < GrammarIndividual.getFitness()))
                {
                	winner = GrammarIndividual;
                }
            }

            parents.add(winner);
        }

        return parents;
    }

	/**
	 * Crossover
	 *
	 * @param progenitors A List of ZoneIndividuals to be reproduced
	 * @return A List of ZoneIndividuals
	 */
	protected List<GrammarIndividual> crossOverBetweenGrammarProgenitors(List<GrammarIndividual> progenitors)
	{
		List<GrammarIndividual> sons = new ArrayList<GrammarIndividual>();
		int sizeProgenitors = progenitors.size();
		int countSons = 0;
		int sonSize = sizeProgenitors * 2;

		while (countSons < sonSize)
		{
			GrammarIndividual[] offspring = progenitors.get(
					Util.getNextInt(0, sizeProgenitors)).crossover(progenitors.get(Util.getNextInt(0, sizeProgenitors)));

			sons.addAll(Arrays.asList(offspring));
			countSons += 2;
		}

		return sons;
	}

	/**
	 * Crossover
	 *
	 * @param progenitors A List of ZoneIndividuals to be reproduced
	 * @return A List of ZoneIndividuals
	 */
	protected List<GrammarIndividual> mutateParents(List<GrammarIndividual> progenitors)
	{
		List<GrammarIndividual> sons = new ArrayList<GrammarIndividual>();
		int sizeProgenitors = progenitors.size();
		int countSons = 0;
		int sonSize = sizeProgenitors * 2;

		while (countSons < sonSize)
		{
			int parent_index = Util.getNextInt(0, sizeProgenitors);
			sons.add(progenitors.get(parent_index).mutate(false));
			sons.add(progenitors.get(parent_index).mutate(false));

			countSons += 2;
		}

		return sons;
	}
    
    private void runInterbreedingApplElites()
    {
    	//If we have receive the even that the dimensions changed, please modify the dimensions and recalculate the cells!
    	if(dimensionsChanged)
    	{
    		RecreateCells();
    		dimensionsChanged = false;
    	}


    	ArrayList<GrammarIndividual> parents = new ArrayList<GrammarIndividual>();
		List<GrammarIndividual> children = new ArrayList<GrammarIndividual>();
		GrammarGACell current = null;
		for(int count = 0; count < 10; count++) //Actual gens
    	{
//			children = new ArrayList<GrammarIndividual>();
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
		children.addAll(crossOverBetweenGrammarProgenitors(parents));
		
		//Evaluate and assign to correct Cell
		CheckAndAssignToCell(children, false);
	
		//Now we sort both populations in a given cell and cut through capacity!!!
		for(GrammarGACell cell : cells)
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
    
    private void noSaveRunNoInterbreedingApplElites()
    {
    	
    	//If we have receive the event that the dimensions changed, please modify the dimensions and recalculate the cells!
    	if(dimensionsChanged)
    	{
    		RecreateCells();
    		dimensionsChanged = false;
    	}
    	
		ArrayList<GrammarIndividual> feasibleParents = new ArrayList<GrammarIndividual>();
		ArrayList<GrammarIndividual> infeasibleParents = new ArrayList<GrammarIndividual>();
		
    	List<GrammarIndividual> feasibleChildren = new ArrayList<GrammarIndividual>();
    	List<GrammarIndividual> infeasibleChildren = new ArrayList<GrammarIndividual>();
		GrammarGACell current = null;

		for(int count = 0; count < breedingGenerations; count++) //Actual gens
    	{
//    			children = new ArrayList<GrammarIndividual>();
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
//		feasibleChildren.addAll(crossOverBetweenGrammarProgenitors(feasibleParents));
//		infeasibleChildren.addAll(crossOverBetweenGrammarProgenitors(infeasibleParents));

		//FIRST ONLY MUTATE
		feasibleChildren.addAll(mutateParents(feasibleParents));
		infeasibleChildren.addAll(mutateParents(infeasibleParents));
		
		CheckAndAssignToCell(feasibleChildren, false);
		CheckAndAssignToCell(infeasibleChildren, true);
		
    	//Now we sort both populations in a given cell and cut through capacity!!!
    	for(GrammarGACell cell : cells)
		{
			cell.SortPopulations(false);
			cell.ApplyElitism();
		}
    	
		
    	//This is only when we want to update the current Generation
    	if(currentGen >= iterationsToPublish)
    	{
    		publishGeneration();
    		currentGen = 0;
    	}
    	else {
    		currentGen++;
    	}
    	
    	realCurrentGen++;
		System.out.println(realCurrentGen);
    }
    
    
    private void saveRunNoInterbreedingApplElites()
    {
    	//Comment or uncomment to store unique rooms every generation (based on what is generated before)
    	storeUniqueRooms();
    	
    	//If we have receive the event that the dimensions changed, please modify the dimensions and recalculate the cells!
    	if(dimensionsChanged)
    	{
    		RecreateCells();
    		dimensionsChanged = false;
    	}
    	
		ArrayList<GrammarIndividual> feasibleParents = new ArrayList<GrammarIndividual>();
		ArrayList<GrammarIndividual> infeasibleParents = new ArrayList<GrammarIndividual>();
		
    	List<GrammarIndividual> feasibleChildren = new ArrayList<GrammarIndividual>();
    	List<GrammarIndividual> infeasibleChildren = new ArrayList<GrammarIndividual>();
		GrammarGACell current = null;

		for(int count = 0; count < breedingGenerations; count++) //Actual gens
    	{
//    			children = new ArrayList<GrammarIndividual>();
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
//		feasibleChildren.addAll(crossOverBetweenGrammarProgenitors(feasibleParents));
//		infeasibleChildren.addAll(crossOverBetweenGrammarProgenitors(infeasibleParents));

		//FIRST ONLY MUTATE
		feasibleChildren.addAll(mutateParents(feasibleParents));
		infeasibleChildren.addAll(mutateParents(infeasibleParents));
		
		CheckAndAssignToCell(feasibleChildren, false);
		CheckAndAssignToCell(infeasibleChildren, true);

    	//Now we sort both populations in a given cell and cut through capacity!!!
    	for(GrammarGACell cell : cells)
		{
			cell.SortPopulations(false);
			cell.ApplyElitism();
		}
    		
    	//This is only when we want to update the current Generation
    	if(currentGen >= iterationsToPublish)
    	{
    		//TODO: For next evaluation
    		saveIterations--;
    		
    		//Uncomment to save everytime we publish
    		if(saveIterations == 0)
    		{
//    			System.out.println("NEXT");
    			saveIterations=2;
    			saveUniqueRoomsToFileAndFlush();
    			currentSaveStep++;
    			EventRouter.getInstance().postEvent(new NextStepSequenceExperiment());

    			int cellsFilled = 0;
    			for(GrammarGACell cell : cells)
    			{
    				if(!cell.GetFeasiblePopulation().isEmpty())
    					cellsFilled++;
    			}
    			System.out.println("cells filled: " + cellsFilled + "; cell count: " + cells.size() + "; CurrentGEN: " + realCurrentGen);
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
		ArrayList<GrammarIndividual> feasibleParents = new ArrayList<GrammarIndividual>();
		ArrayList<GrammarIndividual> infeasibleParents = new ArrayList<GrammarIndividual>();
		
    	List<GrammarIndividual> feasibleChildren = new ArrayList<GrammarIndividual>();
    	List<GrammarIndividual> infeasibleChildren = new ArrayList<GrammarIndividual>();
		GrammarGACell current = null;
		
		for(int count = 0; count < breedingGenerations; count++) //Actual gens
    	{
//    			children = new ArrayList<GrammarIndividual>();
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
		feasibleChildren.addAll(crossOverBetweenGrammarProgenitors(feasibleParents));
		infeasibleChildren.addAll(crossOverBetweenGrammarProgenitors(infeasibleParents));
		
		CheckAndAssignToCell(feasibleChildren, false);
		CheckAndAssignToCell(infeasibleChildren, true);
		
    	//This is only when we want to update the current Generation
    	if(currentGen >= iterationsToPublish)
    	{
        	//Now we sort both populations in a given cell and cut through capacity!!!
        	for(GrammarGACell cell : cells)
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

		ArrayList<GrammarIndividual> parents = new ArrayList<GrammarIndividual>();
		List<GrammarIndividual> children = new ArrayList<GrammarIndividual>();
		
		GrammarGACell current = null;
		
		for(int count = 0; count < breedingGenerations; count++) //Actual gen
		{
			
//    		children = new ArrayList<GrammarIndividual>();
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
		children.addAll(crossOverBetweenGrammarProgenitors(parents));
		CheckAndAssignToCell(children, false);
    	
    	
    	//This is only when we want to update the current Generation
    	if(currentGen >= iterationsToPublish)
    	{
        	//Now we sort both populations in a given cell and cut through capacity!!!
        	for(GrammarGACell cell : cells)
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
//		MAPECollector.getInstance().SaveGeneration(realCurrentGen, MAPElitesDimensions, cells, false); //store the cells in memory
		
		//This should be in a call when the ping happens! --> FIXME!!
//		UpdateConfigFile();

		List<GrammarIndividual> feasibleChildren = new ArrayList<GrammarIndividual>();
		List<GrammarIndividual> nonFeasibleChildren = new ArrayList<GrammarIndividual>();
		
		//To impulse diversity, every 100 gens, we create a mutation of all the populations
		//and we evaluate that new population and the previous population and assign the right to the right cell
		for(GrammarGACell cell : cells)
		{
			feasibleChildren.addAll(createMutatedChildren(cell.GetFeasiblePopulation(), 0.6f));
			feasibleChildren.addAll(cell.GetFeasiblePopulation());
			nonFeasibleChildren.addAll(cell.GetInfeasiblePopulation());
			cell.GetFeasiblePopulation().clear();
			cell.GetInfeasiblePopulation().clear();
		}
		
		//We add a untouched copy of the currently edited room into the population (with the hope that it will be incorporated as an elite)
		//FixME: THIS does not happen yet
//		GrammarIndividual ind = new GrammarIndividual(mutationProbability);
//
//		if(evaluateGrammarIndividual(ind))
//		{
//			feasibleChildren.add(ind);
//		}
//		else
//		{
//			nonFeasibleChildren.add(ind);
//		}

//		ind.SetDimensionValues(MAPElitesDimensions, this.axiom);

		 //Check and assign the cells!
		CheckAndAssignToCell(feasibleChildren, false);
		CheckAndAssignToCell(nonFeasibleChildren, true);
		
		//Sort the populations in the cell and Eliminate low performing cells individuals
    	for(GrammarGACell cell : cells)
		{
			cell.SortPopulations(false);
			cell.ApplyElitism();
		}

    	//"restart" current gen!
		currentGen = 0;
//		stop = true;
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
//        int generations = config.getGenerations();
        int generations = 5;
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
//        	runInterbreedingExperiment(); 
        	
        	if(save_data)
        	{
        		saveRunNoInterbreedingApplElites();//This is actually the good one!! 2019-04-23
        	}
        	else 
        	{
        		noSaveRunNoInterbreedingApplElites();
        	}
        	
//        	runNoInterbreedingApplElites(); //This is actually the good one!! 2019-04-23
//        	runInterbreedingApplElites();
        }
        
       ////////////////////////// POST END MAP-ELITES /////////////////////////////
        
        for(GrammarGACell cell : cells)
		{
			cell.SortPopulations(false);
			cell.ApplyElitism();
		}
		
//        broadcastResultedRooms();
        
        //We save the last generation
//        MAPECollector.getInstance().SaveGeneration(realCurrentGen, MAPElitesDimensions, cells, "STOP", false);
//        EventRouter.getInstance().postEvent(new SaveDisplayedCells());

		//FixME: to publish

//        for(GrammarGACell cell : cells)
//		{
//        	if(!cell.GetFeasiblePopulation().isEmpty())
//        	{
//        		room = cell.GetFeasiblePopulation().get(0).getPhenotype().getMap(-1, -1, null, null, null);
//        		break;
//        	}
//		}
//
        
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
		NarrativeStructMAPElitesDone ev = new NarrativeStructMAPElitesDone();
        ev.setID(id);
        int cellIndex = 0;
        for(GrammarGACell cell : cells)
		{

//        	System.out.println("CELL = " + cellIndex++);
//        	System.out.println("SYMMETRY: " + cell.GetDimensionValue(DimensionTypes.SIMILARITY) + ", PAT: " + cell.GetDimensionValue(DimensionTypes.SYMMETRY));
//        	cell.BroadcastCellInfo();
        	if(cell.GetFeasiblePopulation().isEmpty())
        	{
        		ev.addCell(null);
//        		System.out.println("NO FIT ROOM!");
        	}
        	else //This is more tricky!!
        	{
				ev.addCell(cell);
        		//TODO: This does not work as it should
//        		if(currentRendered.get(cellIndex) != null)
//        		{
//        			double increasedAmount = currentRendered.get(cellIndex).getFitness() * 0.01; //1% different
//
//        			if(cell.GetFeasiblePopulation().get(0).getFitness() >= currentRendered.get(cellIndex).getFitness() + increasedAmount)
//        			{
//        				currentRendered.set(cellIndex, cell.GetFeasiblePopulation().get(0));
//        			}
//        		}
//        		else
//        		{
//        			currentRendered.set(cellIndex, cell.GetFeasiblePopulation().get(0));
//        		}
        		
//        		ev.addRoom(currentRendered.get(cellIndex).getPhenotype().getMap(-1, -1, null, null, null));
//        		ev.addRoom(cell.GetFeasiblePopulation().get(0).getPhenotype().getMap(-1, -1, null, null, null));



        		//Uncomment top to get previous result!
        		
//        		cell.GetFeasiblePopulation().get(0).BroadcastIndividualDimensions();
        		evaluateFeasibleGrammarIndividual(cell.GetFeasiblePopulation().get(0));
//        		System.out.println("FIT ROOM Fitness: " + cell.GetFeasiblePopulation().get(0).getFitness() + 
//        							", symmetry: " + cell.GetFeasiblePopulation().get(0).getDimensionValue(DimensionTypes.SIMILARITY) +
//        							", pat: " + cell.GetFeasiblePopulation().get(0).getDimensionValue(DimensionTypes.SYMMETRY));
        	}	
        	
//        	System.out.println("------------------");
        	
        	cellIndex++;
		}
        
		EventRouter.getInstance().postEvent(ev);
	}
	
	protected void CheckAndAssignToCell(List<GrammarIndividual> individuals, boolean infeasible)
	{
		 for (GrammarIndividual individual : individuals)
	        {
	        	if(infeasible)
	        		individual.setChildOfInfeasibles(true);

	        	if(individual.isEvaluated())
				{
					individual.SetDimensionValues(MAPElitesDimensions, this.axiom);

					for(GrammarGACell cell : cells)
					{
						if(cell.BelongToCell(individual, individual.isFeasible()))
							break;
					}
				}
	        	else {
					evaluateGrammarIndividual(individual);

					if(individual.isFeasible())
					{
						if(infeasible)
							infeasiblesMoved++;

						evaluateFeasibleGrammarIndividual(individual);
					}
					else
					{
						evaluateInfeasibleGrammarIndividual(individual);
					}

					individual.SetDimensionValues(MAPElitesDimensions, this.axiom);

					for(GrammarGACell cell : cells)
					{
						if(cell.BelongToCell(individual, individual.isFeasible()))
							break;
					}

				}
	        }
	}
	
	//Selects which cell to pick parents (Rnd)
	protected GrammarGACell SelectCell(boolean feasible)
	{
		GrammarGACell selected = null;
		
		List<GrammarGACell> cellsWithPop = new ArrayList<GrammarGACell>();
		
		for(GrammarGACell cell : cells)
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

		//This is where you would add the code to UCB
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
	
	protected ArrayList<GrammarIndividual> createMutatedChildren(List<GrammarIndividual> population, float mProbability)
	{
		ArrayList<GrammarIndividual> mutatedChildren = new ArrayList<GrammarIndividual>();
		GrammarIndividual child = null;
		for(GrammarIndividual ind : population)
		{
			if(rnd.nextFloat() < mProbability)
			{
				child = ind.mutate(false);
//				child = new GrammarIndividual(config, new ZoneGenotype(config, ind.getGenotype().getChromosome().clone(),
//						ind.getGenotype().GetRootChromosome()), this.mutationProbability);
//
//				child.mutateAll(mProbability, this.roomWidth, this.roomHeight);
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
//		for(GrammarGACell cell : cells)
//		{
//			for(GrammarIndividual ind : cell.GetFeasiblePopulation())
//			{
//				boolean unique = true;
//				Room individualRoom = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
//				for (Room key : uniqueGeneratedRooms.keySet())
//				{
//				    if(SimilarityGADimension.sameRooms(key, individualRoom))
//				    {
//				    	unique = false;
//				    	break;
//				    }
//				}
//
//				if(unique)
//				{
//					Room copy = new Room(individualRoom);
//					copy.calculateAllDimensionalValues();
//					copy.setSpeficidDimensionValue(DimensionTypes.SIMILARITY,
//							SimilarityGADimension.calculateValueIndependently(copy, originalRoom));
//					copy.setSpeficidDimensionValue(DimensionTypes.INNER_SIMILARITY,
//							CharacteristicSimilarityGADimension.calculateValueIndependently(copy, originalRoom));
//					uniqueGeneratedRooms.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
//					uniqueGeneratedRoomsFlush.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
////					uniqueGeneratedRoomsFlush.put(copy, ind.getFitness());
//
//				}
//
//			}
//		}
	}
	
	protected void storeRoom(GrammarIndividual ind ) //Only feasible
	{
//		boolean unique = true;
//		Room individualRoom = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
//		for (Room key : uniqueGeneratedRooms.keySet())
//		{
//		    if(SimilarityGADimension.sameRooms(key, individualRoom))
//		    {
//		    	unique = false;
//		    	break;
//		    }
//		}
//
//		if(unique)
//		{
//			Room copy = new Room(individualRoom);
//			copy.calculateAllDimensionalValues();
//			copy.setSpeficidDimensionValue(DimensionTypes.SIMILARITY,
//					SimilarityGADimension.calculateValueIndependently(copy, originalRoom));
//			copy.setSpeficidDimensionValue(DimensionTypes.INNER_SIMILARITY,
//					CharacteristicSimilarityGADimension.calculateValueIndependently(copy, originalRoom));
////			uniqueGeneratedRooms.put(copy, ind.getFitness());
////			uniqueGeneratedRoomsFlush.put(copy, ind.getFitness());
//			uniqueGeneratedRooms.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
//			uniqueGeneratedRoomsFlush.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
//		}

	}
	
	//ok
	public void storeMAPELITESXml()
	{
//		Document dom;
//	    Element e = null;
//	    Element next = null;
//
//	    File file = new File(DataSaverLoader.projectPath + "\\summer-school\\" + InteractiveGUIController.runID + "\\algorithm\\" + id);
//		if (!file.exists()) {
//			file.mkdirs();
//		}
//
//	    String xml = System.getProperty("user.dir") + "\\my-data\\summer-school\\"+ InteractiveGUIController.runID + "\\algorithm\\" + id + "\\algorithm-" + id + "_" + saveCounter + ".xml";
//
//	    // instance of a DocumentBuilderFactory
//	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//	    try {
//	        // use factory to get an instance of document builder
//	        DocumentBuilder db = dbf.newDocumentBuilder();
//	        // create instance of DOM
//	        dom = db.newDocument();
//
//	        // create the root element
//	        Element rootEle = dom.createElement("Run");
//	        rootEle.setAttribute("ID", id.toString());
////	        rootEle.setAttribute("TIME", TIMESTAMP);
//
//	        // create data elements and place them under root
//	        e = dom.createElement("Dimensions");
//	        rootEle.appendChild(e);
//
//	        //DIMENSIONS
//	        for(GADimension dimension : MAPElitesDimensions)
//	        {
//	        	 next = dom.createElement("Dimension");
//	 	        next.setAttribute("name", dimension.GetType().toString());
//	 	        next.setAttribute("granularity", Double.toString(dimension.GetGranularity()));
//	 	        e.appendChild(next);
//	        }
//
//	        //ROOMS
//	        e = dom.createElement("Cells");
//	        rootEle.appendChild(e);
//
//	        for(GrammarGACell cell : cells)
//			{
//		        next = dom.createElement("Cell");
//		        if(cell.GetFeasiblePopulation().isEmpty())
//		        {
//		        	next.setAttribute("ROOM_ID", "NULL");
//		        }
//		        else
//		        {
//		        	cell.GetFeasiblePopulation().get(0).getPhenotype().getMap(-1, -1, null, null, null).saveRoomXMLMapElites("algorithm\\" + id + "\\algorithm-" + saveCounter + "_");
//		        	next.setAttribute("ROOM_ID", cell.GetFeasiblePopulation().get(0).getPhenotype().getMap(-1, -1, null, null, null).toString());
//			        next.setAttribute("fitness", Double.toString(cell.GetFeasiblePopulation().get(0).getFitness()));
//
//		        }
//		        e.appendChild(next);
//			}
//
//	        dom.appendChild(rootEle);
//
//	        try {
//	            Transformer tr = TransformerFactory.newInstance().newTransformer();
//	            tr.setOutputProperty(OutputKeys.INDENT, "yes");
//	            tr.setOutputProperty(OutputKeys.METHOD, "xml");
//	            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//	            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "algorithm.dtd");
//	            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//
//	            // send DOM to file
//	            tr.transform(new DOMSource(dom),
//	                                 new StreamResult(new FileOutputStream(xml)));
//
//	        } catch (TransformerException te) {
//	            System.out.println(te.getMessage());
//	        } catch (IOException ioe) {
//	            System.out.println(ioe.getMessage());
//	        }
//	    } catch (ParserConfigurationException pce) {
//	        System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
//	    }
//
//	    saveCounter++;
	}

	
}
