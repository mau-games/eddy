package gui;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.Game;
import generator.config.Config.TLevel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.Util;

/**
 * This class is simply a program launcher.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class ProgramStarter extends Application {
	
	final static Logger logger = LoggerFactory.getLogger(ProgramStarter.class);
	private Game game;

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
			root = FXMLLoader.load(getClass().getResource("/gui/MainScene.fxml"));
	        Scene scene = new Scene(root, 800, 600);
	    
	        stage.setTitle("Eddy - Evolutionary Dungeon Designer");
	        stage.setScene(scene);
	        stage.show();
	        
	        game = new Game(10, 10, 3, TLevel.EASY);
	        
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
