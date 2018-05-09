package generator.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import finder.PatternFinder;
import finder.Populator;
import finder.geometry.Polygon;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import finder.patterns.meso.Ambush;
import finder.patterns.meso.DeadEnd;
import finder.patterns.meso.GuardRoom;
import finder.patterns.meso.GuardedTreasure;
import finder.patterns.meso.TreasureRoom;
import finder.patterns.micro.Connector;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Enemy;
import finder.patterns.micro.Room;
import finder.patterns.micro.Treasure;
import game.Game;
import game.Map;
import game.MapContainer;
import game.TileTypes;
import generator.config.GeneratorConfig;
import util.Point;
import util.Util;
import util.algorithms.Node;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.AlgorithmStarted;
import util.eventrouting.events.GenerationDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.StatusMessage;

public class Algorithm extends Thread {
	private UUID id;
	private final Logger logger = LoggerFactory.getLogger(Algorithm.class);
	private GeneratorConfig config;
	
	private int populationSize; 
	private float mutationProbability;
	private float offspringSize;
	
	private List<ZoneIndividual> feasiblePopulation;
	private List<ZoneIndividual> infeasiblePopulation;
	private ZoneIndividual best;
	private List<ZoneIndividual> feasiblePool;
	private List<ZoneIndividual> infeasiblePool;
	private boolean stop = false;
	private int feasibleAmount;
	private double roomTarget;
	private double corridorTarget;

	private Map oldMap = null;
	
	private int infeasiblesMoved = 0;
	private int movedInfeasiblesKept = 0;

	private AlgorithmTypes algorithmTypes;

	public enum AlgorithmTypes
	{
		Native,
		Symmetry,
		Similarity,
		SymmetryAndSimilarity
	}
	
	public Algorithm(GeneratorConfig config){
		this.config = config;
		id = UUID.randomUUID();
		populationSize = config.getPopulationSize();
		mutationProbability = (float)config.getMutationProbability();
		offspringSize = (float)config.getOffspringSize();
		feasibleAmount = (int)((double)populationSize * config.getFeasibleProportion());
		roomTarget = config.getRoomProportion();
		corridorTarget = config.getCorridorProportion();

		// Uncomment this for silly debugging
//		System.out.println("Starting run #" + id);
		initPopulations();
	}
	public Algorithm(GeneratorConfig config, AlgorithmTypes algorithmTypes){
		this.config = config;
		this.algorithmTypes = algorithmTypes;
		if(algorithmTypes == AlgorithmTypes.Similarity)
			this.algorithmTypes = AlgorithmTypes.Native;
		if(algorithmTypes == AlgorithmTypes.SymmetryAndSimilarity)
			this.algorithmTypes = AlgorithmTypes.Symmetry;
			
		id = UUID.randomUUID();
		populationSize = config.getPopulationSize();
		mutationProbability = (float)config.getMutationProbability();
		offspringSize = (float)config.getOffspringSize();
		feasibleAmount = (int)((double)populationSize * config.getFeasibleProportion());
		roomTarget = config.getRoomProportion();
		corridorTarget = config.getCorridorProportion();

		// Uncomment this for silly debugging
//		System.out.println("Starting run #" + id);
		initPopulations();
	}
	
	/**
	 * Create an Algorithm run using mutations of a given map
	 * @param map
	 */
	public Algorithm(Map map, AlgorithmTypes algorithmTypes){
		this.config = map.getCalculatedConfig();
		this.algorithmTypes = algorithmTypes;
		map.setConfig(this.config);
		id = UUID.randomUUID();
		populationSize = config.getPopulationSize();
		mutationProbability = (float)config.getMutationProbability();
		offspringSize = (float)config.getOffspringSize();
		feasibleAmount = (int)((double)populationSize * config.getFeasibleProportion());
		roomTarget = config.getRoomProportion();
		corridorTarget = config.getCorridorProportion();

		// Uncomment this for silly debugging
//		System.out.println("Starting run #" + id);
		initPopulations(map);
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
		MapUpdate ev = new MapUpdate(best);
        ev.setID(id);
		EventRouter.getInstance().postEvent(ev);
	}
	
	

