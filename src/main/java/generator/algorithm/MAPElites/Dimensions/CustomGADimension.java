package generator.algorithm.MAPElites.Dimensions;


import game.Room;
import game.Tile;
import game.tiles.BossEnemyTile;
import generator.algorithm.MetricInterpreter;
import generator.algorithm.ZoneIndividual;

public class CustomGADimension extends GADimension {

	double patternMultiplier = 4.0;
	MetricInterpreter metricGAIntepreter;

	public CustomGADimension(float granularity)
	{
		super();
		dimension = DimensionTypes.CUSTOM;
		this.granularity = granularity;
	}

	public CustomGADimension(MetricInterpreter metric_interpreter, float granularity)
	{
		super();
		metricGAIntepreter = metric_interpreter;
		dimension = DimensionTypes.CUSTOM;
		this.granularity = granularity;
	}

	public void setMetricGAIntepreter(MetricInterpreter metricGAIntepreter) {
		this.metricGAIntepreter = metricGAIntepreter;
	}

	@Override
	public double CalculateValue(ZoneIndividual individual, Room target) 
	{
		Room individualRoom = individual.getPhenotype().getMap(-1, -1, null, null, null);
		return metricGAIntepreter.calculateMetric(individualRoom);
	}

	@Override
	public double CalculateValue(Room individualRoom, Room target) {

		return metricGAIntepreter.calculateMetric(individualRoom);
	}
	
	public static double getValue(Room individualRoom)
	{
		return  0.0;
	}
}
