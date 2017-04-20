package finder.patterns.meso;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import finder.graph.Edge;
import finder.graph.Graph;
import finder.graph.Node;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import game.Map;

public class DeadEnd extends CompositePattern {
	
	//TODO: NOTE, the quality of a dead end should be related to the amount of content in it.

	/**
	 * Searches a map for instances of this pattern and returns a list of found
	 * instances.
	 * 
	 * @param map The map to search for patterns in.
	 * @param boundary A boundary in which the pattern is searched for.
	 * @return A list of found instances.
	 */
	public static List<CompositePattern> matches(Map map, Graph<Pattern> patternGraph) {
		
		//How to find dead ends:
		//1. Find the critical path
		//2. For each node in the critical path, perform a BFS starting at each edge connecting to a node that isn't in the critical path
		//3. If an edge is part of a cycle, it is not part of a dead end
		//4. If an edge is NOT part of a cycle, every node beyond it is in a dead end.
		
		List<CompositePattern> deadEnds = new ArrayList<>();
		
		return deadEnds;
	}
	
	/**
	 * Returns all the nodes with only one edge from a pattern graph (the leaves).
	 * 
	 * @param patternGraph The pattern graph.
	 * @return Am ArrayList of leaves.
	 */
	List<Node<Pattern>> getLeaves(Graph<Pattern> patternGraph){
		List<Node<Pattern>> leaves = new ArrayList<Node<Pattern>>();
		for(Node<Pattern> n : patternGraph.getNodes().values()){
			if (n.countEdges() == 1)
				leaves.add(n);
		}
		return leaves;
	}
	
}
