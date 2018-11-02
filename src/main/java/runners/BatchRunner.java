package runners;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collectors.GenerationCollector;
import collectors.MapCollector;
import collectors.RenderedMapCollector;
import game.Game;
import gui.utils.MapRenderer;
import javafx.application.Application;
import javafx.stage.Stage;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.BatchDone;
import util.eventrouting.events.StartBatch;

/**
 * This class is used to run headless dungeon generation batches
 * 
 * @author Johan Holmberg, Malmö University
 * @author Alexander Baldwin, Malmö University
 */
public class BatchRunner extends Application implements Listener {
	
	//TODO: THIS CLASS NEEDS TO BE FIXED/UPDATED TO THE NEWEST CHANGES
	final static Logger logger = LoggerFactory.getLogger(BatchRunner.class);

	private Game game;
	private MapCollector mapCollector;
	private RenderedMapCollector renderedMapCollector;
	private GenerationCollector generationCollector;
	//private String batchConfig = "config/test_batches/room0.2_corridor0.8_area9_square0.5_size0.5_length4.json";
	List<String> configsToRun = new ArrayList<String>();

	/**
	 * This is the GUI entry point.
	 * 
	 * @param args Those arguments aren't used.
	 */
	public static void main(String[] args) {
//		BatchRunner runner = new BatchRunner();
//		runner.start();
		logger.info("Starting program");
		launch(args);
	}


	@Override
	public void ping(PCGEvent e) {
		if(e instanceof BatchDone){
			if(!configsToRun.isEmpty())
			{
				String config = configsToRun.remove(0);
				String configName = config.substring(config.indexOf('/')+ 1,config.indexOf('.'));
				generationCollector.setPath("~/eddy/batches/" + configName + "/");
				mapCollector.setPath("~/eddy/batches/" + configName + "/maps/");
				renderedMapCollector.setPath("~/eddy/batches/" + configName + "/maps/rendered/");
				EventRouter.getInstance().postEvent(new StartBatch(config,100));
			}
			else
				System.exit(0);
		}
		
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Set up a new game
		game = new Game();

		// Set up a bunch of collectors
		mapCollector = new MapCollector();
		renderedMapCollector = new RenderedMapCollector();
		generationCollector = new GenerationCollector();
		
		

		// Creates a map renderer instance.
		MapRenderer.getInstance();

		configsToRun.add("config/bigrooms.json");
		configsToRun.add("config/smallrooms.json");
		configsToRun.add("config/bendycorridors.json");
		
		String config = configsToRun.remove(0);
		String configName = config.substring(config.indexOf('/')+ 1,config.indexOf('.'));
		
		generationCollector.setPath("~/eddy/batches/" + configName + "/");
		mapCollector.setPath("~/eddy/batches/" + configName + "/maps/");
		renderedMapCollector.setPath("~/eddy/batches/" + configName + "/maps/rendered/");
		
		EventRouter.getInstance().postEvent(new StartBatch(config,100));
		
		EventRouter.getInstance().registerListener(this, new BatchDone());
	}
	
	
}
