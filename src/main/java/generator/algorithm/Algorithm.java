package generator.algorithm;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import designerModeling.DesignerModel;
import designerModeling.archetypicalPaths.ArchetypicalPath;
import designerModeling.archetypicalPaths.SubsetPath;
import org.apache.commons.io.FileUtils;

import finder.PatternFinder;
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
import finder.patterns.micro.Boss;
import finder.patterns.micro.Chamber;
import finder.patterns.micro.Treasure;
import game.Dungeon;
import game.Room;
import game.Tile;
import game.MapContainer;
import generator.algorithm.MAPElites.Dimensions.CharacteristicSimilarityGADimension;
import generator.algorithm.MAPElites.Dimensions.SimilarityGADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.config.GeneratorConfig;
import machineLearning.PreferenceModel;
import util.Point;
import util.Util;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.AlgorithmStarted;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.NextStepSequenceExperiment;
import util.eventrouting.events.RoomEdited;
import util.eventrouting.events.SaveCurrentGeneration;
import util.eventrouting.events.StatusMessage;
import util.eventrouting.events.UpdatePreferenceModel;

import game.AlgorithmSetup;
/**
 * This class is the base class of genetic algorithms
 * It implements all the basic functionality of a FI-2POP strategies
 * -- It have lists of lists of individuals for it to be extendible to for instance, MAP-Elites, multi populations, etc.
 * 
 * @author Johan Holmberg, Malmö University
 * @author Alberto Alvarez, Malmö University
 *
 */
public class Algorithm extends Thread implements Listener {
	protected UUID id;
//	protected final Logger logger = LoggerFactory.getLogger(Algorithm.class);
	protected GeneratorConfig config;
	
	protected int populationSize; 
	protected float mutationProbability;
	protected float offspringSize;
	
	protected List<ZoneIndividual> feasiblePopulation;
	protected List<ZoneIndividual> infeasiblePopulation;
	protected ZoneIndividual best;
	protected List<ZoneIndividual> feasiblePool;
	protected List<ZoneIndividual> infeasiblePool;
	protected boolean stop = false;
	protected int feasibleAmount;
	protected double roomTarget;
	protected double corridorTarget;

	protected Room originalRoom = null;
	protected Room relativeRoom = null;
	
	protected int infeasiblesMoved = 0;
	protected int movedInfeasiblesKept = 0;

	protected AlgorithmTypes algorithmTypes;
	
	//needed info
	protected int roomWidth;
	protected int roomHeight;
	protected List<Point> roomDoorPositions;
	protected List<Tile> roomCustomTiles;
	protected Dungeon roomOwner; //-->> This probably will need to be some  type of static variable with an instance.
	
	//For the Expressive range test
	private int iterationsToPublish = 50;
	private int breedingGenerations = 5; //this relates to how many generations will it breed 
	private int realCurrentGen = 0;
	private int currentGen = 0;
	protected int iter_generations = 5000;

	public boolean room_changed = false;
	
	
//	ArrayList<Room> uniqueGeneratedRooms = new ArrayList<Room>();
	HashMap<Room, Double[]> uniqueGeneratedRooms = new HashMap<Room, Double[]>();
	HashMap<Room, Double[]> uniqueGeneratedRoomsFlush= new HashMap<Room, Double[]>();
	HashMap<Room, Double[]> uniqueGeneratedRoomsSince = new HashMap<Room, Double[]>();
	
	StringBuilder uniqueRoomsData = new StringBuilder();
	StringBuilder uniqueRoomsSinceData = new StringBuilder();
	
	private int saveIterations = 2;
	private int currentSaveStep = 0;
	
	//This is for testing the preference MODEL TODO: for fitness
	protected PreferenceModel userPreferences; //TODO: PROBABLY THIS WILL BE REPLACED for a class to calculate fitness in different manners!
	
	protected boolean save_data = false;


	//For novelty search:
	ArrayList<ZoneIndividual> novelty_archive = new ArrayList<ZoneIndividual>();
	private int l = 5;



	public enum AlgorithmTypes //TODO: This needs to change
	{
		Native,
		Symmetry,
		Similarity,
		SymmetryAndSimilarity
	}
	
	public Algorithm()
	{
		//I do nothing! :D 
	}
	
	public Algorithm(GeneratorConfig config) //Called by the BATCH RUN!
	{
		//TODO: I have to work to fix the batch runner! I don't know how useful it can be without setting the room config values (Doors and sizes)
		//probably it is a lot easier to generate a room from the config and then pass it 
	}
	
