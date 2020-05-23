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
import game.Room;
import generator.config.GeneratorConfig;

public class DefeatBoss extends CompositePattern{
	
	public DefeatBoss(double mesoQuality) {
		quality = mesoQuality;
	}
	
	public static DefeatBoss getBestDefeatBoss(List<CompositePattern> currentMeso) {
		
		DefeatBoss tempDB;
		DefeatBoss bestDB = null;
		
		for (CompositePattern current : currentMeso)
		{
			if (current instanceof GuardRoom || current instanceof Ambush)
			{
				boolean containsBoss = false;
				
				for (Pattern p : current.getPatterns())
				{
					if (p instanceof Boss)
						containsBoss = true;
				}
				
				if (!containsBoss)
					continue;
				
				tempDB = new DefeatBoss(current.getQuality());
				
				if (bestDB == null || tempDB.quality > bestDB.quality)
					bestDB = tempDB;
			}
		}
		
		return bestDB;
	}
}