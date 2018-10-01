package finder.patterns.micro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import finder.geometry.Bitmap;
import finder.geometry.Geometry;
import finder.patterns.Pattern;
import finder.patterns.SpacialPattern;
import finder.patterns.micro.Connector.ConnectorType;
import game.Game;
import game.Room;
import game.TileTypes;
import generator.config.GeneratorConfig;
import util.Point;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;

public class Corridor extends SpacialPattern {

	private int targetLength;
	
	public Corridor(GeneratorConfig config, Geometry geometry){
		boundaries = geometry;
		targetLength = config.getCorridorTargetLength();
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
	 * @param room
	 * @param boundary
	 * @return
	 */
	public static List<Pattern> matches(Room room, Geometry boundary){
		List<Pattern> results = new ArrayList<Pattern>();
		
		boolean[][] corridorTiles = new boolean[room.getRowCount()][room.getColCount()];
		boolean[][] visited = new boolean[room.getRowCount()][room.getColCount()];
		boolean[][] allocated = room.getAllocationMatrix();
		
		for(int j = 0; j < room.getRowCount(); j++)
			for(int i = 0; i < room.getColCount(); i++)
				if(IsCorridorTile(room,i,j))
					corridorTiles[j][i] = true;

		List<List<SearchNode>> candidateCorridors = new ArrayList<List<SearchNode>>();
    	
    	for(int j = 0; j < room.getRowCount(); j++)
    	{
    		for(int i = 0; i < room.getColCount(); i++)
			{
				if(!visited[j][i] && corridorTiles[j][i]){
					List<SearchNode> c = new ArrayList<SearchNode>();
					
					Queue<SearchNode> queue = new LinkedList<SearchNode>();
			    	SearchNode root = new SearchNode(new Point(i,j), null);
			    	queue.add(root);
			    	visited[j][i] = true;
			    	allocated[j][i] = true;
			    	
			    	while(!queue.isEmpty()){
			    		SearchNode current = queue.remove();
			    		c.add(current);
			    		
			    		int ii = current.position.getX();
			    		int jj = current.position.getY();
			    		
			    		if(ii > 0 && !visited[jj][ii-1] && corridorTiles[jj][ii-1]){
			    			queue.add(new SearchNode(new Point(ii-1,jj), null));
			    			visited[jj][ii-1] = true;
							allocated[jj][ii-1] = true;
			    		}
			    		if(jj > 0 && !visited[jj - 1][ii] && corridorTiles[jj -1][ii]){
			    			queue.add(new SearchNode(new Point(ii ,jj -1), null));
			    			visited[jj - 1][ii] = true;
							allocated[jj - 1][ii] = true;
			    		}
			    		if(ii < room.getColCount() - 1 && !visited[jj][ii+1] && corridorTiles[jj][ii+1]){
			    			queue.add(new SearchNode(new Point(ii+1,jj), null));
			    			visited[jj][ii+1] = true;
							allocated[jj][ii+1] = true;
			    		}
			    		if(jj < room.getRowCount() - 1 && !visited[jj+1][ii] && corridorTiles[jj+1][ii]){
			    			queue.add(new SearchNode(new Point(ii,jj+1), null));
			    			visited[jj+1][ii] = true;
							allocated[jj+1][ii] = true;
			    		}
			    		
			    	}
			    	
			    	if(c.size() < 2){
			    		for(SearchNode sn : c){
			    			corridorTiles[sn.position.getY()][sn.position.getX()] = false;
							allocated[sn.position.getY()][sn.position.getX()] = false;
			    		}
			    	} else {
			    		candidateCorridors.add(c);
			    	}
			    		//candidateCorridors.add(c);
		
				} else {
					visited[j][i] = true;
				}

			}
    	}
    	
    	//For all the remaining floor tiles, if check if they are connectors
    	for(int j = 0; j < room.getRowCount(); j++)
    		for(int i = 0; i < room.getColCount(); i++)
				if(!corridorTiles[j][i] && !allocated[j][i])
				{
					if(isTurnConnector(room,i,j)){
						Bitmap b = new Bitmap();
						b.addPoint(new finder.geometry.Point(i,j));
						results.add(new Connector(room.getConfig(),b, ConnectorType.TURN));
						allocated[j][i] = true;
					} else if (isIntersectionConnector(room,i,j)){
						Bitmap b = new Bitmap();
						b.addPoint(new finder.geometry.Point(i,j));
						results.add(new Connector(room.getConfig(),b, ConnectorType.INTERSECTION));
						allocated[j][i] = true;
					}
					
				}

    	//For each corridor, build the geometry and create a Corridor pattern   	
    	for(List<SearchNode> l : candidateCorridors){
    		Bitmap b = new Bitmap();
			for(SearchNode sn : l){
				b.addPoint(new finder.geometry.Point(sn.position.getX(),sn.position.getY()));
			}
			results.add(new Corridor(room.getConfig(),b));
    	}
    	
    	return results;
	}
	
	private static boolean IsWall(Room room, int x, int y){
		return x < 0 || y < 0 || x == room.getColCount() || y == room.getRowCount() || room.getTile(x,y).GetType() == TileTypes.WALL;
	}
	
	private static boolean isFloor(Room room, int x, int y){
		return x >= 0 && y >= 0 && x < room.getColCount() && y < room.getRowCount() && room.getTile(x,y).GetType() != TileTypes.WALL;
	}
	
	private static boolean MightAsWellBeAWall(Room room,int x, int y, int i, int j){
		int xSign = (int)Math.signum(x-i);
		int ySign = (int)Math.signum(y-j);
		return xSign != 0 && ySign != 0 && IsWall(room, i + xSign, j) && IsWall(room, i, j + ySign);

	}
	
	private static boolean IsCorridorTile(Room room, int x, int y){
		return !IsWall(room,x,y) && IsTileFlanked(room,x,y);// || Count8DirectionalWallNeighbours(map,x,y) >= 6);
	}
	
	/**
	 * Returns true if the given tile is a turn connector
	 * 
	 * 
	 * @param room
	 * @param x
	 * @param y
	 * @return true if the given tile is a turn connector
	 */
	private static boolean isTurnConnector(Room room, int x, int y){
		if(IsWall(room,x,y)) 
			return false;
		
		/*
		 * Case 1:
		 * 
		 *  ?C#
		 *  #XC
		 *  ?#?
		 */
		if(IsWall(room,x-1,y) && IsWall(room,x,y+1) && IsWall(room,x+1,y-1) && isFloor(room,x+1,y) && isFloor(room,x,y-1))
			return true;
		/*
		 * Case 2:
		 * 
		 *  ?#?
		 *  #XC
		 *  ?C#
		 */
		else if(IsWall(room,x-1,y) && IsWall(room,x,y-1) && IsWall(room,x+1,y+1) && isFloor(room,x+1,y) && isFloor(room,x,y+1))
			return true;
		/*
		 * Case 3:
		 * 
		 *  ?#?
		 *  CX#
		 *  #C?
		 */
		else if(IsWall(room,x+1,y) && IsWall(room,x,y-1) && IsWall(room,x-1,y+1) && isFloor(room,x-1,y) && isFloor(room,x,y+1))
			return true;
		/*
		 * Case 4:
		 * 
		 *  #C?
		 *  CX#
		 *  ?#?
		 */
		else if(IsWall(room,x+1,y) && IsWall(room,x,y+1) && IsWall(room,x-1,y-1) && isFloor(room,x-1,y) && isFloor(room,x,y-1))
			return true;
		
		//Not a turn connector:
		return false;
	}
	
	/**
	 * Returns true if all the adjacent floor tiles are corridor tiles
	 * 
	 * TODO: Experiment with using IsFloor instead of IsCorridorTile
	 * 
	 * @param room
	 * @param x
	 * @param y
	 * @return
	 */
	private static boolean isIntersectionConnector(Room room, int x, int y){
		if(IsWall(room,x,y)) 
			return false;
		
		return 	(IsWall(room,x-1,y) || IsCorridorTile(room,x-1,y))
			&&  (IsWall(room,x+1,y) || IsCorridorTile(room,x+1,y))
			&&  (IsWall(room,x,y-1) || IsCorridorTile(room,x,y-1))
			&&  (IsWall(room,x,y+1) || IsCorridorTile(room,x,y+1));

	}
	
	private static int Count8DirectionalWallNeighbours(Room room, int x, int y){
		int wallNeighbours = 0;
		for(int i = -1; i<= 1; i++)
			for(int j = -1; j<= 1; j++)
				if((i != 0 || j != 0) && IsWall(room,x+i,y+j))
					wallNeighbours++;
		return wallNeighbours;
	}
	
	private static int Count8DirectionalNonBlockedWallNeighbours(Room room, int x, int y){
		int wallNeighbours = 0;
		for(int i = -1; i<= 1; i++)
			for(int j = -1; j<= 1; j++)
				if((i != 0 || j != 0) && (IsWall(room,x+i,y+j) || MightAsWellBeAWall(room,x+i,y+j,i,j)))
					wallNeighbours++;
		return wallNeighbours;
	}
	
	private static boolean IsTileFlanked(Room room, int x, int y){
		return IsWall(room, x-1, y) && IsWall(room, x+1,y) || IsWall(room, x, y-1) && IsWall(room, x, y+1);
	}
	
}
