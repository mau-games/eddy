package util.algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import game.Map;
import util.algorithms.Point2D;

public class BFS {

	private Map mMap;

    public BFS(Map map)
    {
        mMap = map;
    }

    /**
     * Get an array of nodes traversed by a BFS when searching from start to goal.
     * TODO: Wouldn't it really be fine to just return a number here? I don't think the returned nodes are ever used
     * 
     * @param start Start position.
     * @param goal Goal position.
     * @return Array of nodes traversed by BFS when searching from start to goal.
     */
    public Node[] getTraversedNodesBetween(Point2D start, Point2D goal){
    	List<Node> visited = new ArrayList<Node>();
    	Queue<Node> queue = new LinkedList<Node>();
    	
    	Node root = new Node(0.0f, start, null);
    	queue.add(root);
    	
    	while(!queue.isEmpty()){
    		Node current = queue.remove();
    		visited.add(current);
    		
    		if(current.equals(goal))
    			return visited.stream().toArray(Node[]::new);
    		
    		List<Point2D> children = mMap.getAvailableCoords(current.position);
            for(Point2D child : children)
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
    	return new Node[0];	    	
    }

    public void setMap(Map map)
    {
        mMap = map;
    }

}
