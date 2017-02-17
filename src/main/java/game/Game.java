package game;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generator.algorithm.Algorithm;
import generator.algorithm.Ranges;
import generator.config.Config;
import generator.config.Config.DifficultyLevel;
import util.Point;
import util.Util;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.Start;

public class Game implements Listener{
	private final Logger logger = LoggerFactory.getLogger(Game.class);

	private ConfigurationUtility config;
	private Algorithm geneticAlgorithm;
	
	//TODO: There must be a better way to handle these public static variables
	public static int sizeM; //Number of columns
    public static int sizeN; //Number of rows
    public static int doorCount;
    public static String gameConfigFileName;
    public static Ranges ranges; 
    public static List<Point> doors = null; 
    public static Config.DifficultyLevel level;  
    
    public static Config.DifficultyLevel getLevel()
    {
        return level;
    }

    public static Ranges getRanges() {
        return ranges;
    }

    public Game() {

		try {
			config = ConfigurationUtility.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}

        
        readConfiguration();
        chooseDoorPositions();
        
        EventRouter.getInstance().registerListener(this, new Start());
        ranges = new Ranges();
		
       
    }
    
    /**
     * Selects positions for between 1 and 4 doors. 
     * The first door is the main entrance. 
     * Doors can't be in corners.
     */
    private void chooseDoorPositions(){
    	doors = new ArrayList<Point>();
    	List<Integer> walls = new ArrayList<Integer>();
    	walls.add(0);
    	walls.add(1);
    	walls.add(2);
    	walls.add(3);
    	for(int i = 0; i < doorCount; i++){
    		switch(walls.remove(Util.getNextInt(0, walls.size()))){
    		case 0: //North
    			doors.add(new Point(Util.getNextInt(1, sizeM - 1), sizeN - 1));
    			break;
    		case 1: //East
    			doors.add(new Point(sizeM - 1, Util.getNextInt(1, sizeN - 1)));
    			break;
    		case 2: //South
    			doors.add(new Point(Util.getNextInt(1, sizeM - 1), 0));
    			break;
    		case 3: //West
    			doors.add(new Point(0, Util.getNextInt(1, sizeN - 1)));
    			break;
    		}
    	}
    }

	/**
	 *  Kicks the algorithm into action.
	 */
    private void startAll()
    {
    	reinit();
    	geneticAlgorithm = new Algorithm(new Config(config.getString("game.profiles.default")));
    	//Start the algorithm on a new thread.
    	geneticAlgorithm.start();
    }
    
    /**
     * Set everything back to its initial state before running the genetic algorithm
     */
    private void reinit(){
    	doors.clear();
    	chooseDoorPositions();
    }
    
    /**
     * Stop the algorithm. Used in the case that the application window is closed.
     */
    public void stop(){
    	if(geneticAlgorithm != null && geneticAlgorithm.isAlive()){
    		geneticAlgorithm.terminate();
    	}
    }

	@Override
	public synchronized void ping(PCGEvent e) {
		if(e instanceof Start){
			readConfiguration();
			startAll();		
		}
	}

	/**
	 * Reads and applies the current configuration.
	 */
	private void readConfiguration() {
        sizeN = config.getInt("game.dimensions.n");
        sizeM = config.getInt("game.dimensions.m");
        doorCount = config.getInt("game.doors");
        gameConfigFileName = config.getString("game.profiles.default");
        Game.level = parseDifficulty(config.getString("game.difficulty"));
        
        chooseDoorPositions();
	}

	// TODO: Bad code smell. This feels really out of place.
	public static DifficultyLevel parseDifficulty(String difficulty) {
		DifficultyLevel level;
		
		if (difficulty.equals("medium")) {
			level = DifficultyLevel.MEDIUM;
		} else if (difficulty.equals("hard")) {
			level = DifficultyLevel.HARD;
		} else {
			level = DifficultyLevel.EASY;
		}
			
		return level;
	}

	
}
