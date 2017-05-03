package finder.patterns.meso;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import finder.graph.Edge;
import finder.graph.Graph;
import finder.graph.Node;
import finder.patterns.CompositePattern;
import finder.patterns.InventorialPattern;
import finder.patterns.Pattern;
import finder.patterns.micro.Room;
import finder.patterns.micro.Treasure;
import game.Map;
import generator.config.GeneratorConfig;

public class TreasureRoom extends CompositePattern {

	private double quality = 0.0f;
	
	public double getQuality(){
		return quality;
	}
	
	public TreasureRoom(GeneratorConfig config, int treasureCount){
		quality = Math.min((double)treasureCount/config.getTreasureRoomTargetTreasureAmount(),1.0);
	}	
	
	public static List<CompositePattern> matches(Map map, Graph<Pattern> patternGraph) {
		List<CompositePattern> treasureRooms = new ArrayList<>();
		
		patternGraph.resetGraph();
		
		Queue<Node<Pattern>> nodeQueue = new LinkedList<Node<Pattern>>();
		nodeQueue.add(patternGraph.getStartingPoint());
		
		while(!nodeQueue.isEmpty()){
			Node<Pattern> current = nodeQueue.remove();
			current.tryVisit();
			if(current.getValue() instanceof Room){
				List<InventorialPattern> containedTreasure = ((Room)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Treasure;}).collect(Collectors.toList());
				if(containedTreasure.size() > 1){
					TreasureRoom t = new TreasureRoom(map.getConfig(),containedTreasure.size());
					
					t.patterns.add(current.getValue());
					t.patterns.addAll(containedTreasure);
					treasureRooms.add(t);
					//System.out.println("Got a treasure room!");
				}
			}
			nodeQueue.addAll(current.getEdges().stream().map((Edge<Pattern> e)->{
				Node<Pattern> ret = null;
				if(e.getNodeA() == current)
					ret = e.getNodeB();
				else
					ret = e.getNodeA();
				return ret;
				}).filter((Node<Pattern> n)->{return !n.isVisited();}).collect(Collectors.toList()));
		}
		
		return treasureRooms;
	}
	
}
