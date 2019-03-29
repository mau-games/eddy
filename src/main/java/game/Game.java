package game;

import java.util.ArrayList;
import java.util.List;

import org.omg.Messaging.SyncScopeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generator.algorithm.Algorithm;
import generator.algorithm.Algorithm.AlgorithmTypes;
import generator.algorithm.MAPElites.MAPEliteAlgorithm;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.config.GeneratorConfig;
import util.Point;
import util.Util;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.AlgorithmStarted;
import util.eventrouting.events.BatchDone;
import util.eventrouting.events.RenderingDone;
import util.eventrouting.events.RequestSuggestionsView;
import util.eventrouting.events.Start;
import util.eventrouting.events.StartBatch;
import util.eventrouting.events.StartGA_MAPE;
import util.eventrouting.events.StartMapMutate;
import util.eventrouting.events.Stop;
import util.eventrouting.events.SuggestedMapsDone;

public class Game implements Listener{
	private final Logger logger = LoggerFactory.getLogger(Game.class);

	private ApplicationConfig config;
	private List<Algorithm> runs = new ArrayList<Algorithm>();
	private int batchRunsLeft = 0;
	private int batchRunsStillToFinish = 0;
	private boolean batch = false;
	private String batchConfig = "";
	private static final int batchThreads = 8;
	
	public static int defaultWidth = 11;
	public static int defaultHeight = 11;
//	public static int defaultMaxDoors = 4;
	

    public enum MapMutationType {
    	Preserving,
    	OriginalConfig,
    	ComputedConfig
    }
    
    public enum PossibleGAs
    {
    	FI_2POP,
    	MAP_ELITES,
    	CVT_MAP_ELITES
    }

	public Game() {
		

		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}

		readConfiguration();

