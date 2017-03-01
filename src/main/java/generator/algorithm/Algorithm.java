package generator.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import finder.geometry.Polygon;
import finder.geometry.Rectangle;
import finder.patterns.Pattern;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Room;
import game.Game;
import game.Map;
import game.TileTypes;
import generator.config.Config;
import util.Point;
import util.Util;
import util.algorithms.Node;
import util.algorithms.Pathfinder;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.StatusMessage;

public class Algorithm extends Thread {
	private final Logger logger = LoggerFactory.getLogger(Config.class);
	private ConfigurationUtility config;
	
	private int populationSize; 
	private float mutationProbability;
	private float offspringSize;
	
	private List<Individual> feasiblePopulation;
	private List<Individual> infeasiblePopulation;
	private Individual best;
	private Config generatorConfig;
	private List<Individual> feasiblePool;
	private List<Individual> infeasiblePool;
	private int size;
	private boolean stop = false;
	private int feasibleAmount;
	

	public Algorithm(Config gConfig){
		try {
			config = ConfigurationUtility.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}

		generatorConfig = gConfig;
		populationSize = config.getInt("generator.population_size");
		mutationProbability = (float) config.getDouble("generator.mutation_probability");
		offspringSize = (float) config.getDouble("generator.offspring_size");
		feasibleAmount = (int)((double)populationSize * config.getDouble("generator.feasible_proportion"));

		initPopulations();
	}
	
	public void terminate(){
		stop = true;
	}
	
	/**
	 * Broadcasts a string describing the algorithm's status.
	 * 
	 * @param status Message to display.
	 */
	private synchronized void broadcastStatusUpdate(String status){
		EventRouter.getInstance().postEvent(new StatusMessage(status));
	}
	
	/**
	 * Broadcasts the best map from the current generation.
	 * 
	 * @param best The best map from the current generation.
	 */
	private synchronized void broadcastMapUpdate(Map best){
		EventRouter.getInstance().postEvent(new MapUpdate(best));
	}
	
	/**
	 * Creates lists for the valid and invalid populations and populates them with individuals.
	 */
	private void initPopulations(){
		broadcastStatusUpdate("Initialising...");
		
		feasiblePool = new ArrayList<Individual>();
		infeasiblePool = new ArrayList<Individual>();
		feasiblePopulation = new ArrayList<Individual>();
		infeasiblePopulation = new ArrayList<Individual>();
		
		int i = 0;
		int j = 0;
		while((i + j) < populationSize){
			Individual ind = new Individual(Game.sizeN * Game.sizeM, mutationProbability);
			ind.initialize();
			
			if(checkIndividual(ind)){
				if(i < feasibleAmount){
					feasiblePool.add(ind);
					i++;
				}
			}
			else {
				if(j < populationSize - feasibleAmount){
					infeasiblePool.add(ind);
					j++;
				}
			}
		}
		
		broadcastStatusUpdate("Population generated.");
	}
	
	/**
	 * Starts the algorithm. Called when the thread starts.
	 */
	public void run(){
		
		broadcastStatusUpdate("Evolving...");

        int generationCount = 1;
        int generations = config.getInt("generator.generations");

        while (generationCount <= generations) {
        	if(stop)
        		return;
        	
        	
        	
        	broadcastStatusUpdate("Generation " + generationCount);

        	evaluateAndTrimPools();
        	copyPoolsToPopulations();
        	
            //evaluateGeneration();
            //insertReadyToValidAndEvaluate();
            //insertReadyToInvalidAndEvaluate();

            //info population valid
            double[] dataValid = infoGenerational(feasiblePopulation, true);
            
            //broadcastStatusUpdate("Generation " + generationCount + " finished.");
//          broadcastStatusUpdate("Average fitness: " + dataValid[0]);
//          broadcastStatusUpdate("Max fitness: " + dataValid[2]);
          broadcastStatusUpdate("BEST fitness: " + best.getFitness());
//          broadcastStatusUpdate("Valids: " + populationValid.size());
//          broadcastStatusUpdate("Invalids: " + populationInvalid.size());
         // broadcastStatusUpdate("Best corridor count: " + Corridor.corridorTileCount(best.getPhenotype().getMap(), null));
        //  broadcastStatusUpdate("Best passable tile count: " + best.getPhenotype().getMap().getNonWallTileCount());

            broadcastMapUpdate(best.getPhenotype().getMap());
  
            Map map = best.getPhenotype().getMap();
          	List<Pattern> rooms = Room.matches(map, new Rectangle(new finder.geometry.Point(0,0),new finder.geometry.Point(map.getColCount()-1,map.getRowCount()-1)));
          	int roomCount = rooms.size();
          	int roomArea = 0;
      	
	      	//Room fitness
	      	for(Pattern p : rooms){
	      		roomArea += ((Polygon)p.getGeometry()).getArea();
	      	}
          
          
          broadcastStatusUpdate("Rooms: " + roomCount);
          broadcastStatusUpdate("Room tiles: " + roomArea);
          broadcastStatusUpdate("Passable tiles: " + best.getPhenotype().getMap().getNonWallTileCount());
            breedFeasibleIndividuals();
            breedInfeasibleIndividuals();
            generationCount++;
        }
        EventRouter.getInstance().postEvent(new AlgorithmDone());
	}
	
