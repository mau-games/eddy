package game;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Map.Entry;

import finder.PatternFinder;
import finder.Populator;
import finder.geometry.Polygon;
import finder.graph.Graph;
import util.algorithms.Node;
import finder.patterns.Pattern;
import finder.patterns.SpacialPattern;
import finder.patterns.micro.Connector;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Enemy;
import finder.patterns.micro.Room;
import finder.patterns.micro.Treasure;
import util.Point;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapLoaded;
import util.eventrouting.events.MapUpdate;
import generator.config.GeneratorConfig;

/**
 * This class represents a dungeon room map.
 * 
 * @author Johan Holmberg
 */
public class Map {
	private int[][] matrix; // The actual map
	private boolean[][] allocated; // A map keeps track of allocated tiles
	private int m;			// The number of rows in a map
	private int n;			// The number of columns in a map
	private int doorCount;	// The number of doors in a map
	private int wallCount;	// The number of wall tiles in a map
	private List<Point> doors = new ArrayList<Point>();
	private List<Point> treasures = new ArrayList<Point>();
	private List<Point> enemies = new ArrayList<Point>();
	//private Graph<SpacialPattern> graph = new Graph<SpacialPattern>();
	private PatternFinder finder;
	private int failedPathsToTreasures;
	private int failedPathsToEnemies;
	private int failedPathsToAnotherDoor;
	private Dictionary<Point, Double> treasureSafety;
	private Point entrance;
	private double entranceSafety;
	private double entranceGreed;
	private GeneratorConfig config = null;
	
	/**
	 * Creates an instance of map.
	 * 
	 * @param types A chromosome transformed into an array of TileTypes.
	 * @param rows The number of rows in a map.
	 * @param cols The number of columns in a map.
	 * @param doorCount The number of doors to be seeded in a map.
	 */
	public Map(GeneratorConfig config, TileTypes[] types, int rows, int cols, int doorCount) {
		init(rows, cols);
		
		this.config = config;
		this.doorCount = Game.doors.size();
		
		initMapFromTypes(types);
		
		markDoors();
		
		finder = new PatternFinder(this);
		//Populator.populate(finder.findMicroPatterns());
        //buildGraph(finder.findMicroPatterns());
		
	}
	
	public GeneratorConfig getConfig(){
		return config;
	}
	
	public void setConfig(GeneratorConfig config){
		this.config = config;
	}
	
	/**
	 * Creates an instance of map.
	 * 
	 * @param rows The number of rows in a map.
	 * @param cols The number of columns in a map.
	 */
	private Map(int rows, int cols) {
		init(rows, cols);
		finder = new PatternFinder(this);
	}
	
	private void init(int rows, int cols) {
		
		treasureSafety = new Hashtable<Point, Double>();
		this.m = cols;
		this.n = rows;
		wallCount = 0;
		this.doorCount = 0;
		
		matrix = new int[n][m];
		allocated = new boolean[m][n];
        
	}
	
	public void resetAllocated(){
		allocated = new boolean[m][n];
	}
	
	private void markDoors(){
		entrance = Game.doors.get(0);
		for(int i = 0; i < doorCount; i++)
        {
			
            // Check if door overrides an enemy
            if (TileTypes.toTileType(matrix[Game.doors.get(i).getX()][Game.doors.get(i).getY()]).isEnemy())
            {
            	int ii = i;
            	enemies.removeIf((x)->x.equals(Game.doors.get(ii)));
            }

            // Check if door overrides a treasure
            if (TileTypes.toTileType(matrix[Game.doors.get(i).getX()][Game.doors.get(i).getY()]).isTreasure())
            {
            	int ii = i;
            	treasures.removeIf((x)->x.equals(Game.doors.get(ii)));
            }

            // Check if door overrides a wall
            if (matrix[Game.doors.get(i).getX()][Game.doors.get(i).getY()] == TileTypes.WALL.getValue())
            {
                wallCount--;
            } 
            
            if(i == 0)
            {
                matrix[Game.doors.get(i).getX()][Game.doors.get(i).getY()] = TileTypes.DOORENTER.getValue();
            }
            else
            {
            	doors.add(Game.doors.get(i));
            	matrix[Game.doors.get(i).getX()][Game.doors.get(i).getY()] = TileTypes.DOOR.getValue();
            }


        }
	}

