package game;

import java.util.List;

import generator.algorithm.Algorithm;
import generator.algorithm.Individual;
import generator.algorithm.Ranges;
import generator.config.Config;
import javafx.geometry.Point2D;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.Start;



// I had to mess around in here a bit because there was quite a lot of Unity-specific code
// Currently very unfinished.
public class Game implements Listener{

    public static int sizeN; //Horizontal room size in tiles
    public static int sizeM; //Vertical room size in tiles
    public static int sizeDoors; //TODO: ???
	public static String randomRangesFileName = "rangesSupervised"; //File containing probability ranges for generating particular tile types
    public static String gameConfigFileName = "zelda"; //TODO: ???
    public static Ranges ranges; 
    public static List<Point2D> doorsPositions = null; //TODO: ???
    public static Config.TLevel level; //Difficulty level

    public static Config.TLevel getLevel()
    {
        return level;
    }

    public static Ranges getRanges()
    {
        return ranges;
    }

    public Game(int n, int m, int doors, Config.TLevel level)
    {
		//Init properties
        //dataPath = Application.dataPath;
        ranges = new Ranges();
        sizeN = n;
        sizeM = m;
        sizeDoors = doors;
        Game.level = level;
        //tileManager = new CTileManager(new ColorPaint());
        
        EventRouter.getInstance().registerListener(this, new Start());
    }

	// Kicks the algorithm into action. Not yet sure where this is called?
    // 
    private void startAll()
    {
    	Algorithm geneticAlgorithm = new Algorithm(Algorithm.POPULATION_SIZE, new Config(gameConfigFileName));
    	
    	//Start the algorithm on a new thread.
    	geneticAlgorithm.start();
    	
    }

	@Override
	public synchronized void ping(PCGEvent e) {
		if(e instanceof Start){
			System.out.println(e);
			startAll();		
		}
	}

	
}
