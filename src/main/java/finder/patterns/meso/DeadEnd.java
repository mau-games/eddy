package finder.patterns.meso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import finder.geometry.Point;
import finder.graph.Edge;
import finder.graph.Graph;
import finder.graph.GraphPathfinder;
import finder.graph.Node;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import finder.patterns.SpacialPattern;
import finder.patterns.micro.Door;
import finder.patterns.micro.Entrance;
import game.Room;
import generator.config.GeneratorConfig;

public class DeadEnd extends CompositePattern {
	
	private double badness = 0.0;
	private double filledness = 1.0f;
	Room room;
	
	public double getQuality(){
		
		double actualFilledness = 0.0;
		List<CompositePattern> mesopatterns = room.getPatternFinder().findMesoPatterns();
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
	
	public DeadEnd(Room room, GeneratorConfig config){
		this.room = room;
		
		
		filledness = config.getDeadEndFilledness();
		
		badness = config.getDeadEndBadness();
	}
	

	/**
	 * Searches a map for instances of this pattern and returns a list of found
	 * instances.
	 * 
	 * @param room The map to search for patterns in.
	 * @param boundary A boundary in which the pattern is searched for.
	 * @return A list of found instances.
	 */
	public static List<CompositePattern> matches(Room room, Graph<Pattern> patternGraph, List<CompositePattern> currentMeso) {
		
		//TODO: This is important to check! this must be solved
		
		//How to find dead ends:
		//1. Find the critical path
		//2. For each node in the critical path, perform a BFS starting at each edge connecting to a node that isn't in the critical path
		//3. If an edge is part of a cycle, it is not part of a dead end
		//4. If an edge is NOT part of a cycle, every node beyond it is in a dead end.
		
		List<CompositePattern> deadEnds = new ArrayList<>();
		
		//Working on this
		Queue<Node<Pattern>> patternQueue = new LinkedList<Node<Pattern>>();
		
		//We get all Spatial patterns that contains a door!
		for(Node<Pattern> nodePattern : patternGraph.getNodes().values())
		{
			if(nodePattern.getValue() instanceof SpacialPattern)
			{
				SpacialPattern sp = (SpacialPattern)nodePattern.getValue();
				
				if(sp.getContainedPatterns().stream().filter((Pattern p) -> {return p instanceof Door;}).findAny().orElse(null) != null &&
						!patternQueue.contains(nodePattern))
				{
					patternQueue.add(nodePattern);
				}
				
			}
		}
		
		List<Pattern> patterns = new ArrayList<Pattern>();
		List<Pattern> finalPatterns = new ArrayList<Pattern>(); //IDK if I should add everything together! 
		patternGraph.resetGraph();
		
		//TODO: This can be optimized!! I know!
		while(!patternQueue.isEmpty()){
			
			Node<Pattern> current = patternQueue.remove();
			depthSearch(room, current, null, patterns, patternGraph);
			
			for(Pattern p : patterns)
			{
				if(p.pathTowardsDeadEnd)
				{
					finalPatterns.add(p);
				}
				
				p.pathTowardsDeadEnd = true; //IDK ABOUT THIS
			}
			
			if(!finalPatterns.isEmpty())
			{
				DeadEnd deadEnd = new DeadEnd(room, room.getConfig());
				deadEnd.getPatterns().addAll(finalPatterns);		
				deadEnds.add(deadEnd);
			}

			patterns.clear();
			finalPatterns.clear();
		}

		return deadEnds;
	}
	
	private static boolean depthSearch(Room room, Node<Pattern> nodePattern, Node<Pattern> prev, List<Pattern> deadEndPatterns, Graph<Pattern> patternGraph)
	{
		if(nodePattern.getValue() instanceof SpacialPattern)
		{
			SpacialPattern sp = (SpacialPattern)nodePattern.getValue();
			nodePattern.tryVisit();
			
			//Check if it has a door
			boolean deadEnd = false;
			
//			micropatterns.stream().filter((Pattern p) -> {return p instanceof Entrance;}).findFirst().get();
			if(sp.getContainedPatterns().stream().filter((Pattern p) -> {return p instanceof Door;}).findAny().orElse(null) != null)
			{
				nodePattern.getValue().pathTowardsDeadEnd = false;
			}
			
			for(Edge<Pattern> e : nodePattern.getEdges())
			{
				Node<Pattern> n = getOtherNode(e,nodePattern);
				if(n != prev)
				{
					if(deadEndPatterns.contains(n.getValue()))
					{
						//if you get back to a pattern that haas been explored, it means you are not in a dead end and
						//that the other pattern is not either ... Maybe this is not true (I should ask what others think!)
						n.getValue().pathTowardsDeadEnd = false;
						nodePattern.getValue().pathTowardsDeadEnd = false;
					}
					else
					{
						deadEndPatterns.add(n.getValue());
						n.tryVisit();
						deadEnd = depthSearch(room, n, nodePattern, deadEndPatterns, patternGraph);
						
						if(nodePattern.getValue().pathTowardsDeadEnd)
						{
							nodePattern.getValue().pathTowardsDeadEnd = deadEnd;
						}
					}
				}
			}
			
			if(!deadEndPatterns.contains(nodePattern.getValue()))
			{
				deadEndPatterns.add(nodePattern.getValue());
			}
			
			return nodePattern.getValue().pathTowardsDeadEnd;
		}
		
		return false;
	}
	
	private static DeadEnd expandDeadEnd(Room room, Node<Pattern> start, Node<Pattern> prev){
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

		DeadEnd deadEnd = new DeadEnd(room, room.getConfig());
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
