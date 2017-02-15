package gui;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.MapCollector;
import game.Game;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;

/**
 * This class is simply a program launcher.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class ProgramStarter extends Application {
	
	final static Logger logger = LoggerFactory.getLogger(ProgramStarter.class);
	private static ConfigurationUtility config;
	private Game game;
	private MapCollector mapCollector;

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
		try {
			config = ConfigurationUtility.getInstance();
			root = FXMLLoader.load(getClass().getResource("/gui/MainScene.fxml"));
	        Scene scene = new Scene(root, 1024, 768);
	    
	        stage.setTitle("Eddy - Evolutionary Dungeon Designer");
	        stage.setScene(scene);
	        stage.show();
	        
	        // Set up a new game
	        // TODO: Bad code smell. This class knows too much about Game's inner workings. Fix Game.
	        game = new Game(
	        		config.getInt("game.dimensions.m"),
	        		config.getInt("game.dimensions.n"),
	        		config.getInt("game.doors"),
	        		Game.parseDifficulty(config.getString("game.difficulty")),
	        		config.getString("game.profiles.default")
	        		);
	        
	        // Set up a new map collector
	        mapCollector = new MapCollector();
	        
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
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