	/**
	 * Evaluates the fitness of all individuals in pools and trims them down to the desired sizes
	 */
	private void evaluateAndTrimPools(){
        //Evaluate valid individuals
        for(Individual ind : feasiblePool)
        {
            if (!ind.isEvaluated())
                evaluateFeasibleIndividual(ind);
        }
        this.sortPopulation(feasiblePool, false);
        feasiblePool = feasiblePool.stream().limit(feasibleAmount).collect(Collectors.toList());

        //Evaluate invalid individuals
        for(Individual ind : infeasiblePool)
        {
            if (!ind.isEvaluated())
                evaluateInfeasibleIndividual(ind);
        }
        this.sortPopulation(infeasiblePool, false);
        infeasiblePool = infeasiblePool.stream().limit(populationSize - feasibleAmount).collect(Collectors.toList());
	}
	
	/**
	 * Copy individuals from pools to populations for breeding etc.
	 */
	private void copyPoolsToPopulations(){
		feasiblePopulation.clear();
		feasiblePool.forEach(individual -> feasiblePopulation.add(individual));
		
		infeasiblePopulation.clear();
		infeasiblePool.forEach(individual -> infeasiblePopulation.add(individual));
	}
	
	/**
	 * Checks if an individual is valid (feasible), that is:
	 * 1. There exist paths between the entrance and all other doors
	 * 2. There exist paths between the entrance and all enemies
	 * 3. There exist paths between the entrance and all treasures
	 * 4. There is at least one enemy
	 * 5. There is at least one treasure
	 * 
	 * @param ind The individual to check
	 * @return Return true if individual is valid, otherwise return false
    */
	private boolean checkIndividual(Individual ind){
		Map map = ind.getPhenotype().getMap();
		List<Node> visited = new ArrayList<Node>();
    	Queue<Node> queue = new LinkedList<Node>();
    	int treasure = 0;
    	int enemies = 0;
    	int doors = 0;
    	
    	Node root = new Node(0.0f, map.getEntrance(), null);
    	queue.add(root);
    	
    	while(!queue.isEmpty()){
    		Node current = queue.remove();
    		visited.add(current);
    		if(map.getTile(current.position) == TileTypes.DOOR)
    			doors++;
    		else if (map.getTile(current.position).isEnemy())
    			enemies++;
    		else if (map.getTile(current.position).isTreasure())
    			treasure++;
    		
    		List<Point> children = map.getAvailableCoords(current.position);
            for(Point child : children)
            {
                if (visited.stream().filter(x->x.equals(child)).findFirst().isPresent() 
                		|| queue.stream().filter(x->x.equals(child)).findFirst().isPresent()) 
                	continue;

                //Create child node
                Node n = new Node(0.0f, child, current);
                queue.add(n);
            }
    	}
    	
    	for(int i = treasure; i < map.getTreasureCount();i++)
    		map.addFailedPathToTreasures();
    	for(int i = doors; i < map.getDoorCount();i++)
    		map.addFailedPathToTreasures();
    	for(int i = enemies; i < map.getEnemyCount();i++)
    		map.addFailedPathToTreasures();
    	
    	return visited.size() == map.getNonWallTileCount() 
    			&& (treasure + doors + enemies == map.getTreasureCount() + map.getDoorCount() + map.getEnemyCount())
    			&& map.getTreasureCount() > 0 && map.getEnemyCount() > 0;
	}
	
