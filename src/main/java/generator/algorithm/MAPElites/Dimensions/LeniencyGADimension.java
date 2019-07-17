package generator.algorithm.MAPElites.Dimensions;

import java.util.List;


import finder.PatternFinder;
import finder.patterns.CompositePattern;
import finder.patterns.meso.ChokePoint;
import finder.patterns.meso.DeadEnd;
import finder.patterns.meso.GuardedTreasure;
import game.Room;
import generator.algorithm.ZoneIndividual;

public class LeniencyGADimension extends GADimension {

	double patternMultiplier = 4.0; 
	
	public LeniencyGADimension(float granularity)
	{
		super();
		dimension = DimensionTypes.LENIENCY;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(ZoneIndividual individual, Room target) 
	{
		Room individualRoom = individual.getPhenotype().getMap(-1, -1, null, null, null);
		individualRoom.getPatternFinder().findMicroPatterns();
		
		double w1 = 0.3;
		double w2 = 0.3;
		double w3 = 0.4;
		
		double a = w1 *  (Math.log10(individualRoom.getEnemyCount()) * individualRoom.calculateEnemySparsity());
		double b = w2 * (Math.log10(individualRoom.getEnemyCount()) * individualRoom.calculateEnemyDensity());
		individualRoom.calculateDoorSafeness();
		
		double c = w3 * (1.0 - individualRoom.getDoorSafeness());
		
		return 1.0 - ((a + b + c));
//
//		double traversableWeight = 0.1;
//		double doorsSafetyWeight = 0.45;
//		double generalDangerWeight = 0.45;
//		double enemyToleranceThreshold = 1.0;
//		
////		double enemies = individualRoom.getEnemyCount();
////		double logEnemyCount = Math.log(enemies);
////		double enemySparse = individualRoom.calculateEnemySparsity();
////		
////		double combo = logEnemyCount * enemySparse;
//		double dangerRate = 0.0;
//		
//		if(individualRoom.getEnemyCount() != 0)
//			dangerRate = Math.min(1.0, (0.5* individualRoom.calculateEnemySparsity() + 0.5* individualRoom.calculateEnemyDensity()));
//		
////		 Math.log10(individualRoom.getEnemyCount()) *
//		individualRoom.calculateDoorSafeness();
//		
//		double v = individualRoom.getDoorSafeness();
//
//		double firstValue = (traversableWeight * individualRoom.emptySpacesRate());
//		double secondValue = (doorsSafetyWeight * (1.0 - individualRoom.getDoorSafeness()));
////		double thirdValue = (generalDangerWeight * (individualRoom.emptySpacesRate() - dangerRate));
//		double thirdValue = (generalDangerWeight * (dangerRate));
//		double enemyTolerance = traversableWeight * (enemyToleranceThreshold - Math.log10(individualRoom.getEnemyCount()));
//		
////		return Math.min(1.0, 1.0 - (firstValue + secondValue - thirdValue));
//		
//		double resultA = 1.0 - (firstValue + secondValue - thirdValue);
//		double resultB = 1.0 - (firstValue + secondValue + thirdValue);
//		double resultA_e = 1.0 - (firstValue + secondValue - (generalDangerWeight * (individualRoom.emptySpacesRate() - dangerRate)));
//		double resultB_e = 1.0 - (firstValue + secondValue + (generalDangerWeight * (individualRoom.emptySpacesRate() - dangerRate)));
//		double resultC = 1.0 - (secondValue + thirdValue + enemyTolerance);
//		
//		return Math.min(1.0, resultC);

	}

	@Override
	public double CalculateValue(Room individualRoom, Room target) {

		individualRoom.getPatternFinder().findMicroPatterns();
		
		double enemyDensityWeight = 0.3;
		double doorsSafetyWeight = 0.3;
		double generalDangerWeight = 0.4;
		double dangerRate = 0.0;
		
		if(individualRoom.getEnemyCount() != 0)
			dangerRate = Math.log10(individualRoom.getEnemyCount()) * individualRoom.calculateEnemySparsity();
		
		individualRoom.calculateDoorSafeness();
		
		double v = individualRoom.getDoorSafeness();

		double firstValue = (enemyDensityWeight * individualRoom.calculateEnemyDensity());
		double secondValue = (doorsSafetyWeight * (1.0 - individualRoom.getDoorSafeness()));
//		double thirdValue = (generalDangerWeight * (individualRoom.emptySpacesRate() - dangerRate));
		double thirdValue = (generalDangerWeight * (dangerRate));
		
//		return Math.min(1.0, 1.0 - (firstValue + secondValue - thirdValue));
		return Math.min(1.0, 1.0 - (firstValue + secondValue + thirdValue));
	}
	
	public static double getValue(Room individualRoom)
	{
		individualRoom.getPatternFinder().findMicroPatterns();
		
		double enemyDensityWeight = 0.3;
		double doorsSafetyWeight = 0.3;
		double generalDangerWeight = 0.4;
		double dangerRate = 0.0;
		
		if(individualRoom.getEnemyCount() != 0)
			dangerRate = Math.log10(individualRoom.getEnemyCount()) * individualRoom.calculateEnemySparsity();
		
		individualRoom.calculateDoorSafeness();

		double firstValue = (enemyDensityWeight * individualRoom.calculateEnemyDensity());
		double secondValue = (doorsSafetyWeight * (1.0 - individualRoom.getDoorSafeness()));
//		double thirdValue = (generalDangerWeight * (individualRoom.emptySpacesRate() - dangerRate));
		double thirdValue = (generalDangerWeight * (dangerRate));
		
//		return Math.min(1.0, 1.0 - (firstValue + secondValue - thirdValue));
		return Math.min(1.0, 1.0 - (firstValue + secondValue + thirdValue));
	}
}
