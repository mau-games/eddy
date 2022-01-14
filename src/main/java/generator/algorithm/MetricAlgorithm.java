package generator.algorithm;

import finder.PatternFinder;
import finder.geometry.Polygon;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import finder.patterns.meso.*;
import finder.patterns.micro.*;
import game.*;
import generator.algorithm.MAPElites.Dimensions.CharacteristicSimilarityGADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.MAPElites.Dimensions.SimilarityGADimension;
import generator.algorithm.MAPElites.MAPEliteAlgorithm;
import generator.config.GeneratorConfig;
import machineLearning.PreferenceModel;
import org.apache.commons.io.FileUtils;
import util.Point;
import util.Util;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * This class is the base class of genetic algorithms
 * It implements all the basic functionality of a FI-2POP strategies
 * -- It have lists of lists of individuals for it to be extendible to for instance, MAP-Elites, multi populations, etc.
 *
 * @author Alberto Alvarez, Malmö University
 *
 */
public class MetricAlgorithm extends Algorithm implements Listener {
	protected UUID id;
//	protected final Logger logger = LoggerFactory.getLogger(Algorithm.class);
	protected GeneratorConfig config;

	protected int populationSize;
	protected float mutationProbability;
	protected float offspringSize;

	protected List<MetricIndividual> feasiblePopulation;
	protected List<MetricIndividual> infeasiblePopulation;
	protected MetricIndividual best;
	protected List<MetricIndividual> feasiblePool;
	protected List<MetricIndividual> infeasiblePool;
	protected boolean stop = false;
	protected int feasibleAmount;

	//Might not be relevant!
	protected double roomTarget;
	protected double corridorTarget;

	//Might not be relevant!
	protected Room originalRoom = null;
	protected Room relativeRoom = null;

	protected int infeasiblesMoved = 0;
	protected int movedInfeasiblesKept = 0;

	protected AlgorithmTypes algorithmTypes;

	//needed info - Might not be relevant
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
	HashMap<Room, Double[]> uniqueGeneratedMetrics = new HashMap<Room, Double[]>();
	HashMap<Room, Double[]> uniqueGeneratedMetricsFlush = new HashMap<Room, Double[]>();
	HashMap<Room, Double[]> uniqueGeneratedMetricsSince = new HashMap<Room, Double[]>();

	StringBuilder uniqueMetricsData = new StringBuilder();
	StringBuilder uniqueMetricsSinceData = new StringBuilder();

	private int saveIterations = 2;
	private int currentSaveStep = 0;

	//This is for testing the preference MODEL TODO: for fitness
	protected PreferenceModel userPreferences; //TODO: PROBABLY THIS WILL BE REPLACED for a class to calculate fitness in different manners!

	protected boolean save_data = false;


	//For novelty search:
	ArrayList<MetricIndividual> novelty_archive = new ArrayList<MetricIndividual>();
	private int l = 5;

	public ArrayList<MetricExampleRooms> testExamples;

	public boolean waitUser = false;
	public boolean examples_changed = false;

	public MetricAlgorithm()
	{
		//I do nothing! :D
	}

	public MetricAlgorithm(GeneratorConfig config) //Called by the BATCH RUN!
	{
		//TODO: I have to work to fix the batch runner! I don't know how useful it can be without setting the room config values (Doors and sizes)
		//probably it is a lot easier to generate a room from the config and then pass it
	}

