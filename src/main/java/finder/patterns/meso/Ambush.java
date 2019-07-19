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
import finder.patterns.micro.Enemy;
import finder.patterns.micro.Entrance;
import finder.patterns.micro.Treasure;
import finder.patterns.micro.Boss;
import finder.patterns.micro.Chamber;
import finder.patterns.micro.Door;
import game.Room;
import generator.config.GeneratorConfig;

public class Ambush extends CompositePattern {

	private double quality = 1.0;
	
	public double getQuality(){
		return quality;	
	}
	
	public Ambush(GeneratorConfig config, int enemies){
//		quality = Math.min((double)enemies/config.getAmbushEnemies(),1.0); //previous
		quality = Math.max(0, 1.0 - (double)Math.abs(enemies - config.getAmbushEnemies())/config.getAmbushEnemies());
	}
	
	public static List<CompositePattern> matches(Room room, Graph<Pattern> patternGraph, List<CompositePattern> currentMeso) {
		List<CompositePattern> ambushes = new ArrayList<CompositePattern>();
		
		patternGraph.resetGraph();
		
		for(Node<Pattern> current : patternGraph.getNodes().values()) //this can be cache
		{
			if(current.getValue() instanceof Chamber)
			{
				List<InventorialPattern> containedEnemies = ((Chamber)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Enemy;}).collect(Collectors.toList());
				List<InventorialPattern> containedDoors = ((Chamber)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Door;}).collect(Collectors.toList());
				List<InventorialPattern> containedBoss = ((Chamber)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Boss;}).collect(Collectors.toList());
				if(containedEnemies.size() >= 1 && containedDoors.size() >= 1)
				{
					Ambush a = new Ambush(room.getConfig(),containedEnemies.size());
					a.patterns.add(current.getValue());
					a.patterns.addAll(containedEnemies);
					a.patterns.addAll(containedDoors);
					ambushes.add(a);
				}
				else if(containedDoors.size() >= 1 && !containedBoss.isEmpty())
				{
					Ambush a = new Ambush(room.getConfig(),5);
					a.patterns.add(current.getValue());
					a.patterns.addAll(containedBoss);
					a.patterns.addAll(containedDoors);
					ambushes.add(a);
				}
			}
		}
		return ambushes;
	
	}
	
}
