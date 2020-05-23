package finder.patterns.macro;

import finder.patterns.CompositePattern;
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
import finder.patterns.meso.Ambush;
import finder.patterns.meso.GuardRoom;
import finder.patterns.meso.TreasureRoom;
import finder.patterns.meso.GuardedTreasure;
import game.Room;
import generator.config.GeneratorConfig;

public class FindTreasure extends CompositePattern {
	
	public FindTreasure(double mesoQuality) {
		quality = mesoQuality;
	}
	
	public static FindTreasure getBestFindTreasure(List<CompositePattern> currentMeso) {
		
		FindTreasure tempFT;
		FindTreasure bestFT = null;
		
		for (CompositePattern current : currentMeso)
		{
			if (current instanceof TreasureRoom || current instanceof GuardedTreasure)
			{
				tempFT = new FindTreasure(current.getQuality());
				
				if (bestFT == null || tempFT.getQuality() > bestFT.getQuality())
					bestFT = tempFT;
			}
		}
		
		return bestFT;
	}
}
