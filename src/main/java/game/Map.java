package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import util.Point;
import util.Util;

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
	private List<Point> doors;		// A list of doors
	private List<Point> treasures;	// A list of treasures
	private List<Point> enemies;		// A list of enemies
	private int failedPathsToTreasures;
	private int failedPathsToEnemies;
	private int failedPathsToAnotherDoor;
	private Dictionary<Point, Double> treasureSafety;
	private Point entrance;
	private double entranceSafetyFitness;
	
	
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
	
	// TODO: What to do with isDoors? (Alex: Remove it??)
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
		doors = new ArrayList<Point>();
		treasures = new ArrayList<Point>();
		enemies = new ArrayList<Point>();
		treasureSafety = new Hashtable<Point, Double>();
		this.m = m;
		this.n = n;
		wallCount = 0;
		
		
		if(Game.doorsPositions != null){
			this.doorCount = Game.doorsPositions.size();
		} else {
			Game.doorsPositions = new ArrayList<Point>();
			this.doorCount = doorCount;
		}
		
		matrix = new int[m][n];
		
		initMapFromTypes(types);
		
		if(isDoors)
			markDoors();
		
	}
	
	private void markDoors(){
		List<Point> positionsValid = new ArrayList<Point>();
		int enterDoorPosition = Util.getNextInt(0, doorCount);
		
		// TODO: Rewrite this to be less insanely inefficient
		// Get valid door positions (non-corner tiles on the room border)
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < m; j++)
            {
                if (countNeighbors(new Point(i,j)) == 3) 
                	positionsValid.add(new Point(i, j));
            }
        }

        // TODO: This placement approach seems very wonky - revisit
        // Place doors randomly
        for(int i = 0; i < doorCount; i++)
        {
            Point pointWithDoor;

            if (Game.doorsPositions.size() < doorCount)
            {
                int randomDoorTile = Util.getNextInt(0, positionsValid.size());
                pointWithDoor = positionsValid.get(randomDoorTile);
                Game.doorsPositions.add(pointWithDoor);
            }
            else
            {
                pointWithDoor = Game.doorsPositions.get(i);
            }
            
            // Check if door overrides an enemy
            if (matrix[(int)pointWithDoor.getX()][(int)pointWithDoor.getY()] == TileTypes.ENEMY.getValue()
            		|| matrix[(int)pointWithDoor.getX()][(int)pointWithDoor.getY()] == TileTypes.ENEMY2.getValue())
            {
            	enemies.removeIf((x)->x.equals(pointWithDoor));
            }

            // Check if door overrides a treasure
            if(matrix[(int)pointWithDoor.getX()][(int)pointWithDoor.getY()] == TileTypes.COIN.getValue()
            		|| matrix[(int)pointWithDoor.getX()][(int)pointWithDoor.getY()] == TileTypes.COIN2.getValue()
            		|| matrix[(int)pointWithDoor.getX()][(int)pointWithDoor.getY()] == TileTypes.COFFER.getValue()
            		|| matrix[(int)pointWithDoor.getX()][(int)pointWithDoor.getY()] == TileTypes.COFFER2.getValue())
            {
            	treasures.removeIf((x)->x.equals(pointWithDoor));
            }

            // Check if door overrides a wall
            if (matrix[(int)pointWithDoor.getX()][(int)pointWithDoor.getY()] == TileTypes.WALL.getValue())
            {
                wallCount--;
            } 
            
            if(enterDoorPosition == i)
            {
                this.entrance = pointWithDoor;
                //Set new tile in matrix
                matrix[(int)pointWithDoor.getX()][(int)pointWithDoor.getY()] = TileTypes.DOORENTER.getValue();
            }
            else
            {
                this.doors.add(pointWithDoor);
                //Set new tile in matrix
                matrix[(int)pointWithDoor.getX()][(int)pointWithDoor.getY()] = TileTypes.DOOR.getValue();
            }

            //Don't pick the same point twice
            positionsValid.remove(pointWithDoor);
        }
	}
	
	private int countNeighbors(Point position) {
		int count = 0;

        //X
        if ((position.getX() + 1) < n)
            count++;
        if ((position.getX() - 1) >= 0)
            count++;
        //Y
        if ((position.getY() - 1) >= 0)
            count++;
        if ((position.getY() + 1) < m)
            count++;

        return count;
	}

	//TODO: Check that this is working properly. Rewritten quite a bit.
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
	public TileTypes getTile(Point point){
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
        return (Game.sizeN * Game.sizeM) - wallCount;
    }
	
	// TODO: Document
	public double getEnemyPercentage()
    {
        int allMap = getNonWallTileCount();
        return getEnemyCount()/(double)allMap;
    }
	
	// TODO: Document
	public double getTreasurePercentage()
    {
        int allMap = getNonWallTileCount();
        return getTreasureCount()/(double)allMap;
    }
	
	// TODO: Document, rename... What on earth does this do? I have a feeling it doesn't actually work.
	public int countCloseWalls()
    {
        int locked = 0;

        for(int i = 0; i < n; i++)
        {
            for (int j = 0; i < m; i++)
            {
                List<Point> moves = getAvailableCoords(new Point(i,j)); // TODO: Check if right order n,m?

                int movesToWalls = 0;
                for (Point v : moves)
                {
                    if (matrix[(int)v.getX()][(int)v.getY()] == TileTypes.WALL.getValue())
                    {
                        movesToWalls++;
                    }
                }

                if (movesToWalls > 3) locked++;
            }
        }

        return locked;
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
					enemies.add(new Point(i, j));
					break;
				case COIN:
				case COIN2:
				case COFFER:
				case COFFER2:
					treasures.add(new Point(i, j));
					break;
				default:
					break;
				}
				matrix[i][j] = tiles[tile++].getValue();
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