	public MetricAlgorithm(Room room, GeneratorConfig config){ //This is called from the batch run and when asked for suggestions view

		//Set info of the original room
		this.originalRoom = room;
		this.relativeRoom = new Room(room);

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

	public MetricAlgorithm(Room room, GeneratorConfig config, AlgorithmTypes algorithmTypes) //THIS IS THE ONE CALLED WHEN IS NOT PRESERVING
	{
		//Set info of the original room
		this.originalRoom = room;
		this.relativeRoom = new Room(room);

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
	public MetricAlgorithm(Room room, AlgorithmTypes algorithmTypes) //THIS IS CALLED WHEN WE WANT TO PRESERVE THE ROOM
	{
		//Set info of the original room
		this.originalRoom = room;
		this.relativeRoom = new Room(room);

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

	public MetricAlgorithm(ArrayList<MetricExampleRooms> input, Room room)
	{
		testExamples = input;


		//Set info of the original room
		this.originalRoom = room;
		this.relativeRoom = new Room(room);

		//This is more important for the MAP-Elites algorithm that we will have!
		this.roomWidth = relativeRoom.getColCount();
		this.roomHeight = relativeRoom.getRowCount();
		this.roomDoorPositions = relativeRoom.getDoors();
		this.roomOwner = originalRoom.owner;
		this.config = relativeRoom.getCalculatedConfig();
		room.setConfig(this.config); //check this.

		id = UUID.randomUUID();
//		populationSize = config.getPopulationSize();
//		populationSize = 500; //Setting same as experiments
//		mutationProbability = (float)config.getMutationProbability();
//		offspringSize = (float)config.getOffspringSize();
//		feasibleAmount = (int)((double)populationSize * config.getFeasibleProportion());
//		feasibleAmount = 625; //Setting same as experiments

		id = UUID.randomUUID();
//		populationSize = config.getPopulationSize();
		populationSize = 250; //Setting same as experiments
		mutationProbability = (float)config.getMutationProbability();
		mutationProbability = 0.3f;
		offspringSize = (float)config.getOffspringSize();
//		feasibleAmount = (int)((double)populationSize * config.getFeasibleProportion());
		feasibleAmount = 125; //Setting same as experiments

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
//		this.iter_generations = AlgorithmSetup.getInstance().getITER_GENERATIONS();
		this.iter_generations = 50;
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
	 * Remember that a generation in this algorithm is a set of metrics!
	 * which includes top 5 candidates from each cell! (0.0 - 1.0)
	 */
	private void publishGeneration()
	{
		this.sortPopulationMetric(feasiblePopulation, false, false);
		MetricUpdate ev = new MetricUpdate(feasiblePopulation.get(0), feasiblePopulation.get(1));
		ev.setID(id);
		EventRouter.getInstance().postEvent(ev);

		//"restart" current gen!
		currentGen = 0;
	}
	

	public void initPopulations(Room room){
		broadcastStatusUpdate("Initialising...");
		
//		EventRouter.getInstance().registerListener(this, new SaveCurrentGeneration());
		//DONT KNOW ABOUT HIS FAKTIST!
		EventRouter.getInstance().registerListener(this, new MAPEGridUpdate(null));
		EventRouter.getInstance().registerListener(this, new UpdatePreferenceModel(null));
		EventRouter.getInstance().registerListener(this, new SaveCurrentGeneration());
		EventRouter.getInstance().registerListener(this, new RoomEdited(null));
		EventRouter.getInstance().registerListener(this, new MetricContinue((MetricExampleRooms) null));

		feasiblePool = new ArrayList<MetricIndividual>();
		infeasiblePool = new ArrayList<MetricIndividual>();
		feasiblePopulation = new ArrayList<MetricIndividual>();
		infeasiblePopulation = new ArrayList<MetricIndividual>();
		
		//initialize the data storage variables
		//All of this needs to be checked and changed to things that make sense with metrics!
		uniqueMetricsData = new StringBuilder();
		uniqueMetricsSinceData = new StringBuilder();
		uniqueMetricsData.append("Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;Score;DIM X;DIM Y;STEP;Gen;Type;Room" + System.lineSeparator());
		uniqueMetricsSinceData.append("Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;Score;DIM X;DIM Y;STEP;Gen;Type;Room" + System.lineSeparator());

		int i = 0;
		int j = 0;
		while((i + j) < populationSize){
			MetricIndividual ind = new MetricIndividual(room, mutationProbability);
//			ind.mutateAll(0.4, roomWidth, roomHeight);
			
			if(checkMetricIndividual(ind)){
				if(i < feasibleAmount){
					checkMetricIndividual(ind);
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
				this.relativeRoom = new Room(originalRoom);

				this.config = relativeRoom.getCalculatedConfig();
				roomTarget = config.getRoomProportion();
				corridorTarget = config.getCorridorProportion();
				this.roomCustomTiles = relativeRoom.customTiles;
				room_changed = true;
			}
		}
		else if(e instanceof MetricContinue)
		{
			testExamples.addAll(((MetricContinue) e).new_examples);
			examples_changed = true;
			waitUser = false;
			System.out.println("LETS CONTINUE THE SEARCH!!");
		}
		else if(e instanceof StopMetricSearch)
		{
			stop = true;
			waitUser = false;
		}
	}
//
//	protected void saveUniqueRoomsToFile()
//	{
//		String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\expressive-range\\";
//		StringBuilder data = new StringBuilder();
//
//		data.append("Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;GEN;Score" + System.lineSeparator());
//
//		//Create the data:
//		for (Entry<Room, Double[]> entry : uniqueGeneratedMetrics.entrySet())
//		{
//		    Room currentRoom = entry.getKey();
//		    data.append(currentRoom.getDimensionValue(DimensionTypes.LENIENCY) + ";");
//		    data.append(currentRoom.getDimensionValue(DimensionTypes.LINEARITY) + ";");
//		    data.append(currentRoom.getDimensionValue(DimensionTypes.SIMILARITY) + ";");
//		    data.append(currentRoom.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN) + ";");
//		    data.append(currentRoom.getDimensionValue(DimensionTypes.NUMBER_PATTERNS) + ";");
//		    data.append(currentRoom.getDimensionValue(DimensionTypes.SYMMETRY) + ";");
//		    data.append(currentRoom.getDimensionValue(DimensionTypes.INNER_SIMILARITY) + ";");
//		    data.append(entry.getValue()[0] + ";");
//		    data.append(entry.getValue()[1] + ";");
//		    data.append("1.0" + System.lineSeparator());
//		}
//
//
//		File file = new File(DIRECTORY + "expressive_range-singleobj.csv");
//		try {
//			FileUtils.write(file, data, true);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	protected void saveUniqueRoomsToFileAndFlush()
//	{
////		String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\expressive-range\\";
//		String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\custom-save\\";
//		//Create the data:
//		for (Entry<Room, Double[]> entry : uniqueGeneratedMetricsFlush.entrySet())
//		{
//		    Room currentRoom = entry.getKey();
//		    uniqueMetricsData.append(currentRoom.getDimensionValue(DimensionTypes.LENIENCY) + ";");
//		    uniqueMetricsData.append(currentRoom.getDimensionValue(DimensionTypes.LINEARITY) + ";");
//		    uniqueMetricsData.append(currentRoom.getDimensionValue(DimensionTypes.SIMILARITY) + ";");
//		    uniqueMetricsData.append(currentRoom.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN) + ";");
//		    uniqueMetricsData.append(currentRoom.getDimensionValue(DimensionTypes.NUMBER_PATTERNS) + ";");
//		    uniqueMetricsData.append(currentRoom.getDimensionValue(DimensionTypes.SYMMETRY) + ";");
//		    uniqueMetricsData.append(currentRoom.getDimensionValue(DimensionTypes.INNER_SIMILARITY) + ";");
//		    uniqueMetricsData.append(entry.getValue()[0] + ";");
//		    uniqueMetricsData.append("1.0;");
////		    uniqueRoomsData.append(dimensions[0].getDimension() + ";");
////		    uniqueRoomsData.append(dimensions[1].getDimension() + ";");
//		    uniqueMetricsData.append("no;");
//		    uniqueMetricsData.append("no;");
//		    uniqueMetricsData.append(currentSaveStep + ";");
//		    uniqueMetricsData.append(entry.getValue()[1] + ";");
////		    uniqueRoomsData.append("GR" + System.lineSeparator()); //TYPE
//			uniqueMetricsData.append("GR" + ";"); //TYPE
//			uniqueMetricsData.append(currentRoom.matrixToStringContinuous(false) + System.lineSeparator()); //ROOM
//		}
//
//
////		File file = new File(DIRECTORY + "expressive_range-" + dimensions[0].getDimension() + "_" + dimensions[1].getDimension() + ".csv");
////		File file = new File(DIRECTORY + "custom-unique-overtime_" + id + ".csv");
//		File file = new File(DIRECTORY + "expressive_range-noveltysearch_" + id + ".csv");
//		try {
//			FileUtils.write(file, uniqueMetricsData, true);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		uniqueGeneratedMetricsFlush.clear();
//		uniqueMetricsData = new StringBuilder();
////		IO.saveFile(FileName, data.getSaveString(), true);
//	}
//
//	protected void storeUniqueRooms() //Only feasible
//	{
//
//		for(MetricIndividual ind: feasiblePopulation)
//		{
//			boolean unique = true;
//			Room individualRoom = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
//			for (Room key : uniqueGeneratedMetrics.keySet())
//			{
//			    if(SimilarityGADimension.sameRooms(key, individualRoom))
//			    {
//			    	unique = false;
//			    	break;
//			    }
//			}
//
//			if(unique)
//			{
//				Room copy = new Room(individualRoom);
//				copy.calculateAllDimensionalValues();
//				copy.setSpeficidDimensionValue(DimensionTypes.SIMILARITY,
//						SimilarityGADimension.calculateValueIndependently(copy, originalRoom));
//				copy.setSpeficidDimensionValue(DimensionTypes.INNER_SIMILARITY,
//						CharacteristicSimilarityGADimension.calculateValueIndependently(copy, originalRoom));
//				uniqueGeneratedMetrics.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
//				uniqueGeneratedMetricsFlush.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
////				uniqueGeneratedRoomsFlush.put(copy, ind.getFitness());
//
//			}
//		}
//	}
//
//	protected void storeAnyRooms() //Only feasible
//	{
//
//		for(MetricIndividual ind: feasiblePopulation)
//		{
//			Room individualRoom = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
////			Room copy = new Room(individualRoom);
//			Room copy = individualRoom;
//			copy.calculateAllDimensionalValues();
//			copy.setSpeficidDimensionValue(DimensionTypes.SIMILARITY,
//					SimilarityGADimension.calculateValueIndependently(copy, originalRoom));
//			copy.setSpeficidDimensionValue(DimensionTypes.INNER_SIMILARITY,
//					CharacteristicSimilarityGADimension.calculateValueIndependently(copy, originalRoom));
////			uniqueGeneratedRooms.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
//			uniqueGeneratedMetricsFlush.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
////				uniqueGeneratedRoomsFlush.put(copy, ind.getFitness());
//		}
//	}
//
//	protected void storeRoom(MetricIndividual ind ) //Only feasible
//	{
//		boolean unique = true;
//		Room individualRoom = ind.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
//		for (Room key : uniqueGeneratedMetrics.keySet())
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
//			uniqueGeneratedMetrics.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
//			uniqueGeneratedMetricsFlush.put(copy, new Double[] {ind.getFitness(), Double.valueOf(realCurrentGen)});
//		}
//
//	}
//

	public List<MetricIndividual> GenerateFreshBatch()
	{
		List<MetricIndividual> freshBatch = new ArrayList<>();

		for(int i = 0; i < populationSize; i++)
		{
			freshBatch.add(new MetricIndividual(relativeRoom, mutationProbability));
		}

		return freshBatch;
	}

	public Room NoSaveRun() throws InterruptedException {
		Room bestRoom = null;
		
		for(int generationCount = 1; generationCount <= iter_generations; generationCount++) {
        	if(stop)
        		return bestRoom;

        	if(waitUser)
        		continue;


			if(room_changed)
			{
				MetricIndividual ind = new MetricIndividual(this.relativeRoom, mutationProbability);

				if(checkMetricIndividual(ind)){
					feasiblePool.add(ind);
				}
				else {
					infeasiblePool.add(ind);
				}

				//Now we need to add everything faktist to the pools so they all get evaluated again! (adaptive change!)
				feasiblePopulation.forEach(MetricIndividual -> feasiblePool.add(MetricIndividual));
				feasiblePopulation.clear();

				infeasiblePopulation.forEach(MetricIndividual -> infeasiblePool.add(MetricIndividual));
				infeasiblePopulation.clear();

				room_changed = false;
			}

			if(examples_changed)
            {
				feasiblePool.clear();
                infeasiblePool.clear();

                //Now we need to add everything faktist to the pools so they all get evaluated again! (adaptive change!)
				ArrayList<MetricIndividual> old_inds = new ArrayList<>(feasiblePopulation);
                feasiblePopulation.clear();

				old_inds.addAll(infeasiblePopulation);
                infeasiblePopulation.clear();

                //To foster new generations!
                old_inds.addAll(GenerateFreshBatch());

                for(MetricIndividual ind : old_inds)
                {
                    if(checkMetricIndividual(ind))
                    {
                        feasiblePool.add(ind);
                    }
                    else
                    {
                        infeasiblePool.add(ind);
                    }
                }
                examples_changed = false;
            }
        	
//        	broadcastStatusUpdate("Generation " + generationCount);



			FilterPopulation(feasiblePool);
			FilterPopulation(infeasiblePool);
			FilterPopulation(feasiblePopulation);
			FilterPopulation(infeasiblePopulation);

        	movedInfeasiblesKept = 0;
        	evaluateAndTrimMetricPools();
        	copyPoolsToPopulations();
//        	
        	breedFeasibleMetricIndividuals();
        	breedInfeasibleMetricIndividuals();
        	
        	//This is only when we want to update the current Generation
        	if(currentGen >= iterationsToPublish) 
        	{
        		//PUBLISH THE BEST!	
        		currentGen = 0;
//				publishGeneration();
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
//	public Room SaveRun()
//	{
//
//		Room bestRoom = null;
//
//		for(int generationCount = 1; generationCount <= iter_generations; generationCount++) {
//        	if(stop)
//        		return bestRoom;
//
////        	broadcastStatusUpdate("Generation " + generationCount);
//
//			if(room_changed)
//			{
//				MetricIndividual ind = new MetricIndividual(this.relativeRoom, mutationProbability);
//
//				if(checkMetricIndividual(ind)){
//					feasiblePool.add(ind);
//				}
//				else {
//					infeasiblePool.add(ind);
//				}
//
//				//Now we need to add everything faktist to the pools so they all get evaluated again! (adaptive change!)
//				feasiblePopulation.forEach(MetricIndividual -> feasiblePool.add(MetricIndividual));
//				feasiblePopulation.clear();
//
//				infeasiblePopulation.forEach(MetricIndividual -> infeasiblePool.add(MetricIndividual));
//				infeasiblePopulation.clear();
//
//				room_changed = false;
//			}
//
////            storeUniqueRooms();
//			//save data
//			storeAnyRooms();
//
//        	movedInfeasiblesKept = 0;
//        	evaluateAndTrimPools();
//        	copyPoolsToPopulations();
//
//            double[] dataValid = infoGenerational(feasiblePopulation, true);
//
//            bestRoom = best.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
////
//        	breedFeasibleMetricIndividuals();
//        	breedInfeasibleMetricIndividuals();
////			saveUniqueRoomsToFileAndFlush();
////			currentSaveStep++;
//
//			//This is only when we want to update the current Generation
//        	if(currentGen >= iterationsToPublish)
//        	{
//        		//TODO: For next evaluation
//        		saveIterations--;
//
//        		//Uncomment to save everytime we publish
//        		if(saveIterations == 0)
//        		{
////        			System.out.println("NEXT");
//        			saveIterations=2;
//        			saveUniqueRoomsToFileAndFlush();
//        			currentSaveStep++;
//        			EventRouter.getInstance().postEvent(new NextStepSequenceExperiment());
//        			System.out.println(realCurrentGen);
//        		}
//				publishGeneration();
//        		currentGen = 0;
//        	}
//        	else
//        	{
//        		currentGen++;
//        	}
//
//        	realCurrentGen++;
//		}
//
//        	return bestRoom;
//	}
	
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

        while(!stop)
		{
			if(save_data)
			{
//        	room = SaveRun();
			}
			else
			{
				try {
					room = NoSaveRun();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println("DONE GENERATING!");

			MAPEliteAlgorithm ind_alg = null;
			sortPopulationMetric(feasiblePopulation, false, false);

			ind_alg = feasiblePopulation.get(0).getPhenotype().getAlgorithm(relativeRoom);
			try {
				ind_alg.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			ind_alg = feasiblePopulation.get(1).getPhenotype().getAlgorithm(relativeRoom);
			try {
				ind_alg.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			EventRouter.getInstance().postEvent(new MetricUpdate(feasiblePopulation.get(0), feasiblePopulation.get(1))); //Top 2
			waitUser = true;

			while(waitUser)
			{
				//Just waiting
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
	 * Evaluates the fitness of all MetricIndividuals in pools and trims them down to the desired sizes
	 */
	protected void evaluateAndTrimMetricPools() throws InterruptedException {

		int counter = 0;

//		MAPEliteAlgorithm ind_alg = null;
//		for(MetricIndividual ind : feasiblePool)
//		{
//			if(!ind.isEvaluated())
//			{
//				ind_alg = ind.getPhenotype().getAlgorithm(relativeRoom);
//				System.out.println(counter++ + ", " + ind.getPhenotype().algorithm_unique_code);
//				ind.setEvaluate(true);
//			}
////			ind_alg.join(); //TODO: STAYED HERE
//		}
//
//		ind_alg.join();

        //Evaluate valid MetricIndividuals
        for(MetricIndividual ind : feasiblePool)
        {
//            if (!ind.isEvaluated())
                evaluateFeasibleMetricIndividual(ind);
                noveltyMetric(ind); //TODO: IMPORTANT CHECK HERE (ADAPTATION)
        }
        this.sortPopulationMetric(feasiblePool, false, false);
//
//        for(int i = 0; i < l; i++)
//		{
////			if(feasiblePool.get(i).getNovelty() > 0.3)
//				novelty_archive.add(feasiblePool.get(i));
//		}

		this.sortPopulationMetric(novelty_archive, false, false);
		novelty_archive = (ArrayList<MetricIndividual>) novelty_archive.stream().limit(100).collect(Collectors.toList());
        feasiblePool = feasiblePool.stream().limit(feasibleAmount).collect(Collectors.toList());
        feasiblePool.forEach(MetricIndividual -> {if(((MetricIndividual)MetricIndividual).isChildOfInfeasibles()) movedInfeasiblesKept++; MetricIndividual.setChildOfInfeasibles(false);});

        //Evaluate invalid MetricIndividuals
        for(MetricIndividual ind : infeasiblePool)
        {
//            if (!ind.isEvaluated())
                evaluateInfeasibleMetricIndividual(ind);
        }
        this.sortPopulationMetric(infeasiblePool, false, true);
        infeasiblePool = infeasiblePool.stream().limit(populationSize - feasibleAmount).collect(Collectors.toList());
	}
	
	/**
	 * Copy MetricIndividuals from pools to populations for breeding etc.
	 */
	protected void copyPoolsToPopulations(){


		feasiblePopulation.clear();
		feasiblePool.forEach(MetricIndividual -> feasiblePopulation.add(MetricIndividual));
		this.sortPopulationMetric(feasiblePopulation, false, false);
		feasiblePopulation = feasiblePopulation.stream().limit(feasibleAmount).collect(Collectors.toList());
//		feasiblePool.clear();
//		feasiblePopulation.forEach(MetricIndividual -> feasiblePool.add(MetricIndividual));
		
		infeasiblePopulation.clear();
		infeasiblePool.forEach(MetricIndividual -> infeasiblePopulation.add(MetricIndividual));
		this.sortPopulationMetric(infeasiblePopulation, false, true);
		infeasiblePopulation = infeasiblePopulation.stream().limit(populationSize - feasibleAmount).collect(Collectors.toList());
//		infeasiblePool.clear();
	}
	
	/**
	 * Checks if an MetricIndividual is valid (feasible), that is:
	 * 1. There exist paths between the entrance and all other doors
	 * 2. There exist paths between the entrance and all enemies
	 * 3. There exist paths between the entrance and all treasures
	 * 4. There is at least one enemy
	 * 5. There is at least one treasure
	 * 
	 * @param ind The MetricIndividual to check
	 * @return Return true if MetricIndividual is valid, otherwise return false
    */
	protected boolean checkMetricIndividual(MetricIndividual ind){

//		Infeasibility is simply that the metric put the  set of iamges in the right "containers"
		return ind.infeasibilityCheck(testExamples);

	}

	/**
	 * Calculate the distance between individuals on the provided dimensions
	 * @param individual_0
	 * @param individual_1
	 * @param dimensions
	 * @return
	 */
	private double distanceBetweenIndividuals(MetricIndividual individual_0, MetricIndividual individual_1, DimensionTypes... dimensions)
	{
		double distance = 0.0;
//		Room to_check = individual_0.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
//		Room in_archive = individual_1.getPhenotype().getMap(roomWidth, roomHeight, roomDoorPositions, roomCustomTiles, roomOwner);
//		in_archive.calculateAllDimensionalValues();
//
////		for(DimensionTypes dimension : dimensions)
////		{
////			distance += Math.pow(to_check.getDimensionValue(dimension) - in_archive.getDimensionValue(dimension), 2);
////		}
////
////		distance = Math.sqrt(distance);
//
//		for(DimensionTypes dimension : dimensions)
//		{
//			distance += Math.abs(to_check.getDimensionValue(dimension) - in_archive.getDimensionValue(dimension));
//		}
//
		//fixme: This metric needs to be developed as well! what is a good novelty metric?

		return distance;
	}

	/**
	 * Note: This is only for feasible individual at the moment. If we want to have FI2NS, we need to correct a bit the code so it checks infeasible populations.
	 * @param ind
	 */
	public void noveltyMetric(MetricIndividual ind)
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


		//Fixme: missing HERE! domain dependant Calculate first the behavior
//		room.calculateAllDimensionalValues();



//		double novelty_score_0 = room.getDimensionValue(DimensionTypes.SYMMETRY);
//		double novelty_score_1 = room.getDimensionValue(DimensionTypes.LENIENCY);

		//Now we need to know the distance to the archive individuals and population
		for(MetricIndividual in_archive : novelty_archive)
		{
			neighbors.add(distanceBetweenIndividuals(ind, in_archive, dimensions_to_check));
		}

		//Now we need to know the distance to the archive individuals and population
		for(MetricIndividual in_archive : feasiblePopulation)
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
	 * Evaluates the fitness of a valid MetricIndividual using the following factors:
	 *  1. Entrance safety (how close are enemies to the entrance)
	 *  2. Proportion of tiles that are enemies
	 *  3. Average treasure safety (Are treasures closer to the door or enemies?)
	 *  4. Proportion of tiles that are treasure
	 *  5. Treasure safety variance (whatever this is!)
	 * 
	 * @param ind The valid MetricIndividual to evaluate
	 */
    public void evaluateFeasibleMetricIndividual(MetricIndividual ind)
    {
    	//fixme: needs to be domain dependant!
		double fitness = 0.0;

		for(MetricExampleRooms example : testExamples)
		{
			double score = ind.getPhenotype().createMetric().calculateMetric(example.room);

			if(example.positive)
			{
				if(score > example.metric_value)
				{
					//penalization for too high values!
					if(score > example.granularity_value.getMaxValue())
					{
						fitness += 1.0 - ((score - example.metric_value)/(1.0 - example.metric_value));
						fitness -= score - example.granularity_value.getMaxValue();
					}
					else
						fitness += 1.0 - ((score - example.metric_value)/(example.granularity_value.getMaxValue() - example.metric_value));

//				fitness += 1.0 - example.metric_value/score;
				}
				else if(score < example.metric_value)
				{
					//Penalization for too small value!
					if(score < example.granularity_value.getMinValue())
					{
						fitness += (score - 0.0)/(example.metric_value - 0.0);
						fitness -= score - example.granularity_value.getMinValue();
					}
					else
						fitness += (score - example.granularity_value.getMinValue())/(example.metric_value - example.granularity_value.getMinValue());
//				double calc = 1.0 - score/example.granularity_value.getMinValue();
//				double cal2 = 1.0 - example.granularity_value.getMinValue()/score;
//				fitness +=  example.granularity_value.getMinValue()/score;
				}
				else
					fitness += 1.0;
			}
			else //negative examples!
			{
				double remainder = 1.0 - example.metric_value;

				if(score > example.metric_value)
				{
					double min = example.metric_value;
					double max = remainder <= 0.5 ? min + min : min + remainder;
					fitness += ((score - min)/(max - min));
				}
				else if(score < example.metric_value)
				{
					double max = example.metric_value;
					double min = remainder >= 0.5 ? max - remainder : 0.0;

					fitness += 1.0 - ((score - min)/(max - min));
				}
				else {
					fitness += 0.0;
				}
			}

		}

		fitness /= testExamples.size();
    	
        //set final fitness
        ind.setFitness(fitness);
        ind.setEvaluate(true);
    }

    /**
     * Evaluates an invalid MetricIndividual's fitness according the following formula:
     * 
     * fitness = 1 - ((1/3) * (pathToEnemiesFail/enemiesCount) +
     *				  (1/3) * (pathToTreasuresFail/treasuresCount) +
     *                (1/3) * (pathToDoorsFail/doorsCount))
     * 
     * @param ind The invalid MetricIndividual to evaluate
     */
	public void evaluateInfeasibleMetricIndividual(MetricIndividual ind)
	{
		//FIXME: This needs to change

		double fitness = 0.0;

		for(MetricExampleRooms example : testExamples)
		{
			if(example.positive)
			{
				double score = ind.getPhenotype().createMetric().calculateMetric(example.room);

				if(score > example.granularity_value.getMaxValue())
					fitness += 1.0 - (score - example.granularity_value.getMaxValue());
				else if(score < example.granularity_value.getMinValue())
					fitness += 1.0 - (example.granularity_value.getMinValue() - score);
			}
			else
			{
				double score = ind.getPhenotype().createMetric().calculateMetric(example.room);

				if(score > example.granularity_value.getMaxValue())
					fitness += (score - example.granularity_value.getMaxValue());
				else if(score < example.granularity_value.getMinValue())
					fitness += (example.granularity_value.getMinValue() - score);
			}

		}

		fitness /= testExamples.size();
	    
	    //set final fitness
	    ind.setFitness(fitness);
        ind.setEvaluate(true);
	}
	
	/**
	 * Evaluate the entire generation
	 */
	public void evaluateGeneration()
    {
        //Evaluate valid MetricIndividuals
        for(MetricIndividual ind : feasiblePopulation)
        {
//            if (!ind.isEvaluated())
                evaluateFeasibleMetricIndividual(ind);
        }

        //Evaluate invalid MetricIndividuals
        for(MetricIndividual ind : infeasiblePopulation)
        {
//            if (!ind.isEvaluated())
                evaluateInfeasibleMetricIndividual(ind);
        }
    }

    protected void FilterPopulation(List<MetricIndividual> population)
	{
		for(MetricIndividual ind : population)
			ind.FilterChromosomes();

		boolean nothingToFilter = false;
		int counter = 0;
		int limit = population.size();
		int duplicates = 0;

		while(limit-- > 0)
		{
			List<MetricIndividual> to_remove = new ArrayList<>();

			if(counter >= population.size())
				counter = population.size() - 1;

			for(int i = 0; i < population.size(); i++)
			{
				if(i == counter)
					continue;

				if(population.get(counter).getGenotype().equals(population.get(i).getGenotype()))
				{
					to_remove.add(population.get(i));
				}
				else if(population.get(counter).equals(population.get(i), testExamples))
				{
					to_remove.add(population.get(i));
				}
			}
			counter++;
			duplicates += to_remove.size();
			population.removeAll(to_remove);
		}

		//Check the duplicates
		duplicates += 1;

	}

    /**
     * Produces a new valid generation according to the following procedure:
     *  1. Select MetricIndividuals from the valid population to breed
     *  2. Crossover these MetricIndividuals
     *  3. Add them back into the population
     */
    protected void breedFeasibleMetricIndividuals()
    {
        //Select parents for crossover
        List<MetricIndividual> parents = tournamentSelectionMetric(feasiblePopulation, false);
        //Crossover parents
        List<MetricIndividual> children = crossOverBetweenProgenitorsMetric(parents);

        if(children == null)
        	return;

        //Assign to a pool based on feasibility
        assignToPoolMetric(children, false);
    }

    /**
     * Produces a new invalid generation according to the following procedure:
     *  1. Select MetricIndividuals from the invalid population to breed
     *  2. Crossover these MetricIndividuals
     *  3. Add them back into the population
     */
    protected void breedInfeasibleMetricIndividuals()
    {
        //Select parents for crossover
        List<MetricIndividual> parents = tournamentSelectionMetric(infeasiblePopulation, true);
        //Crossover parents
        List<MetricIndividual> children = crossOverBetweenProgenitorsMetric(parents);
        //Assign to a pool based on feasibility
        infeasiblesMoved = 0;
        assignToPoolMetric(children, true);
    }
			
    /**
     * Crossover 
     * 
     * @param progenitors A List of MetricIndividuals to be reproduced
     * @return A List of MetricIndividuals
     */
    protected List<MetricIndividual> crossOverBetweenProgenitorsMetric(List<MetricIndividual> progenitors)
    {
    	if(progenitors == null)
    		return null;

        List<MetricIndividual> sons = new ArrayList<MetricIndividual>();
        int sizeProgenitors = progenitors.size();
        int countSons = 0;
        int sonSize = sizeProgenitors * 2;

        while (countSons < sonSize)
        {
            MetricIndividual[] offspring = progenitors.get(
            									Util.getNextInt(0, sizeProgenitors)).twoPointCrossover(
            											progenitors.get(Util.getNextInt(0, sizeProgenitors)));

            if(offspring != null)
            	sons.addAll(Arrays.asList(offspring));

            countSons += 2;
        }

        return sons;
    }

    
    /**
     * Selects parents from a population using (deterministic) tournament selection - i.e. the winner is always the MetricIndividual with the "best" fitness.
     * See: https://en.wikipedia.org/wiki/Tournament_selection
     * 
     * @param population A whole population of MetricIndividuals
     * @return A list of chosen progenitors
     */
    protected List<MetricIndividual> tournamentSelectionMetric(List<MetricIndividual> population, boolean infeasible_population)
    { 
        List<MetricIndividual> parents = new ArrayList<MetricIndividual>();
        int numberOfParents = (int)(offspringSize * population.size()) / 2;

        if(population.size() < 4)
        	return null;

        while(parents.size() < numberOfParents)
        {
        	//Select at least one MetricIndividual to "fight" in the tournament
            int tournamentSize = Util.getNextInt(1, population.size());

            MetricIndividual winner = null;
            for(int i = 0; i < tournamentSize; i++)
            {
                int progenitorIndex = Util.getNextInt(0, population.size());
                MetricIndividual MetricIndividual = population.get(progenitorIndex);

				if(winner == null || (winner.getFitness() < MetricIndividual.getFitness()))
				{
					winner = MetricIndividual;
				}

				//TODO: Dont forget about this!

//                //select the MetricIndividual with the highest fitness
//				if(infeasible_population || AlgorithmSetup.getInstance().algorithm_type == AlgorithmSetup.AlgorithmType.OBJECTIVE)
//				{
//					if(winner == null || (winner.getFitness() < MetricIndividual.getFitness()))
//					{
//						winner = MetricIndividual;
//					}
//				}
//				else {
//					if(winner == null || (winner.getNovelty() < MetricIndividual.getNovelty()))
//					{
//						winner = MetricIndividual;
//					}
//				}

            }

            parents.add(winner);
        }

        return parents;
    }

    /**
     * Assign the given MetricIndividuals to either the feasible or infeasible pools
     * depending on whether or not they are feasible.
     * 
     * @param sons MetricIndividuals to add
     * @param infeasible Are the MetricIndividuals the offspring of infeasible parents?
     */
    protected void assignToPoolMetric(List<MetricIndividual> sons, boolean infeasible)
    {
        for (MetricIndividual son : sons)
        {
        	if(infeasible)
        		son.setChildOfInfeasibles(true);
            if(checkMetricIndividual(son))
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
     * @param population A List of MetricIndividuals to sort
     * @param ascending true for ascending order, false for descending
     */
    protected void sortPopulationMetric(List<MetricIndividual> population, boolean ascending, boolean infeasible)
    {
		population.sort((x, y) -> (ascending ? 1 : -1) * Double.compare(x.getFitness(),y.getFitness()));

		//TODO: Dont forget about this!

//    	if(infeasible || AlgorithmSetup.getInstance().algorithm_type == AlgorithmSetup.AlgorithmType.OBJECTIVE)
//        	population.sort((x, y) -> (ascending ? 1 : -1) * Double.compare(x.getFitness(),y.getFitness()));
//    	else
//			population.sort((x, y) -> (ascending ? 1 : -1) * Double.compare(x.getNovelty(),y.getNovelty()));
    }


	/**
	 * Add an MetricIndividual to the valid population if the population size is less than POPULATION_SIZE.
	 * 
	 * @param valid A valid MetricIndividual.
	 */
    protected void addValidMetricIndividual(MetricIndividual valid)
    {
        if (feasiblePopulation.size() < populationSize)
        {
            feasiblePopulation.add(valid);
        }
    }

	/**
	 * Add an MetricIndividual to the invalid population if the population size is less than POPULATION_SIZE.
	 * 
	 * @param invalid An invalid MetricIndividual.
	 */
    protected void addInvalidMetricIndividual(MetricIndividual invalid)
    {
        if (infeasiblePopulation.size() < populationSize)
        {
            infeasiblePopulation.add(invalid);
        }
    }


}