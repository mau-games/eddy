package generator.algorithm;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import generator.config.Config;

public class Algorithm extends Thread {
	public static int POPULATION_SIZE = 100;
	public static float MUTATION_PROB = 0.9f;
	public static float SON_SIZE = 0.7f;
	
	private List<Individual> populationValid;
	private List<Individual> populationInvalid;
	private Individual best;
	private Config mConfig;
	private int indexWorst = 0;
	private List<Individual> readyToValid;
	private List<Individual> readyToInvalid;
	private int size;
	

	public Algorithm(int size, Config config){
		this.size = size;
		mConfig = config;
		init();		
	}
	
	private void init(){
		readyToValid = new ArrayList<Individual>();
		readyToInvalid = new ArrayList<Individual>();
		
		try{
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
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	//Return true if individual is valid, otherwise return false
    /*
        An individual is invalid if there does not exist a path between the entry door and the other doors
        and if there do not exist paths between the entry door and all enemies and treasures
    */
	private boolean checkIndividual(Individual ind){
		// TODO: Implement this when Map is done
		
		//Get the individual's phenotype
		//Check if there's a path between the entry door and all other doors
		//Check if there's a path between the entry door and all enemies
		//Check if there's a path between the entry door and all treasure
		
		//For now, just return true:
		return true;
	}
	
	
	//Uses Breadth First Search to calculate a score for the safety of the rooms entry door.
	/*
    f = 1 / [ (width * height) - Numero_muros ] * SUM for all areas for each enemy TODO: THIS DOES NOT SEEM ACCURATE, INVESTIGATE!
	*/
	public float evaluateSafetyEnterDoor(Individual ind)
    {
		// TODO: Implement this when Map is done
//        Map map = ind.getPhenotype().getMap();
//        Map.Point2D startDoor = map.getEnterDoor();
//        int countWalls = map.getCountWalls();
//        //int totalAreas = 0;
        int minArea = 0;

//        IBFS bfs = new BFS(map);
//
//        foreach(Map.Point2D enemy in map.getEnemies())
//        {
//            //totalAreas += bfs.find(Map.Point2D.toUnityVector2(startDoor), Map.Point2D.toUnityVector2(enemy)).Length;
//            int area = bfs.find(Map.Point2D.toUnityVector2(startDoor), Map.Point2D.toUnityVector2(enemy)).Length;
//            if (minArea == 0 || minArea < area)
//                minArea = area;
//        }
//
//        //Formula
//        //float safetyFitness = 1.0f / ((Game.sizeN * Game.sizeM) - countWalls);
//
//        //Prod with totalAreas
//        //safetyFitness *= totalAreas;
//        //safetyFitness *= minArea;

        return minArea;
    }
	
	
	// TODO: Uhhh... what?
	/*
    s(t,i) = min {
        max {
            0,
            d(t,j) - d(t,i) / d(t,j) + d(t,i)
        }
    }
    Where -> 1 <= j <= Nm and j != i
    
    
	 */
	public void evaluateSafetyTreasuresWithDoorsForIndividualsValid(Individual ind)
	{
		//TODO: Figure out what the hell this method does. (When Map is done!)
		
//	    Map map = ind.getPhenotype().getMap();
//	
//	    if(map.getCountEnemies() > 0)
//	    {
//	        int treasuresSize = map.getCountTreasures();
//	        int enemiesSize = map.getCountEnemies();
//	        Map.Point2D doorEnter = map.getEnterDoor();
//	        List<double> maxs = new List<double>();
//	
//	        //Pathfinder
//	        IPathFinder pathfinder = new PathFinder(map);
//	
//	        for (int i = 0; i < treasuresSize; i++)
//	        {
//	            Map.Point2D treasure = map.getTreasures()[i];
//	            //enemiesSize = 0;
//	            for(int j = 0; j < enemiesSize; j++)
//	            {
//	                Map.Point2D enemy = map.getEnemies()[j];
//	
//	                //To Calculate
//	                //din = Distance in nodes
//	
//	                //Distance in nodes from treasure i to enemy j
//	                int dinTreasureToEnemy = pathfinder
//	                    .find(new Vector2(treasure.x, treasure.y), new Vector2(enemy.x, enemy.y))
//	                    .Count();
//	
//	                //Distance in nodes from treasure i to enter door
//	                int dinTreasureToStartDoor = pathfinder
//	                    .find(new Vector2(treasure.x, treasure.y), new Vector2(doorEnter.x, doorEnter.y))
//	                    .Count();
//	
//	                //Formule result
//	                /*
//	                    (dti,mj - dti,i) /
//	                    (dti,mj - dti,i)
//	                */
//	                double result = (double)
//	                    (dinTreasureToEnemy - dinTreasureToStartDoor) / 
//	                    (dinTreasureToEnemy + dinTreasureToStartDoor);
//	
//	                if (double.IsNaN(result))
//	                {
//	                    result = 0.0f;
//	                }
//	
//	                //Calculate and store max
//	                double max = System.Math.Max(0.0, result);
//	                
//	
//	                maxs.Add(max);
//	            }
//	
//	            //The safety is the min of maxs result
//	            if(maxs.Count() > 0)
//	            {
//	                double min = maxs.ToArray().Min();
//	                if (double.IsNaN(min))
//	                {
//	                    Debug.Log("Is nan!");
//	                }
//	                map.putSafetyTreasure(treasure, min);
//	            }
//	            
//	        }
//	    }
	}
	
	
	// Evaluate the fitness of an individual
	// TODO: Implement this when Map is done.
    public void evaluateValidIndividual(Individual ind)
    {
//        double fitness = 0.0f;
//        
//        
//        Map map = ind.getPhenotype().getMap();
//        
//        int tilesPassables = map.getCountPassables();
//
//        //security area (1)
//        double fitness_security_area = (((evaluateSafetyEnterDoor(ind) * 100) / tilesPassables) * 0.01);
//        map.setFitnessSafetyEnterDoor(fitness_security_area);
//        fitness_security_area -= mConfig.getSecurityArea();
//
//        //# enemies (2)
//        double[] expectedEnemiesRange = mConfig.getEnemiesQuantity();
//        double fitness_enemies_proportion = 0.0;
//        
//        if(map.getPercentEnemies() > expectedEnemiesRange[0])
//        {
//            fitness_enemies_proportion = map.getPercentEnemies() - expectedEnemiesRange[1];
//        }
//        else
//        {
//            fitness_enemies_proportion = map.getPercentEnemies() - expectedEnemiesRange[0];
//        }
//
//        //avg seg tesoros (3)
//        evaluateSafetyTreasuresWithDoorsForIndividualsValid(ind);
//        double[] safeties = map.getAllSafetyTreasures();
//        double safeties_average = calcAverage(safeties);
//        double fitness_avg_treasures_security = safeties_average - mConfig.getAvgTreasuresSecurity();
//
//
//        //# treasures (4)
//        double[] expectedTreasuresRange = mConfig.getTreasuresQuantity();
//        double fitness_treasures_proportion = 0.0;
//
//        if(map.getPercentTreasures() > expectedTreasuresRange[0])
//        {
//            fitness_treasures_proportion = map.getPercentTreasures() - expectedTreasuresRange[1];
//        }
//        else
//        {
//            fitness_treasures_proportion = map.getPercentTreasures() - expectedTreasuresRange[0];
//        }
//
//        //variance treasures security
//        double safeties_variance = calcVariance(safeties, safeties_average);
//        double expectedSafetyVariance = mConfig.getTreasuresSecurityVariance();
//
//        double fitness_treasures_security_variance = safeties_variance - expectedSafetyVariance;
//
//        //Check objects locked
//        double objectsLocked = map.countCloseWalls();
//
//        fitness =
//            (System.Math.Abs(fitness_security_area) * 0.1) +
//            (System.Math.Abs(fitness_enemies_proportion) * 0.2) +
//            (System.Math.Abs(fitness_avg_treasures_security) * 0.1) +
//            (System.Math.Abs(fitness_treasures_proportion) * 0.2) +
//            (System.Math.Abs(fitness_treasures_security_variance) * 0.2) +
//            (objectsLocked * 0.2);
//
//        //set final fitness
//        ind.setFitness(fitness);
//        ind.setEvaluate(true);
    }
    
    // Evaluate the fitness of an invalid individual
    // Note: should this really be done differently from a valid individual?
    // TODO: Make the treatment of valid and invalid individuals more consistent?
    public void evaluateInvalidIndividual(Individual ind)
    {
        double fitness = evaluateTheWorstItIsAIndividual(ind);
        
        //set final fitness
        ind.setFitness(fitness);
        ind.setEvaluate(true);
    }

    // TODO: Surely this and the following method should be moved to another class (Utilities or somesuch)
    private double calcAverage(double[] numbers)
    {
        double sum = 0;
        for(double n : numbers)
        {
            sum += n;
        }

        return sum / numbers.length;
    }

    private double calcVariance(double[] numbers, double average)
    {
        double result = 0;
        for(double n : numbers)
        {
            result += (n - average) * (n - average);
        }

        return result / numbers.length;
    }
    
    
    // TODO: Implement this when Map is done
    /*
    Evaluate a invalid individual
    Return the fitness for that individual

    1 - { 
            [ (1/3) * (pathToEnemiesFail/enemiesCount) ] +
            [ (1/3) * (pathToTreasuresFail/treasuresCount) ] +
            [ (1/3) * (pathToDoorsFail/doorsCount) ]
        }
    values between 0 and 1 (include both)
	*/
	public double evaluateTheWorstItIsAIndividual(Individual ind)
	{
//	    Map map = ind.getPhenotype().getMap();
//	
//	    double enemies = (map.getCountPathToEnemiesFail() / (double)map.getCountEnemies());
//	    if (Double.isNaN(enemies)) 
//	    	enemies = 1.0;
//	    
//	    double treasures = (map.getCountPathToTreasuresFail() / (double)map.getCountTreasures());
//	    if (Double.isNaN(treasures)) 
//	    	treasures = 1.0;
//	    
//	    double doors = (map.getCountPathToDoorsFail() / (double)map.getCountDoors());
//	    
//	    if (Double.isNaN(doors)) 
//	    	doors = 1.0;
//	
//	    double weighing = (1.0f / 3.0f);
//	
//	    double result = 1 -
//	        ((weighing * enemies) +
//	        (weighing * treasures) +
//	        (weighing * doors));
//	
//	    return (result < 0)? 0 : result;
		
		//For now:
		return 0;
	}
	
	//Evaluate the entire generation
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

	// TODO: Figure out what this is for
    public void insertReadyToValidAndEvaluate()
    {
        sortPopulation(populationValid, false);

        int i = 0;
        boolean flag = false;
        for (Individual valid : readyToValid)
        {
            if (i != populationValid.size() - 1 && !flag)
            {
                evaluateValidIndividual(valid);
                populationValid.add(i, valid); // TODO: not sure about this. Investigate.
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

    // TODO: Figure out what this is for
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

    
    private void produceNextValidGeneration()
    {
        //Select progenitors for crossover
        List<Individual> progenitors = selectProgenitors(populationValid);
        //Crossover progenitors
        List<Individual> sons = crossOverBetweenProgenitors(progenitors);
        //Re-insert to population valid (Replace worst for sons)
        replaceSonsInPopulationValid(sons);
    }

    
    private void produceNextInvalidGeneration()
    {
        //Select progenitors for crossover
        List<Individual> progenitors = selectProgenitors(populationInvalid);
        //Crossover progenitors
        List<Individual> sons = crossOverBetweenProgenitors(progenitors);
        //Re-insert to population invalid (Replace worst for sons)
        replaceSonsInPopulationInvalid(sons);
    }
			
    private List<Individual> crossOverBetweenProgenitors(List<Individual> progenitors)
    {
        List<Individual> sons = new ArrayList<Individual>();
        int sizeProgenitors = progenitors.size();
        int countSons = 0;
        int sonSize = sizeProgenitors * 2;

        while (countSons < sonSize)
        {
            Individual[] offspring;
            // TODO: I think getNextInt has an exclusive upper bound, so surely this should be changed?
            offspring = progenitors.get(Game.getRanges().getNextInt(0, sizeProgenitors - 1)).reproduce(progenitors.get(Game.getRanges().getNextInt(0, sizeProgenitors - 1)));
            sons.add(offspring[0]);
            sons.add(offspring[1]);
            countSons += 2;
        }

        return sons;
    }

	//Seleccion de progenitores por TORNEO Alex: ???????????????????????????????????? TODO: Investigate this
    private List<Individual> selectProgenitors(List<Individual> population)
    {
        int countProgenitors = (int)(SON_SIZE * population.size()) / 2;
        List<Individual> progenitors = new ArrayList<Individual>();

        while(countProgenitors > 0)
        {
            int individuals_to_select = Game.getRanges().getNextInt(1, population.size());

            Individual bestProgenitor = null;
            for(int i = 0; i < individuals_to_select; i++)
            {
                int progenitorIndex = Game.getRanges().getNextInt(0, population.size() - 1);
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

        indexWorst = i;

    }

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

    //sorting population in ascending or descending order
    // TODO: Double-check I haven't fucked this up. Consider neatening this up by making Individual implement Comparable.
    private void sortPopulation(List<Individual> population, boolean ascending)
    {
        population.sort((x, y) -> (ascending ? 1 : -1) * compareFitness(x.getFitness(),y.getFitness()));
    }
    
    private int compareFitness(double a, double b){
    	if( a == b)
    		return 0;
    	if( a > b)
    		return 1;
    	return -1;
    }
	
	public void run(){
		
		//MessagesPool.add("Evolucionando...");

        int generationCount = 1;
        int generations = 10; // Why? TODO: investigate

        double avgFitness = Double.POSITIVE_INFINITY;
        double avgFitnessTarget = 0.09f; //?? Why? TODO: investigate

        try
        {
            while (generationCount <= generations)
            //while(avgFitness >= avgFitnessTarget)
            {
                String messageGeneration = "";

                evaluateGeneration();
                insertReadyToValidAndEvaluate();
                insertReadyToInvalidAndEvaluate();
                //break;

                // TODO: Sort out output messages later
//                //info population valid
//                double[] dataValid = infoGenerational(populationValid, true);
//                avgFitness = dataValid[0];
//                //info population invalid
//                double[] dataInvalid = infoGenerational(populationInvalid);
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

                produceNextValidGeneration();
                produceNextInvalidGeneration();
                generationCount++;
            }
        }
        catch(Exception ex)
        {
            //MessagesPool.add(ex.Message + "\n" + ex.Source + "\n" + ex.StackTrace);
        	ex.printStackTrace();
        }
	}
	
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

    public List<Individual> getPopulationInvalid()
    {
        return populationInvalid;
    }

    public List<Individual> getPopulationValid()
    {
        return populationValid;
    }

    public Individual getBest()
    {
        return best;
    }

    private void addValidIndividual(Individual valid)
    {
        if (populationValid.size() < POPULATION_SIZE)
        {
            populationValid.add(valid);
        }
    }

    private void addInvalidIndividual(Individual invalid)
    {
        if (populationInvalid.size() < POPULATION_SIZE)
        {
            populationInvalid.add(invalid);
        }
    }
	
}
