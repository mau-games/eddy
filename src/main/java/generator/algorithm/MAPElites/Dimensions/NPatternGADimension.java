package generator.algorithm.MAPElites.Dimensions;

import finder.PatternFinder;
import game.Room;
import generator.algorithm.ZoneIndividual;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;

public class NPatternGADimension extends GADimension {
	
	double patternMultiplier = 4.0; 
	
	public NPatternGADimension(float granularity)
	{
		super();
		dimension = DimensionTypes.NUMBER_PATTERNS;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(ZoneIndividual individual, Room target) {
		Room individualRoom = individual.getPhenotype().getMap(-1, -1, null, null);
		PatternFinder finder = individualRoom.getPatternFinder();
		finder.findMesoPatterns();
		
		double maxPatterns = individualRoom.getColCount() <= individualRoom.getRowCount() 
							? individualRoom.getColCount() -1 : individualRoom.getRowCount() - 1;
							
		maxPatterns = Math.max(maxPatterns * patternMultiplier, 1.0);
		
		return Math.min((double) finder.getPatternGraph().countNodes() / maxPatterns, 1.0);
	}

}
