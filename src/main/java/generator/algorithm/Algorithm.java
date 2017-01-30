package generator.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import game.Game;
import game.Map;
import generator.config.Config;
import generator.config.MissingConfigurationException;
import javafx.geometry.Point2D;
import util.Util;
import util.algorithms.BFS;
import util.algorithms.Node;
import util.algorithms.Pathfinder;
import util.eventrouting.EventRouter;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.StatusMessage;

public class Algorithm extends Thread {
	// TODO: Is it weird that the starting population is 100 split between both valid and invalid, 
	// but later invalid and valid are allowed 100 EACH? Look into this.
	public static int POPULATION_SIZE = 100; 
	public static float MUTATION_PROB = 0.9f;
	public static float SON_SIZE = 0.7f;
	
	private List<Individual> populationValid;
	private List<Individual> populationInvalid;
	private Individual best;
	private Config mConfig;
	private List<Individual> readyToValid;
	private List<Individual> readyToInvalid;
	private int size;
	

	public Algorithm(int size, Config config){
		this.size = size;
		mConfig = config;
		initPopulations();		
	}
	
	/**
	 * Broadcasts a string describing the algorithm's status.
	 * 
	 * @param status Message to display.
	 */
	private void broadcastStatusUpdate(String status){
		EventRouter.getInstance().postEvent(new StatusMessage(status));
	}
	
	/**
	 * Broadcasts the best map from the current generation.
	 * 
	 * @param best The best map from the current generation.
	 */
	private void broadcastMapUpdate(Map best){
		EventRouter.getInstance().postEvent(new MapUpdate(best));
	}
	
	/**
	 * Creates lists for the valid and invalid populations and populates them with individuals.
	 * TODO: Figure out what the difference between readyToValid and populationValid is
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
			Individual ind = new Individual(Game.sizeN * Game.sizeM);
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
        int generations = 100; // Why? TODO: investigate

        // TODO: Should there be a fixed number of generations, or should it go until a desired fitness is reached?
        while (generationCount <= generations)
        {
        	broadcastStatusUpdate("Generation " + generationCount);

            evaluateGeneration();
            insertReadyToValidAndEvaluate();
            insertReadyToInvalidAndEvaluate();

            // TODO: Sort out fancy output messages later
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
	}
	
	/**
	 * Checks if an individual is valid (feasible), that is:
	 * 1. There exist paths between the entrance and all other doors
	 * 2. There exist paths between the entrance and all enemies
	 * 3. There exist paths between the entrance and all treasures
	 * 4. There is at least one enemy TODO: Why?
	 * 5. There is at least one treasure TODO: Why?
	 * 
	 * @param ind The individual to check
	 * @return Return true if individual is valid, otherwise return false
    */
	private boolean checkIndividual(Individual ind){
		Map map = ind.getPhenotype().getMap();
        Pathfinder pathfinder = new Pathfinder(map);
        Point2D entrance = map.getEntrance();

        //Check if there is a path between the entrance and all other doors
        for (Point2D door : map.getDoors())
        {
            Node[] path = pathfinder.find(entrance,door);

            if (path.length == 0)
                map.addFailedPathToDoors();
        }

        //Check if there is a path between the entrance and all enemies
        //enemies
        for (Point2D enemy : map.getEnemies())
        {
            Node[] path = pathfinder.find(entrance,enemy);

            if (path.length == 0)
                map.addFailedPathToEnemies();
        }

        //Check if there is a path between the entrance and all treasures
        for (Point2D treasure : map.getTreasures())
        {
            Node[] path = pathfinder.find(entrance,treasure);

            if (path.length == 0)
                map.addFailedPathToTreasures();
        }

        // TODO: Think about why it is a requirement that the room contains enemies and treasure.
        return (map.getFailedPathsToAnotherDoor() == 0) &&
                (map.getFailedPathsToEnemies() == 0) &&
                (map.getFailedPathsToTreasures() == 0) &&
                (map.getEnemyCount() > 0) &&
                (map.getTreasureCount() > 0);
	}
	
	/**
	 * Uses Breadth First Search to calculate a score for the safety of the room's entrance.
	 * According to the old source, the formula should be:
	 * 
	 * 	f = 1 / [ (width * height) - Numero_muros ] * SUM for all areas for each enemy
	 * 
	 * ...but this is not currently the case. TODO: Look into this.
	 * 
	 * @param ind The individual to evaluate.
	 * @return The safety value for the room's entrance.
	 */
	public float evaluateSafetyEnterDoor(Individual ind)
    {
        Map map = ind.getPhenotype().getMap();
        Point2D startDoor = map.getEntrance();
        //int countWalls = map.getWallCount();
        //int totalAreas = 0;
        int minArea = 0;

        BFS bfs = new BFS(map);

        for(Point2D enemy : map.getEnemies())
        {
            //totalAreas += bfs.find(Map.Point2D.toUnityVector2(startDoor), Map.Point2D.toUnityVector2(enemy)).Length;
            int area = bfs.getTraversedNodesBetween(startDoor, enemy).length;
            if (minArea == 0 || minArea < area)
                minArea = area;
        }

        // Note: These comments were in the original source.
        //Formula
        //float safetyFitness = 1.0f / ((Game.sizeN * Game.sizeM) - countWalls);

        //Prod with totalAreas
        //safetyFitness *= totalAreas;
        //safetyFitness *= minArea;

        return minArea;
    }
	
