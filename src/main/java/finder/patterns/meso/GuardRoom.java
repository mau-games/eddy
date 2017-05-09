package finder.patterns.meso;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import finder.geometry.Bitmap;
import finder.graph.Edge;
import finder.graph.Graph;
import finder.graph.Node;
import finder.patterns.CompositePattern;
import finder.patterns.InventorialPattern;
import finder.patterns.Pattern;
import finder.patterns.micro.Enemy;
import finder.patterns.micro.Room;
import finder.patterns.micro.Treasure;
import game.Map;
import generator.config.GeneratorConfig;

public class GuardRoom extends CompositePattern {
	
	private double quality = 0.0f;
	
	public double getQuality(){
		return quality;
	}
	
	public GuardRoom(GeneratorConfig config, int enemyCount){
		quality = Math.min((double)enemyCount/config.getGuardRoomTargetEnemyAmount(),1.0);
	}	
	
	public static List<CompositePattern> matches(Map map, Graph<Pattern> patternGraph) {
		List<CompositePattern> guardRooms = new ArrayList<CompositePattern>();
		
		patternGraph.resetGraph();
		
		Queue<Node<Pattern>> nodeQueue = new LinkedList<Node<Pattern>>();
		nodeQueue.add(patternGraph.getStartingPoint());
		
		while(!nodeQueue.isEmpty()){
			Node<Pattern> current = nodeQueue.remove();
			current.tryVisit();
			if(current.getValue() instanceof Room){
				List<InventorialPattern> containedEnemies = ((Room)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Enemy;}).collect(Collectors.toList());
				if(containedEnemies.size() >= 1 && containedEnemies.size() == ((Room)current.getValue()).getContainedPatterns().size()){
					GuardRoom g = new GuardRoom(map.getConfig(), containedEnemies.size());
					g.patterns.add(current.getValue());
					g.patterns.addAll(containedEnemies);
					guardRooms.add(g);
					//System.out.println("Got a guard room!");
				}
			}
			
			for(Edge<Pattern> e : current.getEdges()){
				Node<Pattern> other = null;
				if(e.getNodeA() == current)
					other = e.getNodeB();
				else
					other = e.getNodeA();
				if(!other.isVisited())
				{
					other.tryVisit();
					nodeQueue.add(other);
				}
			}
//			nodeQueue.addAll(current.getEdges().stream().map((Edge<Pattern> e)->{
//				Node<Pattern> ret = null;
//				if(e.getNodeA() == current)
//					ret = e.getNodeB();
//				else
//					ret = e.getNodeA();
//				return ret;
//				}).filter((Node<Pattern> n)->{return !n.isVisited();}).collect(Collectors.toList()));
		}
		
		return guardRooms;
	}
}