	/**
	 * Uses flood fill to calculate a score for the safety of the room's entrance
	 * 
	 * The safety value is between 0 and 1. 
	 * 0 means there is an enemy adjacent to the entry door. 
	 * 1 means there is no enemy in the room (impossible).
	 * In practice the highest safety will be achieved when an enemy is as far away from the entrance as possible.
	 * 
	 * @param ind The individual to evaluate.
	 * @return The safety value for the room's entrance.
	 */
	public float evaluateEntranceSafety(Individual ind)
    {
        Map map = ind.getPhenotype().getMap();

        List<Node> visited = new ArrayList<Node>();
    	Queue<Node> queue = new LinkedList<Node>();
    	
    	Node root = new Node(0.0f, map.getEntrance(), null);
    	queue.add(root);
    	
    	while(!queue.isEmpty()){
    		Node current = queue.remove();
    		visited.add(current);
    		if(map.getTile(current.position).isEnemy())
    			break;
    		
    		List<Point> children = map.getAvailableCoords(current.position);
            for(Point child : children)
            {
                if (visited.stream().filter(x->x.equals(child)).findFirst().isPresent() 
                		|| queue.stream().filter(x->x.equals(child)).findFirst().isPresent()) 
                	continue;

                //Create child node
                Node n = new Node(0.0f, child, current);
                queue.add(n);
            }
            
    	}
    	return (float)visited.size()/map.getNonWallTileCount();  
    }
	
	/**
	 * Evaluates the treasure safety of a valid individual 
	 * See Sentient Sketchbook for a description of the method
	 * 
	 * @param ind The individual to evaluate
	 */
	public void evaluateTreasureSafeties(Individual ind)
	{
	    Map map = ind.getPhenotype().getMap();
	
	    if(map.getEnemyCount() > 0)
	    {

	        Point doorEnter = map.getEntrance();
	        
	        Pathfinder pathfinder = new Pathfinder(map);
	
	        for (Point treasure: map.getTreasures())
	        {
	        	//Find the closest enemy
	            List<Node> visited = new ArrayList<Node>();
	        	Queue<Node> queue = new LinkedList<Node>();
	        	
	        	Node root = new Node(0.0f, treasure, null);
	        	queue.add(root);
	        	Point closestEnemy = null;
	        	
	        	while(!queue.isEmpty()){
	        		Node current = queue.remove();
	        		visited.add(current);
	        		if(map.getTile(current.position).isEnemy()){
	        			closestEnemy = current.position;
	        			break;
	        		}
	        		
	        		List<Point> children = map.getAvailableCoords(current.position);
	                for(Point child : children)
	                {
	                    if (visited.stream().filter(x->x.equals(child)).findFirst().isPresent() 
	                    		|| queue.stream().filter(x->x.equals(child)).findFirst().isPresent()) 
	                    	continue;

	                    //Create child node
	                    Node n = new Node(0.0f, child, current);
	                    queue.add(n);
	                }
	        	}
	        	
	            int dinTreasureToEnemy = pathfinder.find(treasure, closestEnemy).length;
	            
                //Distance in nodes from treasure to entrance
                int dinTreasureToStartDoor = pathfinder.find(treasure, doorEnter).length;
	
                double result = (double)
                    (dinTreasureToEnemy - dinTreasureToStartDoor) / 
                    (dinTreasureToEnemy + dinTreasureToStartDoor);

                if (Double.isNaN(result))
                    result = 0.0f;
                
                map.setTreasureSafety(treasure, Math.max(0.0, result));
	        }
	    }
	}
	