	/**
	 * Gets a list of positions of tiles adjacent to a given position
	 * 
	 * @param position The position of a tile
     * @return A list of points 
	 */
	public List<Point> getAvailableCoords(Point position){
		List<Point> availableCoords = new ArrayList<Point>();
		
		if(position.getX() > 0 && getTile((int)position.getX() - 1, (int)position.getY()) != TileTypes.WALL)
			availableCoords.add(new Point(position.getX()-1,position.getY()));
		if(position.getX() < m - 1 && getTile((int)position.getX() + 1, (int)position.getY()) != TileTypes.WALL)
			availableCoords.add(new Point(position.getX()+1,position.getY()));
		if(position.getY() > 0 && getTile((int)position.getX(), (int)position.getY() - 1) != TileTypes.WALL)
			availableCoords.add(new Point(position.getX(),position.getY() - 1));
		if(position.getY() < n - 1 && getTile((int)position.getX(), (int)position.getY() + 1) != TileTypes.WALL)
			availableCoords.add(new Point(position.getX(),position.getY() + 1));
		
		return availableCoords;
			
	}
	
	/**
	 * Sets a specific tile to a value.
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param tile A tile.
	 */
	public void setTile(int x, int y, TileTypes tile) {
		matrix[x][y] = tile.getValue();
	}
	
	/**
	 * Gets the type of a specific tile.
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @return A tile.
	 */
	public TileTypes getTile(int x, int y) {
		return TileTypes.toTileType(matrix[x][y]);
	}
	
	/**
	 * Gets the type of a specific tile.
	 * 
	 * @param point The position.
	 * @return A tile.
	 */
	public TileTypes getTile(Point point){
		if (point == null) {
			return null;
		}
		
		return TileTypes.toTileType(matrix[point.getX()][point.getY()]);
	}
	
	/**
	 * Returns the number of columns in a map. 
	 * 
	 * @return The number of columns.
	 */
	public int getColCount() {
		return m;
	}
	
	/**
	 * Returns the number of rows in a map.
	 * 
	 * @return The number of rows.
	 */
	public int getRowCount() {
		return n;
	}
	
	/**
	 *	Gets the number of traversable tiles. 
	 * 
	 * @return The number of traversable tiles.
	 */
	public int countTraversables() {
		return m * n - wallCount;
	}
	
	/**
	 * Returns the position of the entry door.
	 * 
	 * @return The entry door's position.
	 */
	public Point getEntrance() {
		return entrance;
	}
	
	/**
	 * Sets the position of the entry door.
	 * 
	 * @param door The position of the new door.
	 */
	public void setEntrance(Point door) {
		entrance = door;
	}
	
