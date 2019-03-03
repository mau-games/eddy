package generator.algorithm.MAPElites.Dimensions;

import finder.PatternFinder;
import game.Room;
import generator.algorithm.ZoneIndividual;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;

public class LinearityGADimension extends GADimension {
	
	public LinearityGADimension(float granularity)
	{
		super();
		dimension = DimensionTypes.LINEARITY;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(ZoneIndividual individual, Room target) {
		
		Room individualRoom = individual.getPhenotype().getMap(-1, -1, null, null);
		PatternFinder finder = individualRoom.getPatternFinder();
		finder.findMesoPatterns();
		int paths = individualRoom.LinearityWithinRoom();
		
//		double maxPaths = individualRoom.getColCount() <= individualRoom.getRowCount() 
//				? individualRoom.getColCount() : individualRoom.getRowCount();
		
//		maxPaths *= individualRoom.getDoors().size();
		double doors = individualRoom.getDoors().size();
		double maxPaths = ((double)finder.getPatternGraph().countNodes()) + (double)(doors * 3) + doors;
		double finalValue = Math.min((double)paths/maxPaths, 1.0);
		finalValue = (1.0 - finalValue);

//		return Math.min(finalValue, 1.0);
		
		return finalValue;
	}

}
