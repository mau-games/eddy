package generator.algorithm.MAPElites.Dimensions;

import java.util.List;


import finder.PatternFinder;
import finder.patterns.CompositePattern;
import finder.patterns.meso.ChokePoint;
import finder.patterns.meso.DeadEnd;
import finder.patterns.meso.GuardedTreasure;
import game.Room;
import generator.algorithm.ZoneIndividual;

public class LenencyGADimension extends GADimension {

	double patternMultiplier = 4.0; 
	
	public LenencyGADimension(float granularity)
	{
		super();
		dimension = DimensionTypes.LENENCY;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(ZoneIndividual individual, Room target) 
	{
		Room individualRoom = individual.getPhenotype().getMap(-1, -1, null, null, null);
		
		double enemyDensityWeight = 0.3;
		double doorsSafetyWeight = 0.3;
		double generalDangerWeight = 0.4;
		double dangerRate = Math.log(individualRoom.getEnemyCount()) * individualRoom.calculateEnemySparsity();
//		individualRoom.calculateDoorSafeness();

		double firstValue = (enemyDensityWeight * individualRoom.calculateEnemyDensity());
		double secondValue = (doorsSafetyWeight * (1.0 - individualRoom.getDoorSafeness()));
		double thirdValue = (generalDangerWeight * (individualRoom.emptySpacesRate() - dangerRate));
		
		return firstValue + secondValue + thirdValue;

	}

	@Override
	public double CalculateValue(Room individualRoom, Room target) {

		double enemyDensityWeight = 0.3;
		double doorsSafetyWeight = 0.3;
		double generalDangerWeight = 0.4;
		double dangerRate = Math.log(individualRoom.getEnemyCount()) * individualRoom.calculateEnemySparsity();
//		individualRoom.calculateDoorSafeness();

		double firstValue = (enemyDensityWeight * individualRoom.calculateEnemyDensity());
		double secondValue = (doorsSafetyWeight * (1.0 - individualRoom.getDoorSafeness()));
		double thirdValue = (generalDangerWeight * (individualRoom.emptySpacesRate() - dangerRate));
		
		return firstValue + secondValue + thirdValue;
	}
	
	public static double getValue(Room individualRoom)
	{
		double enemyDensityWeight = 0.3;
		double doorsSafetyWeight = 0.3;
		double generalDangerWeight = 0.4;
		double dangerRate = Math.log(individualRoom.getEnemyCount()) * individualRoom.calculateEnemySparsity();
		individualRoom.calculateDoorSafeness();

		double firstValue = (enemyDensityWeight * individualRoom.calculateEnemyDensity());
		double secondValue = (doorsSafetyWeight * (1.0 - individualRoom.getDoorSafeness()));
		double thirdValue = (generalDangerWeight * (individualRoom.emptySpacesRate() - dangerRate));
		
		return firstValue + secondValue + thirdValue;
	}
}