		EventRouter.getInstance().registerListener(this, new StartGA_MAPE(null, null));
        EventRouter.getInstance().registerListener(this, new Start());
        EventRouter.getInstance().registerListener(this, new StartMapMutate(null));
        EventRouter.getInstance().registerListener(this, new Stop());
        EventRouter.getInstance().registerListener(this, new AlgorithmDone(null, null));
        EventRouter.getInstance().registerListener(this, new RenderingDone());
        EventRouter.getInstance().registerListener(this, new StartBatch());
        EventRouter.getInstance().registerListener(this, new RequestSuggestionsView(null, 0));
	}

    private void RunMAPElites(Room room,  MAPEDimensionFXML[] dimensions)
    {
    	Algorithm ga = new MAPEliteAlgorithm(room, AlgorithmTypes.Native);
		runs.add(ga);
		((MAPEliteAlgorithm)ga).initPopulations(room, dimensions);
		ga.start();
    }

    private void mutateFromMap(Room room, int mutations, MapMutationType mutationType, AlgorithmTypes algorithmType, boolean randomise){

    	
    	System.out.println("LETS CREATE!, mutation TYPE: " + mutationType + ", algorithmTypes: " + algorithmType);

//    	mutationType = MapMutationType.ComputedConfig;
//    	randomise = false;
		for(int i = 0; i < mutations; i++){
			switch(mutationType){
			case ComputedConfig:
			{
				GeneratorConfig gc = room.getCalculatedConfig();
				if(randomise)
					gc.mutate();
				Algorithm ga = new Algorithm(room, gc, algorithmType);
				runs.add(ga);
				ga.initPopulations();
				ga.start();
				break;
			}
			case OriginalConfig:
			{
				GeneratorConfig gc = new GeneratorConfig(room.getConfig());
				if(randomise)
					gc.mutate();
				Algorithm ga = new Algorithm(room, gc, algorithmType);
				runs.add(ga);
				ga.initPopulations();
				ga.start();
				break;
			}
			case Preserving:
			{
				Algorithm ga = new Algorithm(room, algorithmType);
				runs.add(ga);
				ga.initPopulations(room);
				ga.start();
				break;
			}
			default:
				break;
			}
		}

	}

	/**
	 *  Kicks the algorithm into action.
	 */
	private void startAll(int runCount, MapContainer container)
	{
		Algorithm geneticAlgorithm = null;

		List<String> configs = new ArrayList<String>();
		
		if(Math.random() < 0.5)
			configs.add("config/bendycorridors.json");
		else
			configs.add("config/bendycorridors_nodeadends.json");
		if(Math.random() < 0.5)
			configs.add("config/straightcorridors.json");
		else
			configs.add("config/straightcorridors_nodeadends.json");
		if(Math.random() < 0.5)
			configs.add("config/smallrooms.json");
		else
			configs.add("config/smallrooms_nodeadends.json");
		configs.add("config/mediumrooms.json");
		configs.add("config/bigrooms.json");
		configs.add("config/roomsandcorridorssquare.json");
		for(int i = 0; i < runCount; i++){
			String c = "config/generator_config.json";
			if(!configs.isEmpty())
				c = configs.remove(Util.getNextInt(0, configs.size()));

			try {
				geneticAlgorithm = new Algorithm(container.getMap(), new GeneratorConfig(c)); //TODO: You need to send the container here (the room)
				geneticAlgorithm.initPopulations();
				runs.add(geneticAlgorithm);
				geneticAlgorithm.start();
			} catch (MissingConfigurationException e) {
				logger.error("Couldn't read generator configuration file:\n" + e.getMessage());
			}
		}

	}

	//TODO: You need to pass a basic room!!!! 
	private void startBatch(String config, int size){
		batch = true;
		batchRunsLeft = size;
		batchRunsStillToFinish = size;
		batchConfig = config;

		runs.clear();

		for(int i = 0; i < batchThreads; i++){
			startBatchRun();
		}

	}

	//TODO: You need to pass a basic room!!!! 
	private void startBatchRun(){
		try {
			Algorithm geneticAlgorithm = new Algorithm(new GeneratorConfig(batchConfig));
			geneticAlgorithm.initPopulations();
			geneticAlgorithm.start();
			runs.add(geneticAlgorithm);
			batchRunsLeft--;
		} catch (MissingConfigurationException e) {
			e.printStackTrace();
		}
	}   

	//    public void batchRun(){
	//    	readConfiguration();
	//    	chooseDoorPositions();
	//    	batch = true;
	//    	runCount = 0;
	//    	batchStep();
	//    	
	//    }

	//    private void batchStep(){
	//    	
	//		EventRouter.getInstance().postEvent(new AlgorithmStarted("" + runCount));
	//		try {
	//			geneticAlgorithm = new Algorithm(new GeneratorConfig());
	//		} catch (MissingConfigurationException e) {
	//			logger.error("Couldn't read generator configuration file:\n" + e.getMessage());
	//		}
	//    	//Start the algorithm on a new thread.
	//    	geneticAlgorithm.start();
	//    	runCount++;
	//    }

	/**
	 * Stop the algorithm. Used in the case that the application window is closed.
	 */
	public void stop(){
		for(Algorithm a : runs){
			if(a.isAlive()) a.terminate();
		}
		//    	if(geneticAlgorithm != null && geneticAlgorithm.isAlive()){
		//    		geneticAlgorithm.terminate();
		//    	}
	}
	
	private void algorithmRunDone(Algorithm geneticAlgorithm)
	{
		runs.remove(geneticAlgorithm);
		
		if(runs.isEmpty())
		{
			EventRouter.getInstance().postEvent(new SuggestedMapsDone());
		}

	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if(e instanceof StartGA_MAPE)
		{
			StartGA_MAPE MAPEinfo = (StartGA_MAPE)e;

			
			RunMAPElites((Room)e.getPayload(), MAPEinfo.getDimensions());
		}
		else if(e instanceof RequestSuggestionsView){ 
			readConfiguration();
			MapContainer container = (MapContainer) e.getPayload();
			startAll(((RequestSuggestionsView) e).getNbrOfThreads(), container);

		}			
		else if (e instanceof StartMapMutate) {
			
			StartMapMutate smm = (StartMapMutate)e;
//			System.out.println("LETS CREATE!, mutation: " + smm.getMutations() + ", algorithmTypes: " + smm.getAlgorithmTypes());
			mutateFromMap((Room)e.getPayload(),smm.getMutations(),smm.getMutationType(),smm.getAlgorithmTypes(),smm.getRandomiseConfig());
		} else if (e instanceof StartBatch) {
			startBatch(((StartBatch)e).getConfig(), ((StartBatch)e).getSize());
		} else if (e instanceof Stop) {
			stop();
		} else if (e instanceof RenderingDone){
			if(batch){
				batchRunsStillToFinish--;
				if(batchRunsLeft > 0) {
					startBatchRun();
				}
				if(batchRunsStillToFinish == 0){
					EventRouter.getInstance().postEvent(new BatchDone());
				}
			}
		}
		else if(e instanceof AlgorithmDone)
		{
			algorithmRunDone(((AlgorithmDone) e).getAlgorithm());
		}
	}

	/**
	 * Reads and applies the current configuration.
	 */
	private void readConfiguration() {  
		//TODO: THIS NEEDS TO BE CHECK
//        sizeWidth = config.getDimensionM();
//        sizeHeight = config.getDimensionN();
//        doorCount = config.getDoors();

	}

}
