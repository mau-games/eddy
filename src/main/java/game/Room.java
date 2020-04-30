package game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Watchable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.UUID;

import javax.swing.text.Position;

import java.util.Map.Entry;
import java.util.stream.Collectors;

import finder.PatternFinder;
import finder.Populator;
import finder.geometry.Bitmap;
import finder.geometry.Multipoint;
import finder.geometry.Polygon;
import finder.graph.Edge;
import finder.graph.Graph;
import util.algorithms.Node;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import finder.patterns.SpacialPattern;
import finder.patterns.meso.Ambush;
import finder.patterns.meso.DeadEnd;
import finder.patterns.meso.GuardRoom;
import finder.patterns.meso.GuardedTreasure;
import finder.patterns.meso.TreasureRoom;
import finder.patterns.micro.Connector;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Door;
import finder.patterns.micro.Enemy;
import finder.patterns.micro.Entrance;
import finder.patterns.micro.Boss;
import finder.patterns.micro.Chamber;
import finder.patterns.micro.Treasure;
import game.roomInfo.RoomSection;
import game.tiles.BossEnemyTile;
import game.tiles.FloorTile;
import game.tiles.HeroTile;
import util.Point;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapLoaded;
import util.eventrouting.events.MapQuestUpdate;
import util.eventrouting.events.MapUpdate;
import generator.algorithm.Algorithm.AlgorithmTypes;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.config.GeneratorConfig;
import gui.InteractiveGUIController;
import gui.controls.Brush.NeighborhoodStyle;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;

import collectors.ActionLogger;
import collectors.DataSaverLoader;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;

import org.w3c.dom.*;

/**
 * This class represents a dungeon room map.
 * 
 * @author Johan Holmberg, Malmö University
 * @author Alexander Baldwin, Malmö University
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 */
public class Room {
	
////////////////////////////NEW/////////////////////////////////
	
	//Maybe I can copy the finder.graph info into other types, I feel that it will give me a lot of constraints as well.
	public finder.graph.Node<Room> node; //This will hold in the edges the doors
	public RoomConfig localConfig;
	
	public int maxNumberDoors; //--> HAHA
	public Bitmap borders = new Bitmap();
	public Bitmap enemyTiles = new Bitmap();
	public Bitmap npcTiles = new Bitmap();
	public Bitmap itemTiles = new Bitmap();
	public Bitmap treasureTiles = new Bitmap();
	public Bitmap bossTiles = new Bitmap();
	public Bitmap walkableTiles = new Bitmap();
	public Bitmap path = new Bitmap();//TODO: For testing
	public Bitmap nonInterFeasibleTiles = new Bitmap();//TODO: For testing
	
	public RoomPathFinder pathfinder;
	public Dungeon owner;
	
	//Might be interesting to know the dimension of the room?
	protected HashMap<DimensionTypes, Double> dimensionValues;
	//I need a special ID for rooms
	UUID specificID = UUID.randomUUID();
	int saveCounter = 1;

/////////////////////////OLD///////////////////////////

	private Tile[] tileMap; //This HAVE to be the real tilemap
	public int[][] matrix; // The actual map
	private boolean[][] allocated; // A map keeps track of allocated tiles
	private int width;			// The number of columns in a map
	private int height;			// The number of rows in a map
	private int wallCount;	// The number of wall tiles in a map
	private List<Point> doors = new ArrayList<Point>();
	private List<Point> treasures = new ArrayList<Point>();
	private List<Point> enemies = new ArrayList<Point>();
	//private Graph<SpacialPattern> graph = new Graph<SpacialPattern>();
	private PatternFinder finder;
	private int failedPathsToTreasures;
	private int failedPathsToEnemies;
	private int failedPathsToAnotherDoor;
	private Dictionary<Point, Double> treasureSafety; //ODL
	
	private double doorSafety; //Sum of all the safeties
	private double doorGreed; //Sum of all the greeds
	
	//DOORS IMPROVED INFO!!!!
	private Map<Point, Double> doorsSafety;
	private Map<Point, Double> doorsGreed;
	
	//Custom tiles (only the center or main needed?) this needs to be copied!
	public ArrayList<Tile> customTiles = new ArrayList<Tile>();
	
	//THERE MUST BE TWO DIFFERENT CONFIGS!! //TODO: This really needs to be done already!
	private GeneratorConfig config = null;
	
	private GeneratorConfig selfGeneratorConfig;
	private GeneratorConfig targetGeneratorConfig;
	
	private double wallDensity 		= -1.0f;
	private double wallSparsity 	= -1.0f;
	private double treasureDensity 	= -1.0f;
	private double treasureSparsity = -1.0f;
	private double enemyDensity 	= -1.0f;
	private double enemySparsity 	= -1.0f;
	
	//NEW THINGS
	public ZoneNode root;

	//SIDE!!!
	public void createLists()
	{
		if(enemies == null)
			enemies = new ArrayList<Point>();
		
		if(treasures == null)
			treasures = new ArrayList<Point>();
	}
	
	/**
	 * Creates an instance of map.
	 * 
	 * @param rows The number of rows in a map.
	 * @param cols The number of columns in a map.
	 */
	private Room(int rows, int cols) { //THIS IS CALLED WHEN LOADING THE ROOM FROM A STRING
		init(rows, cols);
		
		finder = new PatternFinder(this);
		pathfinder = new RoomPathFinder(this);
		
		for (int j = 0; j < height; j++)
		{
			for (int i = 0; i < width; i++) 
			{
				tileMap[j * width + i] = new Tile(i, j, matrix[j][i]);
			}
		}
		
		root = new ZoneNode(null, this, getColCount(), getRowCount());
	}
	
	
	/**
	 * Creates an instance of map.
	 * 
	 * @param types A chromosome transformed into an array of TileTypes.
	 * @param rows The number of rows in a map.
	 * @param cols The number of columns in a map.
	 * @param doorCount The number of doors to be seeded in a map.
	 */
	public Room(GeneratorConfig config, TileTypes[] types, int rows, int cols, List<Point> doorPositions, List<Tile> customTiles, Dungeon owner) { //THIS IS CALLED WHEN CREATIMNG THE PHENOTYPE
		init(rows, cols);

		this.config = config;
//		localConfig = new RoomConfig(this, 40); //TODO: NEW ADDITION --> HAVE TO BE ADDED EVERYWHERE

		initMapFromTypes(types);
		copyDoors(doorPositions);
		copyCustomTiles(customTiles);
		this.owner = owner;

		finder = new PatternFinder(this);
		pathfinder = new RoomPathFinder(this);
		
		root = new ZoneNode(null, this, getColCount(), getRowCount());

	}
	
	public Room(Dungeon owner, GeneratorConfig config, int rows, int cols, int scaleFactor) //THIS IS CALLED WHEN ADDING ROOMS TO THE DUNGEON!
	{
		init(rows, cols);
		this.owner = owner;

		this.config = config;
		localConfig = new RoomConfig(this, scaleFactor); //TODO: NEW ADDITION --> HAVE TO BE ADDED EVERYWHERE
		
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {

				tileMap[j * width + i] = new Tile(i , j, TileTypes.toTileType(matrix[j][i]));
			}
		}

		finder = new PatternFinder(this);
		pathfinder = new RoomPathFinder(this);
		
