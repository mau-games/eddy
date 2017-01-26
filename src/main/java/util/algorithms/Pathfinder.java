package util.algorithms;

import java.util.List;
import java.util.ArrayList;
import game.Map;
import javafx.geometry.Point2D;
import util.Util;


// TODO: Check if this is implemented properly
public class Pathfinder {
	//private Point2D goal;
    private Map map;

    public Pathfinder(Map map)
    {
        this.map = map;
    }

	//A Start algorithm 
    public Node[] find(Point2D start, Point2D goal)
    {
        List<Node> openList = new ArrayList<Node>();
        List<Node> closedList = new ArrayList<Node>();
        //this.goal = goal;

        //init open list and init
        openList.add(new Node(Util.manhattanDistance(start, goal), start, null));

        while (openList.size() > 0)
        {
            Node current = popNode(openList);
            if (current.equals(goal)) 
            	return expandTreeFromLast(current);

            //Add current to close list
            closedList.add(current);

            //Get all children for current node
            List<Point2D> children = map.getAvailableCoords(current.position);

            for (Point2D child : children)
            {
                if (existsPointInArrayNodes(closedList, child)) continue;

                //Calculate F's child
                Node n = new Node(closedList.size() + Util.manhattanDistance(child, goal), child, current);

                if ((n.f < current.f) || !existsPointInArrayNodes(openList, child))
                {
                    openList.add(n);
                }
            }

            //Sort openList by its f
            //openList = qSortOpenList(openList);
            openList.sort((a,b) -> Float.compare(a.f, b.f));
        }

        return (Node[]) openList.toArray();
    }

    private Node[] expandTreeFromLast(Node last)
    {
        List<Node> nodes = new ArrayList<Node>();
        nodes.add(last);

        while(last.parent != null)
        {
            last = last.parent;
            nodes.add(last);
        }

        return (Node[]) nodes.toArray();
    }

    // TODO: Horrible. Worst implementation of quicksort ever.
//    private List<Node> qSortOpenList(List<Node> open_list)
//    {
//        if (open_list.size() == 0) 
//        	return new ArrayList<Node>();
//        List<Node> left = new ArrayList<Node>();
//        List<Node> right = new ArrayList<Node>();
//        Node pivot = open_list.get(0);
//
//        for(int i = 1; i < open_list.size(); i++)
//        {
//            if (open_list.get(i).f < pivot.f)
//                left.add(open_list.get(i));
//            else
//                right.add(open_list.get(i)); 
//        }
//
//        left.add(pivot);
//        left.addAll(right);
//        return qSortOpenList(left);
//    }

    private boolean existsPointInArrayNodes(List<Node> closed_list, Point2D point)
    {
        for(Node n : closed_list)
        {
            if (n.position == point) return true;
        }
        return false;
    }

    private Node popNode(List<Node> nodes)
    {
        return nodes.remove(0);
    }

    public void setMap(Map map)
    {
        this.map = map;
    }
}
