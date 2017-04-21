package finder.patterns.micro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import finder.geometry.Geometry;
import finder.geometry.Point;
import finder.geometry.Rectangle;
import finder.patterns.InventorialPattern;
import finder.patterns.Pattern;
import game.Map;
import util.Util;
import util.algorithms.Node;
import util.algorithms.Pathfinder;

/**
 * This class represents the dungeon game design pattern called Entrance.
 * 
 * @author Johan Holmberg
 */
public class Entrance extends InventorialPattern {
	
	private double quality;
	
	public Entrance(Geometry geometry, Map map) {
		boundaries = geometry;
		this.map = map;
	}
	
	@Override
	/**
	 * Returns a measure of the quality of this pattern.
	 * 
	 * <p>The quality for a room is decided by two factors:<br>
	 * * The ratio of the room's area versus it's bounding rectangle<br>
	 * * The deviation from a set area
	 *  
	 * @return A number between 0.0 and 1.0 representing the quality of the pattern (where 1 is best)
	 */
	public double getQuality() {
		return quality;
	}

	// TODO: Consider non-rectangular geometries in the future.
	/**
	 * Searches a map for entrances. The searchable area can be limited by a set of
	 * boundaries. If these boundaries are invalid, no search will be
	 * performed.
	 * 
	 * @param map The map to search.
	 * @param boundary The boundary that limits the searchable area.
	 * @return A list of found room pattern instances.
	 */
	public static List<Pattern> matches(Map map, Geometry boundary) {
		ArrayList<Pattern> results = new ArrayList<Pattern>();
		
		if (map == null || map.getColCount() == 0) {
			return results;
		}
		
		if (boundary == null) {
			boundary = new Rectangle(new Point(0, 0),
					new Point(map.getColCount() -1 , map.getRowCount() - 1));
		}
		
		double quality = calculateEntranceQuality(map);
		
		util.Point p = map.getEntrance();
		Point p_ = new Point(p.getX(),p.getY());
		if(((Rectangle)boundary).contains(p_)){
			Entrance e = new Entrance(p_,map);
			e.quality = quality;
			results.add(e);
		}

		return results;
	}
	
	private static boolean isEntrance(int[][] map, int x, int y) {
		return map[x][y] == 5;
	}
	
	private static double calculateEntranceQuality(Map map){
		//Entrance safety
	    double entranceSafetyQuality = evaluateEntranceSafety(map);
	    map.setEntranceSafety(entranceSafetyQuality);
	    entranceSafetyQuality = Math.abs(entranceSafetyQuality - map.getConfig().getEntranceSafety());
	    
	    //Average treasure safety
        evaluateTreasureSafeties(map);
        Double[] safeties = map.getAllTreasureSafeties();
        double safeties_average = Util.calcAverage(safeties);
        double averageTreasureSafetyQuality = 0.0;
        averageTreasureSafetyQuality = Math.abs(safeties_average - map.getConfig().getAverageTreasureSafety());
        
        //Treasure Safety Variance
        double safeties_variance = Util.calcVariance(safeties);
        double expectedSafetyVariance = 0.0;
		expectedSafetyVariance = map.getConfig().getTreasureSafetyVariance();
        double treasureSafetyVarianceQuality = Math.abs(safeties_variance - expectedSafetyVariance);
        
        //Entrance greed
        double entranceGreedQuality = evaluateEntranceGreed(map); //Note - this has been changed from the Unity version
        map.setEntranceGreed(entranceGreedQuality);
    	entranceGreedQuality = Math.abs(entranceGreedQuality - map.getConfig().getEntranceGreed());
        
        double quality = 0.2*entranceSafetyQuality + 0.2 * entranceGreedQuality + 0.2 * averageTreasureSafetyQuality + 0.4 * treasureSafetyVarianceQuality;
        return quality;
		
	}
	