		root = new ZoneNode(null, this, getColCount(), getRowCount());
		node = new finder.graph.Node<Room>(this);

	}
	
	public Room(Room copyMap) //THIS IS CALLED WHEN CREATING A ZONE IN THE TREE (TO HAVE A COPY OF THE DOORS)
	{
		init(copyMap.getRowCount(), copyMap.getColCount());
		this.config = copyMap.config;
		
//		for (int j = 0; j < height; j++)
//		{
//			for (int i = 0; i < width; i++) 
//			{
//				setTile(i, j, copyMap.getTile(i, j));
////				matrix[j][i] = suggestions.matrix[j][i];
////				tileMap[j * width + i] = new Tile(suggestions.tileMap[j * width + i]);
//			}
//		}	
		
		for (int j = 0; j < height; j++)
		{
			for (int i = 0; i < width; i++) 
			{
				matrix[j][i] = copyMap.matrix[j][i];
				tileMap[j * width + i] = new Tile(copyMap.tileMap[j * width + i]);
			}
		}	
		
		for (int j = 0; j < height; j++){
			for (int i = 0; i < width; i++) {
				switch (TileTypes.toTileType(matrix[j][i])) {
				case WALL:
					wallCount++;
					break;
				case ENEMY:
					enemies.add(new Point(i, j));
					break;
				case TREASURE:
					treasures.add(new Point(i, j));
					break;
				default:
					break;
				}
			}
		}
		

		this.owner = copyMap.owner;	
		copyDoors(copyMap.getDoors());
		copyCustomTiles(copyMap.customTiles);
		
		finder = new PatternFinder(this);
		SetDimensionValues(copyMap.dimensionValues);
		pathfinder = new RoomPathFinder(this);
		root = new ZoneNode(null, this, getColCount(), getRowCount());	
	}
	
	public Room(Room copyMap, ZoneNode zones) //THIS IS CALLED WHEN CREATING A ZONE IN THE TREE (TO HAVE A COPY OF THE DOORS)
	{	
		init(copyMap.getRowCount(), copyMap.getColCount());
		this.config = copyMap.config;
		
//		for (int j = 0; j < height; j++)
//		{
//			for (int i = 0; i < width; i++) 
//			{
//				setTile(i, j, copyMap.getTile(i, j));
////				matrix[j][i] = suggestions.matrix[j][i];
////				tileMap[j * width + i] = new Tile(suggestions.tileMap[j * width + i]);
//			}
//		}	
		
		for (int j = 0; j < height; j++)
		{
			for (int i = 0; i < width; i++) 
			{
				matrix[j][i] = copyMap.matrix[j][i];
				tileMap[j * width + i] = new Tile(copyMap.tileMap[j * width + i]);
			}
		}	
		
//		for (int j = 0; j < height; j++){
//			for (int i = 0; i < width; i++) {
//				switch (TileTypes.toTileType(matrix[j][i])) {
//				case WALL:
//					wallCount++;
//					break;
//				case ENEMY:
//					enemies.add(new Point(i, j));
//					break;
//				case TREASURE:
//					treasures.add(new Point(i, j));
//					break;
//				default:
//					break;
//				}
//			}
//		}

		this.owner = copyMap.owner;
		copyDoors(copyMap.getDoors());
		copyCustomTiles(copyMap.customTiles);

		finder = new PatternFinder(this);
		pathfinder = new RoomPathFinder(this);
		root = zones;	
	}
	
	public Room(GeneratorConfig config, ZoneNode rootCopy, int[] chromosomes, int rows, int cols) //THIS IS CALLED FROM THE PHENOTYPE BUT WHEN WE HAVE THE ZONES
	{
		init(rows, cols);
		
		this.config = config;
//		this.localConfig = new RoomConfig(this, 40); //TODO: NEW ADDITION --> HAVE TO BE ADDED EVERYWHERE
		
		CloneMap(rootCopy.GetMap(), chromosomes);
	}
	
	private void CloneMap(Room room, int[] chromosomes) //FROM THE PREVIOUS CONSTRUCTOR
	{
//		this.tileMap = room.tileMap.clone();
//		this.matrix = room.matrix.clone();


		for (int j = 0; j < height; j++)
		{
			for (int i = 0; i < width; i++) 
			{
//				if(room.tileMap[j * width + i].GetType() == TileTypes.ENEMY_BOSS)
//				{
//					System.out.println();
//				}
//				
//				setTile(i, j, room.tileMap[j * width + i]);
				if(!room.tileMap[j * width + i].GetImmutable())
				{
					setTile(i, j, room.tileMap[j * width + i]);
				}
				else
				{
					tileMap[j * width + i] = new Tile(room.tileMap[j * width + i]);
					matrix[j][i] = chromosomes[j * width + i];
				}
			}
		}	
		
		wallCount = 0;
		enemies.clear();
		treasures.clear();
//		
		for (int j = 0; j < height; j++){
			for (int i = 0; i < width; i++) {
				switch (TileTypes.toTileType(matrix[j][i])) {
				case WALL:
					wallCount++;
					break;
				case ENEMY:
					enemies.add(new Point(i, j));
					break;
				case TREASURE:
					treasures.add(new Point(i, j));
					break;
				default:
					break;
				}
			}
		}
		
		this.owner = room.owner;
		copyDoors(room.getDoors());
		copyCustomTiles(room.customTiles);

//		
		finder = new PatternFinder(this);
		pathfinder = new RoomPathFinder(this);
		root = new ZoneNode(null, this, width, height);
		
	}
	
	/***
	 * Basic init code to initialize all the aspects in the room
	 * @param rows
	 * @param cols
	 */
	private void init(int rows, int cols) 
	{
		wallDensity 		= -1.0f;
		wallSparsity 		= -1.0f;
		treasureDensity 	= -1.0f;
		treasureSparsity 	= -1.0f;
		enemyDensity 		= -1.0f;
		enemySparsity 		= -1.0f;
		
		treasureSafety = new Hashtable<Point, Double>();
		doorsSafety = new Hashtable<Point, Double>();
		doorsGreed = new Hashtable<Point, Double>();
		enemies.clear();
		treasures.clear();
		doors.clear();
		
		this.width = cols;
		this.height = rows;
		wallCount = 0;
		
		matrix = new int[height][width];
		allocated = new boolean[height][width];
		tileMap = new Tile[rows * cols];
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				if(y == 0 || y == height - 1)
				{
					borders.addPoint(new finder.geometry.Point(x, y));
				}
				else if(x == 0 || x == width - 1 )
				{
					borders.addPoint(new finder.geometry.Point(x, y));
				}	
			}
		}
	}
	
	/**
	 * Copy doors from a list of existing ones (/another room) and override whatever tile was there
	 * This is for the new phenotypes when created
	 * @param doorPositions
	 * @param entrance
	 */
	private void copyDoors(List<Point> doorPositions)
	{
		for(int i = 0; i < doorPositions.size(); i++)
        {
            if (TileTypes.toTileType(matrix[doorPositions.get(i).getY()][doorPositions.get(i).getX()]).isEnemy())  // Check if door overrides an enemy
            {
            	int ii = i;
            	enemies.removeIf((x)->x.equals(doorPositions.get(ii)));
            }
            else if (TileTypes.toTileType(matrix[doorPositions.get(i).getY()][doorPositions.get(i).getX()]).isTreasure()) // Check if door overrides a treasure
            {
            	int ii = i;
            	treasures.removeIf((x)->x.equals(doorPositions.get(ii)));
            }
            else if (matrix[doorPositions.get(i).getY()][doorPositions.get(i).getX()] == TileTypes.WALL.getValue()) // Check if door overrides a wall
            {
                wallCount--;
            } 

        	setTile(doorPositions.get(i).getX(), doorPositions.get(i).getY(), TileTypes.DOOR);
            addDoor(doorPositions.get(i));
            borders.removePoint(Point.castToGeometry(doorPositions.get(i))); //remove this point from the "usable" border
            
    	}
	}
	
	/**
	 * Copy doors from a list of existing ones (/another room) and override whatever tile was there
	 * This is for the new phenotypes when created
	 * @param doorPositions
	 * @param entrance
	 */
	private void copyCustomTiles(List<Tile> customs)
	{
		customTiles.clear();
		customTiles.addAll(customs);
	}
	
	public void setHeroPosition(Point heroPosition)
	{
		 if (TileTypes.toTileType(matrix[heroPosition.getY()][heroPosition.getX()]).isEnemy())
        {
        	enemies.removeIf((x)->x.equals(heroPosition));
        }
        else if (TileTypes.toTileType(matrix[heroPosition.getY()][heroPosition.getX()]).isTreasure())   // Check if door overrides a treasure
        {
        	treasures.removeIf((x)->x.equals(heroPosition));
        }
        else if (matrix[heroPosition.getY()][heroPosition.getX()] == TileTypes.WALL.getValue()) // Check if door overrides a wall
        {
            wallCount--;
        } 
 
	    ActionLogger.getInstance().storeAction(ActionType.CHANGE_TILE, 
													View.WORLD, 
													TargetPane.WORLD_MAP_PANE,
													true,
													this, //ROOM A
													heroPosition, //Pos A
													getTile(heroPosition).GetType(), //TILE A
													TileTypes.HERO); //TILE B
	    
        setTile(heroPosition.getX(), heroPosition.getY(), new HeroTile());
        borders.removePoint(Point.castToGeometry(heroPosition)); //remove this point from the "usable" border
	}
	
	/**
	 * To be called when you change the position of the hero
	 * @param heroPosition
	 */
	public void removeHero(Point heroPosition) 
	{
		 ActionLogger.getInstance().storeAction(ActionType.CHANGE_TILE, 
												View.WORLD, 
												TargetPane.WORLD_MAP_PANE,
												true,
												this, //ROOM A
												heroPosition, //Pos A
												TileTypes.HERO, //TILE A
												TileTypes.FLOOR); //TILE B
		 
		setTile(heroPosition.getX(), heroPosition.getY(), new FloorTile());
		if(isBorder(heroPosition))borders.addPoint(Point.castToGeometry(heroPosition));
	}
	
	private boolean isBorder(Point point)
	{
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				if(y == 0 || y == height - 1)
				{
					if(point.getY() == y && point.getX() == x)
						return true;
				}
				else if(x == 0 || x == width - 1 )
				{
					if(point.getY() == y && point.getX() == x)
						return true;
				}	
			}
		}
		
		return false;
	}
	
	/***
	 * Create a door in the specified location (It should be guaranteed to be in the border of the room)
	 * This method is for when you connect two different rooms through doors
	 * @param doorPosition
	 */
	public void createDoor(Point doorPosition)
	{
		//Check what will be overwritten
		// Check if door overrides an enemy
        if (TileTypes.toTileType(matrix[doorPosition.getY()][doorPosition.getX()]).isEnemy())
        {
        	enemies.removeIf((x)->x.equals(doorPosition));
        }
        else if (TileTypes.toTileType(matrix[doorPosition.getY()][doorPosition.getX()]).isTreasure())   // Check if door overrides a treasure
        {
        	treasures.removeIf((x)->x.equals(doorPosition));
        }
        else if (matrix[doorPosition.getY()][doorPosition.getX()] == TileTypes.WALL.getValue()) // Check if door overrides a wall
        {
            wallCount--;
        } 

        setTile(doorPosition.getX(), doorPosition.getY(), TileTypes.DOOR);
        addDoor(doorPosition);
        borders.removePoint(Point.castToGeometry(doorPosition)); //remove this point from the "usable" border
	}
	
	/**
	 * To be called when you remove a room or a connection
	 * @param doorPosition
	 */
	public void removeDoor(Point doorPosition) 
	{
		doors.remove(doorPosition);
		setTile(doorPosition.getX(), doorPosition.getY(), TileTypes.FLOOR);
		borders.addPoint(Point.castToGeometry(doorPosition));
		
	}
	
	public void applySuggestion(Room suggestions)
	{
		//TODO: NOW THE PROBLEM IS WITH THE DOORS!!!!
		init(suggestions.getRowCount(), suggestions.getColCount());
		this.config = suggestions.config;
		
		for (int j = 0; j < height; j++)
		{
			for (int i = 0; i < width; i++) 
			{
				setTile(i, j, suggestions.getTile(i, j));
//				matrix[j][i] = suggestions.matrix[j][i];
//				tileMap[j * width + i] = new Tile(suggestions.tileMap[j * width + i]);
				EventRouter.getInstance().postEvent(new MapQuestUpdate(getTile(i,j),suggestions.getTile(i,j), this));
			}
		}	
		
		this.owner = suggestions.owner; //NOt clear
		copyDoors(suggestions.getDoors());
		
		for(Tile custom : this.customTiles)
		{
			owner.removeBoss((BossEnemyTile) custom);
		}
		
		copyCustomTiles(suggestions.customTiles);
		
		for(Tile custom : this.customTiles)
		{
			owner.addBoss((BossEnemyTile) custom);
		}
		
		SetDimensionValues(suggestions.dimensionValues);
		
		finder = new PatternFinder(this);
		pathfinder = new RoomPathFinder(this);
		root = suggestions.root;	
	}
	
	
	/***
	 * Updates the different tile-matrix-components of the room based on changes in the zones //TODO: THIS NEEDS TO BE FIX!!!
	 * @param updatedMatrix
	 */
	public void Update(int[] updatedMatrix)
	{
		int tile = 0;
		treasureSafety = new Hashtable<Point, Double>();
		
		for (int j = 0; j < height; j++) 
		{
			for (int i = 0; i < width; i++) 
			{
				setTile(i, j, updatedMatrix[tile++]);
			}
		}
		
		wallCount = 0;
		enemies.clear();
		treasures.clear();
		
		for (int j = 0; j < height; j++) 
		{
			for (int i = 0; i < width; i++) 
			{	
				switch (TileTypes.toTileType(matrix[j][i])) {
				case WALL:
					wallCount++;
					break;
				case ENEMY:
					enemies.add(new Point(i, j));
					break;
				case TREASURE:
					treasures.add(new Point(i, j));
					break;
				default:
					break;
				}
			}
		}
		
		finder = new PatternFinder(this);
		pathfinder = new RoomPathFinder(this);
	}

	/**
	 * Invalidates the current calculations and forces a re-evaluation of the
	 * map.
	 * 
	 * TODO: This isn't very efficient, but shouldn't be so frequently used, as
	 * to warrant any particular worry. Maybe replace it with a more granular
	 * approach sometime? indeed
	 * 
	 */
	public void forceReevaluation() {
		treasures.clear();
		enemies.clear();
		doors.clear();
		treasureSafety = new Hashtable<Point, Double>();
		wallCount = 0;
		allocated = new boolean[height][width];
		
		for (int j = 0; j < height; j++){
			for (int i = 0; i < width; i++) {
				switch (TileTypes.toTileType(matrix[j][i])) {
				case WALL:
					wallCount++;
					break;
				case ENEMY:
					enemies.add(new Point(i, j));
					break;
				case TREASURE:
					treasures.add(new Point(i, j));
					break;
				case DOOR:
					addDoor(new Point(i, j));
					break;
				default:
					break;
				}
			}
		}
		
		//markDoors();
		finder = new PatternFinder(this);
		pathfinder = new RoomPathFinder(this);
		
		//Update the zones - It could be more efficient by only updating the affected zone
		//(As we already have separated the map)
		root = new ZoneNode(null, this, getColCount(), getRowCount());
	}

	public GeneratorConfig getConfig(){
		return config;
	}

	public void setConfig(GeneratorConfig config){
		this.config = config;
	}

	public void resetAllocated(){
		allocated = new boolean[height][width];
	}

	/**
	 * Gets a list of positions of tiles adjacent to a given position which are not walls (passable tiles)
	 * 
	 * @param position The position of a tile
	 * @return A list of points 
	 */
	public List<Point> getAvailableCoords(Point position){
		List<Point> availableCoords = new ArrayList<Point>();

		if(position.getX() > 0 && getTile((int)position.getX() - 1, (int)position.getY()).GetType() != TileTypes.WALL)
			availableCoords.add(new Point(position.getX()-1,position.getY()));
		if(position.getX() < width - 1 && getTile((int)position.getX() + 1, (int)position.getY()).GetType() != TileTypes.WALL)
			availableCoords.add(new Point(position.getX()+1,position.getY()));
		if(position.getY() > 0 && getTile((int)position.getX(), (int)position.getY() - 1).GetType() != TileTypes.WALL)
			availableCoords.add(new Point(position.getX(),position.getY() - 1));
		if(position.getY() < height - 1 && getTile((int)position.getX(), (int)position.getY() + 1).GetType() != TileTypes.WALL)
			availableCoords.add(new Point(position.getX(),position.getY() + 1));

		return availableCoords;
	}
	
	/**
	 * Gets a list of positions of tiles adjacent to a given position which are not walls (passable tiles)
	 * 
	 * @param position The position of a tile
	 * @return A list of points 
	 */
	public List<Point> getNonAvailableCoords(Point position){
		List<Point> availableCoords = new ArrayList<Point>();

		if(position.getX() > 0 && getTile((int)position.getX() - 1, (int)position.getY()).GetType() == TileTypes.WALL)
			availableCoords.add(new Point(position.getX()-1,position.getY()));
		if(position.getX() < width - 1 && getTile((int)position.getX() + 1, (int)position.getY()).GetType() == TileTypes.WALL)
			availableCoords.add(new Point(position.getX()+1,position.getY()));
		if(position.getY() > 0 && getTile((int)position.getX(), (int)position.getY() - 1).GetType() == TileTypes.WALL)
			availableCoords.add(new Point(position.getX(),position.getY() - 1));
		if(position.getY() < height - 1 && getTile((int)position.getX(), (int)position.getY() + 1).GetType() == TileTypes.WALL)
			availableCoords.add(new Point(position.getX(),position.getY() + 1));

		return availableCoords;
	}
	
	private void ChangeQuantities(int x, int y, TileTypes newTile)
	{
		//TODO: I THINK THE PROBLEM CAN BE HERE!
		if(newTile.equals(TileTypes.toTileType(matrix[y][x])))
			return;
		
		switch (TileTypes.toTileType(matrix[y][x])) {
		case WALL:
			wallCount--;
			break;
		case ENEMY:
			enemies.remove(new Point(x, y));
			break;
		case TREASURE:
			treasures.remove(new Point(x, y));
			break;
		default:
			break;
		}
		
		switch (newTile) {
		case WALL:
			wallCount++;
			break;
		case ENEMY:
			enemies.add(new Point(x, y));
			break;
		case TREASURE:
			treasures.add(new Point(x, y));
			break;
		default:
			break;
		}
	}

	/**
	 * Sets a specific tile to a value.
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param tile A tile.
	 */
	public void setTile(int x, int y, TileTypes tile) 
	{
		wallDensity 		= -1.0f;
		wallSparsity 		= -1.0f;
		treasureDensity 	= -1.0f;
		treasureSparsity 	= -1.0f;
		enemyDensity 		= -1.0f;
		enemySparsity 		= -1.0f;
		
		if(localConfig != null) localConfig.getWorldCanvas().setRendered(false); //THIS IS NEEDED TO FORCE RENDERING IN THE WORLD VIEW
		ChangeQuantities(x, y, tile);
		matrix[y][x] = tile.getValue();
		tileMap[y * width + x].SetType(tile); //Just changing the type but not changing the tile? //FIXME!!
	}
	
	/**
	 * Sets a specific tile object.
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param tile A tile object.
	 */
	public void setTile(int x, int y, Tile tile) 
	{
		wallDensity 		= -1.0f;
		wallSparsity 		= -1.0f;
		treasureDensity 	= -1.0f;
		treasureSparsity 	= -1.0f;
		enemyDensity 		= -1.0f;
		enemySparsity 		= -1.0f;
		
		if(localConfig != null) localConfig.getWorldCanvas().setRendered(false); //THIS IS NEEDED TO FORCE RENDERING IN THE WORLD VIEW
		ChangeQuantities(x, y, tile.GetType());
		matrix[y][x] = tile.GetType().getValue();
		tileMap[y * width + x] = tile;
	}
	
	/**
	 * Sets a tile based on a value
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param tileValue value of a tiletype.
	 */
	public void setTile(int x, int y, int tileValue)
	{
		wallDensity 		= -1.0f;
		wallSparsity 		= -1.0f;
		treasureDensity 	= -1.0f;
		treasureSparsity 	= -1.0f;
		enemyDensity 		= -1.0f;
		enemySparsity 		= -1.0f;
		
		if(localConfig != null) localConfig.getWorldCanvas().setRendered(false); //THIS IS NEEDED TO FORCE RENDERING IN THE WORLD VIEW
		ChangeQuantities(x, y, TileTypes.toTileType(tileValue));
		matrix[y][x] = tileValue;
		tileMap[y * width + x].SetType(TileTypes.toTileType(tileValue));
	}

	/**
	 * Gets the type of a specific tile.
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @return A tile.
	 */
	public Tile getTile(int x, int y) {
		
		return tileMap[y * width + x];

//		return TileTypes.toTileType(matrix[y][x]);
	}

	/**
	 * Gets the type of a specific tile.
	 * 
	 * @param point The position.
	 * @return A tile.
	 */
	public Tile getTile(Point point){
		if (point == null) {
			return null;
		}

		return tileMap[point.getY() * width + point.getX()];

	}

	/**
	 * Returns the number of columns in a map. (WIDTH)
	 * 
	 * @return The number of columns.
	 */
	public int getColCount() {
		return width;
	}

	/**
	 * Returns the number of rows in a map. (HEIGHT)
	 * 
	 * @return The number of rows.
	 */
	public int getRowCount() {
		return height;
	}

	/**
	 *	Gets the number of traversable tiles. 
	 * 
	 * @return The number of traversable tiles.
	 */
	public int countTraversables() {
		return width * height - wallCount;
	}
	
	public double emptySpacesRate()
	{
		return (double)countTraversables()/(double)(width * height);
	}
	
	/**
	 * Returns the closest door to the specified point p
	 * @param p
	 * @return
	 */
	public Point getClosestDoor(Point p)
	{
		Point closestDoor = null;
		int dist = 10000;
		
		for(Point door : getDoors())
		{
			int cur = Math.abs(door.getX() - p.getX()) + Math.abs(door.getY() - p.getY());
			if(cur < dist)
			{
				dist = cur;
				closestDoor = door;
			}
		}
		
		return closestDoor;
	}

	/**
	 * Adds a door to the map.
	 * 
	 * @param door The position of a new door.
	 */
	public void addDoor(Point door) {
		doors.add(door);
	}

	/**
	 * Returns the positions of all doors.
	 * 
	 * @return The doors.
	 */
	public List<Point> getDoors() {
		return doors;
	}

	/**
	 * Gets the number of enemies on a map.
	 * 
	 * @return The number of enemies.
	 */
	public int getEnemyCount() {
		return enemies.size();
	}

	/**
	 * Calculates the enemy density by comparing the number of traversable
	 * tiles to the number of enemies.
	 * 
	 * @return The enemy density.
	 */
	public double calculateEnemyDensity()
	{
		if(enemyDensity > -1.0)
			return enemyDensity;
		
		enemyDensity = (double)enemies.size() / (double)countTraversables();
		return enemyDensity;
	}
	
	/**
	 * Calculates the enemy density by comparing the number of traversable
	 * tiles to the number of enemies.
	 * 
	 * @return The enemy density.
	 */
	public double calculateEnemyDensitySparsity()
	{
		if(enemyDensity > -1.0)
			return enemyDensity;
		
		double denseThreshold = 4.0;
		
		if(enemies.isEmpty())
		{
			enemySparsity = 0.0;
			enemyDensity = 0.0;
			return 0.0;
		}
		
		Queue<Node> queue = new LinkedList<Node>();
		ArrayList<Bitmap> enemyChunks = new ArrayList<Bitmap>();
		ArrayList<Point> enemyPoints = new ArrayList<Point>(enemies);

		Node root = new Node(0.0f, enemyPoints.remove(0), null);
    	queue.add(root);
    	
    	while(!enemyPoints.isEmpty())
    	{
    		Bitmap enemyChunk = new Bitmap();
    		
    		while(!queue.isEmpty()){
        		Node current = queue.remove();
        		Tile currentTile = getTile(current.position);
        		
        		//We need to remove the door!
        		enemyPoints.remove(current.position);
        		enemyChunk.addPoint(Point.castToGeometry(current.position));
        		
        		
        		List<Point> children = getAvailableCoords(current.position);
                for(Point child : children)
                {
                	 
                	if(!enemyPoints.contains(child))
                		continue;

                    //Create child node
                    Node n = new Node(0.0f, child, current);
                    queue.add(n);
                }
        	}
    		
    		enemyChunk.CalculateMedoid();
    		enemyChunks.add(enemyChunk);
    		
    		if(!enemyPoints.isEmpty())
    			queue.add(new Node(0.0f, enemyPoints.get(0), null));
    	}
		
    	double dens = 0.0;
    	double sparse = 0.0;
    	double distances = 0.0;
    	
    	boolean calculateSparsity = enemyChunks.size() > 1;
    	
		//CLusters are done, now calculate density
    	for(Bitmap enemyChunk : enemyChunks)
    	{
    		dens += Math.min(1.0, (double)enemyChunk.getPoints().size()/denseThreshold);
    		
    		if(calculateSparsity)
    		{
	    		//CLusters are done, now calculate density
	        	for(Bitmap otherChunk : enemyChunks)
	        	{
	        		if(otherChunk.equals(enemyChunk))
	        			continue;
	        		
	        		sparse += (double)otherChunk.distManhattan(otherChunk.medoid, enemyChunk.medoid)/(width + height);
	        		distances++;
	        	}
    		}
    	}
    	
    	enemyDensity = dens/(double)enemyChunks.size();
		enemySparsity = calculateSparsity == true ? sparse/distances : 0.0;
		
		return enemyDensity;
	}
	
	/**
	 * Calculates the enemy density by comparing the number of traversable
	 * tiles to the number of enemies.
	 * 
	 * @return The enemy density.
	 */
	public double calculateTreasureDensitySparsity()
	{
		if(treasureDensity > -1.0)
			return treasureDensity;
		
		double denseThreshold = 4.0;
		
		if(treasures.isEmpty())
		{
			treasureSparsity = 0.0;
			treasureDensity = 0.0;
			return 0.0;
		}
		
		Queue<Node> queue = new LinkedList<Node>();
		ArrayList<Bitmap> treasureChunks = new ArrayList<Bitmap>();
		ArrayList<Point> treasurePoints = new ArrayList<Point>(treasures);
		
		Node root = new Node(0.0f, treasurePoints.remove(0), null);
    	queue.add(root);
    	
    	while(!treasurePoints.isEmpty())
    	{
    		Bitmap treasureChunk = new Bitmap();
    		
    		while(!queue.isEmpty()){
        		Node current = queue.remove();
        		Tile currentTile = getTile(current.position);
        		
        		//We need to remove the door!
        		treasurePoints.remove(current.position);
        		treasureChunk.addPoint(Point.castToGeometry(current.position));
        		
        		
        		List<Point> children = getAvailableCoords(current.position);
                for(Point child : children)
                {
                	 
                	if(!treasurePoints.contains(child))
                		continue;

                    //Create child node
                    Node n = new Node(0.0f, child, current);
                    queue.add(n);
                }
        	}
    		
    		treasureChunk.CalculateMedoid();
    		treasureChunks.add(treasureChunk);
    		
    		if(!treasurePoints.isEmpty())
    			queue.add(new Node(0.0f, treasurePoints.get(0), null));
    	}
		
    	double dens = 0.0;
    	double sparse = 0.0;
    	double distances = 0.0;
    	
    	boolean calculateSparsity = treasureChunks.size() > 1;
    	
		//CLusters are done, now calculate density
    	for(Bitmap treasureChunk : treasureChunks)
    	{
    		dens += Math.min(1.0, (double)treasureChunk.getPoints().size()/denseThreshold);
    		
    		if(calculateSparsity)
    		{
	    		//CLusters are done, now calculate density
	        	for(Bitmap otherChunk : treasureChunks)
	        	{
	        		if(otherChunk.equals(treasureChunk))
	        			continue;
	        		
	        		sparse += (double)otherChunk.distManhattan(otherChunk.medoid, treasureChunk.medoid)/(width + height);
	        		distances++;
	        	}
    		}
    	}
    	
    	treasureDensity = dens/(double)treasureChunks.size();
		treasureSparsity = calculateSparsity == true ? sparse/distances : 0.0;
		
		return treasureDensity;
	}
	
	/**
	 * Calculates the enemy density by comparing the number of traversable
	 * tiles to the number of enemies.
	 * 
	 * @return The enemy density.
	 */
	public double calculateWallDensitySparsity()
	{
		if(wallDensity > -1.0)
			return wallDensity;
		
		double denseThreshold = 6.0;

		Queue<Node> queue = new LinkedList<Node>();
		ArrayList<Bitmap> wallChunks = new ArrayList<Bitmap>();
		ArrayList<Point> wallPoints = new ArrayList<Point>();
		
		for (int j = 0; j < height; j++)
		{
			for (int i = 0; i < width; i++) 
			{
				if(tileMap[j * width + i].GetType() == TileTypes.WALL)
					wallPoints.add(new Point(i,j));
			}
		}
		
		if(wallPoints.isEmpty())
		{
			wallSparsity = 0.0;
			wallDensity = 0.0;
			return 0.0;
		}

		Node root = new Node(0.0f, wallPoints.remove(0), null);
    	queue.add(root);
    	
    	while(!wallPoints.isEmpty())
    	{
    		Bitmap wallChunk = new Bitmap();
    		
    		while(!queue.isEmpty()){
        		Node current = queue.remove();
        		Tile currentTile = getTile(current.position);
        		
        		//We need to remove the door!
        		wallPoints.remove(current.position);
        		wallChunk.addPoint(Point.castToGeometry(current.position));
        		
        		
        		List<Point> children = getAvailableCoords(current.position);
                for(Point child : children)
                {
                	 
                	if(!wallPoints.contains(child))
                		continue;

                    //Create child node
                    Node n = new Node(0.0f, child, current);
                    queue.add(n);
                }
        	}
    		
    		wallChunk.CalculateMedoid();
    		wallChunks.add(wallChunk);
    		
    		if(!wallPoints.isEmpty())
    			queue.add(new Node(0.0f, wallPoints.get(0), null));
    	}
		
    	double dens = 0.0;
    	double sparse = 0.0;
    	double distances = 0.0;
    	
    	boolean calculateSparsity = wallChunks.size() > 1;
    	
		//CLusters are done, now calculate density
    	for(Bitmap wallChunk : wallChunks)
    	{
    		dens += Math.min(1.0, (double)wallChunk.getPoints().size()/denseThreshold);
    		
    		if(calculateSparsity)
    		{
	    		//CLusters are done, now calculate density
	        	for(Bitmap otherChunk : wallChunks)
	        	{
	        		if(otherChunk.equals(wallChunk))
	        			continue;
	        		
	        		sparse += (double)otherChunk.distManhattan(otherChunk.medoid, wallChunk.medoid)/(width + height);
	        		distances++;
	        	}
    		}
    	}
    	
    	wallDensity = dens/(double)wallChunks.size();
		wallSparsity = calculateSparsity == true ? sparse/distances : 0.0;
		
		return wallDensity;
	}

	/**
	 * Gets the number of treasures in a map.
	 * 
	 * @return The number of treasures.
	 */
	public int getTreasureCount() {
		return treasures.size();
	}

	/**
	 * Calculates the treasure density by comparing the number of traversable
	 * tiles to the number of treasures.
	 * 
	 * @return The treasure density.
	 */
	public double calculateTreasureDensity() {
//		System.out.println("TRABERSE: " + countTraversables());
//		System.out.println("TREASUREEEES: " + treasures.size());
		
		if(treasureDensity > -1.0)
			return treasureDensity;
		
		treasureDensity = (double)treasures.size() / (double)countTraversables();
		return treasureDensity;
	}

	/***
	 * Returns the number of doors in a map
	 * @param entrance if you want to count or not the entrance
	 * @return
	 */
	public int getDoorCount() 
	{
		return doors.size();
	}
	

	/**
	 * Returns the number of wall tiles in a map.
	 * 
	 * @return The number of walls.
	 */
	public int getWallCount() {
		return wallCount;
	}

	/**
	 * Returns the number of non-wall tiles in a map.
	 * 
	 * @return The number of non-wall tiles.
	 */
	public int getNonWallTileCount()
    {
        return (width * height) - wallCount;
    }

	/**
	 * Get the ratio of enemy tiles to non-wall tiles.
	 * 
	 * @return The ratio of enemy tiles to non-wall tiles.
	 */
	public double getEnemyPercentage()
	{
		int allMap = getNonWallTileCount();
		return getEnemyCount()/(double)allMap;
	}

	/**
	 * Get the ratio of treasure tiles to non-wall tiles.
	 * 
	 * @return The ratio of treasure tiles to non-wall tiles.
	 */
	public double getTreasurePercentage()
	{
		int allMap = getNonWallTileCount();
		return getTreasureCount()/(double)allMap;
	}

	/**
	 * Returns the number of treasures.
	 * 
	 * @return The number of treasures.
	 */
	public List<Point> getTreasures() {
		return treasures;
	}

	private void clearFailedPaths()
	{
		failedPathsToTreasures = 0;
		failedPathsToEnemies = 0;
		failedPathsToAnotherDoor = 0;
	}
	
	/**
	 * Increases the number of failed path searches for treasures by one.
	 */
	public void addFailedPathToTreasures() {
		failedPathsToTreasures++;
	}

	/**
	 * Increases the number of failed path searches for enemies by one.
	 */
	public void addFailedPathToEnemies() {
		failedPathsToEnemies++;
	}

	/**
	 * Increases the number of failed path searches for doors by one.
	 */
	public void addFailedPathToDoors() {
		failedPathsToAnotherDoor++;
	}

	/**
	 * Gets the number of failed path searches for doors.
	 * 
	 * @return The number of failed path searches.
	 */
	public int getFailedPathsToAnotherDoor() {
		return failedPathsToAnotherDoor;
	}

	/**
	 * Gets the number of failed path searches for treasures.
	 * 
	 * @return The number of failed path searches.
	 */
	public int getFailedPathsToTreasures() {
		return failedPathsToTreasures;
	}

    /**
     * Gets the number of failed path searches for enemies.
     * 
     * @return The number of failed path searches.
     */
    public int getFailedPathsToEnemies() {
        return failedPathsToEnemies;
    }
    
    /**
     * Sets the safety value for a treasure.
     * 
     * @param treasure The position of the treasure.
     * @param safety The safety value.
     */
    public void setTreasureSafety(Point treasure, double safety) {
    	treasureSafety.put(treasure, safety);
    }
    
    /**
     * Gets the safety value for at treasure.
     * 
     * @param treasure The position of the treasure.
     * @return The safety value.
     */
    public double getTreasureSafety(Point treasure) {
    	return (double) treasureSafety.get(treasure);
    }
    
    /**
     * Gets the complete list of treasure safety values.
     * 
     * @return A dictionary containing all treasures and their safety values.
     */
    public Dictionary<Point, Double> getTreasureSafety() {
    	return treasureSafety;
    }
    
    /**
     * Gets the complete array of treasure safety values.
     * 
     * @return An array of Doubles containing all treasures safety values.
     */
    public Double[] getAllTreasureSafeties()
    {
    	return Collections.list(treasureSafety.elements()).stream().toArray(Double[]::new);
    }
    
    
    /**
     * Gets a list of all enemies in a map.
     * 
     * @return A list of enemies.
     */
    public List<Point> getEnemies() {
    	return enemies;
    }
    
    public void setDoorSafety(Point door, double safety)
    {
    	doorsSafety.put(door, safety);
    }
    
    public void calculateDoorSafeness()
    {
    	double value = 0.0;
    	
    	for (Double safety : doorsSafety.values()) {
    	    value += safety;
    	}
    	
    	doorSafety = value / (double)doorsSafety.size();
    }
    
    public double getDoorSafeness() {return doorSafety;}
    
    public void setDoorGreed(Point door, double greed)
    {
    	doorsGreed.put(door, greed);
    }
    
    public void calculateDoorGreedness()
    {
    	double value = 0.0;
    	
    	for (Double safety : doorsGreed.values()) {
    	    value += safety;
    	}
    	
    	doorGreed = value / (double)doorsGreed.size();
    }
    
    public double getDoorGreedness() {return doorGreed;}
    
    /**
     * Sets the safety value for the map's entry point.
     *TODO: THIS CODE MUST DISAPPEAR
     * 
     * @param safety A safety value.
     */
    public void setEntranceSafety(double safety) {
    	//TODO:
    }
    
    /**
     * Sets the map's entrance "greed" (that is, how close is the nearest treasure?).
     * 
     * @param safety A safety value.
     */
    public void setEntranceGreed(double greed) {
    	//FIXME
    }
    
    /**
     * Gets the safety value for the map's entry point.
     * 
     * @return The safety value.
     */
    public double getEntranceSafety() {
    	return -1.0;
    }
    
    /**
     * Gets the allocation matrix.
     * 
     * @return The allocation matrix.
     */
    public boolean[][] getAllocationMatrix() {
    	return allocated;
    }
    
    /**
     * Gets the pattern finder associated with this map.
     * 
     * @return A pattern finder.
     */
    public PatternFinder getPatternFinder() {
    	return finder;
    }
    
    public Tile[] getTileBasedMap()
    {
    	return tileMap;
    }
    
	/**
	 * Initialises a map.
	 * 
	 * @param tiles A list of tiles.
	 */
	private void initMapFromTypes(TileTypes[] tiles) {
		enemies.clear();
		treasures.clear();
		
		int tile = 0;
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				switch (tiles[tile]) {
				case WALL:
					wallCount++;
					break;
				case ENEMY:
					enemies.add(new Point(i, j));
					break;
				case TREASURE:
					treasures.add(new Point(i, j));
					break;
				default:
					break;
				}
				tileMap[tile] = new Tile(i , j, tiles[tile]);
				matrix[j][i] = tiles[tile++].getValue();

			}
		}
	}
	
	//TODO: THIS METHOD IS GOING TO CHANGE NOW-- SOON!
	public GeneratorConfig getCalculatedConfig(){
		GeneratorConfig newConfig = new GeneratorConfig(config);
		//Make a new pattern finder in case the map has been manually edited since the patterns were found
		//finder = new PatternFinder(this);

		List<Enemy> enemies = new ArrayList<Enemy>();
		List<Treasure> treasures = new ArrayList<Treasure>();
		List<Corridor> corridors = new ArrayList<Corridor>();
		List<Connector> connectors = new ArrayList<Connector>();
		List<Chamber> chambers = new ArrayList<Chamber>();

		for (Pattern p : finder.findMicroPatterns()) {
			if (p instanceof Enemy) {
				enemies.add((Enemy) p);
			} else if (p instanceof Treasure) {
				treasures.add((Treasure) p);
			} else if (p instanceof Corridor) {
				corridors.add((Corridor) p);
			} else if (p instanceof Connector) {
				connectors.add((Connector) p);
			} else if (p instanceof Chamber) {
				chambers.add((Chamber) p);
			}
		}

		//FIXME: Now is time to fix you! 
		//TODO: Also take into account other patterns!!!

		//CORRIDOR LENGTH

		double rawCorridorArea = 0;

		for(Pattern p : corridors){
			rawCorridorArea += ((Polygon)p.getGeometry()).getArea();
		}

		int avgCorridorLength = (int)Math.ceil(rawCorridorArea/corridors.size());
		newConfig.setCorridorTargetLength(Math.max(3,avgCorridorLength));

		//ROOM AND CORRIDOR RATIOS

		double passableTiles = getNonWallTileCount();
		double rawRoomArea = 0.0;
		double totalSquareness = 0.0;

		for(Pattern p : chambers){
			rawRoomArea += ((Polygon)p.getGeometry()).getArea();
			totalSquareness += ((Chamber)p).getSquareness();
		}

		double roomProportion = rawRoomArea / passableTiles;

		newConfig.setRoomProportion(roomProportion);
		newConfig.setCorridorProportion(1.0 - roomProportion);

		//CHAMBER AREA

		if(chambers.size() > 0){
			int avgArea = (int)Math.ceil(rawRoomArea/chambers.size());
			newConfig.setChamberTargetArea(avgArea);
		}
		//CHAMBER SQUARENESS AND SIZE

		//		if(rooms.size() > 0){
		//			double avgSquareness = totalSquareness / rooms.size();
		//			newConfig.setChamberTargetSquareness(avgSquareness);
		//			newConfig.setChamberAreaCorrectness(1.0 - avgSquareness);
		//		}

		//ENEMIES
		double enemyProportion = (double)enemies.size()/passableTiles;
		newConfig.setEnemyQuantityRange(enemyProportion, enemyProportion);

		//TREASURE
		double treasureProportion = (double)treasures.size()/passableTiles;
		newConfig.setTreasureQuantityRange(treasureProportion, treasureProportion);

		return newConfig;
	}


	/**
	 * Exports this map as a 2D matrix of integers
	 * 
	 * @return A matrix of integers.
	 */
	public int[][] toMatrix() {
		return matrix;
	}

	/**
	 * Hacky load map implementation - TODO: should probably not be here
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void LoadMap(File file) throws IOException{
		FileReader reader = new FileReader(file);
		String mapString = "";
		int lineCounter = 0;
		int charCounter = 0;
		while(reader.ready()){
			char c = (char) reader.read();
			mapString += c;
		}
		Room room = fromString(mapString);
		PatternFinder finder = room.getPatternFinder();
		MapContainer result = new MapContainer();
		result.setMap(room);
		result.setMicroPatterns(finder.findMicroPatterns());
		result.setMesoPatterns(finder.findMesoPatterns());
		result.setMacroPatterns(finder.findMacroPatterns());
		EventRouter.getInstance().postEvent(new MapLoaded(result));
	}
	
	/**
	 * Intra Room Feasibility refers to the feasibility within the room and is calculated as follows:
	 * 1) At least one type of all tile type
	 * 2) At least 1 door (which will be used as the entrance)
	 * 3) No unreachable tiles (areas) that do not have doors within the area (In the future it can be other ways of access) 
	 * USE: This is used by the evolutionary algorithm
	 * @return true if the room is intra feasible
	 */
	public boolean isIntraFeasible()
	{
    	Queue<Node> queue = new LinkedList<Node>();
    	clearFailedPaths();
    	walkableTiles.clearAllPoints();
    	int treasure = 0;
    	int enemies = 0;
    	int doors = 0;
    	
    	//TODO: CREISI
    	ArrayList<Point> walkableSpaces = new ArrayList<Point>();
    	ArrayList<RoomSection> walkableSections = new ArrayList<RoomSection>();
    	
    	for (int j = 0; j < height; j++) 
		{
			for (int i = 0; i < width; i++) 
			{	
				switch (TileTypes.toTileType(matrix[j][i])) {
				case WALL:
					break;
				default:
					walkableSpaces.add(new Point(i,j));
					break;
				}
			}
		}
		
    	
    	Node root = new Node(0.0f, this.getDoors().get(0), null);
    	queue.add(root);
    	
    	while(!walkableSpaces.isEmpty())
    	{
    		RoomSection section = new RoomSection();
    		
    		while(!queue.isEmpty()){
        		Node current = queue.remove();
        		Tile currentTile = getTile(current.position);
        		
        		//We need to remove the door!
        		walkableSpaces.remove(current.position);

        		if(currentTile.GetType() == TileTypes.DOOR)
        		{
        			doors++;
        			section.doorFound();
        		}
        		else if (currentTile.GetType().isEnemy())
        			section.addEnemy();
        		else if (currentTile.GetType().isTreasure())
        			section.addTreasure();
        		
        		List<Point> children = getAvailableCoords(current.position);
                for(Point child : children)
                {
                	if(!walkableSpaces.contains(child))
                		continue;
                	
            		walkableSpaces.remove(child);
            		section.addPoint(child);

                    //Create child node
                    Node n = new Node(0.0f, child, current);
                    queue.add(n);
                }
        	}
    		
    		walkableSections.add(section);
    		
    		if(!walkableSpaces.isEmpty())
    			queue.add(new Node(0.0f, walkableSpaces.get(0), null));
    	}
    	
    	//TODO: This is a super testing part!
//    	clearPath();
//    	
//    	for(int i = 1; i < walkableSections.size(); ++i)
//    	{
//    		if(!walkableSections.get(i).hasDoor())
//    		{
//        		for(Point walkableTile : walkableSections.get(i).getPositions())
//        		{
//        			path.addPoint(Point.castToGeometry(walkableTile));
//        		}
//    		}
//
//    	}
//    	
//    	paintPath(true);
    	//TODO: PART OF THE TESTING
    	
    	boolean sectionsReachable = true;
    	
    	//What is reachable and what is not!
    	for(RoomSection section : walkableSections)
		{
			if(section.hasDoor())
			{
				treasure += section.getTreasures();
				enemies += section.getEnemies();
				walkableTiles.AddAllPoints(section.getPositions().stream().map(Point::castToGeometry).collect(Collectors.toList()));
			}
			else
			{
				sectionsReachable = false;
			}
		}

    	//TODO: check roomsection reachability

    	for(int i = treasure; i < getTreasureCount();i++)
    		addFailedPathToTreasures();
    	for(int i = doors; i < getDoorCount();i++)
    		addFailedPathToDoors();
    	for(int i = enemies; i < getEnemyCount();i++)
    		addFailedPathToEnemies();
    	
    	
    	//TODO: THE _future_ renewed warning canvas should consider every reason why it is not feasible probably a feasible clasS? that holds the info
    	//For testing purposes
//    	System.out.println(treasure + "=" + getTreasureCount());
//    	System.out.println(doors + "=" + getDoorCount(true));
//    	System.out.println(enemies + "=" + getEnemyCount());
//    	System.out.println(allSectionsReachable(walkableSections));

    	return  (treasure + doors + enemies == getTreasureCount() + getDoorCount() + getEnemyCount()) //Same amount of treasure+enemies+doors
//    			&& getTreasureCount() > 0 && getEnemyCount() > 0 //Finns at least 1(one) enemy and one treasure
    			&& sectionsReachable; //All sections in the room are reachable!!!
		
//		return true;
	}
	
	public boolean walkableSectionsReachable()
	{
		Queue<Node> queue = new LinkedList<Node>();
    	int treasure = 0;
    	int enemies = 0;
    	int doors = 0;
    	
    	//TODO: CREISI
    	ArrayList<Point> walkableSpaces = new ArrayList<Point>();
    	ArrayList<RoomSection> walkableSections = new ArrayList<RoomSection>();
    	
    	for (int j = 0; j < height; j++) 
		{
			for (int i = 0; i < width; i++) 
			{	
				switch (TileTypes.toTileType(matrix[j][i])) {
				case WALL:
					break;
				default:
					walkableSpaces.add(new Point(i,j));
					break;
				}
			}
		}
		
    	
    	Node root = new Node(0.0f, this.getDoors().get(0), null);
    	queue.add(root);
    	
    	while(!walkableSpaces.isEmpty())
    	{
    		RoomSection section = new RoomSection();
    		
    		while(!queue.isEmpty()){
        		Node current = queue.remove();
        		Tile currentTile = getTile(current.position);
        		
        		//We need to remove the door!
        		walkableSpaces.remove(current.position);

        		if(currentTile.GetType() == TileTypes.DOOR)
        		{
        			doors++;
        			section.doorFound();
        		}
        		else if (currentTile.GetType().isEnemy())
        			section.addEnemy();
        		else if (currentTile.GetType().isTreasure())
        			section.addTreasure();
        		
        		List<Point> children = getAvailableCoords(current.position);
                for(Point child : children)
                {
                	if(!walkableSpaces.contains(child))
                		continue;
                	
            		walkableSpaces.remove(child);
            		section.addPoint(child);

                    //Create child node
                    Node n = new Node(0.0f, child, current);
                    queue.add(n);
                }
        	}
    		
    		walkableSections.add(section);
    		
    		if(!walkableSpaces.isEmpty())
    			queue.add(new Node(0.0f, walkableSpaces.get(0), null));
    	}
    	
    	boolean sectionsReachable = true;
    	
    	//What is reachable and what is not!
    	for(RoomSection section : walkableSections)
		{
			if(section.hasDoor())
			{
				treasure += section.getTreasures();
				enemies += section.getEnemies();
			}
			else
			{
				sectionsReachable = false;
			}
		}
    	
    	return sectionsReachable;
	}
	
	private boolean allSectionsReachable(ArrayList<RoomSection> sections)
	{
		for(RoomSection section : sections)
		{
			if(!section.hasDoor())
				return false;
		}
		
		return true;
	}
	
	/**
	 * Inter Room Feasibility refers to the feasibility of the room in the dungeon and the connections. It is calculated as follows:
	 * 1) Room is reachable from the initial room (and initial position)
	 * 2) All doors must be reachable from the initial room (and initial position)
	 * USE: This is used by the dungeon but maybe it should also be used by the evolutionary run, in case it blocks some space
	 * @return true if the room is Inter feasible
	 */
	public boolean isInterFeasible(boolean visualize)
	{
		this.localConfig.getWorldCanvas().setInterFeasibilityVisible(visualize);
		
		if(!visualize) return false; //Maybe is better to just calculate the feasibility but do not show it
		
		//I think I should first check if things have change? before doing this  as it can be heavy
		nonInterFeasibleTiles.clearAllPoints();

		boolean interFeasible = true;
		
		//this basically is, if the room is not reachable through the GRAPH it is completely infeasible
		if(!owner.ttNetwork(this))
		{
			for (int j = 0; j < height; j++) 
			{
				for (int i = 0; i < width; i++) 
				{	
					nonInterFeasibleTiles.addPoint(Point.castToGeometry(new Point(i,j)));
					
				}
			}
	
			this.localConfig.getWorldCanvas().drawInterFeasibility();
	    	
			return false;
		}
		
		//Now lets check each door, can we reach them from the initial room?
		for(Point door : doors)
		{
			if(!owner.traverseTillDoor(this, door))
			{
				nonInterFeasibleTiles.addPoint(Point.castToGeometry(door));
				interFeasible = false;
			}
		}
		
		this.localConfig.getWorldCanvas().drawInterFeasibility();
		
		return interFeasible;
	}

	//TODO: Double check maybe it can be useful to know this
	public boolean EveryRoomVisitable(){
		List<Node> visited = new ArrayList<Node>();
    	Queue<Node> queue = new LinkedList<Node>();
    	int treasure = 0;
    	int enemies = 0;
    	int doors = 0;
    	
    	Node root = new Node(0.0f, this.doors.get(0), null);
    	queue.add(root);
    	
    	while(!queue.isEmpty()){
    		Node current = queue.remove();
    		visited.add(current);
    		Tile currentTile = getTile(current.position);
    		
    		if(currentTile.GetType() == TileTypes.DOOR)
    			doors++;
    		else if (currentTile.GetType().isEnemy())
    			enemies++;
    		else if (currentTile.GetType().isTreasure())
    			treasure++;
    		
    		List<Point> children = getAvailableCoords(current.position);
            for(Point child : children)
            {
                if (visited.stream().filter(x->x.equals(child)).findFirst().isPresent() 
                		|| queue.stream().filter(x->x.equals(child)).findFirst().isPresent()) 
                	continue;

                //Create child node
                Node n = new Node(0.0f, child, current);
                queue.add(n);
            }
    	}

    	
    	return visited.size() == getNonWallTileCount();
	}
	
	public boolean isPointInBorder(Point p)
	{
		return borders.contains(Point.castToGeometry(p));
	}
	
	public void calculateAllDimensionalValues() //TODO:
	{
		dimensionValues = new HashMap<DimensionTypes, Double>();
		
		for(DimensionTypes dimension : DimensionTypes.values())
        {
        	if(dimension != DimensionTypes.DIFFICULTY && dimension != DimensionTypes.GEOM_COMPLEXITY && dimension != DimensionTypes.REWARD)
        	{
        		dimensionValues.put(dimension, GADimension.calculateIndividualValue(dimension, this));
        	}
        }
	}
	
	public void setSpeficidDimensionValue(DimensionTypes dimension, double value)
	{
		dimensionValues.put(dimension, value);
	}
	
	public void SetDimensionValues(ArrayList<GADimension> dimensions)
	{
		dimensionValues = new HashMap<DimensionTypes, Double>();
		
		for(GADimension dimension : dimensions)
		{
			dimensionValues.put(dimension.GetType(), dimension.CalculateValue(this, this));
		}
	}
	
	public void SetDimensionValues(HashMap<DimensionTypes, Double> dimensions)
	{
		if(dimensions != null)
		{
			dimensionValues = new HashMap<DimensionTypes, Double>(dimensions);
		}
		
//		for(GADimension dimension : dimensions)
//		{
//			dimensionValues.put(dimension.GetType(), dimension.CalculateValue(this, this));
//		}
	}
	
	public double getDimensionValue(DimensionTypes currentDimension)
	{
		if(dimensionValues == null || !dimensionValues.containsKey(currentDimension)) return -1.0;
		return dimensionValues.get(currentDimension);
	}
	
	public double calculateWallDensity()
	{
//		double wc = (double)getWallCount();
//		double size =(double)(getRowCount() * getColCount());
//		double den = (double)getWallCount() / (double)(getRowCount() * getColCount());
		if(wallDensity > -1.0f)
			return wallDensity;
		
		wallDensity = (double)getWallCount() / (double)((getRowCount() -1) * (getColCount() -1));
		return wallDensity;
	}
	
	public double calculateWallSparsity()
	{
		if(wallSparsity > -1.0f)
			return wallSparsity;
			
		Queue<Node> queue = new LinkedList<Node>();
		ArrayList<Bitmap> wallChunks = new ArrayList<Bitmap>();
		ArrayList<Point> wallPoints = new ArrayList<Point>();
		
		for (int j = 0; j < height; j++)
		{
			for (int i = 0; i < width; i++) 
			{
				if(tileMap[j * width + i].GetType() == TileTypes.WALL)
					wallPoints.add(new Point(i,j));
			}
		}
		
		if(wallPoints.isEmpty())
		{
			wallSparsity = 0.0;
			return 0.0;
		}
		
		Node root = new Node(0.0f, wallPoints.remove(0), null);
    	queue.add(root);
    	
    	while(!wallPoints.isEmpty())
    	{
    		Bitmap wallChunk = new Bitmap();
    		
    		while(!queue.isEmpty()){
        		Node current = queue.remove();
        		Tile currentTile = getTile(current.position);
        		
        		//We need to remove the door!
        		wallPoints.remove(current.position);
        		wallChunk.addPoint(Point.castToGeometry(current.position));
        		
        		
        		List<Point> children = getNonAvailableCoords(current.position);
                for(Point child : children)
                {
                	if(!wallPoints.contains(child))
                		continue;

                    //Create child node
                    Node n = new Node(0.0f, child, current);
                    queue.add(n);
                }
        	}
    		
    		wallChunk.CalculateMedoid();
    		wallChunks.add(wallChunk);
    		
    		if(!wallPoints.isEmpty())
    			queue.add(new Node(0.0f, wallPoints.get(0), null));
    	}
    	
    	if(wallChunks.size() < 2)
    	{
    		wallSparsity = 0.0;
    		return 0.0;
    	}
    	
//    	Bitmap current = wallChunks.remove(0);
    	double sparseness = 0.0;
    	double chunkSizes = wallChunks.size();
//    	while(!wallChunks.isEmpty())
//    	{
//    		int minDist = Integer.MAX_VALUE;
//    		Bitmap next = null;
//    		for(Bitmap otherChunk : wallChunks)
//        	{
//    			int dist = current.distManhattan(current.medoid, otherChunk.medoid);
//    			if(dist < minDist)
//    			{
//    				minDist = dist;
//    				next = otherChunk;
//    			}
//        		
//        	}
//    		
//    		sparseness += minDist;
//    		current = next;
//    		wallChunks.remove(next);
//    	}
    	
    	for(Bitmap otherChunk : wallChunks)
    	{
    		int minDist = Integer.MAX_VALUE;
//    		Bitmap next = null;
    		for(Bitmap o : wallChunks)
        	{
    			if(o.equals(otherChunk))
    				continue;
    			
    			int dist = otherChunk.distManhattan(otherChunk.medoid, o.medoid);
    			if(dist < minDist)
    			{
    				minDist = dist;
//    				next = otherChunk;
    			}
        	}
    		sparseness += minDist;
    	
    	}
    	
//    	System.out.println("SPARSE: " + sparseness);
//    	System.out.println("DENOMINATOR: " + (double)((height-1 + width-1)*chunkSizes));
//    	System.out.println("ANOTHER CALCULATION: " + chunkSizes/sparseness);
//    	System.out.println("MAYBE CORRECT: " + (1.0 - chunkSizes/sparseness)); // TODO:CHECK THIS
//    	System.out.println("FINAL: " + (chunkSizes/sparseness)/(height-1 + width-1));
    	sparseness = sparseness/(double)((height-1 + width-1)*chunkSizes);
//    	System.out.println("SPARSE I USE: " + sparseness); // TODO:CHECK THIS
    	wallSparsity = sparseness;
    	return wallSparsity;
	}
	
	public double calculateEnemySparsity()
	{
		if(enemySparsity > -1.0f)
			return enemySparsity;
		
		Queue<Node> queue = new LinkedList<Node>();
		ArrayList<Bitmap> enemyChunks = new ArrayList<Bitmap>();
		ArrayList<Point> enemyPoints = new ArrayList<Point>(enemies);
		
		if(enemyPoints.isEmpty())
		{
			enemySparsity = 0.0;
			return 0.0;
		}

		Node root = new Node(0.0f, enemyPoints.remove(0), null);
    	queue.add(root);
    	
    	while(!enemyPoints.isEmpty())
    	{
    		Bitmap enemyChunk = new Bitmap();
    		
    		while(!queue.isEmpty()){
        		Node current = queue.remove();
        		Tile currentTile = getTile(current.position);
        		
        		//We need to remove the door!
        		enemyPoints.remove(current.position);
        		enemyChunk.addPoint(Point.castToGeometry(current.position));
        		
        		
        		List<Point> children = getAvailableCoords(current.position);
                for(Point child : children)
                {
                	 
                	if(!enemyPoints.contains(child))
                		continue;

                    //Create child node
                    Node n = new Node(0.0f, child, current);
                    queue.add(n);
                }
        	}
    		
    		enemyChunk.CalculateMedoid();
    		enemyChunks.add(enemyChunk);
    		
    		if(!enemyPoints.isEmpty())
    			queue.add(new Node(0.0f, enemyPoints.get(0), null));
    	}
    	
    	if(enemyChunks.size() < 2)
    	{
    		enemySparsity = 0.0;
    		return 0.0;
    	}
    	
//    	Bitmap current = wallChunks.remove(0);
    	double sparseness = 0.0;
    	double chunkSizes = enemyChunks.size();
    	
    	for(Bitmap otherChunk : enemyChunks)
    	{
    		int minDist = Integer.MAX_VALUE;
//    		Bitmap next = null;
    		for(Bitmap o : enemyChunks)
        	{
    			if(o.equals(otherChunk))
    				continue;
    			
    			int dist = otherChunk.distManhattan(otherChunk.medoid, o.medoid);
    			if(dist < minDist)
    			{
    				minDist = dist;
//    				next = otherChunk;
    			}
        	}
    		sparseness += minDist;
    	
    	}
    	
//    	System.out.println("SPARSE: " + sparseness);
//    	System.out.println("DENOMINATOR: " + (double)((height-1 + width-1)*chunkSizes));
//    	System.out.println("ANOTHER CALCULATION: " + chunkSizes/sparseness);
//    	System.out.println("MAYBE CORRECT: " + (1.0 - chunkSizes/sparseness)); // TODO:CHECK THIS
//    	System.out.println("FINAL: " + (chunkSizes/sparseness)/(height-1 + width-1));
    	sparseness = sparseness/(double)((height-1 + width-1)*chunkSizes);
//    	System.out.println("SPARSE I USE: " + sparseness); // TODO:CHECK THIS
    	
    	enemySparsity = sparseness;
    	return enemySparsity;
	}
	
	public double calculateTreasureSparsity()
	{
		if(treasureSparsity > -1.0f)
			return treasureSparsity;
		
		Queue<Node> queue = new LinkedList<Node>();
		ArrayList<Bitmap> treasureChunks = new ArrayList<Bitmap>();
		ArrayList<Point> treasurePoints = new ArrayList<Point>(treasures);
		
		if(treasurePoints.isEmpty())
		{
			treasureSparsity = 0.0;
			return 0.0;
		}

		Node root = new Node(0.0f, treasurePoints.remove(0), null);
    	queue.add(root);
    	
    	while(!treasurePoints.isEmpty())
    	{
    		Bitmap treasureChunk = new Bitmap();
    		
    		while(!queue.isEmpty()){
        		Node current = queue.remove();
        		Tile currentTile = getTile(current.position);
        		
        		//We need to remove the door!
        		treasurePoints.remove(current.position);
        		treasureChunk.addPoint(Point.castToGeometry(current.position));
        		
        		
        		List<Point> children = getAvailableCoords(current.position);
                for(Point child : children)
                {
                	 
                	if(!treasurePoints.contains(child))
                		continue;

                    //Create child node
                    Node n = new Node(0.0f, child, current);
                    queue.add(n);
                }
        	}
    		
    		treasureChunk.CalculateMedoid();
    		treasureChunks.add(treasureChunk);
    		
    		if(!treasurePoints.isEmpty())
    			queue.add(new Node(0.0f, treasurePoints.get(0), null));
    	}
    	
    	if(treasureChunks.size() < 2)
    	{
    		treasureSparsity = 0.0;
    		return 0.0;
    	}
    	
//    	Bitmap current = wallChunks.remove(0);
    	double sparseness = 0.0;
    	double chunkSizes = treasureChunks.size();
    	
    	for(Bitmap otherChunk : treasureChunks)
    	{
    		int minDist = Integer.MAX_VALUE;
//    		Bitmap next = null;
    		for(Bitmap o : treasureChunks)
        	{
    			if(o.equals(otherChunk))
    				continue;
    			
    			int dist = otherChunk.distManhattan(otherChunk.medoid, o.medoid);
    			if(dist < minDist)
    			{
    				minDist = dist;
//    				next = otherChunk;
    			}
        	}
    		sparseness += minDist;
    	
    	}
    	
//    	System.out.println("SPARSE: " + sparseness);
//    	System.out.println("DENOMINATOR: " + (double)((height-1 + width-1)*chunkSizes));
//    	System.out.println("ANOTHER CALCULATION: " + chunkSizes/sparseness);
//    	System.out.println("MAYBE CORRECT: " + (1.0 - chunkSizes/sparseness)); // TODO:CHECK THIS
//    	System.out.println("FINAL: " + (chunkSizes/sparseness)/(height-1 + width-1));
    	sparseness = sparseness/(double)((height-1 + width-1)*chunkSizes);
//    	System.out.println("SPARSE I USE: " + sparseness);//TODO: CHECK THIS
    	treasureSparsity = sparseness;
    	return treasureSparsity;
	}
	
	
	
	////////////////////////// TESTING PATHS TO ALL DOORS ////////////////////////////////////////////////
	
	//This has problems once you start to lock rooms
	//* This definetely should be done another way, I think that the paths should be calculated by the patterns itself and
	// then used as a way to show the linearity value... moreover, maybe it is interesting to show paths within the room
	public int LinearityWithinRoom()
	{
		int pathCounter = 1;
		
		if(getDoorCount() == 1) return 1;
		
		finder.graph.Node<Pattern> anyDoor = null;
		Queue<finder.graph.Node<Pattern>> patternQueue = new LinkedList<finder.graph.Node<Pattern>>();
		
		//We get all Spatial patterns that contains a door!
		for(finder.graph.Node<Pattern> nodePattern : finder.getPatternGraph().getNodes().values())
		{
			if(nodePattern.getValue() instanceof SpacialPattern)
			{
				SpacialPattern sp = (SpacialPattern)nodePattern.getValue();
				
//				if(sp.getContainedPatterns().stream().filter((Pattern p) -> {return p instanceof Entrance;}).findAny().orElse(null) != null)
//				{
//					anyDoor = nodePattern;
//				}
//				else 
				if(sp.getContainedPatterns().stream().filter((Pattern p) -> {return p instanceof Door;}).findAny().orElse(null) != null &&
						!patternQueue.contains(nodePattern))
				{
					patternQueue.add(nodePattern);
					
					if(anyDoor == null)
					{
						anyDoor = nodePattern;
					}
					
				}
				
			}
		}
		
		List<Pattern> patterns = new ArrayList<Pattern>();
		List<Pattern> finalPatterns = new ArrayList<Pattern>(); //IDK if I should add everything together! 
		
		if(patternQueue.size() == 1)
			return 1;
		
		while(!patternQueue.isEmpty())
		{
			finder.getPatternGraph().resetGraph();
			finder.graph.Node<Pattern> current = patternQueue.remove();
			int auxCounter = 0;
			
			if(current.getValue().equals(anyDoor.getValue()))
				continue;
			
			pathCounter += search(current, null, anyDoor, finder.getPatternGraph(), auxCounter, new ArrayList<finder.graph.Node<Pattern>> () );
		}
		
		return pathCounter;
	}
	
	//This should return how many paths from the node pattern of the entrance to the node pattern of a door!
	private int search(finder.graph.Node<Pattern> nodePattern, finder.graph.Node<Pattern> prev 
			/*, List<Pattern> deadEndPatterns*/, finder.graph.Node<Pattern> target, Graph<Pattern> patternGraph, int counter,
			List<finder.graph.Node<Pattern>> trail)
	{

		if(nodePattern.getValue() instanceof SpacialPattern)
		{
			SpacialPattern sp = (SpacialPattern)nodePattern.getValue();
			trail.add(nodePattern);
			
			
//			micropatterns.stream().filter((Pattern p) -> {return p instanceof Entrance;}).findFirst().get();
			
			if(sp.getContainedPatterns().contains(target.getValue()) || sp.equals(target.getValue()))
			{
				counter += 1;
				return counter;
//				System.out.println("CONTAINS!");
			}

			if(nodePattern.isVisited())
				return counter;
			
			nodePattern.tryVisit();
			
			for(Edge<Pattern> e : nodePattern.getEdges())
			{
				finder.graph.Node<Pattern> n = getOtherNode(e,nodePattern);
				if(n != prev && !trail.contains(n))
				{
//					n.tryVisit();
					counter = search(n, nodePattern, target, patternGraph, counter, new ArrayList<finder.graph.Node<Pattern>> (trail));
				}
			}
			
			return counter;
		}
		
		return counter;
	}
	
	private finder.graph.Node<Pattern> getOtherNode(Edge<Pattern> e, finder.graph.Node<Pattern> node){
		if (e.getNodeA() == node)
			return e.getNodeB();
		return e.getNodeA();
	}
	
	public int TraverseTo(int counter, Point source, Point target, Stack<Point> visited, ArrayList<Point> path)
	{
//		if(r == end) //Is done
//		{
//			Stack<Room> temp = new Stack<Room>();
//			for(Room rcp : ConnectionPath)
//			{
//				temp.push(rcp);
////				System.out.println("ROOM " + rooms.indexOf(rcp));
//			}
//			temp.push(r);
////			temp.push(init);
//			connectionPaths.add(temp);
//		}
//		else if(!ConnectionPath.contains(r))
//		{
////			ConnectionPath.push(r);
//			testTraverseNetwork(r, end);
//			ConnectionPath.pop();
//		}
		visited.push(source);
		if(source.equals(target))
		{
			counter += 1;
			return counter;
		}
		
		for(Point neighbor : getAvailableCoords(source))
		{
			if(visited.contains(neighbor))
				continue;
			
//			path.add(neighbor);

			counter = TraverseTo(counter, neighbor, target, visited, path);
			visited.pop();
//			path.remove(neighbor);
		}
		
		return counter;
	}
	
	/////////////////////////////// A* INTERNAL PATHFINDING  ///////////////////////////////////////////////////

	public boolean pathExists(Point start, Point end) //for now it will just apply A*
	{
		return pathfinder.calculateBestPath(start, end);
	}
	
	public void applyPathfinding(Point start, Point goal)
	{
		pathfinder.calculateBestPath(start, goal);
//		pathfinder.printPath();
		
		for(PathFindingTile pft : pathfinder.path)
		{
			path.addPoint(Point.castToGeometry(pft.position));
		}
	}
	
	public void clearPath()
	{
		path.clearAllPoints();
		paintPath(false);
	}
	
	public void paintPath(boolean paint)
	{
		this.localConfig.getWorldCanvas().forcePathDrawing(paint);
	}
	
	///////////////////////////////END ---- A* INTERNAL PATHFINDING  ///////////////////////////////////////////////////
	
	////////////////////////////// TILE INFORMATION /////////////////////////////
	
	private boolean CheckCustomTile(Tile tileToCheck, int maxAmount)
	{
		int current = 0;
		
		for(Tile tile : customTiles)
		{
			if(tile.GetType().equals(tileToCheck.GetType()))
			{
				current++;
			}
		}
		
		return !(current == maxAmount); //Return false if we cannot place the tile
	}
	
	private void ReplaceAllTiles(Tile prevCustom)
	{
		for(finder.geometry.Point p : prevCustom.GetPositions())
		{
			setTile(p.getX(), p.getY(), new FloorTile(p, TileTypes.FLOOR));
		}
	}
	
	private Tile returnFirstInstance(Tile customTile)
	{
		for(Tile tile : customTiles)
		{
			if(tile.GetType().equals(customTile.GetType()))
			{
				return tile;
			}
		}
		
		return null;
	}
	
	public Tile addCustomTile(Tile customTile, int maxAmount)
	{
		if(CheckCustomTile(customTile, maxAmount))
		{
			customTiles.add(customTile);
			
			if(customTile instanceof BossEnemyTile)
			{
				owner.addBoss((BossEnemyTile) customTile);
			}
			
		}
		else
		{
			Tile prevCustom = returnFirstInstance(customTile);
			
			if(prevCustom != null)
			{
				if(customTile instanceof BossEnemyTile)
				{
					owner.replaceBoss((BossEnemyTile) customTile, (BossEnemyTile) prevCustom);
				}
				
				ReplaceAllTiles(prevCustom);
				customTiles.remove(prevCustom);
				customTiles.add(customTile);
				return new FloorTile(prevCustom);
			}
		}
		
		return null;
	}
	
	private Tile getCustom(Tile other)
	{
		for(Tile tile : customTiles)
		{
			if(tile.GetPositions().contains(other.GetCenterPosition()))
			{
				return tile;
			}
		}
		
		return null;
	}
	
	public Tile replaceCustomForNormal(Tile customTile)
	{
		Tile custom = getCustom(customTile);
		System.out.println(owner.getBosses().contains(custom));
		
		if(custom.GetType() == TileTypes.ENEMY_BOSS)
		{
			owner.removeBoss((BossEnemyTile) custom);
		}
		
		ReplaceAllTiles(custom);
		customTiles.remove(custom);
		return new FloorTile(custom);
	}
	
	
	
	/////////////////////////////// LOADING MAPS AND STRING DEBUG //////////////////////////////////////////////

	//TODO: This will need to be REMADE how to load/save
	/**
	 * Builds a map from a string representing a rectangular room. Each row in
	 * the string, separated by a newline (\n), represents a row in the
	 * resulting map's matrix.
	 * 
	 * @param string A string
	 */
	public static Room fromString(String string) {
		String[] rows = string.split("[\\r\\n]+");
		// Had we just stuck to the specs, this wouldn't have been necessary...
		if (rows.length < 2) {
			rows = string.split("[\\n]+");
		}
		int rowCount = rows.length;
		int colCount = rows[0].length();
		TileTypes type = null;



		Room room = new Room(rowCount, colCount);
		try {
			room.setConfig(new GeneratorConfig());
		} catch (MissingConfigurationException e) {
			e.printStackTrace();
		}
//
//		Game.doors.clear();
//		
////		for (int j = 0; j < colCount; j++){
////			 for (int i = 0; i < rowCount; i++) {
//
//		Point p1 = null;
//		Point p2 = null;
//		Point p3 = null;
//		Point p4 = null;
//		
//		//TODO: CHANGE THIS, Is too hardcoded, why use this values when we have the columns and rows?
//		// South
//		Point south = new Point(11/2, 11-1);
//		// East
//		Point east = new Point(11-1, 11/2);
//		// North
//		Point north = new Point(11/2, 0);
//		// West
//		Point west = new Point(0, 11/2);
//
//		for (int i = 0; i < rowCount; i++) {
//			for (int j = 0; j < colCount; j++) {
//				type = TileTypes.toTileType(Integer.parseInt("" + rows[i].charAt(j), 16));
//				room.setTile(i, j, type);
//				switch (type) {
//				case WALL:
//					room.wallCount++;
//					break;
//				case ENEMY:
//					room.enemies.add(new Point(i, j));
//					break;
//				case TREASURE:
//					room.treasures.add(new Point(i, j));
//					break;
//				case DOOR:
//					room.addDoor(new Point(i, j));
//					Point temp = new Point(i, j);
//
//					if(temp.equals(north)) {
//						p1 = temp;
//					}
//					if(temp.equals(east)) {
//						p2 = temp;
//					}
//					if(temp.equals(south)) {
//						p3 = temp;
//					}
//					if(temp.equals(west)) {
//						p4 = temp;
//					}
//
//					Game.doors.add(new Point(i,j));
//					break;
//				case DOORENTER:
//					room.setEntrance(new Point(i, j));
//					Point temp2 = new Point(i, j);
//
//					if(temp2.equals(north)) {
//						p1 = temp2;
//					}
//					if(temp2.equals(east)) {
//						p2 = temp2;
//					}
//					if(temp2.equals(south)) {
//						p3 = temp2;
//					}
//					if(temp2.equals(west)) {
//						p4 = temp2;
//					}
//					room.addDoor(new Point(i, j));
//					Game.doors.add(0, new Point(i,j));
//					break;
//				default:
//				}
//
//			}
//
//		}
//		if (Game.doors.isEmpty()) {
//			room = new Room(11, 11, 0); //TODO: ??
//		}
//		else {
//			GeneratorConfig gc;
//			try {
//				gc = new GeneratorConfig();
//				Room newMap = new Room (gc, 11, 11, p1, p2, p3, p4);
//
//				for (int i = 0; i < rowCount; i++) {
//					for (int j = 0; j < colCount; j++) {
//						type = TileTypes.toTileType(Integer.parseInt("" + rows[i].charAt(j), 16));
//						room.setTile(i, j, type);
//						switch (type) {
//						case WALL:
//							newMap.wallCount++;
//							Point temp = new Point(i, j);
//							newMap.matrix[temp.getX()][temp.getY()] = 1;
//							break;
//						case ENEMY:
//							newMap.enemies.add(new Point(i, j));
//							Point temp2 = new Point(i, j);
//							newMap.matrix[temp2.getX()][temp2.getY()] = 3;
//							break;
//						case TREASURE:
//							newMap.treasures.add(new Point(i, j));
//							Point temp3 = new Point(i, j);
//							newMap.matrix[temp3.getX()][temp3.getY()] = 2;
//							break;
//						default:
//						}
//
//					}
//
//				}
//
//
//				room = newMap;
//			} catch (MissingConfigurationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

		return room;
	}
