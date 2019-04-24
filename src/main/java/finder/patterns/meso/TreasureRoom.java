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
import finder.patterns.micro.Chamber;
import finder.patterns.micro.Treasure;
import game.Room;
import generator.config.GeneratorConfig;

public class TreasureRoom extends CompositePattern {

	private double quality = 0.0f;
	
	public double getQuality(){
		return quality;
	}
	
	public TreasureRoom(GeneratorConfig config, int treasureCount){
		quality = Math.max(0, 1.0 - (double)Math.abs(treasureCount - config.getTreasureRoomTargetTreasureAmount())/config.getTreasureRoomTargetTreasureAmount());
		//quality = Math.min((double)treasureCount/config.getTreasureRoomTargetTreasureAmount(),1.0);
	}	
	
	public static List<CompositePattern> matches(Room room, Graph<Pattern> patternGraph) 
	{
		List<CompositePattern> treasureRooms = new ArrayList<>();		
		patternGraph.resetGraph(); //This maybe can be avoided and everything and/or rather than adding a new list we can just change the chamber to treasure chamber?

		
		//Why not just iterate through all the patterns and just check for the chambers
		for(Node<Pattern> current : patternGraph.getNodes().values()) //this can be cache
		{
			if(current.getValue() instanceof Chamber)
			{
				List<InventorialPattern> containedTreasure = ((Chamber)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Treasure;}).collect(Collectors.toList());
				List<InventorialPattern> containedEnemies = ((Chamber)current.getValue()).getContainedPatterns().stream().filter(p->{return p instanceof Enemy;}).collect(Collectors.toList());
				if(containedTreasure.size() >= 2 && containedEnemies.size() == 0) //TODO: this hard code can be changed
				{
					TreasureRoom t = new TreasureRoom(room.getConfig(),containedTreasure.size());
					
					t.patterns.add(current.getValue());
					t.patterns.addAll(containedTreasure);
					treasureRooms.add(t);
					//System.out.println("Got a treasure room!");
				}
			}
		}
		return treasureRooms;
	}
	
}
