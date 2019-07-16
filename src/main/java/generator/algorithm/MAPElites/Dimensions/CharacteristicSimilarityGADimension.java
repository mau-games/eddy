package generator.algorithm.MAPElites.Dimensions;

import finder.PatternFinder;
import game.Room;
import generator.algorithm.ZoneIndividual;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;

public class CharacteristicSimilarityGADimension extends GADimension {
	
	public CharacteristicSimilarityGADimension(float granularity)
	{
		super();
		dimension = DimensionTypes.INNER_SIMILARITY;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(ZoneIndividual individual, Room target) {
		
		Room individualRoom = individual.getPhenotype().getMap(-1, -1, null, null, null);
		
		double abstractSimilarity = 0.0;
		
		double enemyDensityDifference = Math.abs(target.calculateEnemyDensity() - individualRoom.calculateEnemyDensity());
		if(enemyDensityDifference != 0.0)
			enemyDensityDifference = Math.min(1.0, Math.abs(Math.log(enemyDensityDifference)));
		
		double enemySparsityDifference = Math.abs(target.calculateEnemySparsity() - individualRoom.calculateEnemySparsity());
		if(enemySparsityDifference != 0.0)
			enemySparsityDifference = Math.min(1.0, Math.abs(Math.log(enemySparsityDifference)));
		
		double treasureDensityDifference = Math.abs(target.calculateTreasureDensity() - individualRoom.calculateTreasureDensity());
		if(treasureDensityDifference != 0.0)
			treasureDensityDifference = Math.min(1.0, Math.abs(Math.log(treasureDensityDifference)));
		
		double treasureSparsityDifference = Math.abs(target.calculateTreasureSparsity() - individualRoom.calculateTreasureSparsity());
		if(treasureSparsityDifference != 0.0)
			treasureSparsityDifference = Math.min(1.0, Math.abs(Math.log(treasureSparsityDifference)));
		
		double wallDensityDifference = Math.abs(target.calculateWallDensity() - individualRoom.calculateWallDensity());
		if(wallDensityDifference != 0.0)
			wallDensityDifference = Math.min(1.0, Math.abs(Math.log(wallDensityDifference)));
		
		double wallSparsityDifference = Math.abs(target.calculateWallSparsity() - individualRoom.calculateWallSparsity());
		if(wallSparsityDifference != 0.0)
			wallSparsityDifference = Math.min(1.0, Math.abs(Math.log(wallSparsityDifference)));

		abstractSimilarity += enemyDensityDifference + enemySparsityDifference + 
								treasureDensityDifference + treasureSparsityDifference +
								wallDensityDifference + wallSparsityDifference;
		
		
		return abstractSimilarity/6.0;
	}


	@Override
	public double CalculateValue(Room individualRoom, Room target) {

		double abstractSimilarity = 0.0;
		
		double enemyDensityDifference = Math.abs(target.calculateEnemyDensity() - individualRoom.calculateEnemyDensity());
		if(enemyDensityDifference != 0.0)
			enemyDensityDifference = Math.min(1.0, Math.abs(Math.log(enemyDensityDifference)));
		
		double enemySparsityDifference = Math.abs(target.calculateEnemySparsity() - individualRoom.calculateEnemySparsity());
		if(enemySparsityDifference != 0.0)
			enemySparsityDifference = Math.min(1.0, Math.abs(Math.log(enemySparsityDifference)));
		
		double treasureDensityDifference = Math.abs(target.calculateTreasureDensity() - individualRoom.calculateTreasureDensity());
		if(treasureDensityDifference != 0.0)
			treasureDensityDifference = Math.min(1.0, Math.abs(Math.log(treasureDensityDifference)));
		
		double treasureSparsityDifference = Math.abs(target.calculateTreasureSparsity() - individualRoom.calculateTreasureSparsity());
		if(treasureSparsityDifference != 0.0)
			treasureSparsityDifference = Math.min(1.0, Math.abs(Math.log(treasureSparsityDifference)));
		
		double wallDensityDifference = Math.abs(target.calculateWallDensity() - individualRoom.calculateWallDensity());
		if(wallDensityDifference != 0.0)
			wallDensityDifference = Math.min(1.0, Math.abs(Math.log(wallDensityDifference)));
		
		double wallSparsityDifference = Math.abs(target.calculateWallSparsity() - individualRoom.calculateWallSparsity());
		if(wallSparsityDifference != 0.0)
			wallSparsityDifference = Math.min(1.0, Math.abs(Math.log(wallSparsityDifference)));

		abstractSimilarity += enemyDensityDifference + enemySparsityDifference + 
								treasureDensityDifference + treasureSparsityDifference +
								wallDensityDifference + wallSparsityDifference;
		
		
		return abstractSimilarity/6.0;
	}
	
	/***
	 * Impossible to calculate without another room
	 * @param individualRoom
	 * @return
	 */
	public static double getValue(Room individualRoom)
	{
		return -1.0;
	}
	
	public static double calculateValueIndependently(Room individualRoom, Room other)
	{
		double abstractSimilarity = 0.0;
		
		double enemyDensityDifference = Math.abs(other.calculateEnemyDensity() - individualRoom.calculateEnemyDensity());
		if(enemyDensityDifference != 0.0)
			enemyDensityDifference = Math.min(1.0, Math.abs(Math.log(enemyDensityDifference)));
		
		double enemySparsityDifference = Math.abs(other.calculateEnemySparsity() - individualRoom.calculateEnemySparsity());
		if(enemySparsityDifference != 0.0)
			enemySparsityDifference = Math.min(1.0, Math.abs(Math.log(enemySparsityDifference)));
		
		double treasureDensityDifference = Math.abs(other.calculateTreasureDensity() - individualRoom.calculateTreasureDensity());
		if(treasureDensityDifference != 0.0)
			treasureDensityDifference = Math.min(1.0, Math.abs(Math.log(treasureDensityDifference)));
		
		double treasureSparsityDifference = Math.abs(other.calculateTreasureSparsity() - individualRoom.calculateTreasureSparsity());
		if(treasureSparsityDifference != 0.0)
			treasureSparsityDifference = Math.min(1.0, Math.abs(Math.log(treasureSparsityDifference)));
		
		double wallDensityDifference = Math.abs(other.calculateWallDensity() - individualRoom.calculateWallDensity());
		if(wallDensityDifference != 0.0)
			wallDensityDifference = Math.min(1.0, Math.abs(Math.log(wallDensityDifference)));
		
		double wallSparsityDifference = Math.abs(other.calculateWallSparsity() - individualRoom.calculateWallSparsity());
		if(wallSparsityDifference != 0.0)
			wallSparsityDifference = Math.min(1.0, Math.abs(Math.log(wallSparsityDifference)));

		abstractSimilarity += enemyDensityDifference + enemySparsityDifference + 
								treasureDensityDifference + treasureSparsityDifference +
								wallDensityDifference + wallSparsityDifference;
		
		
		return abstractSimilarity/6.0;

	}
}