	/**
	 * Evaluates the fitness of a valid individual using the following factors:
	 *  1. Entrance safety (how close are enemies to the entrance)
	 *  2. Proportion of tiles that are enemies
	 *  3. Average treasure safety (Are treasures closer to the door or enemies?)
	 *  4. Proportion of tiles that are treasure
	 *  5. Treasure safety variance (whatever this is!)
	 * 
	 * @param ind The valid individual to evaluate
	 */
    public void evaluateFeasibleIndividual(Individual ind)
    {
//        double fitness = 0.0;
//        Map map = ind.getPhenotype().getMap();
//
//
//        //Entrance safety (1)
//        double entranceSafetyFitness = evaluateEntranceSafety(ind); //Note - this has been changed from the Unity version
//        map.setEntranceSafety(entranceSafetyFitness);
//        try {
//			entranceSafetyFitness = Math.abs(entranceSafetyFitness - generatorConfig.getEntranceSafety());
//		} catch (MissingConfigurationException e) {
//			e.printStackTrace();
//		}
//
//        //Enemy density (2)
//        double[] expectedEnemiesRange = null;
//		try {
//			expectedEnemiesRange = generatorConfig.getEnemyQuantityRange();
//		} catch (MissingConfigurationException e) {
//			e.printStackTrace();
//		}
//        double enemyDensityFitness = 0.0;
//        double enemyPercent = map.getEnemyPercentage();
//        if(enemyPercent < expectedEnemiesRange[0])
//        {
//            enemyDensityFitness = expectedEnemiesRange[0] - enemyPercent;
//        }
//        else if(enemyPercent > expectedEnemiesRange[1])
//        { 
//            enemyDensityFitness = enemyPercent - expectedEnemiesRange[1];
//        }
//
//        //Average treasure safety (3)
//        evaluateTreasureSafeties(ind);
//        Double[] safeties = map.getAllTreasureSafeties();
//        double safeties_average = Util.calcAverage(safeties);
//       
//        double averageTreasureSafetyFitness = 0.0;
//        try {
//			averageTreasureSafetyFitness = Math.abs(safeties_average - generatorConfig.getAverageTreasureSafety());
//		} catch (MissingConfigurationException e) {
//			e.printStackTrace();
//		}
//
//
//        //Treasure density (4)
//        double[] expectedTreasuresRange = null;
//		try {
//			expectedTreasuresRange = generatorConfig.getTreasureQuantityRange();
//		} catch (MissingConfigurationException e) {
//			e.printStackTrace();
//		}
//        double treasureDensityFitness = 0.0;
//        double treasurePercent = map.getTreasurePercentage();
//        if(treasurePercent < expectedTreasuresRange[0])
//        {
//            treasureDensityFitness = expectedTreasuresRange[0] - treasurePercent;
//        }
//        else if (treasurePercent > expectedTreasuresRange[1])
//        {
//            treasureDensityFitness = treasurePercent - expectedTreasuresRange[1];
//        }
//
//        //Treasure Safety Variance (5)
//        double safeties_variance = Util.calcVariance(safeties);
//        double expectedSafetyVariance = 0.0;
//		try {
//			expectedSafetyVariance = generatorConfig.getTreasureSafetyVariance();
//		} catch (MissingConfigurationException e) {
//			e.printStackTrace();
//		}
//
//        double treasureSafetyVarianceFitness = Math.abs(safeties_variance - expectedSafetyVariance);
//
//        //Removed countCloseWalls method because it ALWAYS returns 0 and the intended function is unclear
//        //Consequently, weights have been adjusted
//
//        fitness =
//            entranceSafetyFitness * 0.2 +
//            enemyDensityFitness * 0.3 +
//            averageTreasureSafetyFitness * 0.1 +
//            treasureDensityFitness * 0.2 +
//            treasureSafetyVarianceFitness * 0.2;

    	
    	//Corridor fitness
    	Map map = ind.getPhenotype().getMap();
    	int passableTiles = map.getNonWallTileCount();
    	int corridors = Corridor.corridorTileCount(map, null);
    	//double fitness = (double)corridors/(double)passableTiles;
    	
    	int roomArea = 0;
    	List<Pattern> rooms = Room.matches(map, new Rectangle(new finder.geometry.Point(0,0),new finder.geometry.Point(map.getColCount()-1,map.getRowCount()-1)));
    	
    	//Room fitness
    	for(Pattern p : rooms){
    		roomArea += ((Polygon)p.getGeometry()).getArea();
    	}
//    
    	int avgRoomArea = 0;
    	if(rooms.size() > 0)
    		avgRoomArea = roomArea/rooms.size();
    	double avgRoomAreaPercent = (double)avgRoomArea / (double)(map.getRowCount()*map.getColCount());
    	double roomAreaDifference = Math.abs(avgRoomAreaPercent - 0.5);
    	
    	//double fitness = roomAreaDifference;
    	double wallProportion = (double)map.getWallCount()/(map.getRowCount()*map.getColCount());
//    	
    	double wallTarget = 0.3 * rooms.size();
    	double wallFitness = Math.abs(wallProportion - wallTarget);
//    	
//    	double fitness = 0.2 - 0.2*(double)roomArea/(double)passableTiles
//    			+ 0.8 * roomAreaDifference;
    	//double fitness = 1.0 - 0.65*(double)roomArea/(double)passableTiles - 0.35*(1-wallFitness);
    	
    	double fitness = (double)roomArea/passableTiles;
    	
        //set final fitness
        ind.setFitness(fitness);
        ind.setEvaluate(true);
    }