//
//	@Override
//	public String toString() {
//		StringBuilder map = new StringBuilder();
//
//		for (int j = 0; j < height; j++){
//			for (int i = 0; i < width; i++)  {
//				map.append(Integer.toHexString(matrix[j][i]));
//			}
//			map.append("\n");
//		}
//
//		return map.toString();
//	}
	
	@Override
	public String toString() {
		return specificID.toString();
	}
	
	public void getRoomFromDungeonXML(String prefix)
	{
		Document dom;
	    Element e = null;
	    Element next = null;
	    String xml = System.getProperty("user.dir") + "\\my-data\\summer-school\\" + InteractiveGUIController.runID + "\\" + prefix + "room-" + this.toString() + ".xml";

	    // instance of a DocumentBuilderFactory
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    try {
	        // use factory to get an instance of document builder
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        // create instance of DOM
	        dom = db.newDocument();

	        // create the root element
	        Element rootEle = dom.createElement("Room");
	        rootEle.setAttribute("ID", this.toString());
	        rootEle.setAttribute("width", Integer.toString(this.getColCount()));
	        rootEle.setAttribute("height", Integer.toString(this.getRowCount()));
	        rootEle.setAttribute("time", new Timestamp(System.currentTimeMillis()).toString());
//	        rootEle.setAttribute("type", "SUGGESTIONS OR MAIN");
	        
	        // create data elements and place them under root
	        e = dom.createElement("Dimensions");
	        rootEle.appendChild(e);
	        
	        //DIMENSIONS --> THIS IS IMPORTANT TO CHANGE!! TODO:!!
	        next = dom.createElement("Dimension");
	        next.setAttribute("name", DimensionTypes.SIMILARITY.toString());
	        next.setAttribute("value", Double.toString(getDimensionValue(DimensionTypes.SIMILARITY)));
	        e.appendChild(next);
	        
	        next = dom.createElement("Dimension");
	        next.setAttribute("name", DimensionTypes.SYMMETRY.toString());
	        next.setAttribute("value", Double.toString(getDimensionValue(DimensionTypes.SYMMETRY)));
	        e.appendChild(next);
	        
	        //TILES
	        e = dom.createElement("Tiles");
	        rootEle.appendChild(e);
	        
	        for (int j = 0; j < height; j++) 
			{
				for (int i = 0; i < width; i++) 
				{
					next = dom.createElement("Tile");
			        next.setAttribute("value", getTile(i, j).GetType().toString());
			        next.setAttribute("immutable", Boolean.toString(getTile(i, j).GetImmutable()));
			        next.setAttribute("PosX", Integer.toString(i));
			        next.setAttribute("PosY", Integer.toString(j));
			        e.appendChild(next);
				}
			}
	        
	        e = dom.createElement("Customs");
	        rootEle.appendChild(e);
	        
	        for(Tile custom : customTiles)
	        {
	        	next = dom.createElement("Custom");
		        next.setAttribute("value", custom.GetType().toString());
		        next.setAttribute("immutable", Boolean.toString(custom.GetImmutable()));
		        next.setAttribute("centerX", Integer.toString(custom.GetCenterPosition().getX()));
		        next.setAttribute("centerY", Integer.toString(custom.GetCenterPosition().getY()));
		        e.appendChild(next);
	        }

	        dom.appendChild(rootEle);

	        try {
	            Transformer tr = TransformerFactory.newInstance().newTransformer();
	            tr.setOutputProperty(OutputKeys.INDENT, "yes");
	            tr.setOutputProperty(OutputKeys.METHOD, "xml");
	            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "room.dtd");
	            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	            // send DOM to file
	            tr.transform(new DOMSource(dom), 
	                                 new StreamResult(new FileOutputStream(xml)));

	        } catch (TransformerException te) {
	            System.out.println(te.getMessage());
	        } catch (IOException ioe) {
	            System.out.println(ioe.getMessage());
	        }
	    } catch (ParserConfigurationException pce) {
	        System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
	    }
	}
	
	public void saveRoomXMLMapElites(String prefix)
	{
		Document dom;
	    Element e = null;
	    Element next = null;
	    
	    String xml = System.getProperty("user.dir") + "\\my-data\\summer-school\\" + InteractiveGUIController.runID + "\\" + prefix + "room-" + this.toString() + "_" + saveCounter++ + ".xml";

	    // instance of a DocumentBuilderFactory
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    try {
	        // use factory to get an instance of document builder
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        // create instance of DOM
	        dom = db.newDocument();

	        // create the root element
	        Element rootEle = dom.createElement("Room");
	        rootEle.setAttribute("ID", this.toString());
	        rootEle.setAttribute("width", Integer.toString(this.getColCount()));
	        rootEle.setAttribute("height", Integer.toString(this.getRowCount()));
	        rootEle.setAttribute("time", new Timestamp(System.currentTimeMillis()).toString());
//	        rootEle.setAttribute("type", "SUGGESTIONS OR MAIN");
	        
	        // create data elements and place them under root
	        e = dom.createElement("Dimensions");
	        rootEle.appendChild(e);
	        
	        //DIMENSIONS --> THIS IS IMPORTANT TO CHANGE!! TODO:!!
	        next = dom.createElement("Dimension");
	        next.setAttribute("name", DimensionTypes.SIMILARITY.toString());
	        next.setAttribute("value", Double.toString(getDimensionValue(DimensionTypes.SIMILARITY)));
	        e.appendChild(next);
	        
	        next = dom.createElement("Dimension");
	        next.setAttribute("name", DimensionTypes.SYMMETRY.toString());
	        next.setAttribute("value", Double.toString(getDimensionValue(DimensionTypes.SYMMETRY)));
	        e.appendChild(next);
	        
	        //TILES
	        e = dom.createElement("Tiles");
	        rootEle.appendChild(e);
	        
	        for (int j = 0; j < height; j++) 
			{
				for (int i = 0; i < width; i++) 
				{
					next = dom.createElement("Tile");
			        next.setAttribute("value", getTile(i, j).GetType().toString());
			        next.setAttribute("immutable", Boolean.toString(getTile(i, j).GetImmutable()));
			        next.setAttribute("PosX", Integer.toString(i));
			        next.setAttribute("PosY", Integer.toString(j));
			        e.appendChild(next);
				}
			}
	        
	        e = dom.createElement("Customs");
	        rootEle.appendChild(e);
	        
	        for(Tile custom : customTiles)
	        {
	        	next = dom.createElement("Custom");
		        next.setAttribute("value", custom.GetType().toString());
		        next.setAttribute("immutable", Boolean.toString(custom.GetImmutable()));
		        next.setAttribute("centerX", Integer.toString(custom.GetCenterPosition().getX()));
		        next.setAttribute("centerY", Integer.toString(custom.GetCenterPosition().getY()));
		        e.appendChild(next);
	        }

	        dom.appendChild(rootEle);

	        try {
	            Transformer tr = TransformerFactory.newInstance().newTransformer();
	            tr.setOutputProperty(OutputKeys.INDENT, "yes");
	            tr.setOutputProperty(OutputKeys.METHOD, "xml");
	            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "room.dtd");
	            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	            // send DOM to file
	            tr.transform(new DOMSource(dom), 
	                                 new StreamResult(new FileOutputStream(xml)));

	        } catch (TransformerException te) {
	            System.out.println(te.getMessage());
	        } catch (IOException ioe) {
	            System.out.println(ioe.getMessage());
	        }
	    } catch (ParserConfigurationException pce) {
	        System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
	    }
	}

	
	public void getRoomXML(String prefix)
	{
		Document dom;
	    Element e = null;
	    Element next = null;
	    
	    File file = new File(DataSaverLoader.projectPath + "\\summer-school\\" + InteractiveGUIController.runID + "\\" + prefix + this.toString());
		if (!file.exists()) {
			file.mkdirs();
		}
	    
	    String xml = System.getProperty("user.dir") + "\\my-data\\summer-school\\" + InteractiveGUIController.runID + "\\" + prefix + this.toString() + "\\room-" + this.toString() + "_" + saveCounter++ + ".xml";

	    // instance of a DocumentBuilderFactory
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    try {
	        // use factory to get an instance of document builder
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        // create instance of DOM
	        dom = db.newDocument();

	        // create the root element
	        Element rootEle = dom.createElement("Room");
	        rootEle.setAttribute("ID", this.toString());
	        rootEle.setAttribute("width", Integer.toString(this.getColCount()));
	        rootEle.setAttribute("height", Integer.toString(this.getRowCount()));
	        rootEle.setAttribute("time", new Timestamp(System.currentTimeMillis()).toString());
//	        rootEle.setAttribute("type", "SUGGESTIONS OR MAIN");
	        
	        // create data elements and place them under root
	        e = dom.createElement("Dimensions");
	        rootEle.appendChild(e);
	        
	        //DIMENSIONS --> THIS IS IMPORTANT TO CHANGE!! TODO:!!
	        next = dom.createElement("Dimension");
	        next.setAttribute("name", DimensionTypes.SIMILARITY.toString());
	        next.setAttribute("value", Double.toString(getDimensionValue(DimensionTypes.SIMILARITY)));
	        e.appendChild(next);
	        
	        next = dom.createElement("Dimension");
	        next.setAttribute("name", DimensionTypes.SYMMETRY.toString());
	        next.setAttribute("value", Double.toString(getDimensionValue(DimensionTypes.SYMMETRY)));
	        e.appendChild(next);
	        
	        //TILES
	        e = dom.createElement("Tiles");
	        rootEle.appendChild(e);
	        
	        for (int j = 0; j < height; j++) 
			{
				for (int i = 0; i < width; i++) 
				{
					next = dom.createElement("Tile");
			        next.setAttribute("value", getTile(i, j).GetType().toString());
			        next.setAttribute("immutable", Boolean.toString(getTile(i, j).GetImmutable()));
			        next.setAttribute("PosX", Integer.toString(i));
			        next.setAttribute("PosY", Integer.toString(j));
			        e.appendChild(next);
				}
			}
	        
	        e = dom.createElement("Customs");
	        rootEle.appendChild(e);
	        
	        for(Tile custom : customTiles)
	        {
	        	next = dom.createElement("Custom");
		        next.setAttribute("value", custom.GetType().toString());
		        next.setAttribute("immutable", Boolean.toString(custom.GetImmutable()));
		        next.setAttribute("centerX", Integer.toString(custom.GetCenterPosition().getX()));
		        next.setAttribute("centerY", Integer.toString(custom.GetCenterPosition().getY()));
		        e.appendChild(next);
	        }

	        dom.appendChild(rootEle);

	        try {
	            Transformer tr = TransformerFactory.newInstance().newTransformer();
	            tr.setOutputProperty(OutputKeys.INDENT, "yes");
	            tr.setOutputProperty(OutputKeys.METHOD, "xml");
	            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "room.dtd");
	            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	            // send DOM to file
	            tr.transform(new DOMSource(dom), 
	                                 new StreamResult(new FileOutputStream(xml)));

	        } catch (TransformerException te) {
	            System.out.println(te.getMessage());
	        } catch (IOException ioe) {
	            System.out.println(ioe.getMessage());
	        }
	    } catch (ParserConfigurationException pce) {
	        System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
	    }
	}
	
	/****
	 * Testing method to evaluate a room based on the fitness used in the Evolutionary algorithm.
	 * @return
	 */
	public double fitnessEvaluation()
	{
		PatternFinder finder = this.getPatternFinder();
        List<Enemy> enemies = new ArrayList<Enemy>();
        List<Boss> bosses = new ArrayList<Boss>();
        List<Treasure> treasures = new ArrayList<Treasure>();
        List<Corridor> corridors = new ArrayList<Corridor>();
        List<Connector> connectors = new ArrayList<Connector>();
        List<Chamber> chambers = new ArrayList<Chamber>();
        
        GeneratorConfig gen = this.getCalculatedConfig();
        
        for (Pattern p : finder.findMicroPatterns()) {
        	if (p instanceof Enemy) {
        		enemies.add((Enemy) p);
        	} else if (p instanceof Treasure) {
        		treasures.add((Treasure) p);
        	} else if (p instanceof Corridor) {
        		corridors.add((Corridor) p);
        	} else if (p instanceof Connector) {
        		connectors.add((Connector) p);
        	} else if (p instanceof Chamber) {
        		chambers.add((Chamber) p);
        	}
        }
        
        List<DeadEnd> deadEnds = new ArrayList<DeadEnd>();
        List<TreasureRoom> treasureRooms = new ArrayList<TreasureRoom>();
        List<GuardRoom> guardRooms = new ArrayList<GuardRoom>();
        List<Ambush> ambushes = new ArrayList<Ambush>();
        List<GuardedTreasure> guardedTreasure = new ArrayList<GuardedTreasure>();
        //Ignore choke points for now
        for(CompositePattern p : finder.findMesoPatterns()){
        	if(p instanceof DeadEnd){
        		deadEnds.add((DeadEnd)p);
        	} else if (p instanceof TreasureRoom){
        		treasureRooms.add((TreasureRoom)p);
        	} else if (p instanceof GuardRoom){
        		guardRooms.add((GuardRoom)p);
        	} else if (p instanceof Ambush){
        		ambushes.add((Ambush)p);
        	} else if (p instanceof GuardedTreasure){
        		guardedTreasure.add((GuardedTreasure)p);
        	}
        	
        }
        
        
        double microPatternWeight = 0.9;
        double mesoPatternWeight = 0.1;
        
        
        //TODO: Is now time to care about this :P 
        //Door Fitness - don't care about this for now
        double doorFitness = 1.0f;
        
        //Entrance Fitness
        double entranceFitness = 1.0;
        
    	for(Pattern p : enemies){
    		entranceFitness -= p.getQuality();
    	}
        
        //Enemy Fitness
        double enemyFitness = 1.0;
    	for(Pattern p : enemies){
    		enemyFitness -= p.getQuality();
    	}
        
        //Treasure Fitness
        double treasureFitness = 1.0;
    	for(Pattern p : treasures){
    		treasureFitness -= p.getQuality();
    	}
        
    	//FIXME: THIS HAVE A LOT TO DO! mostly because the quality is not really working as it should! --> TRIPLE CHECK THIS!
    	//This is also called INVENTORIAL PATTERN FITNESS
        double treasureAndEnemyFitness = 0.0 * doorFitness + 0.2 * entranceFitness + 0.4 * enemyFitness + 0.4 * treasureFitness;
    	
        
    	//Corridor fitness
    	double passableTiles = this.getNonWallTileCount();
    	double corridorArea = 0;	
    	double rawCorridorArea = 0;
    	for(Pattern p : corridors){
    		rawCorridorArea += ((Polygon)p.getGeometry()).getArea();
    		
    		double mesoContribution = 0.0;
    		for(DeadEnd de : deadEnds){
    			if(de.getPatterns().contains(p)){
    				mesoContribution = de.getQuality();
    				//System.out.println(mesoContribution);
    			}
    				
    		}
    		
    		corridorArea += ((Polygon)p.getGeometry()).getArea() * (p.getQuality()*microPatternWeight +mesoContribution*mesoPatternWeight);
    		
    	}
    	double corridorFitness = corridorArea/passableTiles; //This is corridor ratio (without the connector)
    	corridorFitness = 1 - Math.abs(corridorFitness - gen.getCorridorProportion())/Math.max(gen.getCorridorProportion(), 1.0 - gen.getCorridorProportion());
    	
    	//Room fitness
    	double roomArea = 0;
    	double rawRoomArea = 0;
    	double onlyMesoPatterns = 0.0;
    	double counter = 0;
    	//Room fitness
    	for(Pattern p : chambers){
    		rawRoomArea += ((Polygon)p.getGeometry()).getArea();
    		counter += 1;
    		double mesoContribution = 0.0;
    		for(DeadEnd de : deadEnds){
    			if(de.getPatterns().contains(p)){
    				mesoContribution +=de.getQuality();
    				onlyMesoPatterns += de.getQuality();
//    				counter += 1;
    			}	
    		}
    		
    		for(TreasureRoom t : treasureRooms){
    			if(t.getPatterns().contains(p)){
    				mesoContribution += t.getQuality();
    				onlyMesoPatterns += t.getQuality();
//    				counter += 1;
    			}
    		}
    		for(GuardRoom g : guardRooms){
    			if(g.getPatterns().contains(p)){
    				mesoContribution += g.getQuality();
    				onlyMesoPatterns += g.getQuality();
//    				counter += 1;
    			}
    		}
    		for(Ambush a : ambushes){
    			if(a.getPatterns().contains(p)){
    				mesoContribution += a.getQuality();
    				onlyMesoPatterns += a.getQuality();
//    				counter += 1;
    			}
    		}
    		for(GuardedTreasure gt: guardedTreasure){
    			if(gt.getPatterns().contains(p)){
    				mesoContribution += gt.getQuality();
    				onlyMesoPatterns += gt.getQuality();
//    				counter += 1;
    			}
    		}
//    		
    		if(mesoContribution > 1)
    			mesoContribution = 1;
    		
    		roomArea += ((Polygon)p.getGeometry()).getArea() * (p.getQuality()*microPatternWeight + mesoContribution * mesoPatternWeight);
    	}
    	
    	double roomFitness = roomArea/passableTiles;
    	roomFitness = 1 - Math.abs(roomFitness - gen.getRoomProportion())/Math.max( gen.getRoomProportion(), 1.0 -  gen.getRoomProportion());

    	//Total fitness
//    	double fitness = ((0.35 * treasureAndEnemyFitness
//    			+  0.35 * (0.3 * roomFitness + 0.7 * corridorFitness) + (0.3 * symmetricFitnessValue))
//    			* similarityFitness);  	
    	
    	double fitness = (0.5 * treasureAndEnemyFitness)
    			+  0.5 * (0.3 * roomFitness + 0.7 * corridorFitness); 

    	return fitness;
	}


	public Bitmap accessibleFloorTiles() {
		return null;
	}
}
