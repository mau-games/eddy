package finder.patterns.micro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import finder.geometry.Bitmap;
import finder.geometry.Geometry;
import finder.patterns.Pattern;
import finder.patterns.micro.Connector.ConnectorType;
import game.Map;
import game.TileTypes;
import util.Point;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;

public class Corridor extends Pattern {

	private ConfigurationUtility config;
	private int targetLength;
	
	public Corridor(Geometry geometry){
		boundaries = geometry;
		try {
			config = ConfigurationUtility.getInstance();
		} catch (MissingConfigurationException e) {
			// This will be caught and reported elsewhere.
		}
		targetLength = config.getInt("patterns.corridor.target_length");
	}
	
	private static class SearchNode {

	    public Point position;    
	    public SearchNode parent;
	    public List<SearchNode> corridor = null;

	    public SearchNode(Point position, SearchNode parent)
	    {
	        this.position = position;
	        this.parent = parent;
	    }

	    public boolean equals(SearchNode n)
	    {
	        return position == n.position;
	    }
	    
	    public boolean equals(Point p){
	    	return position.getX() == p.getX() && position.getY() == p.getY();
	    }
	}

	@Override
	public double getQuality(){
		return Math.min((double)((Bitmap)boundaries).getArea()/targetLength,1.0);
	}
	
	
	
	/**
	 * Algorithm description:
	 * 
	 * 
	 * 
	 * @param map
	 * @param boundary
	 * @return
	 */
	public static List<Pattern> matches(Map map, Geometry boundary){
		List<Pattern> results = new ArrayList<Pattern>();
		
		boolean[][] corridorTiles = new boolean[map.getColCount()][map.getRowCount()];
		boolean[][] visited = new boolean[map.getColCount()][map.getRowCount()];
		boolean[][] allocated = map.getAllocationMatrix();
		
		for(int i = 0; i < map.getColCount(); i++)
			for(int j = 0; j < map.getRowCount(); j++)
				if(IsCorridorTile(map,i,j))
					corridorTiles[i][j] = true;

		List<List<SearchNode>> candidateCorridors = new ArrayList<List<SearchNode>>();
    	
    	for(int i = 0; i < map.getColCount(); i++)
    	{
			for(int j = 0; j < map.getRowCount(); j++)
			{
				if(!visited[i][j] && corridorTiles[i][j]){
					List<SearchNode> c = new ArrayList<SearchNode>();
					
					Queue<SearchNode> queue = new LinkedList<SearchNode>();
			    	SearchNode root = new SearchNode(new Point(i,j), null);
			    	queue.add(root);
			    	visited[i][j] = true;
			    	allocated[i][j] = true;
			    	
			    	while(!queue.isEmpty()){
			    		SearchNode current = queue.remove();
			    		c.add(current);
			    		
			    		int ii = current.position.getX();
			    		int jj = current.position.getY();
			    		
			    		if(ii > 0 && !visited[ii-1][jj] && corridorTiles[ii-1][jj]){
			    			queue.add(new SearchNode(new Point(ii-1,jj), null));
			    			visited[ii-1][jj] = true;
							allocated[ii - 1][jj] = true;
			    		}
			    		if(jj > 0 && !visited[ii][jj - 1] && corridorTiles[ii][jj - 1]){
			    			queue.add(new SearchNode(new Point(ii,jj - 1), null));
			    			visited[ii][jj - 1] = true;
							allocated[ii][jj - 1] = true;
			    		}
			    		if(ii < map.getColCount() - 1 && !visited[ii+1][jj] && corridorTiles[ii+1][jj]){
			    			queue.add(new SearchNode(new Point(ii+1,jj), null));
			    			visited[ii+1][jj] = true;
							allocated[ii + 1][jj] = true;
			    		}
			    		if(jj < map.getRowCount() - 1 && !visited[ii][jj + 1] && corridorTiles[ii][jj + 1]){
			    			queue.add(new SearchNode(new Point(ii,jj + 1), null));
			    			visited[ii][jj + 1] = true;
							allocated[ii][jj + 1] = true;
			    		}
			    		
			    	}
			    	
			    	if(c.size() < 2){
			    		for(SearchNode sn : c){
			    			corridorTiles[sn.position.getX()][sn.position.getY()] = false;
							allocated[sn.position.getX()][sn.position.getY()] = false;
			    		}
			    	} else {
			    		candidateCorridors.add(c);
			    	}
			    		//candidateCorridors.add(c);
		
				} else {
					visited[i][j] = true;
				}

			}
    	}
    	
    	//For all the remaining floor tiles, if check if they are connectors
    	for(int i = 0; i < map.getColCount(); i++)
			for(int j = 0; j < map.getRowCount(); j++)
				if(!corridorTiles[i][j] && !allocated[i][j])
				{
					if(isTurnConnector(map,i,j)){
						Bitmap b = new Bitmap();
						b.addPoint(new finder.geometry.Point(i,j));
						results.add(new Connector(b, ConnectorType.TURN));
						allocated[i][j] = true;
					} else if (isIntersectionConnector(map,i,j)){
						Bitmap b = new Bitmap();
						b.addPoint(new finder.geometry.Point(i,j));
						results.add(new Connector(b, ConnectorType.INTERSECTION));
						allocated[i][j] = true;
					}
					
				}

    	//For each corridor, build the geometry and create a Corridor pattern   	
    	for(List<SearchNode> l : candidateCorridors){
    		Bitmap b = new Bitmap();
			for(SearchNode sn : l){
				b.addPoint(new finder.geometry.Point(sn.position.getX(),sn.position.getY()));
			}
			results.add(new Corridor(b));
    	}
    	
    	return results;
	}
	