    /**
     * Evaluates an invalid individual's fitness according the following formula:
     * 
     * fitness = 1 - ((1/3) * (pathToEnemiesFail/enemiesCount) +
     *				  (1/3) * (pathToTreasuresFail/treasuresCount) +
     *                (1/3) * (pathToDoorsFail/doorsCount))
     * 
     * @param ind The invalid individual to evaluate
     */
	public void evaluateInfeasibleIndividual(Individual ind)
	{
		double fitness = 0.0;
	    Map map = ind.getPhenotype().getMap();
	
	    double enemies = (map.getFailedPathsToEnemies() / (double)map.getEnemyCount());
	    if (Double.isNaN(enemies)) 
	    	enemies = 1.0;
	    
	    double treasures = (map.getFailedPathsToTreasures() / (double)map.getTreasureCount());
	    if (Double.isNaN(treasures)) 
	    	treasures = 1.0;
	    
	    double doors = (map.getFailedPathsToAnotherDoor() / (double)map.getDoorCount());
	    if (Double.isNaN(doors)) 
	    	doors = 1.0;
	
	    double weight = 1.0/3.0;
	    fitness = 1 - ((weight * enemies) + (weight * treasures) + (weight * doors));
	
	    fitness = (fitness < 0)? 0 : fitness;
	    
	    //set final fitness
	    ind.setFitness(fitness);
        ind.setEvaluate(true);
	}
	
	/**
	 * Evaluate the entire generation
	 */
	public void evaluateGeneration()
    {
        //Evaluate valid individuals
        for(Individual ind : feasiblePopulation)
        {
            if (!ind.isEvaluated())
                evaluateFeasibleIndividual(ind);
        }

        //Evaluate invalid individuals
        for(Individual ind : infeasiblePopulation)
        {
            if (!ind.isEvaluated())
                evaluateInfeasibleIndividual(ind);
        }
    }

    /**
     * Produces a new valid generation according to the following procedure:
     *  1. Select individuals from the valid population to breed
     *  2. Crossover these individuals
     *  3. Add them back into the population
     */
    private void breedFeasibleIndividuals()
    {
        //Select parents for crossover
        List<Individual> parents = tournamentSelection(feasiblePopulation);
        //Crossover parents
        List<Individual> children = crossOverBetweenProgenitors(parents);
        //Assign to a pool based on feasibility
        assignToPool(children);
    }

    /**
     * Produces a new invalid generation according to the following procedure:
     *  1. Select individuals from the invalid population to breed
     *  2. Crossover these individuals
     *  3. Add them back into the population
     */
    private void breedInfeasibleIndividuals()
    {
        //Select parents for crossover
        List<Individual> parents = tournamentSelection(infeasiblePopulation);
        //Crossover parents
        List<Individual> children = crossOverBetweenProgenitors(parents);
        //Assign to a pool based on feasibility
        assignToPool(children);
    }
			
    /**
     * Crossover 
     * 
     * @param progenitors A List of Individuals to be reproduced
     * @return A List of Individuals
     */
    private List<Individual> crossOverBetweenProgenitors(List<Individual> progenitors)
    {
        List<Individual> sons = new ArrayList<Individual>();
        int sizeProgenitors = progenitors.size();
        int countSons = 0;
        int sonSize = sizeProgenitors * 2;

        while (countSons < sonSize)
        {
            Individual[] offspring = progenitors.get(Util.getNextInt(0, sizeProgenitors)).rectangularCrossover(progenitors.get(Util.getNextInt(0, sizeProgenitors)));
            sons.addAll(Arrays.asList(offspring));
            countSons += 2;
        }

        return sons;
    }

    
    /**
     * Selects parents from a population using (deterministic) tournament selection - i.e. the winner is always the individual with the "best" fitness.
     * See: https://en.wikipedia.org/wiki/Tournament_selection
     * TODO: Make sure this is properly implemented.
     * 
     * @param population A whole population of individuals
     * @return A list of chosen progenitors
     */
    private List<Individual> tournamentSelection(List<Individual> population)
    { 
        List<Individual> parents = new ArrayList<Individual>();
        int numberOfParents = (int)(offspringSize * population.size()) / 2;

        while(parents.size() < numberOfParents)
        {
        	//Select at least one individual to "fight" in the tournament
            int tournamentSize = Util.getNextInt(1, population.size());

            Individual winner = null;
            for(int i = 0; i < tournamentSize; i++)
            {
                int progenitorIndex = Util.getNextInt(0, population.size());
                Individual individual = population.get(progenitorIndex);

                //select the individual with the highest fitness
                if(winner == null || (winner.getFitness() < individual.getFitness()))
                {
                	winner = individual;
                }
            }

            parents.add(winner);
        }

        return parents;
    }
    
