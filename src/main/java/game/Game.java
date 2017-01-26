package game;

import java.util.List;

import generator.algorithm.Algorithm;
import generator.algorithm.Individual;
import generator.algorithm.Ranges;
import generator.config.Config;
import javafx.geometry.Point2D;



// I had to mess around in here a bit because there was quite a lot of Unity-specific code
// Currently very unfinished.
public class Game {

    public static int sizeN; //Horizontal room size in tiles
    public static int sizeM; //Vertical room size in tiles
    public static int sizeDoors; //TODO: ???
	public boolean mapTextures = false; //TODO: ???
	//public CRenderer renderer;
    //private static CTileManager tileManager;
	public static String randomRangesFileName = "rangesSupervised"; //File containing probability ranges for generating particular tile types
    public static String gameConfigFileName = "gameConfig"; //TODO: ???
    public static boolean debug = false; //TODO: ???
    public Algorithm geneticAlgorithm; //The genetic algorithm in all its glory!
    //public static String dataPath; //TODO: ???
    public boolean run = true; //TODO: ???
    public static Ranges ranges; 
    public static List<Point2D> doorsPositions = null; //TODO: ???
    public static Config.TLevel level; //Difficulty level?
    //private GameObject tileMap = null;

//    public static CTileManager getTileManager()
//    {
//        return tileManager;
//    }

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
        this.level = level;
        //tileManager = new CTileManager(new ColorPaint());
    }

    //No longer needed...
//    void initGen()
//    {
//        geneticAlgorithm = new Algorithm(Algorithm.POPULATION_SIZE, new Config(gameConfigFileName));
//    }

    //called when RunOffMainThread finishes
    public void ResultCallback()
    {
        initRenderer();
        instantiateTileMap(geneticAlgorithm.getBest());
    }

	// Kicks the algorithm into action. Not yet sure where this is called?
    // 
    public void startAll()
    {
        //StartCoroutine(RunOffMainThread(initGen, ResultCallback));
    	geneticAlgorithm = new Algorithm(Algorithm.POPULATION_SIZE, new Config(gameConfigFileName));
    	
    	//Start the algorithm on a new thread.
    	geneticAlgorithm.start();
    	
    	//TODO: Figure out how to deal with the thread when execution is finished
    	//ResultCallback();
    	
    }

    //No longer needed...
//    IEnumerator RunOffMainThread(Action toRun, Action callback)
//    {
//        bool done = false;
//        new Thread(() => {
//            toRun();
//            done = true;
//        }).Start();
//        while (!done)
//            yield return null;
//        callback();
//    }

//For now assume we don't need this... TODO: check!
//    void Update () {
//        if(MessagesPool.hasContent())
//        {
//            object obj = MessagesPool.get();
//            if(obj.GetType() == typeof(string))
//            {
//                GUIManager.showMessage((string)obj);
//            }
//            else
//            {
//                initRenderer();
//                instanciateTileMap((Individual)obj);
//            }
//            
//        }
//	}

	//instantiate a new TileMap prefab
    void initRenderer()
    {
		//TODO: Replace this with OUR way of doing things
//        if(tileMap != null)
//        {
//            Destroy(tileMap);
//            tileMap = null;
//        }
//
//        GameObject prefab = Resources.Load<GameObject>("Prefabs/TileMap");
//
//        tileMap = Instantiate(prefab, new Vector3(0, 0, 0), Quaternion.identity) as GameObject;
//        renderer = tileMap.GetComponent<CRenderer>();
    }

    //Note: I removed a LOT of commented out code in this method - TODO: refer to original source to see if anything was important
	void instantiateTileMap(Individual ind)
	{
		//TODO: Do whatever this does
//		Map map = ind.getPhenotype ().getMap ();
//
//		//render the map
//        renderer.draw(map.getRendeableTiles());
//        renderer.finish ();
    }
	
}