	/**
	 * Evaluates the treasure safety of a valid individual 
	 * TODO: Look into this one more deeply.
	 * TODO: Rename!
	 * 
	 * @param ind The individual to evaluate
	 */
	public void evaluateSafetyTreasuresWithDoorsForIndividualsValid(Individual ind)
	{
	    Map map = ind.getPhenotype().getMap();
	
	    if(map.getEnemyCount() > 0)
	    {
	        int treasuresSize = map.getTreasureCount();
	        int enemiesSize = map.getEnemyCount();
	        Point2D doorEnter = map.getEntrance();
	        List<Double> maxs = new ArrayList<Double>();
	        
	        Pathfinder pathfinder = new Pathfinder(map);
	
	        for (int i = 0; i < treasuresSize; i++)
	        {
	            Point2D treasure = map.getTreasures().get(i);
	            //enemiesSize = 0;
	            for(int j = 0; j < enemiesSize; j++)
	            {
	                Point2D enemy = map.getEnemies().get(j);
	
	                //To Calculate
	                //din = Distance in nodes
	
	                //Distance in nodes from treasure i to enemy j
	                int dinTreasureToEnemy = pathfinder.find(treasure, enemy).length;
	
	                //Distance in nodes from treasure i to enter door
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
	
	                //Calculate and store max
	                maxs.add(Math.max(0.0, result));
	            }
	
	            //The safety is the min of maxs result
	            if(maxs.size() > 0)
	            {
	                Double min = maxs.stream().min((a,b) -> Double.compare(a, b)).get();
	                map.setTreasureSafety(treasure, min);
	            }
	            
	        }
	    }
	}
	
	/**
	 * Evaluates the fitness of a valid individual
	 * TODO: Explain how this is done
	 * 
	 * @param ind The valid individual to evaluate
	 */
    public void evaluateValidIndividual(Individual ind)
    {
        double fitness = 0.0;
        Map map = ind.getPhenotype().getMap();
        int tilesPassables = map.getNonWallTileCount();

        //security area (1)
        double fitness_security_area = evaluateSafetyEnterDoor(ind) / (double)tilesPassables;
        map.setEntrySafetyFitness(fitness_security_area);
        try {
			fitness_security_area -= mConfig.getSecurityAreaVariance();
		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //# enemies (2)
        double[] expectedEnemiesRange = null;
		try {
			expectedEnemiesRange = mConfig.getEnemyQuantityRange();
		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
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
        evaluateSafetyTreasuresWithDoorsForIndividualsValid(ind);
        Double[] safeties = map.getAllTreasureSafeties();
        double safeties_average = Util.calcAverage(safeties);
        double fitness_avg_treasures_security = 0.0;
        try {
			fitness_avg_treasures_security = safeties_average - mConfig.getAverageTreasureSecurity();
		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


        //# treasures (4)
        double[] expectedTreasuresRange = null;
		try {
			expectedTreasuresRange = mConfig.getTreasureQuantityRange();
		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
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
			expectedSafetyVariance = mConfig.getTreasureSecurityVariance();
		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        double fitness_treasures_security_variance = safeties_variance - expectedSafetyVariance;

        //Check objects locked
        double objectsLocked = map.countCloseWalls();

        // TODO: Witness the fitness. (Investigate why these values are used)
        fitness =
            (Math.abs(fitness_security_area) * 0.1) +
            (Math.abs(fitness_enemies_proportion) * 0.2) +
            (Math.abs(fitness_avg_treasures_security) * 0.1) +
            (Math.abs(fitness_treasures_proportion) * 0.2) +
            (Math.abs(fitness_treasures_security_variance) * 0.2) +
            (objectsLocked * 0.2);

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
     * TODO: This only takes into account how far from being valid the individual is.
     * 		 Should it also take into account other fitness factors?
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
	 * Honestly, this method doesn't make much sense.
	 * TODO: Investigate what this method is REALLY supposed to do
	 */
    public void insertReadyToValidAndEvaluate()
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
                populationValid.add(i, valid);
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
	 * Honestly, this method doesn't make much sense.
	 * TODO: Investigate what this method is REALLY supposed to do
	 */
    public void insertReadyToInvalidAndEvaluate()
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
     * Produces a new invalid generation by some arcane means TODO: Document this better
     */
    private void produceNextInvalidGeneration()
    {
        //Select progenitors for crossover
        List<Individual> progenitors = selectProgenitors(populationInvalid);
        //Crossover progenitors
        List<Individual> sons = crossOverBetweenProgenitors(progenitors);
        //Re-insert to population invalid (Replace worst for sons)
        replaceSonsInPopulationInvalid(sons);
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
            Individual[] offspring = progenitors.get(Util.getNextInt(0, sizeProgenitors)).reproduce(progenitors.get(Util.getNextInt(0, sizeProgenitors)));
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
        int countProgenitors = (int)(SON_SIZE * population.size()) / 2;
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
     * TODO: This seems like a clunky way of doing things - rework?
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
        Individual worstIndividual = null;


        for (int i = 0; i < population.size(); i++)
        {
            double currFitness = population.get(i).getFitness();
            avgFitness += currFitness;
            if (currFitness < minFitness)
            {
                minFitness = currFitness;
                worstIndividual = population.get(i);
                // TODO: Investigate - surely it can't simultaneously be worst AND best!?!?
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
	 * Add an individual to the valid population if the population size is less than POPULATION_SIZE
	 * @param valid A valid individual
	 */
    private void addValidIndividual(Individual valid)
    {
        if (populationValid.size() < POPULATION_SIZE)
        {
            populationValid.add(valid);
        }
    }

	/**
	 * Add an individual to the invalid population if the population size is less than POPULATION_SIZE
	 * @param invalid An invalid individual
	 */
    private void addInvalidIndividual(Individual invalid)
    {
        if (populationInvalid.size() < POPULATION_SIZE)
        {
            populationInvalid.add(invalid);
        }
    }
	
}
