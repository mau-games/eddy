package game;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javafx.geometry.Point2D;

// TODO: Make sure that m represents the rows and n the columns
// TODO: Choose between x + y & Point2D
/**
 * This class represents a dungeon room map.
 * 
 * @author Johan Holmberg
 */
public class Map {
	private int[][] matrix; // The actual map
	private int m;			// The number of rows in a map
	private int n;			// The number of columns in a map
	private int doorCount;	// The number of doors in a map
	private int wallCount;	// The number of wall tiles in a map
	private List<Point2D> doors;		// A list of doors
	private List<Point2D> treasures;	// A list of treasures
	private List<Point2D> enemies;		// A list of enemies
	private int failedPathsToTreasures;
	private int failedPathsToEnemies;
	private int failedPathsToAnotherDoor;
	private Dictionary<Point2D, Double> treasureSafety;
	private Point2D entrance;
	private double entranceSafetyFitness;
	
	/**
	 * Creates an instance of Map
	 */
	public Map() {
		m = n = 0;
		doorCount = wallCount = 0;
		doors = new ArrayList<Point2D>();
		treasures = new ArrayList<Point2D>();
		enemies = new ArrayList<Point2D>();
		treasureSafety = new Hashtable<Point2D, Double>();
		int[][] matrix = new int[m][n];
	}
	
	// TODO: What to do with types?
	/**
	 * Creates an instance of map.
	 * 
	 * @param types
	 * @param m The number of rows in a map.
	 * @param n The number of columns in a map.
	 * @param doorCount The number of doors to be seeded in a map.
	 */
	public Map(TileTypes[] types, int m, int n, int doorCount) {
		this(types, m, n, doorCount, true);
	}
	
	// TODO: What to do with isDoors?
	/**
	 * Creates an instance of map.
	 * 
	 * @param types
	 * @param m The number of rows in a map.
	 * @param n The number of columns in a map.
	 * @param doorCount The number of doors to be seeded in a map.
	 * @param isDoors
	 */
	public Map(TileTypes[] types, int n, int m, int doorCount, boolean isDoors) {
		this();
		this.m = m;
		this.n = n;
		this.doorCount = doorCount;
		
		initMapFromTypes(types);
		
		// TODO: Figure out WTH the isDoors argument does.
	}
	
	//TODO: Check that this is working properly. Rewritten quite a bit.
	/**
	 * Gets a list of positions of tiles adjacent to a given position
	 * 
	 * @param position The position of a tile
     * @return A list of points 
	 */
	public List<Point2D> getAvailableCoords(Point2D position){
		List<Point2D> availableCoords = new ArrayList<Point2D>();
		
		if(position.getX() > 0 && getTile((int)position.getX() - 1, (int)position.getY()) != TileTypes.WALL)
			availableCoords.add(new Point2D(position.getX()-1,position.getY()));
		if(position.getX() < m - 1 && getTile((int)position.getX() + 1, (int)position.getY()) != TileTypes.WALL)
			availableCoords.add(new Point2D(position.getX()+1,position.getY()));
		if(position.getY() > 0 && getTile((int)position.getX(), (int)position.getY() - 1) != TileTypes.WALL)
			availableCoords.add(new Point2D(position.getX(),position.getY() - 1));
		if(position.getY() < n - 1 && getTile((int)position.getX(), (int)position.getY() + 1) != TileTypes.WALL)
			availableCoords.add(new Point2D(position.getX(),position.getY() + 1));
		
		return availableCoords;
			
	}
	
	// TODO: This might be off...
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
	
	// TODO: This might be off...
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
	
	// TODO: This might be off...
	/**
	 * Gets the type of a specific tile.
	 * 
	 * @param point The position.
	 * @return A tile.
	 */
	public TileTypes getTile(Point2D point){
		return TileTypes.toTileType(matrix[(int)point.getX()][(int)point.getY()]);
	}
	
	// TODO: Check this...
	/**
	 * Returns the number of columns in a map. 
	 * 
	 * @return The number of columns.
	 */
	public int getColCount() {
		return n;
	}
	
	// TODO: Check this...
	/**
	 * Returns the number of rows in a map.
	 * 
	 * @return The number of rows.
	 */
	public int getRowCount() {
		return m;
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
	public Point2D getEntrance() {
		return entrance;
	}
	
	/**
	 * Sets the position of the entry door.
	 * 
	 * @param door The position of the new door.
	 */
	public void setEntrance(Point2D door) {
		entrance = door;
	}
	
	/**
	 * Adds a door to the map.
	 * 
	 * @param door The position of a new door.
	 */
	public void addDoor(Point2D door) {
		doors.add(door);
		doorCount++;
	}
	
	/**
	 * Returns the positions of all doors.
	 * 
	 * @return The doors.
	 */
	public List<Point2D> getDoors() {
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
	 * Returns the number of treasures.
	 * 
	 * @return The number of treasures.
	 */
	public List<Point2D> getTreasures() {
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
    public void setTreasureSafety(Point2D treasure, double safety) {
    	treasureSafety.put(treasure, safety);
    }
    
    /**
     * Gets the safety value for at treasure.
     * 
     * @param treasure The position of the treasure.
     * @return The safety value.
     */
    public double getTreasureSafety(Point2D treasure) {
    	return (double) treasureSafety.get(treasure);
    }
    
    /**
     * Gets the complete list of treasure safety values.
     * 
     * @return A dictionary containing all treasures and their safety values.
     */
    public Dictionary<Point2D, Double> getTreasureSafety() {
    	return treasureSafety;
    }
    
    /**
     * Gets a list of all enemies in a map.
     * 
     * @return A list of enemies.
     */
    public List<Point2D> getEnemies() {
    	return enemies;
    }
    
    /**
     * Sets the safety fitness value for the map's entry point.
     * 
     * @param fitness A safety fitness value.
     */
    public void setEntrySafetyFitness(double fitness) {
    	entranceSafetyFitness = fitness;
    }
    
    /**
     * Gets the safety fitness value for the map's entry point.
     * 
     * @return The safety fitness value.
     */
    public double getEntrySafetyFitness() {
    	return entranceSafetyFitness;
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
				case ENEMY2:
					enemies.add(new Point2D(m, n));
					break;
				case COIN:
				case COIN2:
				case COFFER:
				case COFFER2:
					treasures.add(new Point2D(m, n));
					break;
				default:
					break;
				}
				matrix[m][n] = tiles[tile++].getValue();
			}
		}
	}

	/**
	 * Exports this map as a 2D matrix of integers
	 * 
	 * @return A quadratic integer matrix.
	 */
	public int[][] toMatrix() {
		// TODO: Does this cut it, with all the new doors?
		
		return matrix;
	}
	
	@Override
	public String toString() {
		StringBuilder map = new StringBuilder();
		
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				map.append(matrix[m][n]);
			}
			map.append("\n");
		}
		
		return map.toString();
	}
}
