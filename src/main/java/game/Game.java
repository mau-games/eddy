package game;

import java.util.ArrayList;
import java.util.List;

import org.omg.Messaging.SyncScopeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generator.algorithm.Algorithm;
import generator.algorithm.Algorithm.AlgorithmTypes;
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
import util.eventrouting.events.StartMapMutate;
import util.eventrouting.events.Stop;

public class Game implements Listener{
	private final Logger logger = LoggerFactory.getLogger(Game.class);

	private ApplicationConfig config;
	private List<Algorithm> runs = new ArrayList<Algorithm>();
	private int batchRunsLeft = 0;
	private int batchRunsStillToFinish = 0;
	private boolean batch = false;
	private String batchConfig = "";
	private static final int batchThreads = 8;

	//TODO: There must be a better way to handle these public static variables
	public static int sizeM; //Number of columns
	public static int sizeN; //Number of rows
	public static int doorCount;
	public static List<Point> doors = new ArrayList<Point>();



	public Game() {

		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}

		readConfiguration();
		chooseDoorPositions();

		EventRouter.getInstance().registerListener(this, new Start());
		EventRouter.getInstance().registerListener(this, new StartMapMutate(null));
		EventRouter.getInstance().registerListener(this, new Stop());
		//EventRouter.getInstance().registerListener(this, new AlgorithmDone(null));
		EventRouter.getInstance().registerListener(this, new RenderingDone());
		EventRouter.getInstance().registerListener(this, new StartBatch());
		EventRouter.getInstance().registerListener(this, new RequestSuggestionsView(null, 0, 0, null, 0));
	}

	/**
	 * Selects positions for between 1 and 4 doors. 
	 * The first door is the main entrance. 
	 * Doors can't be in corners.
	 */
	private void chooseDoorPositions(){
		doors.clear();
		List<Integer> walls = new ArrayList<Integer>();
		walls.add(0);
		walls.add(1);
		walls.add(2);
		walls.add(3);
		for(int i = 0; i < doorCount; i++){
			switch(walls.remove(Util.getNextInt(0, walls.size()))){
			case 0: //North
				//doors.add(new Point(Util.getNextInt(1, sizeM - 1), sizeN - 1));
				doors.add(new Point(sizeM / 2, sizeN - 1));
				break;
			case 1: //East
				//doors.add(new Point(sizeM - 1, Util.getNextInt(1, sizeN - 1)));
				doors.add(new Point(sizeM - 1, sizeN / 2));
				break;
			case 2: //South
				//doors.add(new Point(Util.getNextInt(1, sizeM - 1), 0));
				doors.add(new Point(sizeM / 2, 0));
				break;
			case 3: //West
				//doors.add(new Point(0, Util.getNextInt(1, sizeN - 1)));
				doors.add(new Point(0, sizeN / 2));
				break;
			}
		}
	}

    public enum MapMutationType {
    	Preserving,
    	OriginalConfig,
    	ComputedConfig
    }

    private void mutateFromMap(Map map, int mutations, MapMutationType mutationType, AlgorithmTypes AlgoType, boolean randomise){
    	sizeM = map.getColCount();
    	sizeN = map.getRowCount();

		for(int i = 0; i < mutations; i++){
			switch(mutationType){
			case ComputedConfig:
			{
				doors.clear();
				if (map.getNorth()) { 	//North
					doors.add(new Point(sizeM / 2, 0));
				}
				if (map.getEast()) {	//East
					doors.add(new Point(sizeM - 1, sizeN / 2));
				}
				if (map.getSouth()) {	//South
					doors.add(new Point(sizeM / 2, sizeN - 1));
				}
				if (map.getWest()) {	//West
					doors.add(new Point(0, sizeN / 2));
				}
				if (doors.isEmpty()) {
					doors.add(map.getEntrance());
					for (Point p : map.getDoors()) {
						doors.add(p);
					}
				}
				GeneratorConfig gc = map.getCalculatedConfig();
				if(randomise)
					gc.mutate();
				Algorithm ga = new Algorithm(gc, AlgoType);
				runs.add(ga);
				ga.start();
				break;
			}
			case OriginalConfig:
			{
				doors.clear();
				if (map.getNorth()) { 	//North
					doors.add(new Point(sizeM / 2, 0));
				}
				if (map.getEast()) {	//East
					doors.add(new Point(sizeM - 1, sizeN / 2));
				}
				if (map.getSouth()) {	//South
					doors.add(new Point(sizeM / 2, sizeN - 1));
				}
				if (map.getWest()) {	//West
					doors.add(new Point(0, sizeN / 2));
				}
				if (doors.isEmpty()) {
					doors.add(map.getEntrance());
					for (Point p : map.getDoors()) {
						doors.add(p);
					}
				}
				GeneratorConfig gc = new GeneratorConfig(map.getConfig());
				if(randomise)
					gc.mutate();
				Algorithm ga = new Algorithm(gc, AlgoType);
				runs.add(ga);
				ga.start();
				break;
			}
			case Preserving:
			{
				doors.clear();
				if (map.getNorth()) { 	//North
					doors.add(new Point(sizeM / 2, 0));
				}
				if (map.getEast()) {	//East
					doors.add(new Point(sizeM - 1, sizeN / 2));
				}
				if (map.getSouth()) {	//South
					doors.add(new Point(sizeM / 2, sizeN - 1));
				}
				if (map.getWest()) {	//West
					doors.add(new Point(0, sizeN / 2));
				}
				if (doors.isEmpty()) {
					doors.add(map.getEntrance());
					for (Point p : map.getDoors()) {
						doors.add(p);
					}
				}
				Algorithm ga = new Algorithm(map, AlgoType);
				runs.add(ga);
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

		reinit(container);
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
				geneticAlgorithm = new Algorithm(new GeneratorConfig(c));
				runs.add(geneticAlgorithm);
				geneticAlgorithm.start();
			} catch (MissingConfigurationException e) {
				logger.error("Couldn't read generator configuration file:\n" + e.getMessage());
			}
		}

	}

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

	private void startBatchRun(){
		try {
			Algorithm geneticAlgorithm = new Algorithm(new GeneratorConfig(batchConfig));
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
	 * Set everything back to its initial state before running the genetic algorithm
	 */
	private void reinit(MapContainer container){
		if (container == null) {
			doors.clear();
			chooseDoorPositions();
		}
		else {
			doors.clear();
			if (container.getMap().getNorth()) { 	//North
				doors.add(new Point(sizeM / 2, 0));
			}
			if (container.getMap().getEast()) {	//East
				doors.add(new Point(sizeM - 1, sizeN / 2));
			}
			if (container.getMap().getSouth()) {	//South
				doors.add(new Point(sizeM / 2, sizeN - 1));
			}
			if (container.getMap().getWest()) {	//West
				doors.add(new Point(0, sizeN / 2));
			}
		}
	}

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

	@Override
	public synchronized void ping(PCGEvent e) {
		if(e instanceof RequestSuggestionsView){ 
			readConfiguration();
			MapContainer container = (MapContainer) e.getPayload();
			doorCount = container.getMap().getNumberOfDoors();
			sizeM = 11;
			sizeN = 11;
			startAll(((RequestSuggestionsView) e).getNbrOfThreads(), container);

		}			
		else if (e instanceof StartMapMutate) {
			StartMapMutate smm = (StartMapMutate)e;
			mutateFromMap((Map)e.getPayload(),smm.getMutations(),smm.getMutationType(),smm.getAlgorithmTypes(),smm.getRandomiseConfig());
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
	}

	/**
	 * Reads and applies the current configuration.
	 */
	private void readConfiguration() {  
		sizeM = config.getDimensionM();
		sizeN = config.getDimensionN();
		doorCount = config.getDoors();
	}

}
