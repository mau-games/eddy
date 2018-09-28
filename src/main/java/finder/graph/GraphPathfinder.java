package finder.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import finder.graph.Graph;
import finder.patterns.Pattern;

public class GraphPathfinder {
	
	
	private class PathNode {
	    public Node<Pattern> graphNode;
	    public PathNode parent;

	    public PathNode(Node<Pattern> graphNode, PathNode parent)
	    {
	        this.graphNode = graphNode;
	        this.parent = parent;
	    }
	    
	}
	
    private Graph<Pattern> patternGraph;

    public GraphPathfinder(Graph<Pattern> patternGraph)
    {
        this.patternGraph = patternGraph;
        
    }

	//BFS
    public List<Node<Pattern>> find(Node<Pattern> start, Node<Pattern> goal)
    {
    	patternGraph.resetGraph();
    	List<Node<Pattern>> path = new ArrayList<Node<Pattern>>();

        Queue<PathNode> queue = new LinkedList<PathNode>();
        
//        if(start != null)
        	queue.add(new PathNode(start,null));

        while(!queue.isEmpty()){
        	PathNode current = queue.remove();
        	current.graphNode.tryVisit();
        	
        	if(current.graphNode == goal){
        		return reconstructPath(current);
        	}
        	
        	for(Edge<Pattern> e : current.graphNode.getEdges()){
				if(e.getNodeA() == current.graphNode && !e.getNodeB().isVisited()){
					queue.add(new PathNode(e.getNodeB(),current));
					e.getNodeB().tryVisit();
				} else if (e.getNodeB() == current.graphNode && !e.getNodeA().isVisited()){
					queue.add(new PathNode(e.getNodeA(),current));
					e.getNodeA().tryVisit();
				}
			}
        }
        
        return path;
        
    }

    private List<Node<Pattern>> reconstructPath(PathNode last)
    {
        List<Node<Pattern>> path = new ArrayList<Node<Pattern>>();
        path.add(last.graphNode);

        while(last.parent != null)
        {
            last = last.parent;
            path.add(last.graphNode);
        }

        return path;
    }

}

