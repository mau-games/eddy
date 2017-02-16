package generator.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private List<Individual> populationValid;
	private List<Individual> populationInvalid;
	private Individual best;
	private Config generatorConfig;
	private List<Individual> readyToValid;
	private List<Individual> readyToInvalid;
	private int size;
	private boolean stop = false;
	

	public Algorithm(int size, Config gConfig){
		try {
			config = ConfigurationUtility.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}
		
		this.size = size;
		generatorConfig = gConfig;
		populationSize = config.getInt("generator.population_size");
		mutationProbability = (float) config.getDouble("generator.mutation_probability");
		offspringSize = (float) config.getDouble("generator.offspring_size");

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
		
		readyToValid = new ArrayList<Individual>();
		readyToInvalid = new ArrayList<Individual>();

		populationValid = new ArrayList<Individual>();
		populationInvalid = new ArrayList<Individual>();
		int i = 0;
		int j = 0;
		while((i + j) < size){
			Individual ind = new Individual(Game.sizeN * Game.sizeM, mutationProbability);
			ind.initialize();
			
			if(checkIndividual(ind)){
				populationValid.add(ind);
				i++;
			}
			else {
				populationInvalid.add(ind);
				j++;
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

            evaluateGeneration();
            insertReadyToValidAndEvaluate();
            insertReadyToInvalidAndEvaluate();

                //info population valid
                double[] dataValid = infoGenerational(populationValid, true);
//                avgFitness = dataValid[0];
                //info population invalid
//                double[] dataInvalid = infoGenerational(populationInvalid, false);
//
//                messageGeneration += "Generation " + generationCount + "\n";
//                messageGeneration += "Avg valid fitness: " + dataValid[0] + "\n";
//                messageGeneration += "Min valid fitness: " + dataValid[1] + "\n";
//                messageGeneration += "Max valid fitness: " + dataValid[2] + "\n\n";
//
//                messageGeneration += "Avg invalid fitness: " + dataInvalid[0] + "\n";
//                messageGeneration += "Min invalid fitness: " + dataInvalid[1] + "\n";
//                messageGeneration += "Max invalid fitness: " + dataInvalid[2] + "\n\n";
//                messageGeneration += "Valids: " + populationValid.Count() + "\n";
//                messageGeneration += "Invalids: " + populationInvalid.Count() + "\n";
//                messageGeneration += "Ready to valids: " + readyToValid.Count() + "\n";
//                messageGeneration += "Ready to invalid: " + readyToInvalid.Count() + "\n";
//                messageGeneration += "BEST: " + best.getFitness();
//
//                MessagesPool.add(messageGeneration);
//                MessagesPool.addObject(best);

            
            //broadcastStatusUpdate("Generation " + generationCount + " finished.");
            broadcastStatusUpdate("Average fitness: " + dataValid[0]);
            broadcastStatusUpdate("Max fitness: " + dataValid[2]);
            broadcastStatusUpdate("BEST fitness: " + best.getFitness());
            broadcastStatusUpdate("Valids: " + populationValid.size());
            broadcastStatusUpdate("Invalids: " + populationInvalid.size());

            broadcastMapUpdate(best.getPhenotype().getMap());
            
            
            produceNextValidGeneration();
            produceNextInvalidGeneration();
            generationCount++;
        }
        EventRouter.getInstance().postEvent(new AlgorithmDone());
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

	
//	/**
//	 * NEW VERSION!
//	 * Uses Flood Fill to calculate a score for the safety of the room's entrance.
//	 * 
//	 * @param ind The individual to evaluate.
//	 * @return The safety value for the room's entrance.
//	 */
//	public float evaluateSafetyEnterDoor(Individual ind)
//    {
//        Map map = ind.getPhenotype().getMap();
//
//        List<Node> visited = new ArrayList<Node>();
//    	Queue<Node> queue = new LinkedList<Node>();
//    	
//    	Node root = new Node(0.0f, map.getEntrance(), null);
//    	queue.add(root);
//    	
//    	while(!queue.isEmpty()){
//    		Node current = queue.remove();
//    		visited.add(current);
//    		
//    		List<Point> children = map.getAvailableCoords(current.position);
//            for(Point child : children)
//            {
//                if (visited.stream().filter(x->x.equals(child)).findFirst().isPresent() 
//                		|| queue.stream().filter(x->x.equals(child)).findFirst().isPresent()) 
//                	continue;
//
//                //Create child node
//                Node n = new Node(0.0f, child, current);
//                queue.add(n);
//            }
//            if(map.getTile(current.position).isEnemy())
//    			break;
//    	}
//    	if(queue.isEmpty())
//    	{
//    		return 0;
//    	}
//    	return 1 - (float)visited.size()/map.getNonWallTileCount();  
//    }
	
	/**
	 * Uses flood fill to calculate a score for the safety of the room's entrance
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
	
	                //Formule result
	                /*
	                    (dti,mj - dti,i) /
	                    (dti,mj - dti,i)
	                */
	                double result = (double)
	                    (dinTreasureToEnemy - dinTreasureToStartDoor) / 
	                    (dinTreasureToEnemy + dinTreasureToStartDoor);
	
	                if (Double.isNaN(result))
	                {
	                    result = 0.0f;
	                }
	                
	                //System.out.println("treasure to enemy 2 " + dinTreasureToEnemy + ", " + result);

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
    public void evaluateValidIndividual(Individual ind)
    {
        double fitness = 0.0;
        Map map = ind.getPhenotype().getMap();


        //security area (1)
        //System.out.println("" + evaluateSafetyEnterDoor(ind) + " " + evaluateSafetyEnterDoor3(ind));
        double fitness_security_area = evaluateEntranceSafety(ind); //Note - this has been changed from the Unity version
        map.setEntrySafetyFitness(fitness_security_area);
        try {
			fitness_security_area = Math.abs(fitness_security_area - generatorConfig.getSecurityAreaVariance());
		} catch (MissingConfigurationException e) {
			e.printStackTrace();
		}

        //# enemies (2)
        double[] expectedEnemiesRange = null;
		try {
			expectedEnemiesRange = generatorConfig.getEnemyQuantityRange();
		} catch (MissingConfigurationException e) {
			e.printStackTrace();
		}
        double fitness_enemies_proportion = 0.0;
        double enemyPercent = map.getEnemyPercentage();
        if(enemyPercent > expectedEnemiesRange[0])
        {
            fitness_enemies_proportion = enemyPercent - expectedEnemiesRange[1];
        }
        else
        { 
            fitness_enemies_proportion = enemyPercent - expectedEnemiesRange[0];
        }

        //avg seg tesoros (3)
        evaluateTreasureSafeties(ind);
        Double[] safeties = map.getAllTreasureSafeties();
        double safeties_average = Util.calcAverage(safeties);
       
        double fitness_avg_treasures_security = 0.0;
        try {
			fitness_avg_treasures_security = safeties_average - generatorConfig.getAverageTreasureSecurity();
		} catch (MissingConfigurationException e) {
			e.printStackTrace();
		}


        //# treasures (4)
        double[] expectedTreasuresRange = null;
		try {
			expectedTreasuresRange = generatorConfig.getTreasureQuantityRange();
		} catch (MissingConfigurationException e) {
			e.printStackTrace();
		}
        double fitness_treasures_proportion = 0.0;
        double treasurePercent = map.getTreasurePercentage();
        if(treasurePercent > expectedTreasuresRange[0])
        {
            fitness_treasures_proportion = treasurePercent - expectedTreasuresRange[1];
        }
        else
        {
            fitness_treasures_proportion = treasurePercent - expectedTreasuresRange[0];
        }

        //variance treasures security
        double safeties_variance = Util.calcVariance(safeties);
        double expectedSafetyVariance = 0.0;
		try {
			expectedSafetyVariance = generatorConfig.getTreasureSecurityVariance();
		} catch (MissingConfigurationException e) {
			e.printStackTrace();
		}

        double fitness_treasures_security_variance = safeties_variance - expectedSafetyVariance;

        //Removed countCloseWalls method because it ALWAYS returns 0 and the intended function is unclear
        //Consequently, weights have been adjusted

        fitness =
            (Math.abs(fitness_security_area) * 0.2) +
            (Math.abs(fitness_enemies_proportion) * 0.3) +
            (Math.abs(fitness_avg_treasures_security) * 0.1) +
            (Math.abs(fitness_treasures_proportion) * 0.2) +
            (Math.abs(fitness_treasures_security_variance) * 0.2);

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
	public void evaluateInvalidIndividual(Individual ind)
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
        for(Individual ind : populationValid)
        {
            if (!ind.isEvaluated())
                evaluateValidIndividual(ind);
        }

        //Evaluate invalid individuals
        for(Individual ind : populationInvalid)
        {
            if (!ind.isEvaluated())
                evaluateInvalidIndividual(ind);
        }
    }

	/**
	 * Add new valid individuals to the valid population
	 * TODO: Check if this and the next method are really well implemented
	 */
    private void insertReadyToValidAndEvaluate()
    {
    	//Sort valid population in descending order
        sortPopulation(populationValid, false);

        int i = 0;
        boolean flag = false;
        for (Individual valid : readyToValid)
        {
            if (i != populationValid.size() - 1 && !flag)
            {
                evaluateValidIndividual(valid); // TODO: Check if this is necessary. Hasn't it already been evaluated?
                populationValid.set(i, valid);
                i++;
            }
            else
            {
                if (!flag) 
                	flag = true;
                addValidIndividual(valid);
            }
        }
    }

    /**
	 * Add new invalid individuals to the invalid population
	 */
    private void insertReadyToInvalidAndEvaluate()
    {
        sortPopulation(populationInvalid, true);

        int i = 0;
        boolean flag = false;
        for (Individual invalid : readyToInvalid)
        {
            if (i != populationInvalid.size() - 1 && !flag)
            {
                evaluateInvalidIndividual(invalid);
                populationInvalid.set(i, invalid);
                i++;
            }
            else
            {
                if(!flag) 
                	flag = true;
                addInvalidIndividual(invalid);
            }
        }
    }

    /**
     * Produces a new valid generation according to the following procedure:
     *  1. Select individuals from the valid population to breed
     *  2. Crossover these individuals
     *  3. Add them back into the population
     */
    private void produceNextValidGeneration()
    {
        //Select progenitors for crossover
        List<Individual> progenitors = selectProgenitors(populationValid);
        //Crossover progenitors
        List<Individual> sons = crossOverBetweenProgenitors(progenitors);
        //Re-insert to population valid (Replace worst for sons)
        replaceSonsInPopulationValid(sons);
    }

    /**
     * Produces a new invalid generation according to the following procedure:
     *  1. Select individuals from the invalid population to breed
     *  2. Crossover these individuals
     *  3. Add them back into the population
     */
    private void produceNextInvalidGeneration()
    {
        //Select parents for crossover
        List<Individual> parents = selectProgenitors(populationInvalid);
        //Crossover parents
        List<Individual> children = crossOverBetweenProgenitors(parents);
        //Re-insert to population invalid (Replace worst for sons)
        replaceSonsInPopulationInvalid(children);
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
            Individual[] offspring = progenitors.get(Util.getNextInt(0, sizeProgenitors)).twoPointCrossover(progenitors.get(Util.getNextInt(0, sizeProgenitors)));
            sons.addAll(Arrays.asList(offspring));
            countSons += 2;
        }

        return sons;
    }

    
    /**
     * Selects progenitors from a population using tournament selection
     * (see https://en.wikipedia.org/wiki/Tournament_selection).
     * TODO: Make sure this is properly implemented.
     * 
     * @param population A whole population of individuals
     * @return A list of chosen progenitors
     */
    private List<Individual> selectProgenitors(List<Individual> population)
    {
        int countProgenitors = (int)(offspringSize * population.size()) / 2;
        List<Individual> progenitors = new ArrayList<Individual>();

        while(countProgenitors > 0)
        {
            int individuals_to_select = Util.getNextInt(1, population.size());

            Individual bestProgenitor = null;
            for(int i = 0; i < individuals_to_select; i++)
            {
                int progenitorIndex = Util.getNextInt(0, population.size() - 1);
                Individual progenitor = population.get(progenitorIndex);

                //select the progenitor with the lowest fitness
                if(bestProgenitor == null || (bestProgenitor.getFitness() > progenitor.getFitness()))
                {
                    bestProgenitor = progenitor;
                }
            }

            progenitors.add(bestProgenitor);
            countProgenitors--;
        }

        return progenitors;
    }

    /**
     * Add individuals to either the valid population or readyToInvalid depending on whether or not they are valid.
     * 
     * @param sons Individuals to add
     */
    private void replaceSonsInPopulationValid(List<Individual> sons)
    {
        sortPopulation(populationValid, false);
        
        int i = 0;
        readyToInvalid = new ArrayList<Individual>();

        for (Individual son : sons)
        {
            if(checkIndividual(son))
            {
                //replace in population valid
                populationValid.set(i,son);
            }
            else
            {
				//mark as ready to invalid
                readyToInvalid.add(son);
            }

            i++;
        }

    }

    /**
     * Add individuals to either the invalid population or readyToValid depending on whether or not they are valid.
     * 
     * @param sons Individuals to add
     */
    private void replaceSonsInPopulationInvalid(List<Individual> sons)
    {
        sortPopulation(populationInvalid, false);
        
        readyToValid = new ArrayList<Individual>();
        int i = 0;
        for (Individual son : sons)
        {
            if(checkIndividual(son))
            {
                //mark as ready to valid
                readyToValid.add(son);
            }
            else
            {
                //replace in population invalid
                populationInvalid.set(i,son);
            }

            i++;
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
        if (populationValid.size() < populationSize)
        {
            populationValid.add(valid);
        }
    }

	/**
	 * Add an individual to the invalid population if the population size is less than POPULATION_SIZE.
	 * 
	 * @param invalid An invalid individual.
	 */
    private void addInvalidIndividual(Individual invalid)
    {
        if (populationInvalid.size() < populationSize)
        {
            populationInvalid.add(invalid);
        }
    }
	
}
