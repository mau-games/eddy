package util.algorithms;

import java.util.ArrayList;
import java.util.List;

import game.Map;
import javafx.geometry.Point2D;


// TODO: This seems a bit inefficient, can this be improved? NO WAY SHOULD THIS BE RECURSIVE
// TODO: Rewrite this.
public class BFS {

	private Map mMap;
    private Point2D goal;

    public BFS(Map map)
    {
        mMap = map;
    }

    public Node[] find(Point2D start, Point2D goal)
    {
        List<Node> openList = new ArrayList<Node>();
        List<Node> closeList = new ArrayList<Node>();

        openList.add(new Node(0.0f, start, null));
        this.goal = goal;

        return find_recursive(openList, closeList);
    }

    private Node[] find_recursive(List<Node> opened, List<Node> closed)
    {
        //Trivial case one <--- WHY??? This will never happen (Alex)
        if (opened.size() == 0) 
        	return new Node[0];

        Node current = popNode(opened);
        closed.add(current);

        //Trivial case two
        if (current.position == goal) 
        	return (Node[]) closed.toArray();

        List<Point2D> children = mMap.getAvailableCoords(current.position);

        for(Point2D child : children)
        {
            if (existsPointInArrayNodes(closed, child)) continue;
            if (existsPointInArrayNodes(opened, child)) continue;

            //Create child node
            Node n = new Node(0.0f, child, null);
            opened.add(n);
        }

        return find_recursive(opened, closed);
    }

    public void setMap(Map map)
    {
        mMap = map;
    }

    private Node popNode(List<Node> nodes)
    {
        return nodes.remove(0);
    }

    private boolean existsPointInArrayNodes(List<Node> list, Point2D point)
    {
        for (Node n : list)
        {
            if (n.position.equals(point)) return true;
        }
        return false;
    }
	
}