	public Algorithm(Room room, GeneratorConfig config){ //This is called from the batch run and when asked for suggestions view
		
		//Set info of the original room
		this.originalRoom = room;
//		this.relativeRoom = new Room(room);
		this.relativeRoom = room;

		this.roomWidth = relativeRoom.getColCount();
		this.roomHeight = relativeRoom.getRowCount();
		this.roomDoorPositions = relativeRoom.getDoors();
		this.roomOwner = originalRoom.owner;


		this.config = config;
		id = UUID.randomUUID();
		populationSize = config.getPopulationSize();
		mutationProbability = (float)config.getMutationProbability();
		offspringSize = (float)config.getOffspringSize();
		feasibleAmount = (int)((double)populationSize * config.getFeasibleProportion());

		if(AlgorithmSetup.getInstance().isAdaptive()) {
			roomTarget = config.getRoomProportion();
			corridorTarget = config.getCorridorProportion();
			this.roomCustomTiles = relativeRoom.customTiles;
		}
		else
		{
			roomTarget = 0.5;
			corridorTarget = 0.5;
		}

		this.save_data = AlgorithmSetup.getInstance().getSaveData();
		this.iter_generations = AlgorithmSetup.getInstance().getITER_GENERATIONS();

		// Uncomment this for silly debugging
//		System.out.println("Starting run #" + id);
//		initPopulations();
	}
	
	public Algorithm(Room room, GeneratorConfig config, AlgorithmTypes algorithmTypes) //THIS IS THE ONE CALLED WHEN IS NOT PRESERVING
	{
		//Set info of the original room
		this.originalRoom = room;
//		this.relativeRoom = new Room(room);
		this.relativeRoom = room;

		this.roomWidth = relativeRoom.getColCount();
		this.roomHeight = relativeRoom.getRowCount();
		this.roomDoorPositions = relativeRoom.getDoors();
		this.roomOwner = originalRoom.owner;
		
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

		if(AlgorithmSetup.getInstance().isAdaptive()) {
			roomTarget = config.getRoomProportion();
			corridorTarget = config.getCorridorProportion();
			this.roomCustomTiles = relativeRoom.customTiles;
		}
		else
		{
			roomTarget = 0.5;
			corridorTarget = 0.5;
		}

		this.save_data = AlgorithmSetup.getInstance().getSaveData();
		this.iter_generations = AlgorithmSetup.getInstance().getITER_GENERATIONS();
		
		// Uncomment this for silly debugging
//		System.out.println("Starting run #" + id);
		
//		initPopulations();
	}
	
	/**
	 * Create an Algorithm run using mutations of a given map
	 * @param room
	 */
	public Algorithm(Room room, AlgorithmTypes algorithmTypes) //THIS IS CALLED WHEN WE WANT TO PRESERVE THE ROOM 
	{
		//Set info of the original room
		this.originalRoom = room;
//		this.relativeRoom = new Room(room);
		this.relativeRoom = room;

		this.roomWidth = relativeRoom.getColCount();
		this.roomHeight = relativeRoom.getRowCount();
		this.roomDoorPositions = relativeRoom.getDoors();
		this.roomOwner = originalRoom.owner;
		this.config = relativeRoom.getCalculatedConfig();

		this.algorithmTypes = algorithmTypes;
		room.setConfig(this.config);
		id = UUID.randomUUID();
//		populationSize = config.getPopulationSize();
		populationSize = 1250; //Setting same as experiments
		mutationProbability = (float)config.getMutationProbability();
		offspringSize = (float)config.getOffspringSize();
//		feasibleAmount = (int)((double)populationSize * config.getFeasibleProportion());
		feasibleAmount = 625; //Setting same as experiments

		if(AlgorithmSetup.getInstance().isAdaptive()) {
			roomTarget = config.getRoomProportion();
			corridorTarget = config.getCorridorProportion();
			this.roomCustomTiles = relativeRoom.customTiles;
		}
		else
		{
			roomTarget = 0.5;
			corridorTarget = 0.5;
		}
		
		this.save_data = AlgorithmSetup.getInstance().getSaveData();
		this.iter_generations = AlgorithmSetup.getInstance().getITER_GENERATIONS();
		
		// Uncomment this for silly debugging
//		System.out.println("Starting run #" + id);
		
//		initPopulations(room);
	}
	
	
	public void terminate(){
		stop = true;
	}
	
	/**
	 * Broadcasts a string describing the algorithm's status.
	 * 
	 * @param status Message to display.
	 */
	protected synchronized void broadcastStatusUpdate(String status){
		EventRouter.getInstance().postEvent(new StatusMessage(status));
	}
	
	/**
	 * Broadcasts the best map from the current generation.
	 * 
	 * @param best The best map from the current generation.
	 */
	protected synchronized void broadcastMapUpdate(Room best){
		MapUpdate ev = new MapUpdate(best);
        ev.setID(id);
		EventRouter.getInstance().postEvent(ev);
	}

