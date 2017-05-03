package runners;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collectors.GenerationCollector;
import collectors.MapCollector;
import collectors.RenderedMapCollector;
import game.Game;
import gui.InteractiveGUIController;
import gui.utils.MapRenderer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.events.Start;

/**
 * This class launches an interactive GUI.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class InteractiveGUI extends Application {

	final static Logger logger = LoggerFactory.getLogger(InteractiveGUI.class);
	private static ConfigurationUtility config;

	private Game game;
	private MapCollector mapCollector;
	private RenderedMapCollector renderedMapCollector;
	private GenerationCollector generationCollector;

	/**
	 * This is the GUI entry point.
	 * 
	 * @param args Those arguments aren't used.
	 */
	public static void main(String[] args) {
		logger.info("Starting program");
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		Parent root;
		
		stage.setOnCloseRequest(e ->{
			Platform.exit();
			System.exit(0);
		});
		
		try {
			root = FXMLLoader.load(getClass().getResource("/gui/interactive/InteractiveGUI.fxml"));

			Scene scene = new Scene(root, 1024, 768);
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/graphics/icon.png"))); 
			stage.setTitle("Eddy - Evolutionary Dungeon Designer");
			stage.setScene(scene);
			stage.show();

			EventRouter router = EventRouter.getInstance();
			
			// Set up a new game
			game = new Game();
			router.postEvent(new Start(3));

			// Set up a bunch of collectors
			mapCollector = new MapCollector();
			renderedMapCollector = new RenderedMapCollector();
			generationCollector = new GenerationCollector();

			MapRenderer.getInstance();

		} catch (Exception e) {
			logger.error("Couldn't load GUI: " + e.getMessage(), e);
			System.exit(0);
		}
	}

	@Override
	public void stop(){
		game.stop();
	}
}