	/**
	 * Adds a door to the map.
	 * 
	 * @param door The position of a new door.
	 */
	public void addDoor(Point door) {
		doors.add(door);
		doorCount++;
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
	public double calculateEnemyDensity() {
		return enemies.size() / countTraversables();
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
		return treasures.size() / countTraversables();
	}
	
	/**
	 * Returns the number of doors in a map, minus the entry door.
	 * 
	 * @return The number of doors.
	 */
	public int getDoorCount() {
		return doorCount - 1;
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
        return (Game.sizeM * Game.sizeN) - wallCount;
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
    
    /**
     * Sets the safety value for the map's entry point.
     * 
     * @param safety A safety value.
     */
    public void setEntranceSafety(double safety) {
    	entranceSafety = safety;
    }
    
    /**
     * Sets the map's entrance "greed" (that is, how close is the nearest treasure?).
     * 
     * @param safety A safety value.
     */
    public void setEntranceGreed(double greed) {
    	entranceGreed = greed;
    }
    
    /**
     * Gets the safety value for the map's entry point.
     * 
     * @return The safety value.
     */
    public double getEntranceSafety() {
    	return entranceSafety;
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
    
	/**
	 * Initialises a map.
	 * 
	 * @param tiles A list of tiles.
	 */
	private void initMapFromTypes(TileTypes[] tiles) {
		int tile = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
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
				matrix[i][j] = tiles[tile++].getValue();
			}
		}
	}
	
	public GeneratorConfig getCalculatedConfig(){
		GeneratorConfig newConfig = new GeneratorConfig(config);
		//Make a new pattern finder in case the map has been manually edited since the patterns were found
		//finder = new PatternFinder(this);
		
        List<Enemy> enemies = new ArrayList<Enemy>();
        List<Treasure> treasures = new ArrayList<Treasure>();
        List<Corridor> corridors = new ArrayList<Corridor>();
        List<Connector> connectors = new ArrayList<Connector>();
        List<Room> rooms = new ArrayList<Room>();
        
        for (Pattern p : finder.findMicroPatterns()) {
        	if (p instanceof Enemy) {
        		enemies.add((Enemy) p);
        	} else if (p instanceof Treasure) {
        		treasures.add((Treasure) p);
        	} else if (p instanceof Corridor) {
        		corridors.add((Corridor) p);
        	} else if (p instanceof Connector) {
        		connectors.add((Connector) p);
        	} else if (p instanceof Room) {
        		rooms.add((Room) p);
        	}
        }
        
        //TODO: Also take into account other patterns!!!
		
		//CORRIDOR LENGTH
		
		double rawCorridorArea = 0;
    	
    	for(Pattern p : corridors){
    		rawCorridorArea += ((Polygon)p.getGeometry()).getArea();
    	}
		
		int avgCorridorLength = (int)Math.ceil(rawCorridorArea/corridors.size());
		newConfig.setCorridorTargetLength(avgCorridorLength);
		
		//ROOM AND CORRIDOR RATIOS
		
    	double passableTiles = getNonWallTileCount();
    	double rawRoomArea = 0.0;
    	double totalSquareness = 0.0;
    	
    	for(Pattern p : rooms){
    		rawRoomArea += ((Polygon)p.getGeometry()).getArea();
    		totalSquareness += ((Room)p).getSquareness();
    	}
    	
    	double roomProportion = rawRoomArea / passableTiles;
		
		newConfig.setRoomProportion(roomProportion);
		newConfig.setCorridorProportion(1.0 - roomProportion);
		
		//CHAMBER AREA
		
		if(rooms.size() > 0){
			int avgArea = (int)Math.ceil(rawRoomArea/rooms.size());
			newConfig.setChamberTargetArea(avgArea);
		}
		//CHAMBER SQUARENESS AND SIZE
		
		if(rooms.size() > 0){
			double avgSquareness = totalSquareness / rooms.size();
			newConfig.setChamberTargetSquareness(avgSquareness);
			newConfig.setChamberAreaCorrectness(1.0 - avgSquareness);
		}

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
		while(reader.ready()){
			char c = (char) reader.read();
			mapString += c;
		}
		Map map = fromString(mapString);
		PatternFinder finder = map.getPatternFinder();
		MapContainer result = new MapContainer();
		result.setMap(map);
		result.setMicroPatterns(finder.findMicroPatterns());
		result.setMesoPatterns(finder.findMesoPatterns());
		result.setMacroPatterns(finder.findMacroPatterns());
        EventRouter.getInstance().postEvent(new MapLoaded(result));
	}
	
	/**
	 * Builds a map from a string representing a rectangular room. Each row in
	 * the string, separated by a newline (\n), represents a row in the
	 * resulting map's matrix.
	 * 
	 * @param string A string
	 */
	public static Map fromString(String string) {
		String[] rows = string.split("[\\r\\n]+");
		int rowCount = rows.length;
		int colCount = rows[0].length();
		TileTypes type = null;
		
		Map map = new Map(rowCount, colCount);
		try {
			map.setConfig(new GeneratorConfig());
		} catch (MissingConfigurationException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < colCount; j++) {
				type = TileTypes.toTileType(Integer.parseInt("" + rows[i].charAt(j), 16));
				map.setTile(i, j, type);
				switch (type) {
				case WALL:
					map.wallCount++;
					break;
				case ENEMY:
					map.enemies.add(new Point(i, j));
					break;
				case TREASURE:
					map.treasures.add(new Point(i, j));
					break;
				case DOOR:
					map.addDoor(new Point(i, j));
					break;
				case DOORENTER:
					map.setEntrance(new Point(i, j));
					break;
				default:
				}
			}
		}
		
		return map;
	}
	
	@Override
	public String toString() {
		StringBuilder map = new StringBuilder();
		
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				map.append(Integer.toHexString(matrix[i][j]));
			}
			map.append("\n");
		}
		
		return map.toString();
	}
	
	public boolean isFeasible(){
		List<Node> visited = new ArrayList<Node>();
    	Queue<Node> queue = new LinkedList<Node>();
    	int treasure = 0;
    	int enemies = 0;
    	int doors = 0;
    	
    	Node root = new Node(0.0f, getEntrance(), null);
    	queue.add(root);
    	
    	while(!queue.isEmpty()){
    		Node current = queue.remove();
    		visited.add(current);
    		if(getTile(current.position) == TileTypes.DOOR)
    			doors++;
    		else if (getTile(current.position).isEnemy())
    			enemies++;
    		else if (getTile(current.position).isTreasure())
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
    	
    	for(int i = treasure; i < getTreasureCount();i++)
    		addFailedPathToTreasures();
    	for(int i = doors; i < getDoorCount();i++)
    		addFailedPathToTreasures();
    	for(int i = enemies; i < getEnemyCount();i++)
    		addFailedPathToTreasures();
    	
    	return visited.size() == getNonWallTileCount() 
    			&& (treasure + doors + enemies == getTreasureCount() + getDoorCount() + getEnemyCount())
    			&& getTreasureCount() > 0 && getEnemyCount() > 0;
	}
}
