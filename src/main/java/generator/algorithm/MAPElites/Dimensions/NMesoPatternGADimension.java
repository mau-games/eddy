package generator.algorithm.MAPElites.Dimensions;

import java.util.List;

import org.hamcrest.core.IsInstanceOf;

import finder.PatternFinder;
import finder.patterns.CompositePattern;
import finder.patterns.meso.ChokePoint;
import finder.patterns.meso.DeadEnd;
import finder.patterns.meso.GuardedTreasure;
import game.Room;
import generator.algorithm.ZoneIndividual;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;

public class NMesoPatternGADimension extends GADimension {

	double patternMultiplier = 4.0; 
	
	public NMesoPatternGADimension(float granularity)
	{
		super();
		dimension = DimensionTypes.NUMBER_MESO_PATTERN;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(ZoneIndividual individual, Room target) {
		Room individualRoom = individual.getPhenotype().getMap(-1, -1, null, null);
		PatternFinder finder = individualRoom.getPatternFinder();
		List<CompositePattern> mesos = finder.getMesoPatterns();
		float mesoCounter = 0.0f;
		for(CompositePattern meso : mesos)
		{
			if(meso instanceof DeadEnd || meso instanceof ChokePoint || meso instanceof GuardedTreasure) //Because we actually not use chokepoints
			{
				continue;
			}
			mesoCounter += 1;
			
		}

		//3.0 is the min chamber size! but i should definietely use some precomputed value
		double maxPatterns = Math.floor(Math.floor((double)individualRoom.getColCount() / 3.0) * Math.floor((double)individualRoom.getRowCount() / 3.0));
		
		
		return Math.min((double) mesoCounter / maxPatterns, 1.0);
	}

}