    /**
     * Selects parents by fitness proportionate selection.
     * See: https://en.wikipedia.org/wiki/Fitness_proportionate_selection
     * Currently allows duplicates, is this wise?
     * 
     * @param population
     * @return
     */
    private List<Individual> fitnessProportionateRouletteWheelSelection(List<Individual> population){
    	sortPopulation(population, false);
    	
    	List<Individual> parents = new ArrayList<Individual>();
    	int numberOfParents = (int)(offspringSize * population.size()) / 2;
    	
    	//Calculate sum of fitnesses:
    	double fitnessSum = population.stream().map((i)->i.getFitness()).reduce(0.0, (acc,f)->acc+f);
    	
    	while(parents.size() < numberOfParents){
    		
        	double rand = Math.random() * fitnessSum;
        	
        	for(int i = 0; i < population.size();i++){
        		rand -= population.get(i).getFitness();
        		if(rand <= 0){
        			parents.add(population.get(i));
        			break;
        		}
        	}
    		
    	}
    	
    	return parents;
    }
    

    /**
     * TODO: Document
     * 
     * @param sons Individuals to add
     */
    private void assignToPool(List<Individual> sons)
    {
        for (Individual son : sons)
        {
            if(checkIndividual(son))
            {
                feasiblePool.add(son);
            }
            else
            {
                infeasiblePool.add(son);
            }
        }
    }


    /**
     * Sorts a population according to fitness
     * 
     * @param population A List of Individuals to sort
     * @param ascending true for ascending order, false for descending
     */
    private void sortPopulation(List<Individual> population, boolean ascending)
    {
        population.sort((x, y) -> (ascending ? 1 : -1) * Double.compare(x.getFitness(),y.getFitness()));
    }
 	
    /**
     * Calculates some statistics about a population and (optionally) saves the "best" individual.
     * 
     * @param population The population to analyse
     * @param saveBest Should the best individual be saved? True should only be used for the valid population
     * @return An array of doubles. Index 0 contain the average fitness. Index 1 contains the minimum fitness. Index 2 contains the maximum fitness.
     */
	private double[] infoGenerational(List<Individual> population, boolean saveBest) //default for saveBest was false
    {
        //avg, min, max
        double[] data = new double[3];

        double avgFitness = 0.0;
        double minFitness = Double.POSITIVE_INFINITY;
        double maxFitness = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < population.size(); i++)
        {
            double currFitness = population.get(i).getFitness();
            avgFitness += currFitness;
            if (currFitness < minFitness)
            {
                minFitness = currFitness;
                if (saveBest)
                    best = population.get(i);
            }
            if (currFitness > maxFitness)
            {
                maxFitness = currFitness;
                
            }
        }

        if (population.size() > 0)
        {
            avgFitness = avgFitness / population.size();
        }

        if(Double.isNaN(avgFitness))
        {
            avgFitness = 0.0f;
        }

        data[0] = avgFitness;
        data[1] = minFitness;
        data[2] = maxFitness;

        return data;
    }

	/**
	 * Add an individual to the valid population if the population size is less than POPULATION_SIZE.
	 * 
	 * @param valid A valid individual.
	 */
    private void addValidIndividual(Individual valid)
    {
        if (feasiblePopulation.size() < populationSize)
        {
            feasiblePopulation.add(valid);
        }
    }

	/**
	 * Add an individual to the invalid population if the population size is less than POPULATION_SIZE.
	 * 
	 * @param invalid An invalid individual.
	 */
    private void addInvalidIndividual(Individual invalid)
    {
        if (infeasiblePopulation.size() < populationSize)
        {
            infeasiblePopulation.add(invalid);
        }
    }
	
}
