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
import finder.patterns.Pattern;
import finder.patterns.SpacialPattern;
import finder.patterns.micro.Enemy;
import finder.patterns.micro.Chamber;
import game.Room;
import generator.config.GeneratorConfig;

/**
 * The GuardedTreasure class represents the GuardedTreasure pattern.
 * 
 * <p>This is not yet properly implemented and is mostly added as a macro
 * pattern placeholder.
 * 
 * @author Johan Holmberg
 */
public class GuardedTreasure extends CompositePattern {
	
	private double quality = 1.0;
	
	public double getQuality(){
		return quality;
	}
	
	public GuardedTreasure(GeneratorConfig config, int enemies){
//		quality = Math.min((double)enemies/config.getGuardedTreasureEnemies(),1.0); //previous
		quality = Math.max(0, 1.0 - (double)Math.abs(enemies - config.getGuardedTreasureEnemies())/config.getGuardedTreasureEnemies());
	}
	
	public static List<CompositePattern> matches(Room room, Graph<Pattern> patternGraph, List<CompositePattern> currentMeso) {
		List<CompositePattern> guardedTreasures = new ArrayList<CompositePattern>();
		
		List<TreasureRoom> treasureRooms = new ArrayList<TreasureRoom>();
		List<GuardRoom> guardRooms = new ArrayList<GuardRoom>();
		List<DeadEnd> deadEnds = new ArrayList<DeadEnd>();
		for(CompositePattern p : room.getPatternFinder().getMesoPatterns()){
			if (p instanceof GuardRoom)
				guardRooms.add((GuardRoom)p);
			else if (p instanceof TreasureRoom)
				treasureRooms.add((TreasureRoom)p);
			else if (p instanceof DeadEnd)
				deadEnds.add((DeadEnd)p);
		}
		
		List<Chamber> treasureRoomRooms = treasureRooms.stream().map(tr->{return (Chamber)tr.getPatterns().get(0);}).collect(Collectors.toList());
		List<Chamber> guardRoomRooms = guardRooms.stream().map(gr->{return (Chamber)gr.getPatterns().get(0);}).collect(Collectors.toList());
		
		//For each dead end, see if it contains both treasure rooms and guard rooms
		for(DeadEnd de : deadEnds){
			List<Chamber> deTreasure = new ArrayList<Chamber>();
			List<Chamber> deGuard = new ArrayList<Chamber>();
			for(Pattern p : de.getPatterns()){
				if(p instanceof Chamber && treasureRoomRooms.contains(p)){
					deTreasure.add((Chamber)p);
				}
				else if(p instanceof Chamber && guardRoomRooms.contains(p)){
					deGuard.add((Chamber)p);
				}
			}
			
			if(deTreasure.size() > 0 && deGuard.size() > 0){
				//If it does contain both, find the "exit" from the dead end (that is, a node with a neighbour not in the dead end).
				Node<Pattern> exit = findDeadEndExit(de, patternGraph);
				
				//If there is a path from each treasure room to the exit that does not pass through a guard room, that treasure room is a not guarded treasure
				//Otherwise, it is.
				for(Chamber r : deTreasure){
					patternGraph.resetGraph();
					List<Pattern> foundGuards = new ArrayList<Pattern>();
					
					boolean foundPath = false;
					Queue<Node<Pattern>> queue = new LinkedList<Node<Pattern>>();
					queue.add(patternGraph.getNode(r));
					
					while(!queue.isEmpty()){
						Node<Pattern> current = queue.remove();
						current.tryVisit();
						if(current == exit){
							foundPath = true;
							break;
						}
						
						for(Edge<Pattern> e : current.getEdges()){
							Node<Pattern> n = getOtherNode(e,current);
							if(!n.isVisited() && !deGuard.contains(n.getValue())){
								queue.add(n);
								n.tryVisit();
							}
							else if(!n.isVisited() && deGuard.contains(n.getValue())){
								foundGuards.add(n.getValue());
								n.tryVisit();
							}
						}	
					}
					
					if(!foundPath){
						
						int enemyCount = 0;
						for(Pattern p : de.getPatterns()){
							if(p instanceof SpacialPattern){
								enemyCount += ((SpacialPattern)p).getContainedPatterns().stream().filter(ip -> ip instanceof Enemy).count();
							}
						}
						GuardedTreasure gt = new GuardedTreasure(room.getConfig(),enemyCount);
						gt.getPatterns().add(r);
						for(TreasureRoom tr : treasureRooms){
							if(tr.getPatterns().contains(r))
								gt.getPatterns().add(tr);
						}
						gt.getPatterns().add(de);
						gt.getPatterns().addAll(foundGuards);
						guardedTreasures.add(gt);
					}
					
				}
				
				
			}
			
		}
		
		//if(guardedTreasures.size() > 0) System.out.println("Found " + guardedTreasures.size() + " guardedTreasure.");
		return guardedTreasures;
	}
	
	private static Node<Pattern> findDeadEndExit(DeadEnd de, Graph<Pattern> patternGraph){
		Node<Pattern> start = patternGraph.getNode(de.getPatterns().get(0));
		
		patternGraph.resetGraph();
		Queue<Node<Pattern>> queue = new LinkedList<Node<Pattern>>();
		queue.add(start);
		
		while(!queue.isEmpty()){
			Node<Pattern> current = queue.remove();
			current.tryVisit();
			if(hasUndeadNeighbour(current,de,patternGraph)){
				return current;
			}
			
			for(Edge<Pattern> e : current.getEdges()){
				Node<Pattern> n = getOtherNode(e,current);
				if(!n.isVisited()){
					queue.add(n);
					n.tryVisit();
				}
			}	
		}
		return null;
	}
	
	private static boolean hasUndeadNeighbour(Node<Pattern> node, DeadEnd de, Graph<Pattern> patternGraph){
		for(Edge<Pattern> e : node.getEdges()){
			if(!de.getPatterns().contains(getOtherNode(e,node).getValue()))
				return true;
		}
		return false;
	}
	
	private static Node<Pattern> getOtherNode(Edge<Pattern> e, Node<Pattern> node){
		if (e.getNodeA() == node)
			return e.getNodeB();
		return e.getNodeA();
	}
	
}
