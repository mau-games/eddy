package generator.algorithm.MAPElites.Dimensions;

import java.util.List;

import finder.PatternFinder;
import finder.patterns.CompositePattern;
import finder.patterns.meso.ChokePoint;
import finder.patterns.meso.DeadEnd;
import finder.patterns.meso.GuardedTreasure;
import game.Room;
import generator.algorithm.ZoneIndividual;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;

public class NPatternGADimension extends GADimension {
	
	static double patternMultiplier = 4.0; 
	
	public NPatternGADimension(float granularity)
	{
		super();
		dimension = DimensionTypes.NUMBER_PATTERNS;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(ZoneIndividual individual, Room target) {
		Room individualRoom = individual.getPhenotype().getMap(-1, -1, null, null, null);
		PatternFinder finder = individualRoom.getPatternFinder();
		finder.findMesoPatterns();
		
		double maxPatterns = individualRoom.getColCount() <= individualRoom.getRowCount() 
							? individualRoom.getColCount() -1 : individualRoom.getRowCount() - 1;
							
		maxPatterns = Math.max(maxPatterns * patternMultiplier, 1.0);
		
		return Math.min((double) finder.getPatternGraph().countNodes() / maxPatterns, 1.0);
	}

	@Override
	public double CalculateValue(Room individualRoom, Room target) {
		PatternFinder finder = individualRoom.getPatternFinder();
		finder.findMesoPatterns();
		
		double maxPatterns = individualRoom.getColCount() <= individualRoom.getRowCount() 
							? individualRoom.getColCount() -1 : individualRoom.getRowCount() - 1;
							
		maxPatterns = Math.max(maxPatterns * patternMultiplier, 1.0);
		
		return Math.min((double) finder.getPatternGraph().countNodes() / maxPatterns, 1.0);
	}
	
	public static double getValue(Room individualRoom)
	{
		PatternFinder finder = individualRoom.getPatternFinder();
		finder.findMesoPatterns();
		
		double maxPatterns = individualRoom.getColCount() <= individualRoom.getRowCount() 
							? individualRoom.getColCount() -1 : individualRoom.getRowCount() - 1;
							
		maxPatterns = Math.max(maxPatterns * patternMultiplier, 1.0);
		
		return Math.min((double) finder.getPatternGraph().countNodes() / maxPatterns, 1.0);
	}

}
