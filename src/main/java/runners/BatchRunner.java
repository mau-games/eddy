package runners;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collectors.GenerationCollector;
import collectors.MapCollector;
import collectors.RenderedMapCollector;
import game.Game;
import gui.utils.MapRenderer;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;

/**
 * This class is used to run headless dungeon generation batches
 * 
 * @author Johan Holmberg, Malmö University
 * @author Alexander Baldwin, Malmö University
 */
public class BatchRunner {
	
	final static Logger logger = LoggerFactory.getLogger(BatchRunner.class);
	private static ConfigurationUtility config;

	private Game game;
	private MapCollector mapCollector;
	private RenderedMapCollector renderedMapCollector;
	private GenerationCollector generationCollector;
	private String batchConfig = "config/test_batches/room0.2_corridor0.8_area9_square0.5_size0.5_length4.json";

	/**
	 * This is the GUI entry point.
	 * 
	 * @param args Those arguments aren't used.
	 */
	public static void main(String[] args) {
		logger.info("Starting program");
		
		BatchRunner runner = new BatchRunner();
		runner.start();
	}

	public void start() {
		// Set up a new game
		game = new Game();

		// Set up a bunch of collectors
		mapCollector = new MapCollector();
		renderedMapCollector = new RenderedMapCollector();
		generationCollector = new GenerationCollector();

		// Creates a map renderer instance.
		MapRenderer.getInstance();	   
	}
}