	private void publishGeneration()
	{
		this.sortPopulation(feasiblePopulation, false, false);
//		feasiblePopulation.sort((x, y) -> (-1) * Double.compare(x.getFitness(),y.getFitness()));
		broadcastMapUpdate(feasiblePopulation.get(0).getPhenotype().getMap(
				roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner));

		//"restart" current gen!
		currentGen = 0;
	}
	

	public void initPopulations(Room room){
		broadcastStatusUpdate("Initialising...");
		
//		EventRouter.getInstance().registerListener(this, new SaveCurrentGeneration()); 
		EventRouter.getInstance().registerListener(this, new MAPEGridUpdate(null));
		EventRouter.getInstance().registerListener(this, new UpdatePreferenceModel(null));
		EventRouter.getInstance().registerListener(this, new SaveCurrentGeneration());
		EventRouter.getInstance().registerListener(this, new RoomEdited(null));
				
		feasiblePool = new ArrayList<ZoneIndividual>();
		infeasiblePool = new ArrayList<ZoneIndividual>();
		feasiblePopulation = new ArrayList<ZoneIndividual>();
		infeasiblePopulation = new ArrayList<ZoneIndividual>();
		
		//initialize the data storage variables
		uniqueRoomsData = new StringBuilder();
		uniqueRoomsSinceData = new StringBuilder();
		uniqueRoomsData.append("Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;Score;DIM X;DIM Y;STEP;Gen;Type;Room" + System.lineSeparator());
		uniqueRoomsSinceData.append("Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;Score;DIM X;DIM Y;STEP;Gen;Type;Room" + System.lineSeparator());

		int i = 0;
		int j = 0;
		while((i + j) < populationSize){
			ZoneIndividual ind = new ZoneIndividual(room, mutationProbability);
			ind.mutateAll(0.4, roomWidth, roomHeight);
			
			if(checkZoneIndividual(ind, true)){
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
	 * Creates lists for the valid and invalid populations and populates them with ZoneIndividuals.
	 */
	public void initPopulations(){
		broadcastStatusUpdate("Initialising...");
		
		feasiblePool = new ArrayList<ZoneIndividual>();
		infeasiblePool = new ArrayList<ZoneIndividual>();
		feasiblePopulation = new ArrayList<ZoneIndividual>();
		infeasiblePopulation = new ArrayList<ZoneIndividual>();
		
		int i = 0;
		int j = 0;
		while((i + j) < populationSize){
			ZoneIndividual ind = new ZoneIndividual(config, roomWidth * roomHeight, mutationProbability);
			ind.initialize();
			
			if(checkZoneIndividual(ind, true)){
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
	
	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if(e instanceof MAPEGridUpdate)
		{
//			this.dimensions = ((MAPEGridUpdate) e).getDimensions(); 
//			dimensionsChanged = true;
		}
		else if(e instanceof UpdatePreferenceModel) 
		{
			this.userPreferences = ((UpdatePreferenceModel) e).getCurrentUserModel(); 
			//TODO: PLEASE CHANGE THIS
//					RecreateCells();
//			dimensionsChanged = true;
		}
		else if(e instanceof SaveCurrentGeneration)
		{
//			storeMAPELITESXml();
		}
		else if(e instanceof RoomEdited)
		{
			originalRoom = (Room) e.getPayload();
			this.roomWidth = originalRoom.getColCount();
			this.roomHeight = originalRoom.getRowCount();
			this.roomDoorPositions = originalRoom.getDoors();
			this.roomOwner = originalRoom.owner;

			//TODO: CHECK TO ENABLE ADAPTATION AGAIN!
			if(AlgorithmSetup.getInstance().isAdaptive())
			{
				//Only if we are adapting!
//				this.relativeRoom = new Room(originalRoom);
				this.relativeRoom = originalRoom;

				this.config = relativeRoom.getCalculatedConfig();
				roomTarget = config.getRoomProportion();
				corridorTarget = config.getCorridorProportion();
				this.roomCustomTiles = relativeRoom.customTiles;
				room_changed = true;
			}
		}
	}
	
	protected void saveUniqueRoomsToFile()
	{
		String DIRECTORY= System.getProperty("user.dir") + File.separator + File.separator + "my-data" +
				File.separator + File.separator + "expressive-range" + File.separator + File.separator;
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
		

		File file = new File(DIRECTORY + "expressive_range-singleobj.csv");
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
		String DIRECTORY= System.getProperty("user.dir") + File.separator + File.separator +"my-data" +
				File.separator + File.separator + "custom-save" + File.separator + File.separator;
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
//		    uniqueRoomsData.append(dimensions[0].getDimension() + ";");
//		    uniqueRoomsData.append(dimensions[1].getDimension() + ";");
		    uniqueRoomsData.append("no;");
		    uniqueRoomsData.append("no;");
		    uniqueRoomsData.append(currentSaveStep + ";");
		    uniqueRoomsData.append(entry.getValue()[1] + ";");
//		    uniqueRoomsData.append("GR" + System.lineSeparator()); //TYPE
			uniqueRoomsData.append("GR" + ";"); //TYPE
			uniqueRoomsData.append(currentRoom.matrixToStringContinuous(false) + System.lineSeparator()); //ROOM
		}


//		File file = new File(DIRECTORY + "expressive_range-" + dimensions[0].getDimension() + "_" + dimensions[1].getDimension() + ".csv");
//		File file = new File(DIRECTORY + "custom-unique-overtime_" + id + ".csv");
		File file = new File(DIRECTORY + "expressive_range-noveltysearch_" + id + ".csv");
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
		
		for(ZoneIndividual ind: feasiblePopulation)
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
//				uniqueGeneratedRoomsFlush.put(copy, ind.getFitness());
				
			}
		}
	}

	protected void storeAnyRooms() //Only feasible
	{

		for(ZoneIndividual ind: feasiblePopulation)
		{
			Room individualRoom = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
//			Room copy = new Room(individualRoom);
			Room copy = individualRoom;
			copy.calculateAllDimensionalValues();
			copy.setSpeficidDimensionValue(DimensionTypes.SIMILARITY,
					SimilarityGADimension.calculateValueIndependently(copy, originalRoom));
			copy.setSpeficidDimensionValue(DimensionTypes.INNER_SIMILARITY,
					CharacteristicSimilarityGADimension.calculateValueIndependently(copy, originalRoom));
//			uniqueGeneratedRooms.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
			uniqueGeneratedRoomsFlush.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
//				uniqueGeneratedRoomsFlush.put(copy, ind.getFitness());
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
	
	public Room NoSaveRun() throws InterruptedException {
		Room bestRoom = null;
		
		for(int generationCount = 1; generationCount <= iter_generations; generationCount++) {
        	if(stop)
        		return bestRoom;

			if(room_changed)
			{
				ZoneIndividual ind = new ZoneIndividual(this.relativeRoom, mutationProbability);

				if(checkZoneIndividual(ind, false)){
					feasiblePool.add(ind);
				}
				else {
					infeasiblePool.add(ind);
				}

				//Now we need to add everything faktist to the pools so they all get evaluated again! (adaptive change!)
				feasiblePopulation.forEach(ZoneIndividual -> feasiblePool.add(ZoneIndividual));
				feasiblePopulation.clear();

				infeasiblePopulation.forEach(ZoneIndividual -> infeasiblePool.add(ZoneIndividual));
				infeasiblePopulation.clear();

				room_changed = false;
			}
        	
//        	broadcastStatusUpdate("Generation " + generationCount);

        	movedInfeasiblesKept = 0;
        	evaluateAndTrimPools();
        	copyPoolsToPopulations();

            double[] dataValid = infoGenerational(feasiblePopulation, true);

            bestRoom = best.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
//        	
        	breedFeasibleZoneIndividuals();
        	breedInfeasibleZoneIndividuals();
        	
        	//This is only when we want to update the current Generation
        	if(currentGen >= iterationsToPublish) 
        	{
        		//PUBLISH THE BEST!	
        		currentGen = 0;
				publishGeneration();
        	}
        	else 
        	{
        		currentGen++;
        	}

        	
        	realCurrentGen++;
		}
        	
        	return bestRoom;
	}

	//FIXME: THIS IS MISSING THE PUBLISH `GENERATION!
	public Room SaveRun()
	{
		
		Room bestRoom = null;
		
		for(int generationCount = 1; generationCount <= iter_generations; generationCount++) {
        	if(stop)
        		return bestRoom;
        	
//        	broadcastStatusUpdate("Generation " + generationCount);

			if(room_changed)
			{
				ZoneIndividual ind = new ZoneIndividual(this.relativeRoom, mutationProbability);

				if(checkZoneIndividual(ind, false)){
					feasiblePool.add(ind);
				}
				else {
					infeasiblePool.add(ind);
				}

				//Now we need to add everything faktist to the pools so they all get evaluated again! (adaptive change!)
				feasiblePopulation.forEach(ZoneIndividual -> feasiblePool.add(ZoneIndividual));
				feasiblePopulation.clear();

				infeasiblePopulation.forEach(ZoneIndividual -> infeasiblePool.add(ZoneIndividual));
				infeasiblePopulation.clear();

				room_changed = false;
			}
        	
//            storeUniqueRooms();
			//save data
			storeAnyRooms();

        	movedInfeasiblesKept = 0;
        	evaluateAndTrimPools();
        	copyPoolsToPopulations();

            double[] dataValid = infoGenerational(feasiblePopulation, true);

            bestRoom = best.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
//        	
        	breedFeasibleZoneIndividuals();
        	breedInfeasibleZoneIndividuals();
//			saveUniqueRoomsToFileAndFlush();
//			currentSaveStep++;

			//This is only when we want to update the current Generation
        	if(currentGen >= iterationsToPublish) 
        	{
        		//TODO: For next evaluation
        		saveIterations--;
        		
        		//Uncomment to save everytime we publish
        		if(saveIterations == 0)
        		{
//        			System.out.println("NEXT");
        			saveIterations=2;
        			saveUniqueRoomsToFileAndFlush();
        			currentSaveStep++;
        			EventRouter.getInstance().postEvent(new NextStepSequenceExperiment());
        			System.out.println(realCurrentGen);
        		}
				publishGeneration();
        		currentGen = 0;
        	}
        	else 
        	{
        		currentGen++;
        	}
        	
        	realCurrentGen++;
		}
        	
        	return bestRoom;
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
        currentGen = 0;
        realCurrentGen = 0;
//        generations = 5;
        Room room = null;
        
        if(save_data)
        {
        	room = SaveRun();
        }
        else
        {
			try {
				room = NoSaveRun();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

        
        System.out.println("FINISHED");

        broadcastMapUpdate(room);
        PatternFinder finder = room.getPatternFinder();
		MapContainer result = new MapContainer();
		result.setMap(room);
		result.setMicroPatterns(finder.findMicroPatterns());
		result.setMesoPatterns(finder.findMesoPatterns());
		result.setMacroPatterns(finder.findMacroPatterns());
        AlgorithmDone ev = new AlgorithmDone(result, this, config.fileName);
        ev.setID(id);
        EventRouter.getInstance().postEvent(ev);
	}
	
	/**
	 * Evaluates the fitness of all ZoneIndividuals in pools and trims them down to the desired sizes
	 */
	protected void evaluateAndTrimPools(){
        //Evaluate valid ZoneIndividuals
        for(ZoneIndividual ind : feasiblePool)
        {
//            if (!ind.isEvaluated())
                evaluateFeasibleZoneIndividual(ind);
                noveltyMetric(ind); //TODO: IMPORTANT CHECK HERE (ADAPTATION)
        }
        this.sortPopulation(feasiblePool, false, false);

        for(int i = 0; i < l; i++)
		{
//			if(feasiblePool.get(i).getNovelty() > 0.3)
				novelty_archive.add(feasiblePool.get(i));
		}

		this.sortPopulation(novelty_archive, false, false);
		novelty_archive = (ArrayList<ZoneIndividual>) novelty_archive.stream().limit(100).collect(Collectors.toList());
        feasiblePool = feasiblePool.stream().limit(feasibleAmount).collect(Collectors.toList());
        feasiblePool.forEach(ZoneIndividual -> {if(((ZoneIndividual)ZoneIndividual).isChildOfInfeasibles()) movedInfeasiblesKept++; ZoneIndividual.setChildOfInfeasibles(false);});

        //Evaluate invalid ZoneIndividuals
        for(ZoneIndividual ind : infeasiblePool)
        {
//            if (!ind.isEvaluated())
                evaluateInfeasibleZoneIndividual(ind);
        }
        this.sortPopulation(infeasiblePool, false, true);
        infeasiblePool = infeasiblePool.stream().limit(populationSize - feasibleAmount).collect(Collectors.toList());
	}
	
	/**
	 * Copy ZoneIndividuals from pools to populations for breeding etc.
	 */
	protected void copyPoolsToPopulations(){


		feasiblePopulation.clear();
		feasiblePool.forEach(ZoneIndividual -> feasiblePopulation.add(ZoneIndividual));
		this.sortPopulation(feasiblePopulation, false, false);
		feasiblePopulation = feasiblePopulation.stream().limit(feasibleAmount).collect(Collectors.toList());
//		feasiblePool.clear();
//		feasiblePopulation.forEach(ZoneIndividual -> feasiblePool.add(ZoneIndividual));
		
		infeasiblePopulation.clear();
		infeasiblePool.forEach(ZoneIndividual -> infeasiblePopulation.add(ZoneIndividual));
		this.sortPopulation(infeasiblePopulation, false, true);
		infeasiblePopulation = infeasiblePopulation.stream().limit(populationSize - feasibleAmount).collect(Collectors.toList());
//		infeasiblePool.clear();
	}
	
	/**
	 * We will also check the designer persona here? or maybe in the fitness function?
	 *
	 *
	 * Checks if an ZoneIndividual is valid (feasible), that is:
	 * 1. There exist paths between the entrance and all other doors
	 * 2. There exist paths between the entrance and all enemies
	 * 3. There exist paths between the entrance and all treasures
	 * 4. There is at least one enemy
	 * 5. There is at least one treasure
	 *
	 *
	 * @param ind The ZoneIndividual to check
	 * @return Return true if ZoneIndividual is valid, otherwise return false
    */
	protected boolean checkZoneIndividual(ZoneIndividual ind, boolean initial){
		Room room = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
//		return room.isFeasible();

		boolean feasible = false;
		boolean correct_persona = true;
		boolean intra_feasible = room.isIntraFeasible();

		//TODO: This is the old way, to check based on feasibility! (now I am trying to do it with the fitness!)
//		if(!initial && AlgorithmSetup.getInstance().isUsingDesignerPersona())
//		{
//			int target_style = DesignerModel.getInstance().designer_persona.specificArchetypicalPathCluster(this.relativeRoom);
//			correct_persona = target_style == room.room_style.current_style;
//			//if(target_style == room.room_style.current_style)
//		}

		return intra_feasible && correct_persona;
	}

	/**
	 * Calculate the distance between individuals on the provided dimensions
	 * @param individual_0
	 * @param individual_1
	 * @param dimensions
	 * @return
	 */
	private double distanceBetweenIndividuals(ZoneIndividual individual_0, ZoneIndividual individual_1, DimensionTypes... dimensions)
	{
		double distance = 0.0;
		Room to_check = individual_0.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
		Room in_archive = individual_1.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
		in_archive.calculateAllDimensionalValues();

//		for(DimensionTypes dimension : dimensions)
//		{
//			distance += Math.pow(to_check.getDimensionValue(dimension) - in_archive.getDimensionValue(dimension), 2);
//		}
//
//		distance = Math.sqrt(distance);

		for(DimensionTypes dimension : dimensions)
		{
			distance += Math.abs(to_check.getDimensionValue(dimension) - in_archive.getDimensionValue(dimension));
		}

		return distance;
	}

	/**
	 * Note: This is only for feasible individual at the moment. If we want to have FI2NS, we need to correct a bit the code so it checks infeasible populations.
	 * @param ind
	 */
	public void noveltyMetric(ZoneIndividual ind)
	{
		double novelty_metric = 0.0;
		int k_novel_neighbors = 20;

		ArrayList<Double> neighbors = new ArrayList<Double>();
//		DimensionTypes[] dimensions_to_check = new DimensionTypes[] {DimensionTypes.SYMMETRY, DimensionTypes.LENIENCY};
//		DimensionTypes[] dimensions_to_check = new DimensionTypes[] {DimensionTypes.NUMBER_MESO_PATTERN, DimensionTypes.LENIENCY};
		DimensionTypes[] dimensions_to_check = new DimensionTypes[] {DimensionTypes.NUMBER_MESO_PATTERN, DimensionTypes.SYMMETRY};
//		DimensionTypes[] dimensions_to_check = new DimensionTypes[] {DimensionTypes.SYMMETRY, DimensionTypes.LENIENCY, DimensionTypes.LINEARITY,
//																	DimensionTypes.NUMBER_MESO_PATTERN, DimensionTypes.NUMBER_PATTERNS,
//																	DimensionTypes.SIMILARITY, DimensionTypes.INNER_SIMILARITY};

		Room room = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
		PatternFinder finder = room.getPatternFinder();

		//Calculate first the behavior
		room.calculateAllDimensionalValues();

//		double novelty_score_0 = room.getDimensionValue(DimensionTypes.SYMMETRY);
//		double novelty_score_1 = room.getDimensionValue(DimensionTypes.LENIENCY);

		//Now we need to know the distance to the archive individuals and population
		for(ZoneIndividual in_archive : novelty_archive)
		{
			neighbors.add(distanceBetweenIndividuals(ind, in_archive, dimensions_to_check));
		}

		//Now we need to know the distance to the archive individuals and population
		for(ZoneIndividual in_archive : feasiblePopulation)
		{
			neighbors.add(distanceBetweenIndividuals(ind, in_archive, dimensions_to_check));
		}

		//Sort based on value
		Collections.sort(neighbors);

		if(k_novel_neighbors >= neighbors.size())
			k_novel_neighbors = neighbors.size() - 1;

		for(int k = 0; k < k_novel_neighbors; k++)
		{
			novelty_metric += neighbors.get(k);
		}

		novelty_metric /= k_novel_neighbors;

		ind.setNovelty(novelty_metric);

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
        Room room = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
		//SubsetPath target_style = DesignerModel.getInstance().designer_persona.specificArchetypicalPathCluster(this.relativeRoom);
//		DesignerModel.getInstance().designer_persona.getRoomsDesignerPersona(this.relativeRoom);
//		float distance_target = DesignerModel.getInstance(). DesignerModel.getInstance().designer_persona.specificArchetypicalPath(this.relativeRoom)

		float persona_weight = 1.0f;

		if(AlgorithmSetup.getInstance().isUsingDesignerPersona()) {
			persona_weight = ArchetypicalPath.distanceToFinalPath(
					DesignerModel.getInstance().designer_persona.specificArchetypicalPath(this.relativeRoom),
					room.room_style.current_style);
		}

        PatternFinder finder = room.getPatternFinder();
        List<Enemy> enemies = new ArrayList<Enemy>();
        List<Boss> bosses = new ArrayList<Boss>();
        List<Treasure> treasures = new ArrayList<Treasure>();
        List<Corridor> corridors = new ArrayList<Corridor>();
        List<Connector> connectors = new ArrayList<Connector>();
        List<Chamber> chambers = new ArrayList<Chamber>();

		//TODO: Temporarily changed for testing without adaptation
//		corridorTarget = 0.5;
//		roomTarget = 0.5;
//
        for (Pattern p : finder.findMicroPatterns()) {
        	if (p instanceof Enemy) {
        		enemies.add((Enemy) p);
        	} else if (p instanceof Treasure) {
        		treasures.add((Treasure) p);
        	} else if (p instanceof Corridor) {
        		corridors.add((Corridor) p);
        	} else if (p instanceof Connector) {
        		connectors.add((Connector) p);
        	} else if (p instanceof Chamber) {
        		chambers.add((Chamber) p);
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
        
        
        //TODO: Is now time to care about this :P 
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
        
    	//FIXME: THIS HAVE A LOT TO DO! mostly because the quality is not really working as it should! --> TRIPLE CHECK THIS!
    	//This is also called INVENTORIAL PATTERN FITNESS
        double treasureAndEnemyFitness = 0.0 * doorFitness + 0.2 * entranceFitness + 0.4 * enemyFitness + 0.4 * treasureFitness;
    	
        
    	//Corridor fitness
    	double passableTiles = room.getNonWallTileCount();
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
    	double corridorFitness = corridorArea/passableTiles; //This is corridor ratio (without the connector)
    	corridorFitness = 1 - Math.abs(corridorFitness - corridorTarget)/Math.max(corridorTarget, 1.0 - corridorTarget);
    	
    	//chamber fitness
    	double roomArea = 0;
    	double rawRoomArea = 0;
    	double onlyMesoPatterns = 0.0;
    	double counter = 0;
    	//Room fitness
    	for(Pattern p : chambers){
    		rawRoomArea += ((Polygon)p.getGeometry()).getArea();
    		counter += 1;
    		double mesoContribution = 0.0;
    		for(DeadEnd de : deadEnds){
						if(de.getPatterns().contains(p)){
							mesoContribution +=de.getQuality();
							onlyMesoPatterns += de.getQuality();
//    				counter += 1;
						}
    		}
    		
    		for(TreasureRoom t : treasureRooms){
    			if(t.getPatterns().contains(p)){
    				mesoContribution += t.getQuality();
    				onlyMesoPatterns += t.getQuality();
//    				counter += 1;
    			}
    		}
    		for(GuardRoom g : guardRooms){
    			if(g.getPatterns().contains(p)){
    				mesoContribution += g.getQuality();
    				onlyMesoPatterns += g.getQuality();
//    				counter += 1;
    			}
    		}
    		for(Ambush a : ambushes){
    			if(a.getPatterns().contains(p)){
    				mesoContribution += a.getQuality();
    				onlyMesoPatterns += a.getQuality();
//    				counter += 1;
    			}
    		}
    		for(GuardedTreasure gt: guardedTreasure){
    			if(gt.getPatterns().contains(p)){
    				mesoContribution += gt.getQuality();
    				onlyMesoPatterns += gt.getQuality();
//    				counter += 1;
    			}
    		}
//    		
    		if(mesoContribution > 1)
    			mesoContribution = 1;
    		
    		roomArea += ((Polygon)p.getGeometry()).getArea() * (p.getQuality()*microPatternWeight + mesoContribution * mesoPatternWeight);
    	}
    	
    	double roomFitness = roomArea/passableTiles;
    	roomFitness = 1 - Math.abs(roomFitness - roomTarget)/Math.max(roomTarget, 1.0 - roomTarget);


    	//Now that we have everything, calculate the fitness!
    	double fitness = (0.5 * treasureAndEnemyFitness)
    			+  0.5 * (0.3 * roomFitness + 0.7 * corridorFitness);

		fitness = fitness * persona_weight;
    	
    	if(userPreferences != null)
    	{
    		fitness = fitness * userPreferences.testWithPreference(room);
//    		fitness = userPreferences.testWithPreference(room);
    	}
    	
    	if(counter > 0)
    		onlyMesoPatterns = onlyMesoPatterns/counter;
    	
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
	    Room room = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
	
	    double enemies = (room.getFailedPathsToEnemies() / (double)room.getEnemyCount());
	    if (Double.isNaN(enemies)) 
	    	enemies = 1.0;
	    
	    double treasures = (room.getFailedPathsToTreasures() / (double)room.getTreasureCount());
	    if (Double.isNaN(treasures)) 
	    	treasures = 1.0;
	    
	    double doors = (room.getFailedPathsToAnotherDoor() / (double)room.getDoorCount()); //I think this should be in
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
//            if (!ind.isEvaluated())
                evaluateFeasibleZoneIndividual(ind);
        }

        //Evaluate invalid ZoneIndividuals
        for(ZoneIndividual ind : infeasiblePopulation)
        {
//            if (!ind.isEvaluated())
                evaluateInfeasibleZoneIndividual(ind);
        }
    }

    /**
     * Produces a new valid generation according to the following procedure:
     *  1. Select ZoneIndividuals from the valid population to breed
     *  2. Crossover these ZoneIndividuals
     *  3. Add them back into the population
     */
    protected void breedFeasibleZoneIndividuals()
    {
        //Select parents for crossover
        List<ZoneIndividual> parents = tournamentSelection(feasiblePopulation, false);
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
    protected void breedInfeasibleZoneIndividuals()
    {
        //Select parents for crossover
        List<ZoneIndividual> parents = tournamentSelection(infeasiblePopulation, true);
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
    protected List<ZoneIndividual> crossOverBetweenProgenitors(List<ZoneIndividual> progenitors)
    {
        List<ZoneIndividual> sons = new ArrayList<ZoneIndividual>();
        int sizeProgenitors = progenitors.size();
        int countSons = 0;
        int sonSize = sizeProgenitors * 2;

        while (countSons < sonSize)
        {
            ZoneIndividual[] offspring = progenitors.get(
            									Util.getNextInt(0, sizeProgenitors)).twoPointCrossover(progenitors.get(Util.getNextInt(0, sizeProgenitors)),
            									roomWidth, 
            									roomHeight);
            
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
    protected List<ZoneIndividual> tournamentSelection(List<ZoneIndividual> population, boolean infeasible_population)
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
				if(infeasible_population || AlgorithmSetup.getInstance().algorithm_type == AlgorithmSetup.AlgorithmType.OBJECTIVE)
				{
					if(winner == null || (winner.getFitness() < ZoneIndividual.getFitness()))
					{
						winner = ZoneIndividual;
					}
				}
				else {
					if(winner == null || (winner.getNovelty() < ZoneIndividual.getNovelty()))
					{
						winner = ZoneIndividual;
					}
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
    protected List<ZoneIndividual> fitnessProportionateRouletteWheelSelection(List<ZoneIndividual> population, boolean infeasible_population){
    	sortPopulation(population, false, infeasible_population); //FIXME: Not correct because we are not checking if it is infeasible or not!
    	
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
    protected void assignToPool(List<ZoneIndividual> sons, boolean infeasible)
    {
        for (ZoneIndividual son : sons)
        {
        	if(infeasible)
        		son.setChildOfInfeasibles(true);
            if(checkZoneIndividual(son, false))
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
    protected void sortPopulation(List<ZoneIndividual> population, boolean ascending, boolean infeasible)
    {
    	if(infeasible || AlgorithmSetup.getInstance().algorithm_type == AlgorithmSetup.AlgorithmType.OBJECTIVE)
        	population.sort((x, y) -> (ascending ? 1 : -1) * Double.compare(x.getFitness(),y.getFitness()));
    	else
			population.sort((x, y) -> (ascending ? 1 : -1) * Double.compare(x.getNovelty(),y.getNovelty()));
    }
 	
    /**
     * Calculates some statistics about a population and (optionally) saves the "best" ZoneIndividual.
     * 
     * @param population The population to analyse
     * @param saveBest Should the best ZoneIndividual be saved? True should only be used for the valid population
     * @return An array of doubles. Index 0 contain the average fitness. Index 1 contains the minimum fitness. Index 2 contains the maximum fitness.
     */
	protected double[] infoGenerational(List<ZoneIndividual> population, boolean saveBest) //default for saveBest was false
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
    protected void addValidZoneIndividual(ZoneIndividual valid)
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
    protected void addInvalidZoneIndividual(ZoneIndividual invalid)
    {
        if (infeasiblePopulation.size() < populationSize)
        {
            infeasiblePopulation.add(invalid);
        }
    }


}