	private static boolean IsWall(Map map, int x, int y){
		return x < 0 || y < 0 || x == map.getColCount() || y == map.getRowCount() || map.getTile(x,y) == TileTypes.WALL;
	}
	
	private static boolean isFloor(Map map, int x, int y){
		return x >= 0 && y >= 0 && x < map.getColCount() && y < map.getRowCount() && map.getTile(x,y) != TileTypes.WALL;
	}
	
	private static boolean MightAsWellBeAWall(Map map,int x, int y, int i, int j){
		int xSign = (int)Math.signum(x-i);
		int ySign = (int)Math.signum(y-j);
		return xSign != 0 && ySign != 0 && IsWall(map, i + xSign, j) && IsWall(map, i, j + ySign);

	}
	
	private static boolean IsCorridorTile(Map map, int x, int y){
		return !IsWall(map,x,y) && IsTileFlanked(map,x,y);// || Count8DirectionalWallNeighbours(map,x,y) >= 6);
	}
	
	/**
	 * Returns true if the given tile is a turn connector
	 * 
	 * 
	 * @param map
	 * @param x
	 * @param y
	 * @return true if the given tile is a turn connector
	 */
	private static boolean isTurnConnector(Map map, int x, int y){
		if(IsWall(map,x,y)) 
			return false;
		
		/*
		 * Case 1:
		 * 
		 *  ?C#
		 *  #XC
		 *  ?#?
		 */
		if(IsWall(map,x-1,y) && IsWall(map,x,y+1) && IsWall(map,x+1,y-1) && isFloor(map,x+1,y) && isFloor(map,x,y-1))
			return true;
		/*
		 * Case 2:
		 * 
		 *  ?#?
		 *  #XC
		 *  ?C#
		 */
		else if(IsWall(map,x-1,y) && IsWall(map,x,y-1) && IsWall(map,x+1,y+1) && isFloor(map,x+1,y) && isFloor(map,x,y+1))
			return true;
		/*
		 * Case 3:
		 * 
		 *  ?#?
		 *  CX#
		 *  #C?
		 */
		else if(IsWall(map,x+1,y) && IsWall(map,x,y-1) && IsWall(map,x-1,y+1) && isFloor(map,x-1,y) && isFloor(map,x,y+1))
			return true;
		/*
		 * Case 4:
		 * 
		 *  #C?
		 *  CX#
		 *  ?#?
		 */
		else if(IsWall(map,x+1,y) && IsWall(map,x,y+1) && IsWall(map,x-1,y-1) && isFloor(map,x-1,y) && isFloor(map,x,y-1))
			return true;
		
		//Not a turn connector:
		return false;
	}
	
	/**
	 * Returns true if all the adjacent floor tiles are corridor tiles
	 * 
	 * TODO: Experiment with using IsFloor instead of IsCorridorTile
	 * 
	 * @param map
	 * @param x
	 * @param y
	 * @return
	 */
	private static boolean isIntersectionConnector(Map map, int x, int y){
		if(IsWall(map,x,y)) 
			return false;
		
		return 	(IsWall(map,x-1,y) || IsCorridorTile(map,x-1,y))
			&&  (IsWall(map,x+1,y) || IsCorridorTile(map,x+1,y))
			&&  (IsWall(map,x,y-1) || IsCorridorTile(map,x,y-1))
			&&  (IsWall(map,x,y+1) || IsCorridorTile(map,x,y+1));

	}
	
	private static int Count8DirectionalWallNeighbours(Map map, int x, int y){
		int wallNeighbours = 0;
		for(int i = -1; i<= 1; i++)
			for(int j = -1; j<= 1; j++)
				if((i != 0 || j != 0) && IsWall(map,x+i,y+j))
					wallNeighbours++;
		return wallNeighbours;
	}
	
	private static int Count8DirectionalNonBlockedWallNeighbours(Map map, int x, int y){
		int wallNeighbours = 0;
		for(int i = -1; i<= 1; i++)
			for(int j = -1; j<= 1; j++)
				if((i != 0 || j != 0) && (IsWall(map,x+i,y+j) || MightAsWellBeAWall(map,x+i,y+j,i,j)))
					wallNeighbours++;
		return wallNeighbours;
	}
	
	private static boolean IsTileFlanked(Map map, int x, int y){
		return IsWall(map, x-1, y) && IsWall(map, x+1,y) || IsWall(map, x, y-1) && IsWall(map, x, y+1);
	}
	
}
