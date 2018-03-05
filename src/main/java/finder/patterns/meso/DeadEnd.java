package finder.patterns.meso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import finder.graph.Edge;
import finder.graph.Graph;
import finder.graph.GraphPathfinder;
import finder.graph.Node;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import finder.patterns.micro.Door;
import finder.patterns.micro.Entrance;
import game.Map;
import generator.config.GeneratorConfig;

public class DeadEnd extends CompositePattern {
	
	private double badness = 0.0;
	private double filledness = 1.0f;
	Map map;
	
	public double getQuality(){
		
		double actualFilledness = 0.0;
		List<CompositePattern> mesopatterns = map.getPatternFinder().findMesoPatterns();
		int contained = 0;
		//actualFilledness is the ratio to patterns involved in meso patterns to the total number
		for(Pattern p : getPatterns()){
			for(CompositePattern cp : mesopatterns){
				if(cp != this && cp.getPatterns().contains(p)){
					contained++;
					break;
				}
			}
		}
		actualFilledness = (double) contained / getPatterns().size();
		
		actualFilledness = 1 - Math.abs(actualFilledness - filledness)/Math.max(filledness, 1.0 - filledness);
		
		double quality = actualFilledness;
		
		return 0.5 * quality + 0.5 * (1 - badness);	
	}
	
	public DeadEnd(Map map, GeneratorConfig config){
		this.map = map;
		
		
		filledness = config.getDeadEndFilledness();
		
		badness = config.getDeadEndBadness();
	}
	

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
		
		//Find doors & entrance
		Entrance entrance = null;
		List<Door> doors = new ArrayList<Door>();
		for(Pattern p : map.getPatternFinder().findMicroPatterns()){
			if (p instanceof Entrance)
				entrance = (Entrance)p;
			else if (p instanceof Door)
				doors.add((Door)p);
		}
		
		//Find paths between entrance and other doors, add these nodes to a set
		GraphPathfinder pathfinder = new GraphPathfinder(patternGraph);
		HashSet<Node<Pattern>> criticalPath = new HashSet<Node<Pattern>>();
		for(Door d : doors){
			if(entrance.getParent() == null)
			{
				System.out.println("ENTRANCE NULL");
			}
			
			for(Node<Pattern> n : pathfinder.find(patternGraph.getNode(entrance.getParent()), patternGraph.getNode(d.getParent()))){
				if(!criticalPath.contains(n))
					criticalPath.add(n);
			}
		}		
		
		//TODO: This can surely be written more elegantly
		//For each node in the set, do the aforementioned procedure...
		patternGraph.resetGraph();
		for(Node<Pattern> n : criticalPath){
			for(Edge<Pattern> e : n.getEdges()){
				
				if(!criticalPath.contains(getOtherNode(e,n)) && !getOtherNode(e,n).isVisited()){
					
					if(!patternGraph.isEdgeInCycle(e)){
						deadEnds.add(expandDeadEnd(map, getOtherNode(e,n),n));
					} else {
					
						Queue<Node<Pattern>> queue = new LinkedList<Node<Pattern>>();
						queue.add(getOtherNode(e,n));
						
						while(!queue.isEmpty()){
							Node<Pattern> current = queue.remove();
							
							for(Edge<Pattern> e2 : current.getEdges()){
								Node<Pattern> n2 = getOtherNode(e2,current);
								if(!criticalPath.contains(n2) && !n2.isVisited()){
									if(!patternGraph.isEdgeInCycle(e2)){
										deadEnds.add(expandDeadEnd(map, n2,current));
									} else {
										queue.add(n2);
										n2.tryVisit();
									}
								}
							}
							
						}
					}
					
				}

			}
		}
		
		return deadEnds;
	}
	
	private static DeadEnd expandDeadEnd(Map map, Node<Pattern> start, Node<Pattern> prev){
		List<Pattern> patterns = new ArrayList<Pattern>();
		
		Queue<Node<Pattern>> queue = new LinkedList<Node<Pattern>>();
		queue.add(start);
		
		while(!queue.isEmpty()){
			Node<Pattern> current = queue.remove();
			current.tryVisit();
			patterns.add(current.getValue());
			
			for(Edge<Pattern> e : current.getEdges()){
				Node<Pattern> n = getOtherNode(e,current);
				if(n != prev && !n.isVisited()){
					queue.add(n);
					n.tryVisit();
				}
			}
		}

		DeadEnd deadEnd = new DeadEnd(map, map.getConfig());
		deadEnd.getPatterns().addAll(patterns);
		return deadEnd;
	}
	
	
	private static Node<Pattern> getOtherNode(Edge<Pattern> e, Node<Pattern> node){
		if (e.getNodeA() == node)
			return e.getNodeB();
		return e.getNodeA();
	}
	
	/**
	 * Returns all the nodes with only one edge from a pattern graph (the leaves).
	 * 
	 * @param patternGraph The pattern graph.
	 * @return Am ArrayList of leaves.
	 */
	private static List<Node<Pattern>> getLeaves(Graph<Pattern> patternGraph){
		List<Node<Pattern>> leaves = new ArrayList<Node<Pattern>>();
		for(Node<Pattern> n : patternGraph.getNodes().values()){
			if (n.countEdges() == 1)
				leaves.add(n);
		}
		return leaves;
	}
	
	
}