	private void initPopulations(Map map){
		broadcastStatusUpdate("Initialising...");
		oldMap = map;
				
		feasiblePool = new ArrayList<ZoneIndividual>();
		infeasiblePool = new ArrayList<ZoneIndividual>();
		feasiblePopulation = new ArrayList<ZoneIndividual>();
		infeasiblePopulation = new ArrayList<ZoneIndividual>();
		int c = 0;
		int i = 0;
		int j = 0;
		while((i + j) < populationSize){
			ZoneIndividual ind = new ZoneIndividual(map, mutationProbability);
			ind.mutateAll(0.4);
			
			if(checkZoneIndividual(ind)){
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
			
			System.out.println(c++);
		}
		
		broadcastStatusUpdate("Population generated.");
	}
	
	/**
	 * Creates lists for the valid and invalid populations and populates them with ZoneIndividuals.
	 */
	private void initPopulations(){
		broadcastStatusUpdate("Initialising...");
		
		feasiblePool = new ArrayList<ZoneIndividual>();
		infeasiblePool = new ArrayList<ZoneIndividual>();
		feasiblePopulation = new ArrayList<ZoneIndividual>();
		infeasiblePopulation = new ArrayList<ZoneIndividual>();
		
		int i = 0;
		int j = 0;
		while((i + j) < populationSize){
			ZoneIndividual ind = new ZoneIndividual(config, Game.sizeHeight * Game.sizeWidth, mutationProbability);
			ind.initialize();
			
			if(checkZoneIndividual(ind)){
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
		AlgorithmStarted as = new AlgorithmStarted();
		as.setID(id);
		EventRouter.getInstance().postEvent(as);
		
		broadcastStatusUpdate("Evolving...");
        int generations = config.getGenerations();
        
        Map map = null;

        for(int generationCount = 1; generationCount <= generations; generationCount++) {
        	if(stop)
        		return;
        	
//        	broadcastStatusUpdate("Generation " + generationCount);

        	
        	movedInfeasiblesKept = 0;
        	evaluateAndTrimPools();
        	copyPoolsToPopulations();

            double[] dataValid = infoGenerational(feasiblePopulation, true);
            
//            broadcastStatusUpdate("BEST fitness: " + best.getFitness());
            
            map = best.getPhenotype().getMap();
            //broadcastMapUpdate(map);
            
          
//        	broadcastStatusUpdate("Corridor Fitness: " + best.getCorridorFitness());
//        	broadcastStatusUpdate("Room Fitness: " + best.getRoomFitness());
//
//        	broadcastStatusUpdate("Corridors & Connectors: " + best.getCorridorArea());
//        	broadcastStatusUpdate("Passable tiles: " + best.getPhenotype().getMap().getNonWallTileCount());
//        	
//        	broadcastStatusUpdate("Infeasibles moved: " + infeasiblesMoved);
//        	broadcastStatusUpdate("Moved infeasibles kept: " + movedInfeasiblesKept);
//        	
        	breedFeasibleZoneIndividuals();
        	breedInfeasibleZoneIndividuals();
//        	
//        	
//        	//Check diversity:
//        	double distance = 0.0;
//        	for(int i = 0; i < feasiblePopulation.size(); i++){
//        		if(feasiblePopulation.get(i) != best)
//        			distance += best.getDistance(feasiblePopulation.get(i));
//        	}
//        	double averageDistance = distance / (double)(feasiblePopulation.size() - 1);
//        	broadcastStatusUpdate("Average distance from best ZoneIndividual: " + averageDistance);
//        	
//        	double passableTiles = map.getNonWallTileCount();
        	
        	//map.getPatternFinder().findMesoPatterns();
        	
        	//Data we want:
        	// Best fitness
        	// Average fitness
        	// Corridor fitness
        	// Room fitness
        	// Corridor proportion (& connector)
        	// Room proportion
        	//String generation = "" + best.getFitness() + "," + dataValid[0] + "," + best.getCorridorFitness() + "," + best.getRoomFitness() + "," + best.getCorridorArea()/passableTiles + "," + best.getRoomArea()/passableTiles + "," + best.getTreasureAndEnemyFitness();
        	//EventRouter.getInstance().postEvent(new GenerationDone(generation));
        }
        broadcastMapUpdate(map); //TODO: SOMETHING HERE
        PatternFinder finder = map.getPatternFinder();
		MapContainer result = new MapContainer();
		result.setMap(map);
		result.setMicroPatterns(finder.findMicroPatterns());
		result.setMesoPatterns(finder.findMesoPatterns());
		result.setMacroPatterns(finder.findMacroPatterns());
        AlgorithmDone ev = new AlgorithmDone(result);
        ev.setID(id);
        EventRouter.getInstance().postEvent(ev);
	}
	
	/**
	 * Evaluates the fitness of all ZoneIndividuals in pools and trims them down to the desired sizes
	 */
	private void evaluateAndTrimPools(){
        //Evaluate valid ZoneIndividuals
        for(ZoneIndividual ind : feasiblePool)
        {
            if (!ind.isEvaluated())
                evaluateFeasibleZoneIndividual(ind);
        }
        this.sortPopulation(feasiblePool, false);
        feasiblePool = feasiblePool.stream().limit(feasibleAmount).collect(Collectors.toList());
        feasiblePool.forEach(ZoneIndividual -> {if(((ZoneIndividual)ZoneIndividual).isChildOfInfeasibles()) movedInfeasiblesKept++; ZoneIndividual.setChildOfInfeasibles(false);});

        //Evaluate invalid ZoneIndividuals
        for(ZoneIndividual ind : infeasiblePool)
        {
            if (!ind.isEvaluated())
                evaluateInfeasibleZoneIndividual(ind);
        }
        this.sortPopulation(infeasiblePool, false);
        infeasiblePool = infeasiblePool.stream().limit(populationSize - feasibleAmount).collect(Collectors.toList());
	}
	
	/**
	 * Copy ZoneIndividuals from pools to populations for breeding etc.
	 */
	private void copyPoolsToPopulations(){
		feasiblePopulation.clear();
		feasiblePool.forEach(ZoneIndividual -> feasiblePopulation.add(ZoneIndividual));
		
		infeasiblePopulation.clear();
		infeasiblePool.forEach(ZoneIndividual -> infeasiblePopulation.add(ZoneIndividual));
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
	private boolean checkZoneIndividual(ZoneIndividual ind){
		Map map = ind.getPhenotype().getMap();
		return map.isFeasibleTwo();
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
    public void evaluateFeasibleZoneIndividual(ZoneIndividual ind)
    {
        Map map = ind.getPhenotype().getMap();
        PatternFinder finder = map.getPatternFinder();
        List<Enemy> enemies = new ArrayList<Enemy>();
        List<Treasure> treasures = new ArrayList<Treasure>();
        List<Corridor> corridors = new ArrayList<Corridor>();
        List<Connector> connectors = new ArrayList<Connector>();
        List<Room> rooms = new ArrayList<Room>();
        
        for (Pattern p : finder.findMicroPatterns()) {
        	if (p instanceof Enemy) {
        		enemies.add((Enemy) p);
        	} else if (p instanceof Treasure) {
        		treasures.add((Treasure) p);
        	} else if (p instanceof Corridor) {
        		corridors.add((Corridor) p);
        	} else if (p instanceof Connector) {
        		connectors.add((Connector) p);
        	} else if (p instanceof Room) {
        		rooms.add((Room) p);
        	}
        }
        
        List<DeadEnd> deadEnds = new ArrayList<DeadEnd>();
        List<TreasureRoom> treasureRooms = new ArrayList<TreasureRoom>();
        List<GuardRoom> guardRooms = new ArrayList<GuardRoom>();
        List<Ambush> ambushes = new ArrayList<Ambush>();
        List<GuardedTreasure> guardedTreasure = new ArrayList<GuardedTreasure>();
        //Ignore choke points for now
        for(CompositePattern p : finder.findMesoPatterns()){
        	if(p instanceof DeadEnd){
        		deadEnds.add((DeadEnd)p);
        	} else if (p instanceof TreasureRoom){
        		treasureRooms.add((TreasureRoom)p);
        	} else if (p instanceof GuardRoom){
        		guardRooms.add((GuardRoom)p);
        	} else if (p instanceof Ambush){
        		ambushes.add((Ambush)p);
        	} else if (p instanceof GuardedTreasure){
        		guardedTreasure.add((GuardedTreasure)p);
        	}
        	
        }
        
        
        double microPatternWeight = 0.9;
        double mesoPatternWeight = 0.1;
        
        
        //Door Fitness - don't care about this for now
        double doorFitness = 1.0f;
        
        //Entrance Fitness
        double entranceFitness = 1.0;
        
    	for(Pattern p : enemies){
    		entranceFitness -= p.getQuality();
    	}
        
        //Enemy Fitness
        double enemyFitness = 1.0;
    	for(Pattern p : enemies){
    		enemyFitness -= p.getQuality();
    	}
        
        //Treasure Fitness
        double treasureFitness = 1.0;
    	for(Pattern p : treasures){
    		treasureFitness -= p.getQuality();
    	}
        

        double treasureAndEnemyFitness = 0.0 * doorFitness + 0.2 * entranceFitness + 0.4 * enemyFitness + 0.4 * treasureFitness;
    	
        
    	//Corridor fitness
    	double passableTiles = map.getNonWallTileCount();
    	double corridorArea = 0;	
    	double rawCorridorArea = 0;
    	for(Pattern p : corridors){
    		rawCorridorArea += ((Polygon)p.getGeometry()).getArea();
    		
    		double mesoContribution = 0.0;
    		for(DeadEnd de : deadEnds){
    			if(de.getPatterns().contains(p)){
    				mesoContribution = de.getQuality();
    				//System.out.println(mesoContribution);
    			}
    				
    		}
    		
    		corridorArea += ((Polygon)p.getGeometry()).getArea() * (p.getQuality()*microPatternWeight +mesoContribution*mesoPatternWeight);
    		
    	}
    	double corridorFitness = corridorArea/passableTiles;
    	corridorFitness = 1 - Math.abs(corridorFitness - corridorTarget)/Math.max(corridorTarget, 1.0 - corridorTarget);
    	
    	//Room fitness
    	double roomArea = 0;
    	double rawRoomArea = 0;
    	
    	//Room fitness
    	for(Pattern p : rooms){
    		rawRoomArea += ((Polygon)p.getGeometry()).getArea();
    		double mesoContribution = 0.0;
    		for(DeadEnd de : deadEnds){
    			if(de.getPatterns().contains(p)){
    				mesoContribution +=de.getQuality();
    			}	
    		}
    		
    		for(TreasureRoom t : treasureRooms){
    			if(t.getPatterns().contains(p)){
    				mesoContribution += t.getQuality();
    			}
    		}
    		for(GuardRoom g : guardRooms){
    			if(g.getPatterns().contains(p)){
    				mesoContribution += g.getQuality();
    			}
    		}
    		for(Ambush a : ambushes){
    			if(a.getPatterns().contains(p)){
    				mesoContribution += a.getQuality();
    			}
    		}
    		for(GuardedTreasure gt: guardedTreasure){
    			if(gt.getPatterns().contains(p)){
    				mesoContribution += gt.getQuality();
    			}
    		}
    		
    		if(mesoContribution > 1)
    			mesoContribution = 1;
    		
    		roomArea += ((Polygon)p.getGeometry()).getArea() * (p.getQuality()*microPatternWeight + mesoContribution * mesoPatternWeight);
    	}
    	
    	double roomFitness = roomArea/passableTiles;
    	roomFitness = 1 - Math.abs(roomFitness - roomTarget)/Math.max(roomTarget, 1.0 - roomTarget);

    	// Similarity Fitness 
    	double similarityFitness = 1.0;
    	if(algorithmTypes == AlgorithmTypes.Similarity ||
    			algorithmTypes == AlgorithmTypes.SymmetryAndSimilarity)
    	{
        	similarityFitness = evaluateSimilarityFitnessValue(oldMap, map, 0.95);    		
    	}
    	// Symmetry Fitness
    	double symmetricFitnessValue = 1.0;
    	if(algorithmTypes == AlgorithmTypes.Symmetry ||
    			algorithmTypes == AlgorithmTypes.SymmetryAndSimilarity)
    	{
    		symmetricFitnessValue = evaluateSymmetryFitnessValue(map);
    	}
    	
    	//Total fitness
    	double fitness = ((0.35 * treasureAndEnemyFitness
    			+  0.35 * (0.3 * roomFitness + 0.7 * corridorFitness) + (0.3 * symmetricFitnessValue))
    			* similarityFitness);  	
    	
    	
        //set final fitness
        ind.setFitness(fitness);
        ind.setTreasureAndEnemyFitness(treasureAndEnemyFitness);
        ind.setRoomFitness(roomFitness);
    	ind.setCorridorFitness(corridorFitness);
    	ind.setRoomArea(rawRoomArea);
    	ind.setCorridorArea(rawCorridorArea);
        ind.setEvaluate(true);
    }
    
    /**
     * Evaluates the percent similarity between the old map with the new ZoneIndividual map and calculates with the ideal percent to give the fitness function
     * 
     * double procentSimilar = similarTiles / totalTiles;
     * 
     * double similarityFitness = procentSimilar / idealProcentSimilarity;
     * 
     * OR (depends on if the procentSimilar is over or under idealProcentSimilarity)
     * 
     * double similarityFitness = (1 - procentSimilar) / (1 - idealProcentSimilarity);   
     * 
     * @param oldMap the map that the new generations take their values from
     * @param newMap the newly created ZoneIndividual map
     * @param idealProcentSimilarity determines how much similar the two maps should be to be ideal.
     */
	private double evaluateSimilarityFitnessValue(Map oldMap, Map newMap, double idealProcentSimilarity)
    {
    	int[][] oldMatrix = oldMap.toMatrix();
    	int[][] newMatrix = newMap.toMatrix();
    	double totalTiles = oldMap.getColCount() * oldMap.getRowCount();
    	double similarTiles = totalTiles;
    	
    	// Calculates how many tiles that are similar between the two maps
    	for(int i = 0; i < oldMap.getColCount(); ++i)
    	{
    		for(int j = 0; j < oldMap.getRowCount(); ++j)
    		{
    			switch (oldMatrix[i][j])
    			{
	    			case 1: // Just walls. Checking if both maps have a wall in the same place.
	        			if(newMatrix[i][j] != 1)
	        			{
	        				similarTiles--;
	        			}
	        			break;
        			default: // Every other floor tile. Checking if that there is no wall.
        				if(newMatrix[i][j] == 1)
	        			{
	        				similarTiles--;
	        			}
        				break;
    			}
    		}
    	}
    	double procentSimilar = similarTiles / totalTiles;
    	
    	// Calculates the simularityFitness with the idealProcentSimilarity to be able to control how much they change
    	double similarityFitness = 1.0;    	
    	if(procentSimilar < idealProcentSimilarity)
		{
    		similarityFitness = procentSimilar / idealProcentSimilarity;
		}
    	else
    	{
    		similarityFitness = (1 - procentSimilar) / (1 - idealProcentSimilarity);    	
    	}
    	return similarityFitness;
    }
	
	/**
     * Evaluates the how much symmetry in the map. It is done four times, Horizontal, Vertical, Frontslash Diagonal and Backslash Diagonal. All passing through the middle of the map
     *
     * @param Map the map that is evaluated
     */
    private double evaluateSymmetryFitnessValue(Map map)
    {
    	int rowCounter = map.getRowCount();
    	int colCounter = map.getColCount();
    	int totalWalls = map.getWallCount();
    	int[][] mapMatrix = map.toMatrix();
    	
    	
    	// Vertical Symmetry Check
    	int middlePoint = rowCounter / 2;
    	int identicalVerticalSplit = 0;
    	for(int i = 0; i < middlePoint; ++i)
    	{
    		for(int j = 0; j < colCounter; ++j)
    		{
    			if(mapMatrix[i][j] == 1 && mapMatrix[rowCounter - 1 - i][j] == 1)
    			{
    				identicalVerticalSplit += 2;
    			}
    		}
    	}
    	
    	// Horizontal Symmetry Check
    	middlePoint = colCounter / 2;
    	int identicalHorizontalSplit = 0;
    	for(int i = 0; i < rowCounter; ++i)
    	{
    		for(int j = 0; j < middlePoint; ++j)
    		{
    			if(mapMatrix[i][j] == 1 && mapMatrix[i][colCounter - 1 - j] == 1)
    			{
    				identicalHorizontalSplit += 2;
    			}
    		}
    	}

    	// Frontslash Diagonal Symmetry Check
    	int identicalFrontslashDiagonalSplit = 0;
    	double k = colCounter / rowCounter;
    	for(int i = 0; i < rowCounter; ++i)
    	{
    		middlePoint = (int)(k * i);
    		for(int j = 0; j < middlePoint; ++j)
    		{
    			if(mapMatrix[i][j] == 1 && mapMatrix[colCounter - 1 - i][rowCounter - 1 - j] == 1)
    			{
    				identicalFrontslashDiagonalSplit += 2;
    			}
    		}
    	}
    	
    	// Backslash Diagonal Symmetry Check
    	int identicalBackslashDiagonalSplit = 0;
    	k = colCounter / rowCounter;
    	for(int i = 0; i < rowCounter; ++i)
    	{
    		middlePoint = (int)(k * i);
    		for(int j = 0; j < middlePoint; ++j)
    		{
    			if(mapMatrix[i][j] == 1 && mapMatrix[colCounter - 1 - i][rowCounter - 1 - j] == 1)
    			{
    				identicalBackslashDiagonalSplit += 2;
    			}
    		}
    	}
    	
    	// Find the highest symmetry
    	int highestSymmetric = 0;
    	highestSymmetric = highestSymmetric < identicalVerticalSplit ? identicalVerticalSplit : highestSymmetric;
    	highestSymmetric = highestSymmetric < identicalHorizontalSplit ? identicalHorizontalSplit : highestSymmetric;
    	highestSymmetric = highestSymmetric < identicalFrontslashDiagonalSplit ? identicalFrontslashDiagonalSplit : highestSymmetric;
    	highestSymmetric = highestSymmetric < identicalBackslashDiagonalSplit ? identicalBackslashDiagonalSplit : highestSymmetric;
    	
    	double symmetricFitness = (double)highestSymmetric / (double)totalWalls;
    	
		//logger.info("rowCounter " + rowCounter + " colCounter " + colCounter + " middlePoint " + middlePoint + " highestSymmetric " + highestSymmetric + " totalWalls " + totalWalls + " symmetricFitness " + symmetricFitness);
    	
		return symmetricFitness;
    }

    /**
     * Evaluates an invalid ZoneIndividual's fitness according the following formula:
     * 
     * fitness = 1 - ((1/3) * (pathToEnemiesFail/enemiesCount) +
     *				  (1/3) * (pathToTreasuresFail/treasuresCount) +
     *                (1/3) * (pathToDoorsFail/doorsCount))
     * 
     * @param ind The invalid ZoneIndividual to evaluate
     */
	public void evaluateInfeasibleZoneIndividual(ZoneIndividual ind)
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
        //Evaluate valid ZoneIndividuals
        for(ZoneIndividual ind : feasiblePopulation)
        {
            if (!ind.isEvaluated())
                evaluateFeasibleZoneIndividual(ind);
        }

        //Evaluate invalid ZoneIndividuals
        for(ZoneIndividual ind : infeasiblePopulation)
        {
            if (!ind.isEvaluated())
                evaluateInfeasibleZoneIndividual(ind);
        }
    }

    /**
     * Produces a new valid generation according to the following procedure:
     *  1. Select ZoneIndividuals from the valid population to breed
     *  2. Crossover these ZoneIndividuals
     *  3. Add them back into the population
     */
    private void breedFeasibleZoneIndividuals()
    {
        //Select parents for crossover
        List<ZoneIndividual> parents = tournamentSelection(feasiblePopulation);
        //Crossover parents
        List<ZoneIndividual> children = crossOverBetweenProgenitors(parents);
        //Assign to a pool based on feasibility
        assignToPool(children, false);
    }

    /**
     * Produces a new invalid generation according to the following procedure:
     *  1. Select ZoneIndividuals from the invalid population to breed
     *  2. Crossover these ZoneIndividuals
     *  3. Add them back into the population
     */
    private void breedInfeasibleZoneIndividuals()
    {
        //Select parents for crossover
        List<ZoneIndividual> parents = tournamentSelection(infeasiblePopulation);
        //Crossover parents
        List<ZoneIndividual> children = crossOverBetweenProgenitors(parents);
        //Assign to a pool based on feasibility
        infeasiblesMoved = 0;
        assignToPool(children, true);
    }
			
    /**
     * Crossover 
     * 
     * @param progenitors A List of ZoneIndividuals to be reproduced
     * @return A List of ZoneIndividuals
     */
    private List<ZoneIndividual> crossOverBetweenProgenitors(List<ZoneIndividual> progenitors)
    {
        List<ZoneIndividual> sons = new ArrayList<ZoneIndividual>();
        int sizeProgenitors = progenitors.size();
        int countSons = 0;
        int sonSize = sizeProgenitors * 2;

        while (countSons < sonSize)
        {
            ZoneIndividual[] offspring = progenitors.get(Util.getNextInt(0, sizeProgenitors)).twoPointCrossover(progenitors.get(Util.getNextInt(0, sizeProgenitors)));
            sons.addAll(Arrays.asList(offspring));
            countSons += 2;
        }

        return sons;
    }

    
    /**
     * Selects parents from a population using (deterministic) tournament selection - i.e. the winner is always the ZoneIndividual with the "best" fitness.
     * See: https://en.wikipedia.org/wiki/Tournament_selection
     * 
     * @param population A whole population of ZoneIndividuals
     * @return A list of chosen progenitors
     */
    private List<ZoneIndividual> tournamentSelection(List<ZoneIndividual> population)
    { 
        List<ZoneIndividual> parents = new ArrayList<ZoneIndividual>();
        int numberOfParents = (int)(offspringSize * population.size()) / 2;

        while(parents.size() < numberOfParents)
        {
        	//Select at least one ZoneIndividual to "fight" in the tournament
            int tournamentSize = Util.getNextInt(1, population.size());

            ZoneIndividual winner = null;
            for(int i = 0; i < tournamentSize; i++)
            {
                int progenitorIndex = Util.getNextInt(0, population.size());
                ZoneIndividual ZoneIndividual = population.get(progenitorIndex);

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
    
    /**
     * Selects parents by fitness proportionate selection.
     * See: https://en.wikipedia.org/wiki/Fitness_proportionate_selection
     * Currently allows duplicates, is this wise?
     * 
     * @param population
     * @return
     */
    private List<ZoneIndividual> fitnessProportionateRouletteWheelSelection(List<ZoneIndividual> population){
    	sortPopulation(population, false);
    	
    	List<ZoneIndividual> parents = new ArrayList<ZoneIndividual>();
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
     * Assign the given ZoneIndividuals to either the feasible or infeasible pools
     * depending on whether or not they are feasible.
     * 
     * @param sons ZoneIndividuals to add
     * @param infeasible Are the ZoneIndividuals the offspring of infeasible parents?
     */
    private void assignToPool(List<ZoneIndividual> sons, boolean infeasible)
    {
        for (ZoneIndividual son : sons)
        {
        	if(infeasible)
        		son.setChildOfInfeasibles(true);
            if(checkZoneIndividual(son))
            {
            	if(infeasible)
            		infeasiblesMoved++;
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
     * @param population A List of ZoneIndividuals to sort
     * @param ascending true for ascending order, false for descending
     */
    private void sortPopulation(List<ZoneIndividual> population, boolean ascending)
    {
        population.sort((x, y) -> (ascending ? 1 : -1) * Double.compare(x.getFitness(),y.getFitness()));
    }
 	
    /**
     * Calculates some statistics about a population and (optionally) saves the "best" ZoneIndividual.
     * 
     * @param population The population to analyse
     * @param saveBest Should the best ZoneIndividual be saved? True should only be used for the valid population
     * @return An array of doubles. Index 0 contain the average fitness. Index 1 contains the minimum fitness. Index 2 contains the maximum fitness.
     */
	private double[] infoGenerational(List<ZoneIndividual> population, boolean saveBest) //default for saveBest was false
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
            }
            if (currFitness > maxFitness)
            {
                maxFitness = currFitness;
                if (saveBest)
                    best = population.get(i);
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
	 * Add an ZoneIndividual to the valid population if the population size is less than POPULATION_SIZE.
	 * 
	 * @param valid A valid ZoneIndividual.
	 */
    private void addValidZoneIndividual(ZoneIndividual valid)
    {
        if (feasiblePopulation.size() < populationSize)
        {
            feasiblePopulation.add(valid);
        }
    }

	/**
	 * Add an ZoneIndividual to the invalid population if the population size is less than POPULATION_SIZE.
	 * 
	 * @param invalid An invalid ZoneIndividual.
	 */
    private void addInvalidZoneIndividual(ZoneIndividual invalid)
    {
        if (infeasiblePopulation.size() < populationSize)
        {
            infeasiblePopulation.add(invalid);
        }
    }
}