	/**
	 * Uses flood fill to calculate a score for the safety of the room's entrance
	 * 
	 * The safety value is between 0 and 1. 
	 * 0 means there is an enemy adjacent to the entry door. 
	 * 1 means there is no enemy in the room (impossible).
	 * In practice the highest safety will be achieved when an enemy is as far away from the entrance as possible.
	 * 
	 * @param map The map to evaluate.
	 * @return The safety value for the room's entrance.
	 */
	private static float evaluateEntranceSafety(Map map)
    {

        List<Node> visited = new ArrayList<Node>();
    	Queue<Node> queue = new LinkedList<Node>();
    	
    	Node root = new Node(0.0f, map.getEntrance(), null);
    	queue.add(root);
    	
    	while(!queue.isEmpty()){
    		Node current = queue.remove();
    		visited.add(current);
    		if(map.getTile(current.position).isEnemy())
    			break;
    		
    		List<util.Point> children = map.getAvailableCoords(current.position);
            for(util.Point child : children)
            {
                if (visited.stream().filter(x->x.equals(child)).findFirst().isPresent() 
                		|| queue.stream().filter(x->x.equals(child)).findFirst().isPresent()) 
                	continue;

                //Create child node
                Node n = new Node(0.0f, child, current);
                queue.add(n);
            }
            
    	}
    	return (float)visited.size()/map.getNonWallTileCount();  
    }
	
	/**
	 * Uses flood fill to calculate a score for the greed of the room's entrance
	 * 
	 * The greed value is between 0 and 1. 
	 * 0 means there is an treasure adjacent to the entry door. 
	 * 1 means there is no treasure in the room (impossible).
	 * In practice the highest greed will be achieved when a treasure is as far away from the entrance as possible.
	 * 
	 * @param map The map to evaluate.
	 * @return The safety value for the room's entrance.
	 */
	private static float evaluateEntranceGreed(Map map)
    {

        List<Node> visited = new ArrayList<Node>();
    	Queue<Node> queue = new LinkedList<Node>();
    	
    	Node root = new Node(0.0f, map.getEntrance(), null);
    	queue.add(root);
    	
    	while(!queue.isEmpty()){
    		Node current = queue.remove();
    		visited.add(current);
    		if(map.getTile(current.position).isTreasure())
    			break;
    		
    		List<util.Point> children = map.getAvailableCoords(current.position);
            for(util.Point child : children)
            {
                if (visited.stream().filter(x->x.equals(child)).findFirst().isPresent() 
                		|| queue.stream().filter(x->x.equals(child)).findFirst().isPresent()) 
                	continue;

                //Create child node
                Node n = new Node(0.0f, child, current);
                queue.add(n);
            }
            
    	}
    	return (float)visited.size()/map.getNonWallTileCount();  
    }
	
	/**
	 * Evaluates the treasure safety of a valid individual 
	 * See Sentient Sketchbook for a description of the method
	 * 
	 * @param ind The individual to evaluate
	 */
	private static void evaluateTreasureSafeties(Map map)
	{
	
	    if(map.getEnemyCount() > 0)
	    {

	        util.Point doorEnter = map.getEntrance();
	        
	        Pathfinder pathfinder = new Pathfinder(map);
	
	        for (util.Point treasure: map.getTreasures())
	        {
	        	//Find the closest enemy
	            List<Node> visited = new ArrayList<Node>();
	        	Queue<Node> queue = new LinkedList<Node>();
	        	
	        	Node root = new Node(0.0f, treasure, null);
	        	queue.add(root);
	        	util.Point closestEnemy = null;
	        	
	        	while(!queue.isEmpty()){
	        		Node current = queue.remove();
	        		visited.add(current);
	        		if(map.getTile(current.position).isEnemy()){
	        			closestEnemy = current.position;
	        			break;
	        		}
	        		
	        		List<util.Point> children = map.getAvailableCoords(current.position);
	                for(util.Point child : children)
	                {
	                    if (visited.stream().filter(x->x.equals(child)).findFirst().isPresent() 
	                    		|| queue.stream().filter(x->x.equals(child)).findFirst().isPresent()) 
	                    	continue;

	                    //Create child node
	                    Node n = new Node(0.0f, child, current);
	                    queue.add(n);
	                }
	        	}
	        	
	            int dinTreasureToEnemy = pathfinder.find(treasure, closestEnemy).length;
	            
                //Distance in nodes from treasure to entrance
                int dinTreasureToStartDoor = pathfinder.find(treasure, doorEnter).length;
	
                double result = (double)
                    (dinTreasureToEnemy - dinTreasureToStartDoor) / 
                    (dinTreasureToEnemy + dinTreasureToStartDoor);

                if (Double.isNaN(result))
                    result = 0.0f;
                
                map.setTreasureSafety(treasure, Math.max(0.0, result));
	        }
	    }
	}
	
	
	
}
