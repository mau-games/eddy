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
import finder.patterns.micro.Entrance;
import finder.patterns.micro.Boss;
import finder.patterns.micro.Chamber;
import finder.patterns.micro.Door;
import finder.patterns.micro.Treasure;
import game.Room;
import generator.config.GeneratorConfig;

public class GuardRoom extends CompositePattern {
	
	private double quality = 0.0f;
	
	public double getQuality(){
		return quality;
	}
	
	public GuardRoom(GeneratorConfig config, int enemyCount){
//		quality = Math.min((double)enemyCount/config.getGuardRoomTargetEnemyAmount(),1.0); //previous
		quality = Math.max(0, 1.0 - (double)Math.abs(enemyCount - config.getGuardRoomTargetEnemyAmount())/config.getGuardRoomTargetEnemyAmount()); //TODO: This is a big issue!½
	}	
	
	public static List<CompositePattern> matches(Room room, Graph<Pattern> patternGraph, List<CompositePattern> currentMeso) {
		List<CompositePattern> guardRooms = new ArrayList<CompositePattern>();
		
		patternGraph.resetGraph();
		
		for(Node<Pattern> current : patternGraph.getNodes().values()) //this can be cache
		{
			if(current.getValue() instanceof Chamber)
			{
				List<InventorialPattern> containedEnemies = ((Chamber)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Enemy;}).collect(Collectors.toList());
				List<InventorialPattern> containedTreasure = ((Chamber)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Treasure;}).collect(Collectors.toList());
				List<InventorialPattern> containedBoss = ((Chamber)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Boss;}).collect(Collectors.toList());
				Pattern containDoor = (Pattern)((Chamber)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Door;}).findAny().orElse(null); 
				
				if(containDoor == null && containedEnemies.size() >= 2 && containedTreasure.size() == 0)
				{
					GuardRoom g = new GuardRoom(room.getConfig(), containedEnemies.size());
					g.patterns.add(current.getValue());
					g.patterns.addAll(containedEnemies);
					guardRooms.add(g);
					//System.out.println("Got a guard room!");
				}
				else if(containDoor == null && !containedBoss.isEmpty()) //Perhaps it requires that this is its own meso (Boss chamber)
				{
					GuardRoom g = new GuardRoom(room.getConfig(), 5);
					g.patterns.add(current.getValue());
					g.patterns.addAll(containedBoss);
					guardRooms.add(g);
				}
				
				
			}
		}
		
		return guardRooms;
	}
}
