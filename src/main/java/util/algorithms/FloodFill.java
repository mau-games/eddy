package util.algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import game.Room;
import util.Point;

public class FloodFill {
	private Room mMap;

    public FloodFill(Room room)
    {
        mMap = room;
    }

    /**
     * Return the number of tiles connected to the tile at the given point (including that tile).
     * 
     * @param start Start position.
     * @return Number of connected tiles.
     */
    public int getConnectedTiles(Point start){
    	List<Node> visited = new ArrayList<Node>();
    	Queue<Node> queue = new LinkedList<Node>();
    	
    	Node root = new Node(0.0f, start, null);
    	queue.add(root);
    	
    	while(!queue.isEmpty()){
    		Node current = queue.remove();
    		visited.add(current);
    		
    		List<Point> children = mMap.getAvailableCoords(current.position);
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
    	
    	//If we get here, we didn't find a path to the goal
    	return visited.size();   	
    }
}
