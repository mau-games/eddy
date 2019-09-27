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
import finder.patterns.micro.Chamber;
import game.Room;
import generator.config.GeneratorConfig;

/**
 * The ChokePoint class represents the choke point pattern.
 * 
 * <p>This is not yet properly implemented and is mostly added as a meso
 * pattern placeholder.
 * 
 * @author Johan Holmberg
 */
public class ChokePoint extends CompositePattern {
	
	private double roomRoomQuality = 1.0;
	private double roomCorridorQuality = 1.0;
	
	public double getQuality(){
		if(getPatterns().get(0) instanceof Chamber && getPatterns().get(1) instanceof Chamber)
			return roomRoomQuality;
		else
			return roomCorridorQuality;
	}
	
	public ChokePoint(GeneratorConfig config){
		roomRoomQuality = config.getChokePointRoomToRoomQuality();
		roomCorridorQuality = config.getChokePointRoomToCorridorQuality();
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
		
		//TODO: Is a potential choke point, a variation of a corridor?? that have in-between one or two chambers???
		
		// How to find a choke point:
		// Look at boundaries between patterns where: one or both of the patterns are rooms.
		// If the width of the edge is 1, we have a potential choke point.
		// The potential choke point is an actual choke point if all paths from pattern A to pattern B must pass through that edge (BFS?)
		
		List<CompositePattern> chokePoints = new ArrayList<>();
		List<Edge<Pattern>> potentialChokeEdges = new ArrayList<>();
		
		patternGraph.resetGraph();
		
		Queue<Node<Pattern>> nodeQueue = new LinkedList<Node<Pattern>>();
//		nodeQueue.add(patternGraph.getStartingPoint());
		nodeQueue.addAll(patternGraph.getNodes().values());
		
		while(!nodeQueue.isEmpty()){
			Node<Pattern> current = nodeQueue.remove();
			current.tryVisit();

			for(Edge<Pattern> e : current.getEdges()){
				if(e.getNodeA() == current && !e.getNodeB().isVisited()){
					nodeQueue.add(e.getNodeB());
					e.getNodeB().tryVisit();
				} else if (e.getNodeB() == current && !e.getNodeA().isVisited()){
					nodeQueue.add(e.getNodeA());
					e.getNodeA().tryVisit();
				}
				if(!potentialChokeEdges.contains(e) && potentialChokeEdge(patternGraph, e)){
					potentialChokeEdges.add(e);
				}
			}
		}
		
		for(Edge<Pattern> e : potentialChokeEdges){
			ChokePoint cp = new ChokePoint(room.getConfig());
			cp.patterns.add(e.getNodeA().getValue());
			cp.patterns.add(e.getNodeB().getValue());
			chokePoints.add(cp);
		}
		
		return chokePoints;		
	}
	
	private static boolean potentialChokeEdge(Graph<Pattern> patternGraph, Edge<Pattern> edge){
		return (edge.getNodeA().getValue() instanceof Chamber || edge.getNodeB().getValue() instanceof Chamber) && edge.getWidth() == 1 && !patternGraph.isEdgeInCycle(edge);
	}
	
}
